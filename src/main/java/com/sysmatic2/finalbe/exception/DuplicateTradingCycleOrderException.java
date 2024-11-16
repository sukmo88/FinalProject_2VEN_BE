package com.sysmatic2.finalbe.exception;

public class DuplicateTradingCycleOrderException extends RuntimeException {
    public DuplicateTradingCycleOrderException(Integer order) {
        super("이미 존재하는 투자주기 순서입니다. Order: " + order);
    }
}