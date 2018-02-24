package com.hoanglm.rxandroidjmdns.dagger;

import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;
import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDeviceImpl;
import com.hoanglm.rxandroidjmdns.socket_device.connection.Connector;
import com.hoanglm.rxandroidjmdns.socket_device.connection.ConnectorImpl;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;
import com.hoanglm.rxandroidjmdns.utils.ConnectionStateChangeListener;
import com.jakewharton.rxrelay.BehaviorRelay;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@DeviceScope
@Subcomponent(modules = {RxSocketDeviceComponent.RxSocketDeviceModule.class, RxSocketDeviceComponent.RxSocketDeviceModuleBinder.class})
public interface RxSocketDeviceComponent {
    @Subcomponent.Builder
    public interface Builder {
        RxSocketDeviceComponent build();

        Builder serviceRxSocketDeviceModule(RxSocketDeviceModule module);
    }

    @Module(subcomponents = RxSocketConnectionComponent.class)
    class RxSocketDeviceModule {
        public static final String IP_ADDRESS = "ip-address";
        public static final String PORT = "port";

        private String ipAddress;
        private int port;

        public RxSocketDeviceModule(String ipAddress, int port) {
            this.ipAddress = ipAddress;
            this.port = port;
        }

        @Provides
        @Named(IP_ADDRESS)
        String provideIpAddress() {
            return ipAddress;
        }

        @Provides
        @Named(PORT)
        int providePort() {
            return port;
        }

        @Provides
        @DeviceScope
        BehaviorRelay<RxSocketConnection.RxSocketConnectionState> provideConnectionStateRelay() {
            return BehaviorRelay.create(RxSocketConnection.RxSocketConnectionState.DISCONNECTED);
        }

        @Provides
        @DeviceScope
        static ConnectionStateChangeListener provideConnectionStateChangeListener(final BehaviorRelay<RxSocketConnection.RxSocketConnectionState> connectionStateBehaviorRelay) {
            return new ConnectionStateChangeListener() {
                @Override
                public void onConnectionStateChange(RxSocketConnection.RxSocketConnectionState rxSocketConnectionState) {
                    connectionStateBehaviorRelay.call(rxSocketConnectionState);
                }
            };
        }
    }

    @Module
    abstract class RxSocketDeviceModuleBinder {
        @Binds
        abstract Connector bindConnector(ConnectorImpl connector);

        @Binds
        abstract RxSocketDevice bindRxSocketDevice(RxSocketDeviceImpl rxSocketDevice);
    }

    RxSocketDevice rxSocketDevice();
}
