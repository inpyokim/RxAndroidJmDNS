package com.hoanglm.rxandroidjmdns.utils;

import com.hoanglm.rxandroidjmdns.connection.ServiceConnector;

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.functions.Action0;

public class ServiceSharingAdapter implements Observable.Transformer<ServiceConnector, ServiceConnector> {

    private final AtomicReference<Observable<ServiceConnector>> connectionObservable = new AtomicReference<>();

    @Override
    public Observable<ServiceConnector> call(Observable<ServiceConnector> source) {
        synchronized (connectionObservable) {
            final Observable<ServiceConnector> rxBleConnectionObservable = connectionObservable.get();

            if (rxBleConnectionObservable != null) {
                return rxBleConnectionObservable;
            }

            final Observable<ServiceConnector> newConnectionObservable = source
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
