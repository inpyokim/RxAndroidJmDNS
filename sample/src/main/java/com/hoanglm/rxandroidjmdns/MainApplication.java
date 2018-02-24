package com.hoanglm.rxandroidjmdns;

import android.app.Application;

import com.hoanglm.rxandroidjmdns.jmdns_service.RxSocketService;
import com.hoanglm.rxandroidjmdns.utils.RxJmDNSLog;

/**
 * Created by hoanglm on 10/28/17.
 */

public class MainApplication extends Application {
    private RxSocketService mRxSocketService;
    @Override
    public void onCreate() {
        super.onCreate();
        RxJmDNSLog.setLogLevel(RxJmDNSLog.VERBOSE);

        mRxSocketService = RxSocketService.create(this);
    }

    public RxSocketService getRxSocketService() {
        return mRxSocketService;
    }
}
