package com.hoanglm.rxandroidjmdns.utils;

public class SetupServiceException extends RxSocketException {
    public enum Reason {
        NOT_DETECTED_IP,
        CAN_NOT_SETUP_MULTICAST,
        SERVER_SETUP_FAILED,
        DISCONNECTED_SERVICE
    }

    private Reason reason;
    private String message;

    public SetupServiceException(Reason reason, String message) {
        super();
        this.reason = reason;
        this.message = message;
    }

    @Override
    public String toString() {
        return "SetupServiceException{"
                + ", reason=" + reason.toString()
                + ", message=" + message
                + toStringCauseIfExists()
                + '}';
    }
}
