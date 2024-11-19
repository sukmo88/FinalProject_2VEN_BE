package com.sysmatic2.finalbe.exception;

public class EmailVerificationFailedException extends RuntimeException {
    public EmailVerificationFailedException(String message) { super(message); }
}
