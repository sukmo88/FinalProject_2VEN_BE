package com.sysmatic2.finalbe.exception;

public class ConsultationAlreadyCompletedException extends RuntimeException {
  public ConsultationAlreadyCompletedException(String message) {
    super(message);
  }
}
