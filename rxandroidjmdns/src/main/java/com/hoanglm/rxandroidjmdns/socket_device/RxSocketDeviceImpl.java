package com.hoanglm.rxandroidjmdns.socket_device;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.dagger.DeviceScope;
import com.hoanglm.rxandroidjmdns.socket_device.connection.Connector;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;
import com.hoanglm.rxandroidjmdns.utils.SocketAlreadyConnectedException;
import com.jakewharton.rxrelay.BehaviorRelay;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.IP_ADDRESS;
import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.PORT;

@DeviceScope
public class RxSocketDeviceImpl implements RxSocketDevice {

    private final BehaviorRelay<RxSocketConnection.RxSocketConnectionState> connectionStateRelay;
    private final Connector connector;
    private final String ipAddress;
    private final int port;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    @Inject
    public RxSocketDeviceImpl(@Named(IP_ADDRESS) String ipAddress,
                              @Named(PORT) int port,
                              BehaviorRelay<RxSocketConnection.RxSocketConnectionState> connectionStateRelay,
                              Connector connector) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.connectionStateRelay = connectionStateRelay;
        this.connector = connector;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Observable<RxSocketConnection.RxSocketConnectionState> observeConnectionStateChanges() {
        return connectionStateRelay.distinctUntilChanged().skip(1);
    }

    @Override
    public RxSocketConnection.RxSocketConnectionState getConnectionState() {
        return connectionStateRelay.getValue();
    }

    @Override
    public Observable<RxSocketConnection> establishConnection(Context context) {
        return Observable.defer(() -> {
            if (isConnected.compareAndSet(false, true)) {
                return connector.prepareConnection()
                        .doOnUnsubscribe(() -> isConnected.set(false));
            } else {
                return Observable.error(new SocketAlreadyConnectedException(ipAddress, port));
            }
        });
    }

    @Override
    public String toString() {
        return "RxSocketDeviceImpl{" +
                "ipAddress=" + ipAddress +
                "port=" + port +
                '}';
    }
}
