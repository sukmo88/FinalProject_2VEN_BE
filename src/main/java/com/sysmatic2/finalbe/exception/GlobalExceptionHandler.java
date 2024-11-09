package com.sysmatic2.finalbe.exception;

import org.apache.coyote.BadRequestException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.MethodNotAllowedException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    //500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map> handleException(Exception e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
    }

    //400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map> handleBadRequestException(BadRequestException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    //400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    //400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    //400
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    //401 - 미인증(Spring Security 자동 예외 발생)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map> handleAuthenticationException(AuthenticationException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "UNAUTHENTICATED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
    }

    //403 - 비인가(Spring Security 자동 예외 발생)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map> handleAccessDeniedException(AccessDeniedException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "UNAUTHORIZED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
    }

    //404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map> handleNoSuchElementException(NoSuchElementException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);
    }

    //405
    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<Map> handleMethodNotAllowedException(MethodNotAllowedException e) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "METHOD_NOT_ALLOWED");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMap);
    }
}

