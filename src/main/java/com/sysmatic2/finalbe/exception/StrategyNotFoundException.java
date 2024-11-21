package com.sysmatic2.finalbe.exception;

/**
 * 전략을 찾을 수 없을 때 발생하는 예외
 */
public class StrategyNotFoundException extends RuntimeException {
  public StrategyNotFoundException(String message) {
    super(message);
  }
}
