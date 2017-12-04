package com.hoanglm.rxandroidjmdns.dagger;

import android.content.Context;

import com.hoanglm.rxandroidjmdns.connection.RxSocketService;
import com.hoanglm.rxandroidjmdns.connection.RxSocketServiceImpl;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@ServiceScope
@Component(modules = {RxSocketServiceComponent.RxSocketServiceModule.class, RxSocketServiceComponent.RxSocketServiceModuleBinder.class})
public interface RxSocketServiceComponent {

    @Module(subcomponents = ServiceConnectorComponent.class)
    class RxSocketServiceModule {
        private final Context context;

        public RxSocketServiceModule(Context context) {
            this.context = context;
        }

        @Provides
        Context provideApplicationContext() {
            return context;
        }
    }

    @Module
    abstract class RxSocketServiceModuleBinder {

        @Binds
        @ServiceScope
        abstract RxSocketService bindRxBleClient(RxSocketServiceImpl rxSocketService);
    }

    RxSocketService rxSocketService();
}
