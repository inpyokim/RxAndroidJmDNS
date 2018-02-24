package com.hoanglm.rxandroidjmdns.dagger;

import com.hoanglm.rxandroidjmdns.socket_device.connection.ConnectionSocket;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxConnectionChecker;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxConnectionCheckerImpl;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnection;
import com.hoanglm.rxandroidjmdns.socket_device.connection.RxSocketConnectionImpl;

import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;

@ConnectionScope
@Subcomponent(modules = {RxSocketConnectionComponent.RxSocketConnectionModule.class,
        RxSocketConnectionComponent.ConnectionModuleBinder.class})
public interface RxSocketConnectionComponent {
    @Subcomponent.Builder
    interface Builder {
        RxSocketConnectionComponent build();

        Builder socketConnectionComponent(RxSocketConnectionModule module);
    }

    @Module
    class RxSocketConnectionModule {

    }

    @Module
    abstract class ConnectionModuleBinder {
        @Binds
        abstract RxConnectionChecker bindRxConnectionChecker(RxConnectionCheckerImpl rxConnectionChecker);

        @Binds
        abstract RxSocketConnection bindRxSocketConnection(RxSocketConnectionImpl rxSocketConnection);
    }

    RxSocketConnection rxSocketConnection();

    RxConnectionChecker rxConnectionChecker();

    @ConnectionScope
    ConnectionSocket provideConnectionSocket();
}
