package com.sysmatic2.finalbe.exception;

public class FolderDeletePermissionException extends RuntimeException {

    public FolderDeletePermissionException(String message) {
        super(message);
    }

    public FolderDeletePermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}