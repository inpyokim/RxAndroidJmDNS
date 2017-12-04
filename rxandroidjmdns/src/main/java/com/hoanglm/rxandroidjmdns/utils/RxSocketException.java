package com.hoanglm.rxandroidjmdns.utils;

public class RxSocketException extends RuntimeException {

    public RxSocketException() {
        super();
    }

    public RxSocketException(String message) {
        super(message);
    }

    public RxSocketException(Throwable throwable) {
        super(throwable);
    }

    String toStringCauseIfExists() {
        Throwable throwableCause = getCause();
        return (throwableCause != null ? ", cause=" + throwableCause.toString() : "");
    }
}
