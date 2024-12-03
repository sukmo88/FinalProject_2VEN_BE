package com.sysmatic2.finalbe.exception;

public class StrategyReviewFailedException extends RuntimeException{
    public StrategyReviewFailedException(String message){super(message);}

    public StrategyReviewFailedException(String message, Throwable cause){super(message, cause);}
}
