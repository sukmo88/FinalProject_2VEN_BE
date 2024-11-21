package com.sysmatic2.finalbe.exception;

/**
 * 투자자를 찾을 수 없을 때 발생하는 예외
 */
public class InvestorNotFoundException extends RuntimeException {
  public InvestorNotFoundException(String message) {
    super(message);
  }
}