package com.hoanglm.rxandroidjmdns.dagger;

import com.hoanglm.rxandroidjmdns.jmdns_service.JmDNSConnector;
import com.hoanglm.rxandroidjmdns.jmdns_service.AndroidDNSSetupHook;
import com.hoanglm.rxandroidjmdns.jmdns_service.AndroidDNSSetupHookImpl;
import com.hoanglm.rxandroidjmdns.jmdns_service.JmDNSConnectorImpl;
import com.hoanglm.rxandroidjmdns.socket_device.connection.DisconnectionRouter;

import java.util.HashMap;
import java.util.Map;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@ServiceConnectorScope
@Subcomponent(modules = {ServiceConnectorComponent.ServiceConnectorModule.class, ServiceConnectorComponent.ServiceConnectorModuleBinder.class})
public interface ServiceConnectorComponent {
    @Subcomponent.Builder
    interface Builder {
        ServiceConnectorComponent build();
        Builder serviceConnectorModule(ServiceConnectorModule module);
    }

    @Module(subcomponents = RxSocketDeviceComponent.class)
    class ServiceConnectorModule {

    }

    @Module
    abstract class ServiceConnectorModuleBinder {
        @Binds
        @ServiceConnectorScope
        abstract AndroidDNSSetupHook bindAndroidDNSSetupHook(AndroidDNSSetupHookImpl androidDNSSetupHookImpl);

        @Binds
        @ServiceConnectorScope
        abstract JmDNSConnector bindServiceConnector(JmDNSConnectorImpl serviceConnectorImpl);
    }

    JmDNSConnector providerServiceConnector();
}
