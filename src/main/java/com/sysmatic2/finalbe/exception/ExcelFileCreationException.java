package com.sysmatic2.finalbe.exception;

public class ExcelFileCreationException extends RuntimeException {
  public ExcelFileCreationException(String message) {
    super(message);
  }

  public ExcelFileCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}