package com.hoanglm.rxandroidjmdns.utils;

import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.functions.Action0;

public class ConnectionSharingAdapter implements Observable.Transformer<RxSocketConnection, RxSocketConnection> {

    private final AtomicReference<Observable<RxSocketConnection>> connectionObservable = new AtomicReference<>();

    @Override
    public Observable<RxSocketConnection> call(Observable<RxSocketConnection> source) {
        synchronized (connectionObservable) {
            final Observable<RxSocketConnection> rxBleConnectionObservable = connectionObservable.get();

            if (rxBleConnectionObservable != null) {
                return rxBleConnectionObservable;
            }

            final Observable<RxSocketConnection> newConnectionObservable = source
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {
                            connectionObservable.set(null);
                        }
                    })
                    .replay(1)
                    .refCount();
            connectionObservable.set(newConnectionObservable);
            return newConnectionObservable;
        }
    }
}
