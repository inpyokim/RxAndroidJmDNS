package com.hoanglm.rxandroidjmdns.connection;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.utils.RxRetry;
import com.jakewharton.rxrelay.BehaviorRelay;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class RxSocketServiceImpl extends RxSocketService {

    @Inject
    ServiceSetup mServiceSetup;

    @Inject
    BehaviorRelay<RxSocketServiceState> mServiceConnectorState;

    private Context mContext;

    @Inject
    public RxSocketServiceImpl(final Context context) {
        mContext = context;
    }

    @Override
    public Observable<ServiceConnector> setup(boolean autoSetup) {
        return mServiceSetup.setupServiceConnection()
                .retryWhen(new RxRetry(autoSetup))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void stop() {
        mServiceSetup.stopService();
    }

    @Override
    public Observable<RxSocketServiceState> observeServiceStateChanges() {
        return mServiceConnectorState.distinctUntilChanged().skip(1);
    }

    @Override
    public RxSocketServiceState getServiceStateChanges() {
        return mServiceConnectorState.getValue();
    }
}
