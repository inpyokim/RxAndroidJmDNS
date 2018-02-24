package com.hoanglm.rxandroidjmdns.jmdns_service;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.hoanglm.rxandroidjmdns.dagger.ServiceConnectorScope;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

import javax.inject.Inject;

@ServiceConnectorScope
public class AndroidDNSSetupHookImpl implements AndroidDNSSetupHook {

    private Context mContext;
    /**
     * Required for WiFi multicast communication.
     */
    private WifiManager.MulticastLock mMulticastLock;

    @Inject
    public AndroidDNSSetupHookImpl(Context androidContext) {
        mContext = androidContext;
    }

    @Override
    public boolean setup() {
        // Acquire mMulticastLock for multicast communication.
        WifiManager wifi = (android.net.wifi.WifiManager)
                mContext.getSystemService(android.content.Context.WIFI_SERVICE);
        mMulticastLock = wifi.createMulticastLock(getClass().getName());
        mMulticastLock.setReferenceCounted(true);
        if (!mMulticastLock.isHeld()) {
            mMulticastLock.acquire();
        } else {
            RxJmDNSLog.i("Muticast lock already held...");
        }
        return true;
    }

    @Override
    public boolean teardown() {
        if (mMulticastLock != null) {
            RxJmDNSLog.i("Releasing multicast mMulticastLock");
            if (mMulticastLock.isHeld()) {
                mMulticastLock.release();
            } else {
                RxJmDNSLog.i("Multicast lock already released");
            }
            mMulticastLock = null;
        }
        return true;
    }
}
