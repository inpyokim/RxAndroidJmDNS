package com.hoanglm.rxandroidjmdns.dagger;

import com.hoanglm.rxandroidjmdns.connection.JmDNSConnector;
import com.hoanglm.rxandroidjmdns.service.AndroidDNSSetupHook;
import com.hoanglm.rxandroidjmdns.service.AndroidDNSSetupHookImpl;
import com.hoanglm.rxandroidjmdns.service.JmDNSConnectorImpl;

import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;

@ServiceConnectorScope
@Subcomponent(modules = {ServiceConnectorComponent.ServiceConnectorModule.class, ServiceConnectorComponent.ServiceConnectorModuleBinder.class})
public interface ServiceConnectorComponent {
    @Subcomponent.Builder
    interface Builder {
        ServiceConnectorComponent build();
        Builder serviceConnectorModule(ServiceConnectorModule module);
    }

    @Module
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
