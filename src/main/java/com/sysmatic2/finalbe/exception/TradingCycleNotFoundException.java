package com.sysmatic2.finalbe.exception;


public class TradingCycleNotFoundException extends RuntimeException {
    public TradingCycleNotFoundException(Integer id) {
        super("투자주기가 존재하지 않습니다. ID: " + id);
    }
}