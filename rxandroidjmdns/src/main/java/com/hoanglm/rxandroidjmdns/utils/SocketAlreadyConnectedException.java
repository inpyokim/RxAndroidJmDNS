package com.hoanglm.rxandroidjmdns.utils;

public class SocketAlreadyConnectedException extends RxJmDNSException {
    public SocketAlreadyConnectedException(String ipAddress, int port) {
        super("Already connected to device with ipAddress = " + ipAddress + " port = " + port);
    }
}