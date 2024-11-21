package com.sysmatic2.finalbe.attachment.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.HttpMethod;

import java.net.URL;
import java.util.Date;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ClientService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Presigned URL 생성
     */
    public String generatePresignedUrl(String fileName, String displayName) {
        // 유효성 검사
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }

        // 만료 시간 상수 정의
        final long PRESIGNED_URL_EXPIRATION_TIME_MILLIS = 1000 * 60 * 15; // 15분
        Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRATION_TIME_MILLIS);

        // ResponseHeaderOverrides 설정 (다운로드 파일 이름 지정)
        ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides()
                .withContentDisposition("attachment; filename=\"" + displayName + "\"");

        try {
            // Presigned URL 요청 생성
            GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileName)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration)
                    .withResponseHeaders(responseHeaders);

            // Presigned URL 생성
            URL presignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest);

            return presignedUrl.toString();
        } catch (AmazonServiceException e) {
            // AWS S3 서비스의 오류 처리
            throw new RuntimeException("Error generating presigned URL: " + e.getErrorMessage(), e);
        } catch (SdkClientException e) {
            // 네트워크 오류 또는 클라이언트 오류 처리
            throw new RuntimeException("Error generating presigned URL due to SDK client exception", e);
        }
    }

    /**
     * 파일 업로드
     */
    public Map<String, String> uploadFile(MultipartFile file, String userId, String category) {
        if (file == null || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("Invalid file input");
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = generateUniqueFileName(originalFileName);
        String s3Key = generateS3Key(userId, category, uniqueFileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucket, s3Key, file.getInputStream(), metadata));

            // S3 파일 URL 반환
            URL fileUrl = amazonS3.getUrl(bucket, s3Key);
            return Map.of(
                    "fileUrl", fileUrl.toString(),
                    "fileName", uniqueFileName,
                    "s3Key", s3Key
            );
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }

        //for test without uploading to S3
//        return Map.of(
//                    "fileUrl", "test-url",
//                    "fileName", uniqueFileName
//            );
    }


    /**
     * 파일 다운로드
     */
    public S3ObjectInputStream downloadFile(String fileName) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, fileName));
        return s3Object.getObjectContent();
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }

    /**
     * 고유한 파일 이름 생성
     */
    public String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }

    /**
     * 파일 경로 생성
     */
    public String generateS3Key(String userId, String category, String fileName) {
        // 파일 경로 생성
        return String.format("%s/%s/%s", userId, category, fileName);
    }

}
