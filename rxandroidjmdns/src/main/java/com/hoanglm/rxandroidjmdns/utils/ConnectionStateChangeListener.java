package com.hoanglm.rxandroidjmdns.utils;

import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;

public interface ConnectionStateChangeListener {
    void onConnectionStateChange(RxSocketConnection.RxSocketConnectionState rxSocketConnectionState);
}