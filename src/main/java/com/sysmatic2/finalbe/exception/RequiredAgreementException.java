package com.sysmatic2.finalbe.exception;

public class RequiredAgreementException extends RuntimeException {

    public RequiredAgreementException(String message) {
        super(message);
    }

    public RequiredAgreementException(String message, Throwable cause) {
        super(message, cause);
    }
}