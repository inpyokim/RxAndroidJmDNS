package com.hoanglm.rxandroidjmdns.jmdns_service;

import com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent;
import com.hoanglm.rxandroidjmdns.dagger.ServiceScope;
import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

public class RxSocketDeviceProvider {
    private final Map<String, RxSocketDeviceComponent> mCachedRxSocketDeviceComponent;
    private final Provider<RxSocketDeviceComponent.Builder> mRxSocketDeviceBuilder;

    @Inject
    public RxSocketDeviceProvider(Map<String, RxSocketDeviceComponent> cachedRxSocketDeviceComponent,
                                  Provider<RxSocketDeviceComponent.Builder> rxSocketDeviceBuilder) {
        mCachedRxSocketDeviceComponent = cachedRxSocketDeviceComponent;
        mRxSocketDeviceBuilder = rxSocketDeviceBuilder;
    }

    public RxSocketDevice getSocketDevice(String ipAddress, int port) {
        synchronized (mCachedRxSocketDeviceComponent) {
            RxSocketDeviceComponent cachedDeviceComponent = mCachedRxSocketDeviceComponent.get(ipAddress);
            if (cachedDeviceComponent != null) {
                RxSocketDevice socketDevice = cachedDeviceComponent.rxSocketDevice();
                if (socketDevice.getPort() == port) {
                    return socketDevice;
                }
            }
            RxSocketDeviceComponent deviceComponent =
                    mRxSocketDeviceBuilder.get()
                            .serviceRxSocketDeviceModule(new RxSocketDeviceComponent.RxSocketDeviceModule(ipAddress, port))
                            .build();
            mCachedRxSocketDeviceComponent.put(ipAddress, deviceComponent);
            return deviceComponent.rxSocketDevice();
        }
    }
}