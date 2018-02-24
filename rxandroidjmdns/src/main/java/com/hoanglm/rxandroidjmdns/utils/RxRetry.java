package com.hoanglm.rxandroidjmdns.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

public class RxRetry implements Func1<Observable<? extends Throwable>, Observable<?>> {
    private static final long DELAY_RETRY = 500L;
    private static final int RETRY_MAX = 3;

    private int retryCount;
    private int retryMax;
    private boolean retryForever;

    public RxRetry() {
        this.retryCount = 0;
        this.retryMax = RETRY_MAX;
    }

    public RxRetry(int retryMax) {
        this.retryCount = 0;
        this.retryMax = retryMax;
    }

    public RxRetry(boolean retryForever) {
        this.retryCount = 0;
        this.retryMax = 0;
        this.retryForever = retryForever;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .flatMap((Func1<Throwable, Observable<?>>) throwable -> {
                    if (retryCount >= retryMax && !retryForever) {
                        return Observable.error(throwable);
                    }
                    retryCount++;
                    return Observable.timer(DELAY_RETRY, TimeUnit.MILLISECONDS);
                });
    }
}
