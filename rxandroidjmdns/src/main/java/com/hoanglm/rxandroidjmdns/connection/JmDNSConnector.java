package com.hoanglm.rxandroidjmdns.connection;

import com.hoanglm.rxandroidjmdns.network.TCPServer;

import java.util.List;

import javax.jmdns.ServiceInfo;

import rx.Observable;

public interface JmDNSConnector {
    Observable<JmDNSConnector> startService(TCPServer serviceServer);
    void stopService();
    Observable<Boolean> restartService();
    Observable<JmDNSConnector> asErrorOnlyObservable();
    Observable<List<ServiceInfo>> getServiceDiscoveredChanged();
}
