package com.hoanglm.rxandroidjmdns.socket_device.connection;

import com.hoanglm.rxandroidjmdns.dagger.RxSocketConnectionComponent;
import com.hoanglm.rxandroidjmdns.utils.ConnectionStateChangeListener;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSDisconnectException;
import com.hoanglm.rxandroidjmdns.utils.RxSocketDisconnectException;
import com.hoanglm.rxandroidjmdns.utils.SchedulerProvider;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Provider;

import rx.Observable;

public class ConnectorImpl implements Connector {
    private Provider<RxSocketConnectionComponent.Builder> mRxSocketConnectionBuilder;
    private DisconnectionRouter mDisconnectionRouter;
    private final SchedulerProvider mSchedulerProvider;
    private final ConnectionStateChangeListener mConnectionStateChangeListener;

    @Inject
    public ConnectorImpl(DisconnectionRouter disconnectionRouter,
                         SchedulerProvider schedulerProvider,
                         ConnectionStateChangeListener connectionStateChangeListener,
                         Provider<RxSocketConnectionComponent.Builder> builderProvider) {
        mDisconnectionRouter = disconnectionRouter;
        mSchedulerProvider = schedulerProvider;
        mConnectionStateChangeListener = connectionStateChangeListener;
        mRxSocketConnectionBuilder = builderProvider;
    }

    @Override
    public Observable<RxSocketConnection> prepareConnection() {
        RxSocketConnectionComponent connectionComponent = mRxSocketConnectionBuilder.get()
                .socketConnectionComponent(new RxSocketConnectionComponent.RxSocketConnectionModule())
                .build();

        final Observable<RxSocketConnection> newConnectionObservable = Observable.fromCallable(new Callable<RxSocketConnection>() {
            @Override
            public RxSocketConnection call() throws Exception {
                return connectionComponent.rxSocketConnection();
            }
        });

        Observable<Boolean> connectedObservable = connectionComponent.provideConnectionSocket().getConnected();
        Observable<RxSocketConnection> disconnectedErrorObservable = mDisconnectionRouter.asErrorOnlyObservable();
        Observable<RxSocketConnection> disconnectedSecondErrorObservable = connectionComponent.rxConnectionChecker().asErrorOnlyObservable();


        return Observable.merge(newConnectionObservable.delaySubscription(connectedObservable),
                disconnectedErrorObservable,
                disconnectedSecondErrorObservable)
                .doOnUnsubscribe(() -> {
                    mConnectionStateChangeListener.onConnectionStateChange(RxSocketConnection.RxSocketConnectionState.DISCONNECTED);
                    connectionComponent.rxConnectionChecker().onDisconnectedException(new RxSocketDisconnectException());
                })
                .subscribeOn(mSchedulerProvider.io())
                .unsubscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui());
    }
}
