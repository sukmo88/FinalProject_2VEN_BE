package com.sysmatic2.finalbe.exception;


/* 실계좌 인증에서 등록한 파일 이름이 바른 형식이 아닐때 예외*/
public class InvalidFileNameException extends RuntimeException {
    public InvalidFileNameException(String message) {
        super(message);
    }
}
