package com.sysmatic2.finalbe.exception;

/**
 * 트레이더를 찾을 수 없을 때 발생하는 예외
 */
public class TraderNotFoundException extends RuntimeException {
  public TraderNotFoundException(String message) { super(message); }
}