package com.sysmatic2.finalbe.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {}
    public MemberNotFoundException(String message) { super(message); }
}
