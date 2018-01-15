package com.hoanglm.rxandroidjmdns.connection;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.utils.NetworkUtil;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSDisconnectException;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSException;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;
import com.hoanglm.rxandroidjmdns.utils.RxWifiStateObservable;
import com.jakewharton.rxrelay.PublishRelay;

import javax.inject.Inject;

import rx.Observable;

public class DisconnectionRouter {
    private final PublishRelay<RxJmDNSException> disconnectionErrorInputRelay = PublishRelay.create();
    private final Observable<RxJmDNSException> disconnectionErrorOutputObservable;

    @Inject
    public DisconnectionRouter(Context context, RxWifiStateObservable rxWifiStateObservable) {
        final Observable<RxJmDNSException> emitErrorWhenWifiIsDisabled = rxWifiStateObservable
                .map(wifiState -> wifiState.isUsable())
                .startWith(NetworkUtil.isWifiConnected(context))
                .filter(isWifiStateUsable -> !isWifiStateUsable)
                .map(isWifiStateUsable -> {
                    RxJmDNSLog.i("wifi state is ON, %b", isWifiStateUsable);
                    return new RxJmDNSDisconnectException("disconnection from wifi state");
                });

        disconnectionErrorOutputObservable = Observable.merge(disconnectionErrorInputRelay,
                emitErrorWhenWifiIsDisabled)
                .first()
                .cache();
        disconnectionErrorOutputObservable.subscribe();
    }

    public void onDisconnectedException(RxJmDNSDisconnectException disconnectedException) {
        disconnectionErrorInputRelay.call(disconnectedException);
    }

    public Observable<RxJmDNSException> asValueOnlyObservable() {
        return disconnectionErrorOutputObservable;
    }

    public <T> Observable<T> asErrorOnlyObservable() {
        return disconnectionErrorOutputObservable.flatMap(exception -> Observable.error(exception));
    }
}
