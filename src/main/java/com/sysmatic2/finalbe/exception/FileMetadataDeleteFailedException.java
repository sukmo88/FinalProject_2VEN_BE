package com.sysmatic2.finalbe.exception;

public class FileMetadataDeleteFailedException extends RuntimeException {
    public FileMetadataDeleteFailedException(String message) {
        super(message);
    }

    public FileMetadataDeleteFailedException(String message, Throwable cause) {super(message, cause);}
}