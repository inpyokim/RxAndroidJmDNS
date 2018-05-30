package com.hoanglm.rxandroidjmdns.jmdns_service;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.dagger.ServiceConnectorComponent;
import com.hoanglm.rxandroidjmdns.dagger.ServiceScope;
import com.hoanglm.rxandroidjmdns.network.AndroidWiFiTCPServer;
import com.hoanglm.rxandroidjmdns.network.Request;
import com.hoanglm.rxandroidjmdns.network.TCPServer;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSException;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;
import com.hoanglm.rxandroidjmdns.utils.ServiceSharingAdapter;
import com.hoanglm.rxandroidjmdns.utils.SetupServiceException;
import com.jakewharton.rxrelay.PublishRelay;

import javax.inject.Inject;
import javax.inject.Provider;

import rx.Observable;
import rx.subjects.PublishSubject;

@ServiceScope
public class ServiceSetup {
    private Context mContext;
    private final PublishSubject<Void> mCancelServiceSubject;
    private final PublishRelay<Request> mTcpServerRequestRelay;
    private final Provider<ServiceConnectorComponent.Builder> mServiceConnectorComponentBuilder;
    private final SchedulerProvider mSchedulerProvider;

    @Inject
    public ServiceSetup(Context context,
                        Provider<ServiceConnectorComponent.Builder> serviceConnectorComponentBuilder,
                        PublishRelay<Request> tcpServerRequestRelay,
                        SchedulerProvider schedulerProvider) {
        mContext = context;
        mCancelServiceSubject = PublishSubject.create();
        mServiceConnectorComponentBuilder = serviceConnectorComponentBuilder;
        mSchedulerProvider = schedulerProvider;
        mTcpServerRequestRelay = tcpServerRequestRelay;
    }

    public void stopService() {
        mCancelServiceSubject.onNext(null);
    }

    public Observable<JmDNSConnector> setupServiceConnection() {
        return Observable.defer(() -> {
            RxJmDNSLog.i("start setup service connection");
            TCPServer tcpServer;
            try {
                tcpServer = AndroidWiFiTCPServer.build(mContext, mTcpServerRequestRelay);
            } catch (Exception e) {
                RxJmDNSLog.e(e, "setupServiceConnection>>");
                if (e instanceof RxJmDNSException) {
                    return Observable.error(e);
                }
                return Observable.error(new SetupServiceException(SetupServiceException.Reason.SERVER_SETUP_FAILED, "Fail to setup service: " + e.toString()));
            }
            ServiceConnectorComponent serviceConnectorComponent = mServiceConnectorComponentBuilder.get()
                    .serviceConnectorModule(new ServiceConnectorComponent.ServiceConnectorModule())
                    .build();
            JmDNSConnector jmDNSConnector = serviceConnectorComponent.providerServiceConnector();
            return Observable.merge(jmDNSConnector.startService(tcpServer),
                    jmDNSConnector.asErrorOnlyObservable())
                    .takeUntil(mCancelServiceSubject)
                    .unsubscribeOn(mSchedulerProvider.io())
                    .doOnUnsubscribe(() -> jmDNSConnector.stopService());
        })
                .compose(new ServiceSharingAdapter())
                .subscribeOn(mSchedulerProvider.io());
    }
}
