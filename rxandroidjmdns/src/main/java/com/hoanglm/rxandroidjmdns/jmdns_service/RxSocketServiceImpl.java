package com.hoanglm.rxandroidjmdns.jmdns_service;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;
import com.hoanglm.rxandroidjmdns.utils.RxRetry;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;
import com.jakewharton.rxrelay.BehaviorRelay;

import javax.inject.Inject;

import rx.Observable;

public class RxSocketServiceImpl extends RxSocketService {

    private final ServiceSetup mServiceSetup;
    private final BehaviorRelay<RxSocketServiceState> mServiceConnectorState;
    private final SchedulerProvider mSchedulerProvider;
    private final RxSocketDeviceProvider mRxSocketDeviceProvider;
    private Context mContext;

    @Inject
    public RxSocketServiceImpl(final Context context,
                               ServiceSetup serviceSetup,
                               BehaviorRelay<RxSocketServiceState> serviceConnectorState,
                               SchedulerProvider schedulerProvider,
                               RxSocketDeviceProvider rxSocketDeviceProvider) {
        mContext = context;
        mServiceSetup = serviceSetup;
        mServiceConnectorState = serviceConnectorState;
        mSchedulerProvider = schedulerProvider;
        mRxSocketDeviceProvider = rxSocketDeviceProvider;
    }

    @Override
    public Observable<JmDNSConnector> setup(boolean autoSetup) {
        return mServiceSetup.setupServiceConnection()
                .retryWhen(new RxRetry(autoSetup))
                .observeOn(mSchedulerProvider.ui());
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

    @Override
    public RxSocketDevice getSocketDevice(String ipAddress, int port) {
        return mRxSocketDeviceProvider.getSocketDevice(ipAddress, port);
    }
}
