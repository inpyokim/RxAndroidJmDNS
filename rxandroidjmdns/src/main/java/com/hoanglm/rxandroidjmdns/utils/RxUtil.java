package com.hoanglm.rxandroidjmdns.utils;


import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public final class RxUtil {

    public static <T> Observable.Transformer<T, T> timeoutJustFirstEmit(long timeout, TimeUnit timeUnit) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .timeout(() -> Observable.timer(timeout, timeUnit)
                                .cast(Object.class), aLong -> Observable.never())
                        .subscribeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static RxRetry retry() {
        return new RxRetry();
    }
}
