package com.hoanglm.rxandroidjmdns.socket_device;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;

import rx.Observable;

public interface RxSocketDevice {
    String getIpAddress();

    int getPort();

    Observable<RxSocketConnection.RxSocketConnectionState> observeConnectionStateChanges();

    RxSocketConnection.RxSocketConnectionState getConnectionState();

    Observable<RxSocketConnection> establishConnection(Context context);
}
