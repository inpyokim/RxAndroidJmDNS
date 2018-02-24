package com.hoanglm.rxandroidjmdns.socket_device.connection;

import android.text.TextUtils;

import com.hoanglm.rxandroidjmdns.define.ConnectionConstans;
import com.hoanglm.rxandroidjmdns.network.TCPClient;
import com.hoanglm.rxandroidjmdns.utils.ConnectionStateChangeListener;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.RxRetry;
import com.hoanglm.rxandroidjmdns.utils.RxUtil;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.IP_ADDRESS;
import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.PORT;

public class ConnectionSocket {
    private static final long PING_TIMEOUT = 5000;
    private static final int RETRY_MAX = 3;

    private final SchedulerProvider mSchedulerProvider;
    private final ConnectionStateChangeListener mConnectionStateChangeListener;
    private final String ipAddress;
    private final int port;

    @Inject
    public ConnectionSocket(@Named(IP_ADDRESS) String ipAddress,
                            @Named(PORT) int port,
                            SchedulerProvider schedulerProvider,
                            ConnectionStateChangeListener connectionStateChangeListener) {
        mSchedulerProvider = schedulerProvider;
        this.ipAddress = ipAddress;
        this.port = port;
        mConnectionStateChangeListener = connectionStateChangeListener;
    }

    public Observable<Boolean> getConnected() {
        return pingConnection(ipAddress, port)
                .filter(response -> TextUtils.equals(response, ConnectionConstans.PING_CHECK_CONNECTION))
                .map(response -> true)
                .take(1)
                .doOnSubscribe(() -> mConnectionStateChangeListener.onConnectionStateChange(RxSocketConnection.RxSocketConnectionState.CONNECTING))
                .doOnNext(success -> mConnectionStateChangeListener.onConnectionStateChange(RxSocketConnection.RxSocketConnectionState.CONNECTED));
    }

    private Observable<String> pingConnection(String ipAddress, int port) {
        return Observable.defer(() -> {
            RxJmDNSLog.d("start ping connection ip address = %s, port = %d", ipAddress, port);
            final String response;
            try {
                response = TCPClient.sendTo(ConnectionConstans.PING_CHECK_CONNECTION,
                        ipAddress,
                        port);
                return Observable.just(response);
            } catch (IOException e) {
                e.printStackTrace();
                RxJmDNSLog.e(e, "pingConnection");
                return Observable.error(e);
            }
        })
                .subscribeOn(mSchedulerProvider.io())
                .compose(RxUtil.timeoutJustFirstEmit(PING_TIMEOUT, TimeUnit.MILLISECONDS))
                .retryWhen(new RxRetry(RETRY_MAX));
    }
}