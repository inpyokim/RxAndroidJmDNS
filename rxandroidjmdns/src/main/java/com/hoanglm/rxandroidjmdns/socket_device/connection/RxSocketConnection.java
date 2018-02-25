package com.hoanglm.rxandroidjmdns.socket_device.connection;

import android.support.annotation.NonNull;

import rx.Observable;

public interface RxSocketConnection {
    enum RxSocketConnectionState {
        CONNECTED("CONNECTED"),
        CONNECTING("CONNECTING"),
        DISCONNECTED("DISCONNECTED");
        private final String description;

        RxSocketConnectionState(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "RxSocketConnectionState{" + description + '}';
        }
    }

    Observable<byte[]> sendMessage(@NonNull byte[] data);
    Observable<byte[]> setupReceivedMessage();
}
