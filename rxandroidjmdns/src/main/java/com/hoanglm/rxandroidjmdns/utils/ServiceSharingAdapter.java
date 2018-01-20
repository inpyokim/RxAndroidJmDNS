package com.hoanglm.rxandroidjmdns.utils;

import com.hoanglm.rxandroidjmdns.connection.JmDNSConnector;

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.functions.Action0;

public class ServiceSharingAdapter implements Observable.Transformer<JmDNSConnector, JmDNSConnector> {

    private final AtomicReference<Observable<JmDNSConnector>> connectionObservable = new AtomicReference<>();

    @Override
    public Observable<JmDNSConnector> call(Observable<JmDNSConnector> source) {
        synchronized (connectionObservable) {
            final Observable<JmDNSConnector> rxBleConnectionObservable = connectionObservable.get();

            if (rxBleConnectionObservable != null) {
                return rxBleConnectionObservable;
            }

            final Observable<JmDNSConnector> newConnectionObservable = source
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
