package com.sysmatic2.finalbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 날짜 중복 발생 시 던지는 사용자 정의 예외 클래스
 */
public class DuplicateDateException extends ResponseStatusException {
    public DuplicateDateException(String message) {
        super(HttpStatus.BAD_REQUEST, message); // HTTP 400 반환
    }
}