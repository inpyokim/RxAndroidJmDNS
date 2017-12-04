package com.hoanglm.rxandroidjmdns.connection;

import rx.Observable;

public interface Connector {
    Observable<RxSocketConnection> prepareConnection(ServiceSetup autoConnect);
}
