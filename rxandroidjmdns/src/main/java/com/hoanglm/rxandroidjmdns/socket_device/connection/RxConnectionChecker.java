package com.hoanglm.rxandroidjmdns.socket_device.connection;

import com.hoanglm.rxandroidjmdns.utils.RxSocketDisconnectException;

import rx.Observable;

public interface RxConnectionChecker {
    <T> Observable<T> asErrorOnlyObservable();
    void onDisconnectedException(RxSocketDisconnectException disconnectedException);
}
