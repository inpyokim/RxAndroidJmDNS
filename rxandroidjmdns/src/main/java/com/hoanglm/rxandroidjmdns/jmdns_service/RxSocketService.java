package com.hoanglm.rxandroidjmdns.jmdns_service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hoanglm.rxandroidjmdns.dagger.DaggerRxSocketServiceComponent;
import com.hoanglm.rxandroidjmdns.dagger.RxSocketServiceComponent;
import com.hoanglm.rxandroidjmdns.socket_device.RxSocketDevice;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

import rx.Observable;

public abstract class RxSocketService {
    public enum RxSocketServiceState {
        READY("READY"),
        SETUP_SUCCESS("SETUP_SUCCESS"),
        RESTART_SUCCESS("RESTART_SUCCESS"),
        STOP_SUCCESS("STOP_SUCCESS");

        private final String description;

        RxSocketServiceState(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "RxSocketServiceState{" + description + '}';
        }
    }

    public static RxSocketService create(@NonNull Context context) {
        return DaggerRxSocketServiceComponent
                .builder()
                .rxSocketServiceModule(new RxSocketServiceComponent.RxSocketServiceModule(context))
                .build()
                .rxSocketService();
    }

    public static void setLogLevel(@RxJmDNSLog.LogLevel int logLevel) {
        RxJmDNSLog.setLogLevel(logLevel);
    }

    public abstract Observable<JmDNSConnector> setup(boolean autoSetup);

    public abstract void stop();

    public abstract Observable<RxSocketService.RxSocketServiceState> observeServiceStateChanges();

    public abstract RxSocketService.RxSocketServiceState getServiceStateChanges();

    public abstract RxSocketDevice getSocketDevice(String ipAddress, int port);
}
