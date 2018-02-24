package com.hoanglm.rxandroidjmdns.socket_device.connection;

import android.content.Context;
import android.text.TextUtils;

import com.hoanglm.rxandroidjmdns.dagger.ConnectionScope;
import com.hoanglm.rxandroidjmdns.define.ConnectionConstans;
import com.hoanglm.rxandroidjmdns.network.TCPClient;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSException;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.RxRetry;
import com.hoanglm.rxandroidjmdns.utils.RxSocketDisconnectException;
import com.hoanglm.rxandroidjmdns.utils.RxUtil;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;
import com.jakewharton.rxrelay.PublishRelay;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.IP_ADDRESS;
import static com.hoanglm.rxandroidjmdns.dagger.RxSocketDeviceComponent.RxSocketDeviceModule.PORT;

@ConnectionScope
public class RxConnectionCheckerImpl implements RxConnectionChecker {
    private static final long PING_INTERVAL = 15000;
    private static final long PING_TIMEOUT = PING_INTERVAL - 1000;
    private static final int RETRY_MAX = 3;

    private final PublishRelay<RxJmDNSException> disconnectionErrorInputRelay = PublishRelay.create();
    private final Observable<RxJmDNSException> disconnectionErrorOutputObservable;
    private final SchedulerProvider mSchedulerProvider;

    @Inject
    public RxConnectionCheckerImpl(Context context,
                                   @Named(IP_ADDRESS) String ipAddress,
                                   @Named(PORT) int port,
                                   SchedulerProvider schedulerProvider) {
        mSchedulerProvider = schedulerProvider;
        final Observable<RxJmDNSException> emitErrorWhenPingFail = Observable.interval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                .flatMap(delay -> pingConnection(ipAddress, port))
                .filter(response -> !TextUtils.equals(response, ConnectionConstans.PING_CHECK_CONNECTION))
                .map(response -> new RxSocketDisconnectException(String.format(Locale.US, " Disconnect to ipAddress = %s, port = %d", ipAddress, port)));

        disconnectionErrorOutputObservable = Observable.merge(emitErrorWhenPingFail,
                disconnectionErrorInputRelay)
                .first()
                .cache();
        disconnectionErrorOutputObservable.subscribe();
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
                .retryWhen(new RxRetry(RETRY_MAX))
                .compose(RxUtil.timeoutJustFirstEmit(PING_TIMEOUT, TimeUnit.MILLISECONDS))
                .onErrorReturn(throwable -> "");
    }

    @Override
    public <T> Observable<T> asErrorOnlyObservable() {
        return disconnectionErrorOutputObservable.flatMap(exception -> Observable.error(exception));
    }

    @Override
    public void onDisconnectedException(RxSocketDisconnectException disconnectedException) {
        disconnectionErrorInputRelay.call(disconnectedException);
    }
}
