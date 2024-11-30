package com.sysmatic2.finalbe.exception;

public class MemberTermNotFoundException extends RuntimeException {

    public MemberTermNotFoundException() {
        super("MemberTerm 데이터를 찾을 수 없습니다.");
    }

    public MemberTermNotFoundException(String message) {
        super(message);
    }

    public MemberTermNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}