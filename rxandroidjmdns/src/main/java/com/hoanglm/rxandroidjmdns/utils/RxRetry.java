package com.hoanglm.rxandroidjmdns.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

public class RxRetry implements Func1<Observable<? extends Throwable>, Observable<?>> {
    private static final long DELAY_RETRY = 500L;

    private boolean mCanRetry;

    public RxRetry(boolean canRetry) {
        mCanRetry = canRetry;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .flatMap((Func1<Throwable, Observable<?>>) throwable -> {
                    RxJmDNSLog.e(throwable, "call>>");
                    if (!mCanRetry) {
                        return Observable.error(throwable);
                    }
                    return Observable.timer(DELAY_RETRY, TimeUnit.MILLISECONDS);
                });
    }
}
