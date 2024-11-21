package com.sysmatic2.finalbe.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ClientServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3ClientService s3ClientService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket = "test-bucket"; // 목 환경용 bucket 값 설정

    @BeforeEach
    void setup() {
        // Inject the bucket value manually
        ReflectionTestUtils.setField(s3ClientService, "bucket", bucket);
    }

    @BeforeEach
    void setUp() {
        amazonS3 = mock(AmazonS3.class);
        s3ClientService = new S3ClientService(amazonS3);
    }

    @Test
    void testGeneratePresignedUrl() {
        // Arrange
        String fileName = "test-file.txt";
        String displayName = "user-friendly-name.txt";
        URL mockUrl = Mockito.mock(URL.class);
        when(mockUrl.toString()).thenReturn("https://test-bucket.s3.amazonaws.com/test-file.txt");

        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);

        // Act
        String result = s3ClientService.generatePresignedUrl(fileName, displayName);

        // Assert
        assertThat(result).isEqualTo(mockUrl.toString());
        verify(amazonS3, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    void testUploadFile_success() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String userId = "user123";
        String category = "images";
        String fileName = "test-file.png";

        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1024]));

        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/user123/images/test-file.png");
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(mockUrl);

        // Act
        Map<String, String> result = s3ClientService.uploadFile(mockFile, userId, category);

        // Assert
        assertThat(result.get("fileUrl")).isEqualTo(mockUrl.toString());
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testUploadFile_invalidFile() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> s3ClientService.uploadFile(mockFile, "user123", "images"));
    }

    @Test
    void testDeleteFile_success() {
        // Arrange
        String fileName = "user123/images/test-file.png";

        // Act
        s3ClientService.deleteFile(fileName);

        // Assert
        verify(amazonS3, times(1)).deleteObject(bucket, fileName);
    }

    @Test
    void testDownloadFile_success() throws Exception {
        // Arrange
        String fileName = "user123/images/test-file.png";
        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockInputStream = new S3ObjectInputStream(new ByteArrayInputStream(new byte[1024]), null);

        when(mockS3Object.getObjectContent()).thenReturn(mockInputStream);
        when(amazonS3.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);

        // Act
        S3ObjectInputStream result = s3ClientService.downloadFile(fileName);

        // Assert
        assertThat(result).isNotNull();
        verify(amazonS3, times(1)).getObject(any(GetObjectRequest.class));
    }
}
