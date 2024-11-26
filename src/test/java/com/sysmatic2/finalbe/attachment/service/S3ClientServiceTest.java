package com.sysmatic2.finalbe.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class S3ClientServiceTest {

    @InjectMocks
    private S3ClientService s3ClientService;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile file;

    private final String bucket = "test-bucket";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Reflection to set @Value property
        ReflectionTestUtils.setField(s3ClientService, "bucket", bucket);
    }

    @Test
    void testGenerateUniqueFileName() {
        String originalFileName = "test.jpg";
        String uniqueFileName = s3ClientService.generateUniqueFileName(originalFileName);

        assertThat(uniqueFileName).isNotNull();
        assertThat(uniqueFileName).endsWith(".jpg");
    }

    @Test
    void testGenerateS3Key() {
        String uploaderId = "testUploader";
        String category = "profile";
        String fileName = "test.jpg";

        String s3Key = s3ClientService.generateS3Key(uploaderId, category, fileName);

        assertThat(s3Key).isEqualTo("testUploader/profile/test.jpg");
    }

    @Test
    void testUploadFile_Success() throws IOException {
        String s3Key = "testUploader/profile/test.jpg";
        String fileUrl = "https://test-bucket.s3.amazonaws.com/" + s3Key;

        // Mock file behavior
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        // Mock AmazonS3 behavior
        when(amazonS3.getUrl(bucket, s3Key)).thenReturn(new java.net.URL(fileUrl));

        String result = s3ClientService.uploadFile(file, s3Key);

        assertThat(result).isEqualTo(fileUrl);

        // Verify that the correct methods were called
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testUploadFile_Failure() throws IOException {
        String s3Key = "testUploader/profile/test.jpg";

        when(file.getInputStream()).thenThrow(new IOException("Failed to read file"));

        assertThrows(RuntimeException.class, () -> s3ClientService.uploadFile(file, s3Key));

        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testDeleteFile_Success() {
        String s3Key = "testUploader/profile/test.jpg";

        // Act
        s3ClientService.deleteFile(s3Key);

        // Verify
        verify(amazonS3, times(1)).deleteObject(bucket, s3Key);
    }

    @Test
    void testDownloadFile_Success() throws IOException {
        String s3Key = "testUploader/profile/test.jpg";
        byte[] expectedBytes = "test".getBytes();

        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream inputStream = new S3ObjectInputStream(new ByteArrayInputStream(expectedBytes), null);

        when(amazonS3.getObject(bucket, s3Key)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(inputStream);

        byte[] result = s3ClientService.downloadFile(s3Key);

        assertThat(result).isEqualTo(expectedBytes);

        // Verify
        verify(amazonS3, times(1)).getObject(bucket, s3Key);
    }

    @Test
    void testDownloadFile_Failure() {
        String s3Key = "testUploader/profile/test.jpg";

        when(amazonS3.getObject(bucket, s3Key)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> s3ClientService.downloadFile(s3Key));

        verify(amazonS3, times(1)).getObject(bucket, s3Key);
    }
}