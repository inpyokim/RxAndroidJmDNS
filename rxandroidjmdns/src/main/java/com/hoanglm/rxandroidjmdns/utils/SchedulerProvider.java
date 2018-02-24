package com.hoanglm.rxandroidjmdns.utils;

import rx.Scheduler;

public interface SchedulerProvider {
    Scheduler ui();
    Scheduler computation();
    Scheduler io();
}
