package com.hoanglm.rxandroidjmdns.connection;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hoanglm.rxandroidjmdns.dagger.DaggerRxSocketServiceComponent;
import com.hoanglm.rxandroidjmdns.dagger.RxSocketServiceComponent;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

import java.util.List;

import javax.jmdns.ServiceInfo;

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

    public abstract Observable<Boolean> setup(boolean autoSetup);

    public abstract Observable<Boolean> restart();

    public abstract void stop();

    public abstract Observable<List<ServiceInfo>> getConnectedSockets();

    public abstract Observable<List<ServiceInfo>> getOnConnectedSockets();

    public abstract Observable<List<ServiceInfo>> getOnServiceInfoDiscovery();

    public abstract Observable<RxSocketService.RxSocketServiceState> observeServiceStateChanges();

    public abstract RxSocketService.RxSocketServiceState getServiceStateChanges();
}
