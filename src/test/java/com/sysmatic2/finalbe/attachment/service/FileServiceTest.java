package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private S3ClientService s3ClientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile_Success() {
        MultipartFile file = mock(MultipartFile.class);
        String uploaderId = "testUploader";
        String category = "profile";
        String fileCategoryItemId = "123";

        FileMetadata savedMetadata = new FileMetadata();
        savedMetadata.setId(1L);
        savedMetadata.setUploaderId(uploaderId);
        savedMetadata.setFileCategory(category);

        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(s3ClientService.generateUniqueFileName(anyString())).thenReturn("unique-test.jpg");
        when(s3ClientService.generateS3Key(anyString(), anyString(), anyString())).thenReturn("s3/key/unique-test.jpg");
        when(s3ClientService.uploadFile(eq(file), anyString())).thenReturn("https://s3.amazonaws.com/test.jpg");
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(savedMetadata);

        FileMetadataDto result = fileService.uploadFile(file, uploaderId, category, fileCategoryItemId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(s3ClientService, times(1)).uploadFile(eq(file), anyString());
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
    }

    @Test
    void testUploadFile_Failure() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        String uploaderId = "testUploader";
        String category = "profile";

        // Mock behavior
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(s3ClientService.generateUniqueFileName("test.jpg")).thenReturn("unique-test.jpg");
        when(s3ClientService.generateS3Key(uploaderId, category, "unique-test.jpg"))
                .thenReturn("s3/key/unique-test.jpg");
        when(s3ClientService.uploadFile(eq(file), eq("s3/key/unique-test.jpg")))
                .thenThrow(new RuntimeException("S3 Upload Failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> fileService.uploadFile(file, uploaderId, category, null));

        // Verify
        verify(s3ClientService, times(1)).uploadFile(eq(file), eq("s3/key/unique-test.jpg"));
        verify(fileMetadataRepository, never()).save(any(FileMetadata.class));
    }

    @Test
    void testDownloadFile_ReturnsBase64ForImage() {
        Long fileId = 1L;
        String uploaderId = "testUploader";
        String category = "profile";

        FileMetadata metadata = new FileMetadata();
        metadata.setUploaderId(uploaderId);
        metadata.setFileCategory(category);
        metadata.setFileName("test.jpg");
        metadata.setContentType("image/jpeg");

        byte[] fileBytes = new byte[]{1, 2, 3};
        String base64String = "AQID";

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        when(s3ClientService.generateS3Key(anyString(), anyString(), anyString())).thenReturn("s3/key/test.jpg");
        when(s3ClientService.downloadFile(anyString())).thenReturn(fileBytes);

        Object result = fileService.downloadFile(fileId, uploaderId, category);

        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo(base64String);
        verify(s3ClientService, times(1)).downloadFile(anyString());
    }

    @Test
    void testDownloadFile_ReturnsBytesForNonImage() {
        Long fileId = 1L;
        String uploaderId = "testUploader";
        String category = "profile";

        FileMetadata metadata = new FileMetadata();
        metadata.setUploaderId(uploaderId);
        metadata.setFileCategory(category);
        metadata.setFileName("test.pdf");
        metadata.setContentType("application/pdf");

        byte[] fileBytes = new byte[]{1, 2, 3};

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        when(s3ClientService.generateS3Key(anyString(), anyString(), anyString())).thenReturn("s3/key/test.pdf");
        when(s3ClientService.downloadFile(anyString())).thenReturn(fileBytes);

        Object result = fileService.downloadFile(fileId, uploaderId, category);

        assertThat(result).isInstanceOf(byte[].class);
        assertThat((byte[]) result).isEqualTo(fileBytes);
        verify(s3ClientService, times(1)).downloadFile(anyString());
    }

    @Test
    void testDeleteFile_Success() {
        Long fileId = 1L;
        String uploaderId = "testUploader";
        String category = "profile";

        FileMetadata metadata = new FileMetadata();
        metadata.setUploaderId(uploaderId);
        metadata.setFileCategory(category);
        metadata.setFileName("test.jpg");

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        when(s3ClientService.generateS3Key(anyString(), anyString(), anyString())).thenReturn("s3/key/test.jpg");

        fileService.deleteFile(fileId, uploaderId, category, true, true);

        verify(s3ClientService, times(1)).deleteFile(anyString());
        verify(fileMetadataRepository, times(1)).delete(any(FileMetadata.class));
    }

    @Test
    void testDeleteFile_FailureInS3() {
        Long fileId = 1L;
        String uploaderId = "testUploader";
        String category = "profile";

        FileMetadata metadata = new FileMetadata();
        metadata.setUploaderId(uploaderId);
        metadata.setFileCategory(category);
        metadata.setFileName("test.jpg");

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        when(s3ClientService.generateS3Key(anyString(), anyString(), anyString())).thenReturn("s3/key/test.jpg");
        doThrow(new RuntimeException("S3 deletion failed")).when(s3ClientService).deleteFile(anyString());

        assertThrows(RuntimeException.class, () -> fileService.deleteFile(fileId, uploaderId, category, true, true));

        verify(s3ClientService, times(1)).deleteFile(anyString());
        verify(fileMetadataRepository, never()).delete(any(FileMetadata.class));
    }
}