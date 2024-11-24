package com.sysmatic2.finalbe.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class S3ClientServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3ClientService s3ClientService;

    private final String bucket = "test-bucket";
    private final String uploaderId = "test-user";
    private final String category = "profile";
    private final String fileName = "test-file.png";
    private final String uniqueFileName = UUID.randomUUID() + ".png";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(s3ClientService, "bucket", bucket); // bucket 값을 설정
    }

    /**
     * 고유한 파일 이름 생성 테스트
     */
    @Test
    void testGenerateUniqueFileName() {
        String originalFileName = "example.png";
        String result = s3ClientService.generateUniqueFileName(originalFileName);

        assertThat(result).isNotNull();
        assertThat(result).endsWith(".png");
    }

    /**
     * S3 키 생성 테스트
     */
    @Test
    void testGenerateS3Key() {
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, fileName);

        assertThat(s3Key).isEqualTo("test-user/profile/test-file.png");
    }

    /**
     * 파일 업로드 성공 테스트
     */
    @Test
    void testUploadFile_Success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        // Mock 파일 스트림, 크기, 콘텐츠 타입
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test-content".getBytes()));
        when(mockFile.getSize()).thenReturn(123L);
        when(mockFile.getContentType()).thenReturn("image/png");

        // Mock S3 URL 반환
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/" + s3Key;
        when(amazonS3.getUrl(bucket, s3Key)).thenReturn(new java.net.URL(expectedUrl));

        // Act
        String result = s3ClientService.uploadFile(mockFile, s3Key);

        // Assert
        assertThat(result).isEqualTo(expectedUrl);

        // ArgumentCaptor를 사용하여 PutObjectRequest를 캡처
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3, times(1)).putObject(captor.capture());

        // 캡처된 값 검증
        PutObjectRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getBucketName()).isEqualTo(bucket);
        assertThat(capturedRequest.getKey()).isEqualTo(s3Key);
        assertThat(capturedRequest.getMetadata().getContentType()).isEqualTo("image/png");
    }

    /**
     * 파일 업로드 실패 테스트
     */
    @Test
    void testUploadFile_Failure() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        when(mockFile.getInputStream()).thenThrow(new IOException("File read error"));

        assertThrows(RuntimeException.class, () -> s3ClientService.uploadFile(mockFile, s3Key));

        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    /**
     * 이미지 파일 다운로드 성공 테스트
     */
    @Test
    void testDownloadImageFileAsBase64_Success() throws IOException {
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);
        byte[] fileContent = "test-content".getBytes();

        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockInputStream = new S3ObjectInputStream(new ByteArrayInputStream(fileContent), null);

        when(mockS3Object.getObjectContent()).thenReturn(mockInputStream);
        when(amazonS3.getObject(bucket, s3Key)).thenReturn(mockS3Object);

        String base64Content = s3ClientService.downloadImageFileAsBase64(s3Key);

        assertThat(base64Content).isEqualTo(Base64.getEncoder().encodeToString(fileContent));
        verify(amazonS3, times(1)).getObject(bucket, s3Key);
    }

    /**
     * 이미지 파일 다운로드 실패 테스트
     */
    @Test
    void testDownloadImageFileAsBase64_S3ObjectNotFound() {
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        when(amazonS3.getObject(bucket, s3Key)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> s3ClientService.downloadImageFileAsBase64(s3Key));

        assertThat(exception.getMessage()).contains("Failed to access image file in S3");
        verify(amazonS3, times(1)).getObject(bucket, s3Key);
    }

    /**
     * 문서 파일 다운로드 성공 테스트
     */
    @Test
    void testDownloadDocumentFile_Success() throws IOException {
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);
        byte[] fileContent = "test-content".getBytes();

        S3Object mockS3Object = mock(S3Object.class);
        when(mockS3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(fileContent), null));
        when(amazonS3.getObject(bucket, s3Key)).thenReturn(mockS3Object);

        byte[] result = s3ClientService.downloadDocumentFile(s3Key);

        assertThat(result).isEqualTo(fileContent);
        verify(amazonS3, times(1)).getObject(bucket, s3Key);
    }

    /**
     * 문서 파일 다운로드 실패 테스트
     */
    @Test
    void testDownloadDocumentFile_S3ObjectNotFound() {
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        // Mock S3Client to return null for S3Object
        when(amazonS3.getObject(bucket, s3Key)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> s3ClientService.downloadDocumentFile(s3Key));

        // Validate the exception message
        assertThat(exception.getMessage()).contains("Failed to access document file in S3: S3 object is null");
        verify(amazonS3, times(1)).getObject(bucket, s3Key);
    }

    /**
     * 파일 삭제 성공 테스트
     */
    @Test
    void testDeleteFile_Success() {
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, fileName);

        s3ClientService.deleteFile(s3Key);

        verify(amazonS3, times(1)).deleteObject(bucket, s3Key);
    }
}