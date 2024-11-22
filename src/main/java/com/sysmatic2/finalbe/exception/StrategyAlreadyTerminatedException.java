package com.sysmatic2.finalbe.exception;

public class StrategyAlreadyTerminatedException extends RuntimeException {
    public StrategyAlreadyTerminatedException(String message) {
        super(message);
    }
}
