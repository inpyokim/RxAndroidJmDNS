package com.hoanglm.rxandroidjmdns.connection;

import com.hoanglm.rxandroidjmdns.network.TCPServer;
import com.jakewharton.rxrelay.BehaviorRelay;

import java.util.List;

import javax.jmdns.ServiceInfo;

import rx.Observable;

public interface ServiceConnector {
    Observable<ServiceConnector> startService(TCPServer serviceServer);
    void stopService();
    boolean restartService();
    BehaviorRelay<RxSocketService.RxSocketServiceState> getOnServiceConnectorState();
    RxSocketService.RxSocketServiceState getConnectorState();
    Observable<ServiceConnector> asErrorOnlyObservable();
    Observable<List<ServiceInfo>> getServiceDiscoveredChanged();
}
