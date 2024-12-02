package com.sysmatic2.finalbe.exception;

public class LiveAccountDataException extends RuntimeException {
    public LiveAccountDataException(String message) {
        super(message);
    }

    public LiveAccountDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
