package com.sysmatic2.finalbe.exception;

public class S3FileDeleteFiledException extends RuntimeException{
    public S3FileDeleteFiledException(String message) {super(message);}

    public S3FileDeleteFiledException(String message, Throwable cause) {super(message, cause);}

}
