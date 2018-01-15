package com.hoanglm.rxandroidjmdns.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.hoanglm.rxandroidjmdns.dagger.ServiceScope;

import javax.inject.Inject;

import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Cancellable;
import rx.internal.operators.OnSubscribeCreate;

@ServiceScope
public class RxWifiStateObservable extends Observable<RxWifiStateObservable.WifiState> {
    public enum WifiState {
        STATE_ON(true),
        STATE_OFF(false);

        private final boolean isUsable;

        WifiState(boolean isUsable) {
            this.isUsable = isUsable;
        }

        public boolean isUsable() {
            return isUsable;
        }
    }

    @Inject
    public RxWifiStateObservable(@NonNull Context context) {
        super(new OnSubscribeCreate<>(
                new Action1<Emitter<WifiState>>() {
                    @Override
                    public void call(final Emitter<WifiState> emitter) {
                        final BroadcastReceiver receiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                String action = intent.getAction();
                                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                                    emitter.onNext(mapToWifiState(NetworkUtil.isWifiConnected(context)));
                                }
                            }
                        };
                        context.registerReceiver(receiver, createFilter());
                        emitter.setCancellation(new Cancellable() {
                            @Override
                            public void cancel() throws Exception {
                                context.unregisterReceiver(receiver);
                            }
                        });
                    }
                },
                Emitter.BackpressureMode.LATEST
        ));
    }

    private static WifiState mapToWifiState(boolean wifiConnected) {
        if (wifiConnected) {
            return WifiState.STATE_ON;
        }
        return WifiState.STATE_OFF;
    }

    private static IntentFilter createFilter() {
        return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    }
}
