package com.hoanglm.rxandroidjmdns.service;

import com.hoanglm.rxandroidjmdns.connection.ServiceConnector;
import com.hoanglm.rxandroidjmdns.connection.RxSocketService;
import com.hoanglm.rxandroidjmdns.dagger.ServiceConnectorScope;
import com.hoanglm.rxandroidjmdns.network.TCPClient;
import com.hoanglm.rxandroidjmdns.network.TCPServer;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.SetupServiceException;
import com.jakewharton.rxrelay.BehaviorRelay;
import com.jakewharton.rxrelay.PublishRelay;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jmdns.*;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@ServiceConnectorScope
public class ServiceConnectorImpl implements ServiceConnector {
    /**
     * If no identity is provided, a generic one is used.
     */
    private static final String DEFAULT_HOST_ID_PREFIX = "RxAndroid-";

    /**
     * What type of mServiceServer should be advertised.
     */
    private static final String SERVICE_TYPE = "_rxandroid._tcp.local.";
    /**
     * What request should be sent to the mServiceServer running on other peers ?
     */
    private static final String REQUEST_MESSAGE = "Ping";

    private final AndroidDNSSetupHook mAndroidDNSSetupHookImpl;
    private final PublishSubject<Void> mCancelPeerRequestSubject;
    private final PublishRelay<SetupServiceException> mDisconnectionErrorInputRelay; // todo call when wifi turn off or changed or stop
    private final BehaviorRelay<RxSocketService.RxSocketServiceState> mServiceConnectorState;
    private final Output<List<ServiceInfo>> mServiceDiscoveredOutput;
    private final Func1<SetupServiceException, Observable<?>> errorMapper = new Func1<SetupServiceException, Observable<?>>() {
        @Override
        public Observable<?> call(SetupServiceException exception) {
            return Observable.error(exception);
        }
    };

    /**
     * Service discovery and advertisement.
     */
    private JmDNS mJmDNS;
    private ServiceInfo mServiceInfo;
    private String mDevId;
    private TCPServer mServiceServer;

    private ServiceEventHandler mServiceEventHandler = new ServiceEventHandler() {
        @Override
        public void handle(List<ServiceInfo> serviceInfos) {
            if (mServiceDiscoveredOutput.hasObservers()) {
                mServiceDiscoveredOutput.valueRelay.call(serviceInfos);
            }
        }
    };

    private Set<ServiceInfo> mDiscoveredPeers = new TreeSet<>((si1, si2) -> si1.getName().compareTo(si2.getName()));

    @Inject
    public ServiceConnectorImpl(AndroidDNSSetupHook androidDNSSetupHookImpl) {
        mAndroidDNSSetupHookImpl = androidDNSSetupHookImpl;
        mCancelPeerRequestSubject = PublishSubject.create();
        mDisconnectionErrorInputRelay = PublishRelay.create();
        mServiceDiscoveredOutput = new Output<>();
        mServiceConnectorState = BehaviorRelay.create(RxSocketService.RxSocketServiceState.READY);
    }

    @Override
    public Observable<ServiceConnector> startService(TCPServer serviceServer) {
        mServiceServer = serviceServer;
        // Call the setup stub
        if (!mAndroidDNSSetupHookImpl.setup()) {
            throw new SetupServiceException(SetupServiceException.Reason.CAN_NOT_SETUP_MULTICAST, "Error during NSD setup");
        }

        RxJmDNSLog.d("Starting jmDNS mServiceServer");
        RxJmDNSLog.d("Address = %s, Port = %d", mServiceServer.listenAddress().getHostAddress(), mServiceServer.listenPort());
        try {
            mDevId = randomDeviceID();
            mJmDNS = JmDNS.create(mServiceServer.listenAddress(), mServiceServer.listenAddress().getHostName());
            // Define the behavior of mServiceServer discovery.
            mJmDNS.addServiceTypeListener(new PingPongServiceTypeListener());

            // Advertise the local mServiceServer in the network
            mServiceInfo = ServiceInfo.create(SERVICE_TYPE, mDevId, mServiceServer.listenPort(), "ping pong");
            mJmDNS.registerService(mServiceInfo);
            mServiceConnectorState.call(RxSocketService.RxSocketServiceState.SETUP_SUCCESS);
            return Observable.just(this);
        } catch (IOException e) {
            RxJmDNSLog.e(e, "ServiceConnectorImpl>> can not setup multicast");
            throw new SetupServiceException(SetupServiceException.Reason.CAN_NOT_SETUP_MULTICAST, "Error multicast setup");
        }
    }

    @Override
    public void stopService() {
        if (mServiceServer != null) {
            mServiceServer.kill();
        }
        if (mJmDNS != null) {
            mJmDNS.unregisterAllServices();
            try {
                mJmDNS.close();
            } catch (IOException e) {
                RxJmDNSLog.e(e, "stopService>>");
            }
            RxJmDNSLog.i("stopService>> Services unregistered");
            mJmDNS = null;
        }
        mAndroidDNSSetupHookImpl.teardown();
        mCancelPeerRequestSubject.onNext(null);
        mServiceConnectorState.call(RxSocketService.RxSocketServiceState.STOP_SUCCESS);
        mDisconnectionErrorInputRelay.call(new SetupServiceException(SetupServiceException.Reason.DISCONNECTED_SERVICE, "stop service is called"));
    }

    @Override
    public boolean restartService() {
        if (mJmDNS == null) {
            throw new IllegalStateException("Please call startService() method in the first time");
        }
        String newId = randomDeviceID();
        if (newId.equals(mDevId)) {
            return true;
        } else {
            mJmDNS.unregisterService(mServiceInfo);
            mDevId = newId;
            mServiceInfo = ServiceInfo.create(SERVICE_TYPE, mDevId, mServiceServer.listenPort(), "ping pong");
            RxJmDNSLog.d("restartService>> Address = %s, Port = %d", mServiceServer.listenAddress().getHostAddress(), mServiceServer.listenPort());
            try {
                mJmDNS.registerService(mServiceInfo);
                RxJmDNSLog.d("Identity changed and advertised");
                mServiceConnectorState.call(RxSocketService.RxSocketServiceState.RESTART_SUCCESS);
                return true;
            } catch (IOException e) {
                RxJmDNSLog.e(e, "Cannot change identity");
                mDisconnectionErrorInputRelay.call(new SetupServiceException(SetupServiceException.Reason.DISCONNECTED_SERVICE, "restart service is failed"));
                return false;
            }
        }
    }

    @Override
    public BehaviorRelay<RxSocketService.RxSocketServiceState> getOnServiceConnectorState() {
        return mServiceConnectorState;
    }

    @Override
    public RxSocketService.RxSocketServiceState getConnectorState() {
        return mServiceConnectorState.getValue();
    }

    @Override
    public Observable<ServiceConnector> asErrorOnlyObservable() {
        return mDisconnectionErrorInputRelay.flatMap(new Func1<SetupServiceException, Observable<ServiceConnectorImpl>>() {
            @Override
            public Observable<ServiceConnectorImpl> call(SetupServiceException e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    public Observable<List<ServiceInfo>> getServiceDiscoveredChanged() {
        return withErrorHandling(mServiceDiscoveredOutput)
                .doOnSubscribe(() -> mServiceDiscoveredOutput.valueRelay.call(getPeers()))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private String randomDeviceID() {
        return DEFAULT_HOST_ID_PREFIX + new Random().nextInt();
    }

    private List<ServiceInfo> getPeers() {
        List<ServiceInfo> peers = new LinkedList<>();
        peers.addAll(mDiscoveredPeers);
        return peers;
    }

    private class PingPongServiceTypeListener implements ServiceTypeListener {
        @Override
        public void serviceTypeAdded(ServiceEvent event) {
            RxJmDNSLog.d("Service discovered: " + event.getType() + " : " + event.getName());
            if (event.getType().equals(SERVICE_TYPE)) {
                mJmDNS.addServiceListener(event.getType(), mServiceListener);
                // Request information about the mServiceServer.
                mJmDNS.requestServiceInfo(event.getType(), event.getName());
            }
        }

        @Override
        public void subTypeForServiceTypeAdded(ServiceEvent ev) {
        }
    }

    private ServiceListener mServiceListener = new ServiceListener() {
        @Override
        public void serviceAdded(ServiceEvent event) {
            RxJmDNSLog.i("Service added " + event.getInfo().toString());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            RxJmDNSLog.i("Service removed " + event.getInfo().toString());
            mDiscoveredPeers.remove(event.getInfo());
            mServiceEventHandler.handle(new ArrayList<>(mDiscoveredPeers));
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            RxJmDNSLog.i("Peer found " + event.getInfo().toString());
            // If I'm not the newly discovered peer, engage in communication
            if (event.getName().equalsIgnoreCase(mDevId)) {
                return;
            }
            RxJmDNSLog.i("Check can be request to this service");
            Observable.just(REQUEST_MESSAGE)
                    .flatMap(requestMessage -> {
                        try {
                            for (InetAddress i : event.getInfo().getInet4Addresses()) {
                                RxJmDNSLog.d("Other peer is: " + i.getHostAddress());
                            }
                            RxJmDNSLog.d("Requesting " + requestMessage);
                            final String response = TCPClient.sendTo(requestMessage,
                                    event.getInfo().getInetAddresses()[0],
                                    event.getInfo().getPort());
                            RxJmDNSLog.d("Response " + response);
                            return Observable.just(event.getInfo());
                        } catch (IOException e) {
                            RxJmDNSLog.d("Error in request:" + e.getMessage());
                            return Observable.error(new Throwable("Error in request:" + e.getMessage()));
                        }
                    })
                    .takeUntil(mCancelPeerRequestSubject)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(serviceInfo -> {
                        mDiscoveredPeers.add(serviceInfo);
                        mServiceEventHandler.handle(new ArrayList<>(mDiscoveredPeers));
                    }, throwable -> RxJmDNSLog.e(throwable, "serviceResolved>>"));
        }
    };

    private interface ServiceEventHandler {
        void handle(List<ServiceInfo> serviceInfos);
    }

    private <T> Observable<T> withErrorHandling(Output<T> output) {
        //noinspection unchecked
        return Observable.merge(
                output.valueRelay,
                (Observable<T>) output.errorRelay.flatMap(errorMapper)
        );
    }

    private static class Output<T> {

        final PublishRelay<T> valueRelay;
        final PublishRelay<SetupServiceException> errorRelay;

        Output() {
            this.valueRelay = PublishRelay.create();
            this.errorRelay = PublishRelay.create();
        }

        boolean hasObservers() {
            return valueRelay.hasObservers() || errorRelay.hasObservers();
        }
    }
}
