package com.sysmatic2.finalbe.exception;

public class FileMetadataNotFoundException extends RuntimeException {
    public FileMetadataNotFoundException(String message) {
        super(message);
    }

    public FileMetadataNotFoundException(String message, Throwable cause) {super(message, cause);}
}