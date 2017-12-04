package com.hoanglm.rxandroidjmdns.connection;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.utils.RxRetry;

import java.util.List;

import javax.inject.Inject;
import javax.jmdns.ServiceInfo;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class RxSocketServiceImpl extends RxSocketService {

    @Inject
    ServiceSetup mServiceSetup;

    private Context mContext;

    @Inject
    public RxSocketServiceImpl(final Context context) {
        mContext = context;
    }

    @Override
    public Observable<Boolean> setup(boolean autoSetup) {
        return mServiceSetup.getServiceConnectorObservale()
                .retryWhen(new RxRetry(autoSetup))
                .map(jmDNSService -> true)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Boolean> restart() {
        return mServiceSetup.getServiceConnectorObservale()
                .map(networkService -> networkService.restartService())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void stop() {
        mServiceSetup.stopService();
    }

    @Override
    public Observable<List<ServiceInfo>> getConnectedSockets() {
        return null; // TODO
    }

    @Override
    public Observable<List<ServiceInfo>> getOnConnectedSockets() {
        return null;// TODO
    }

    @Override
    public Observable<List<ServiceInfo>> getOnServiceInfoDiscovery() {
        return mServiceSetup.getServiceConnectorObservale()
                .flatMap(networkService -> networkService.getServiceDiscoveredChanged());
    }

    @Override
    public Observable<RxSocketServiceState> observeServiceStateChanges() {
        return mServiceSetup.getSharedServiceConnectorWithoutAutoSetup()
                .flatMap(networkService -> networkService.getOnServiceConnectorState())
                .distinctUntilChanged().skip(1);
    }

    @Override
    public RxSocketServiceState getServiceStateChanges() {
        return mServiceSetup.getServiceConnector().getConnectorState();
    }
}
