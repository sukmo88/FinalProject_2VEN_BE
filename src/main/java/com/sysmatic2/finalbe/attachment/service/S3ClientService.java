package com.sysmatic2.finalbe.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ClientService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 고유한 파일 이름 생성
     */
    public String generateUniqueFileName(String originalFileName) {
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + fileExtension;
    }

    /**
     * S3 키 생성
     */
    public String generateS3Key(String uploaderId, String category, String fileName) {
        return String.format("%s/%s/%s", uploaderId, category, fileName);
    }

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String s3Key) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, s3Key, file.getInputStream(), metadata));
            return amazonS3.getUrl(bucket, s3Key).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * 이미지 파일을 다운로드하고 Base64로 인코딩
     */
    public String downloadImageFileAsBase64(String s3Key) {
        try (S3Object s3Object = amazonS3.getObject(bucket, s3Key);
             S3ObjectInputStream inputStream = (s3Object != null ? s3Object.getObjectContent() : null)) {

            if (s3Object == null || inputStream == null) {
                throw new RuntimeException("S3 Error: Failed to access image file in S3: S3 object is null");
            }

            // S3ObjectInputStream을 바이트 배열로 변환
            byte[] fileBytes = inputStream.readAllBytes();

            // Base64로 인코딩하여 반환
            return Base64.getEncoder().encodeToString(fileBytes);

        } catch (AmazonS3Exception e) {
            throw new RuntimeException("S3 Error: Failed to access image file in S3", e);
        } catch (IOException e) {
            throw new RuntimeException("IO Error: Failed to read image file content from S3", e);
        }
    }

    /**
     * 문서 파일을 다운로드하여 바이너리 데이터로 반환
     */
    public byte[] downloadDocumentFile(String s3Key) {
        try {
            S3Object s3Object = amazonS3.getObject(bucket, s3Key);

            // S3Object가 null인 경우 예외 처리
            if (s3Object == null) {
                throw new RuntimeException("Failed to access document file in S3: S3 object is null");
            }

            try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                return inputStream.readAllBytes();
            }
        } catch (AmazonS3Exception e) {
            throw new RuntimeException("S3 Error: Failed to access document file in S3", e);
        } catch (IOException e) {
            throw new RuntimeException("IO Error: Failed to read document file content from S3", e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String s3Key) {
        amazonS3.deleteObject(bucket, s3Key);
    }
}
