package com.hoanglm.rxandroidjmdns.dagger;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.jmdns_service.RxSocketService;
import com.hoanglm.rxandroidjmdns.jmdns_service.RxSocketServiceImpl;
import com.hoanglm.rxandroidjmdns.socket_device.connection.DisconnectionRouter;
import com.hoanglm.rxandroidjmdns.utils.AppSchedulerProvider;
import com.hoanglm.rxandroidjmdns.utils.RxWifiStateObservable;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;
import com.jakewharton.rxrelay.BehaviorRelay;

import java.util.HashMap;
import java.util.Map;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import rx.Observable;

@ServiceScope
@Component(modules = {RxSocketServiceComponent.RxSocketServiceModule.class, RxSocketServiceComponent.RxSocketServiceModuleBinder.class})
public interface RxSocketServiceComponent {

    @Module(subcomponents = {ServiceConnectorComponent.class, RxSocketDeviceComponent.class})
    class RxSocketServiceModule {
        private final Context context;

        public RxSocketServiceModule(Context context) {
            this.context = context;
        }

        @Provides
        Context provideApplicationContext() {
            return context;
        }

        @Provides
        @ServiceScope
        static BehaviorRelay<RxSocketService.RxSocketServiceState>  provideRxSocketServiceStateRelay() {
            return BehaviorRelay.create(RxSocketService.RxSocketServiceState.READY);
        }

        @Provides
        @ServiceScope
        SchedulerProvider provideScheduler() {
            return new AppSchedulerProvider();
        }

        @Provides
        Map<String, RxSocketDeviceComponent> providerCachedRxSocketDeviceComponent() {
            return new HashMap<>();
        }

        @Provides
        DisconnectionRouter bindDisconnectionRouter(Context context, RxWifiStateObservable stateObservable) {
            return new DisconnectionRouter(context, stateObservable);
        }
    }

    @Module
    abstract class RxSocketServiceModuleBinder {

        @Binds
        @ServiceScope
        abstract Observable<RxWifiStateObservable.WifiState> bindWifiState(RxWifiStateObservable stateObservable);

        @Binds
        @ServiceScope
        abstract RxSocketService bindRxSocketService(RxSocketServiceImpl rxSocketService);
    }

    RxSocketService rxSocketService();
}
