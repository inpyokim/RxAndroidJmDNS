package com.hoanglm.rxandroidjmdns.connection;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.dagger.ServiceConnectorComponent;
import com.hoanglm.rxandroidjmdns.dagger.ServiceScope;
import com.hoanglm.rxandroidjmdns.network.AndroidWiFiTCPServer;
import com.hoanglm.rxandroidjmdns.network.TCPServer;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.RxSocketException;
import com.hoanglm.rxandroidjmdns.utils.ServiceSharingAdapter;
import com.hoanglm.rxandroidjmdns.utils.SetupServiceException;

import javax.inject.Inject;
import javax.inject.Provider;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@ServiceScope
public class ServiceSetup {
    private Context mContext;
    private Observable<ServiceConnector> mSharedServiceConnector;
    private final PublishSubject<Void> mCancelServiceSubject;
    private final ServiceConnector mServiceConnector;

    @Inject
    public ServiceSetup(Context context, Provider<ServiceConnectorComponent.Builder> serviceConnectorComponentBuilder) {
        mContext = context;
        mSharedServiceConnector = setupServiceConnection();
        mCancelServiceSubject = PublishSubject.create();
        ServiceConnectorComponent serviceConnectorComponent = serviceConnectorComponentBuilder.get()
                .serviceConnectorModule(new ServiceConnectorComponent.ServiceConnectorModule())
                .build();
        mServiceConnector = serviceConnectorComponent.providerServiceConnector();
    }

    public Observable<ServiceConnector> getServiceConnectorObservale() {
        return mSharedServiceConnector;
    }

    public Observable<ServiceConnector> getSharedServiceConnectorWithoutAutoSetup() {
        return Observable.just(mServiceConnector);
    }

    public ServiceConnector getServiceConnector() {
        return mServiceConnector;
    }

    public void stopService() {
        mCancelServiceSubject.onNext(null);
    }

    private Observable<ServiceConnector> setupServiceConnection() {
        return Observable.defer(() -> {
            RxJmDNSLog.i("start setup service connection");
            TCPServer tcpServer;
            try {
                tcpServer = AndroidWiFiTCPServer.build(mContext);
            } catch (Exception e) {
                RxJmDNSLog.e(e, "setupServiceConnection>>");
                if (e instanceof RxSocketException) {
                    return Observable.error(e);
                }
                return Observable.error(new SetupServiceException(SetupServiceException.Reason.SERVER_SETUP_FAILED, "Fail to setup service: " + e.toString()));
            }
            return Observable.merge(mServiceConnector.startService(tcpServer),
                    mServiceConnector.asErrorOnlyObservable())
                    .takeUntil(mCancelServiceSubject)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .doOnUnsubscribe(() -> mServiceConnector.stopService());
        })
                .compose(new ServiceSharingAdapter())
                .subscribeOn(Schedulers.io());
    }
}
