package com.hoanglm.rxandroidjmdns.utils;

public class RxJmDNSException extends RuntimeException {

    public RxJmDNSException() {
        super();
    }

    public RxJmDNSException(String message) {
        super(message);
    }

    public RxJmDNSException(Throwable throwable) {
        super(throwable);
    }

    String toStringCauseIfExists() {
        Throwable throwableCause = getCause();
        return (throwableCause != null ? ", cause=" + throwableCause.toString() : "");
    }
}
