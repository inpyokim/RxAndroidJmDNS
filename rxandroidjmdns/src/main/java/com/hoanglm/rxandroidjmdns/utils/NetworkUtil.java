package com.hoanglm.rxandroidjmdns.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static boolean isWifiConnected(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        final NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            return info.isConnected()
                    && info.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }
}
