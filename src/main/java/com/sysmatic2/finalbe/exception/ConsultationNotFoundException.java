package com.sysmatic2.finalbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 리소스 찾기 예외
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ConsultationNotFoundException extends RuntimeException {
  public ConsultationNotFoundException(String message) {
    super(message);
  }
}
