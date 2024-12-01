package com.sysmatic2.finalbe.exception;


/* S3 업로드 실패시 발생하는 예외 */
public class S3FileUploadFailedException extends RuntimeException {
    public S3FileUploadFailedException(String message) {
        super(message);
    }
}
