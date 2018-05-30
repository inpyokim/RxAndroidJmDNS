package com.hoanglm.rxandroidjmdns.jmdns_service;

import com.hoanglm.rxandroidjmdns.network.TCPServer;
import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;

import java.util.List;

import javax.jmdns.ServiceInfo;

import rx.Observable;

public interface JmDNSConnector {
    Observable<JmDNSConnector> startService(TCPServer serviceServer);
    void stopService();
    Observable<Boolean> restartService();
    Observable<JmDNSConnector> asErrorOnlyObservable();
    Observable<List<ServiceInfo>> getServiceDiscoveredChanged();
    RxSocketDevice getRxSocketDevice(ServiceInfo serviceInfo);
    String getHostAddress();
    int getPort();
}
