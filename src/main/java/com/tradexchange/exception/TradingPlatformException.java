package com.tradexchange.exception;

public class TradingPlatformException extends RuntimeException {

    private final String errorCode;

    public TradingPlatformException(String message) {
        super(message);
        this.errorCode = message;
    }

    public TradingPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
