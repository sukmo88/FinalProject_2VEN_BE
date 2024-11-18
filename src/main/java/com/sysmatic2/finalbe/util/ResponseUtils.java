package com.sysmatic2.finalbe.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

public class ResponseUtils {

    /**
     * 공통 에러 응답 생성 메서드
     *
     * @param error     에러 코드
     * @param errorType 예외 타입 (클래스명)
     * @param message   사용자에게 전달할 메시지
     * @param status    HTTP 상태 코드
     * @return ResponseEntity<Object>
     */
    public static ResponseEntity<Object> buildErrorResponse(String error, String errorType, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "error", error,
                "errorType", errorType,
                "message", message,
                "timestamp", Instant.now()
        ));
    }

    /**
     * 필드별 에러를 포함한 응답 생성
     *
     * @param fieldErrors 필드별 에러 메시지
     * @param errorType   예외 타입 (클래스명)
     * @param message     사용자에게 전달할 메시지
     * @param status      HTTP 상태 코드
     * @return ResponseEntity<Object>
     */
    public static ResponseEntity<Object> buildFieldErrorResponse(Map<String, String> fieldErrors, String errorType, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "errors", fieldErrors,
                "errorType", errorType,
                "message", message,
                "timestamp", Instant.now()
        ));
    }
}