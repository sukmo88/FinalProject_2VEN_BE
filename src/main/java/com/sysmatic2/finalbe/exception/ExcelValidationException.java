package com.sysmatic2.finalbe.exception;

public class ExcelValidationException extends RuntimeException {
  public ExcelValidationException(String message) {
    super(message);
  }

  public ExcelValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
