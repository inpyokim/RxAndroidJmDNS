package com.hoanglm.rxandroidjmdns.socket_device.connection;

import com.hoanglm.rxandroidjmdns.jmdns_service.ServiceSetup;

import rx.Observable;

public interface Connector {
    Observable<RxSocketConnection> prepareConnection();
}
