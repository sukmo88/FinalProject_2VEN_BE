package com.sysmatic2.finalbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


// 사용자 인증 시 비밀번호가 틀렸을 때 발생하는 예외
@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401 상태 코드 반환
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    /**
     * 메시지를 지정할 수 있는 생성자
     * @param message 사용자 정의 메시지
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}