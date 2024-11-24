package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private S3ClientService s3ClientService;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile_WithFileCategoryItemId() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test-image.png");
        when(mockFile.getSize()).thenReturn(123L);
        when(mockFile.getContentType()).thenReturn("image/png");

        String uploaderId = "user123";
        String category = "profile";
        String fileCategoryItemId = "item-456";

        // Create mock metadata object
        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(1L);
        mockMetadata.setDisplayName("test-image.png");
        mockMetadata.setFileName("unique-file-name.png");
        mockMetadata.setFilePath("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");
        mockMetadata.setFileCategory(category);
        mockMetadata.setUploaderId(uploaderId);
        mockMetadata.setContentType("image/png");
        mockMetadata.setFileSize(123L);
        mockMetadata.setFileCategoryItemId(fileCategoryItemId);

        // Mock S3ClientService and FileMetadataRepository
        when(s3ClientService.generateUniqueFileName("test-image.png")).thenReturn("unique-file-name.png");
        when(s3ClientService.generateS3Key(uploaderId, category, "unique-file-name.png"))
                .thenReturn("test-user/profile/unique-file-name.png");
        when(s3ClientService.uploadFile(mockFile, "test-user/profile/unique-file-name.png"))
                .thenReturn("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(mockMetadata);

        // Act
        FileMetadata savedMetadata = fileService.uploadFile(mockFile, uploaderId, category, fileCategoryItemId);

        // Assert
        assertThat(savedMetadata).isNotNull();
        assertThat(savedMetadata.getFileCategoryItemId()).isEqualTo(fileCategoryItemId);
        assertThat(savedMetadata.getDisplayName()).isEqualTo("test-image.png");
        assertThat(savedMetadata.getFilePath()).isEqualTo("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");

        // Verify interactions
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
        verify(s3ClientService, times(1)).uploadFile(eq(mockFile), anyString());
    }

    @Test
    void testUploadFile_WithoutFileCategoryItemId() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test-image.png");
        when(mockFile.getSize()).thenReturn(123L);
        when(mockFile.getContentType()).thenReturn("image/png");

        String uploaderId = "user123";
        String category = "profile";

        // Create mock metadata object
        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(1L);
        mockMetadata.setDisplayName("test-image.png");
        mockMetadata.setFileName("unique-file-name.png");
        mockMetadata.setFilePath("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");
        mockMetadata.setFileCategory(category);
        mockMetadata.setUploaderId(uploaderId);
        mockMetadata.setContentType("image/png");
        mockMetadata.setFileSize(123L);

        // Mock S3ClientService and FileMetadataRepository
        when(s3ClientService.generateUniqueFileName("test-image.png")).thenReturn("unique-file-name.png");
        when(s3ClientService.generateS3Key(uploaderId, category, "unique-file-name.png"))
                .thenReturn("test-user/profile/unique-file-name.png");
        when(s3ClientService.uploadFile(mockFile, "test-user/profile/unique-file-name.png"))
                .thenReturn("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(mockMetadata);

        // Act
        FileMetadata savedMetadata = fileService.uploadFile(mockFile, uploaderId, category, null);

        // Assert
        assertThat(savedMetadata).isNotNull();
        assertThat(savedMetadata.getFileCategoryItemId()).isNull();
        assertThat(savedMetadata.getDisplayName()).isEqualTo("test-image.png");
        assertThat(savedMetadata.getFilePath()).isEqualTo("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");
    }

    @Test
    void testUploadFile_WithNullOriginalFilename() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null); // 파일 이름이 null인 경우
        when(mockFile.getSize()).thenReturn(123L);
        when(mockFile.getContentType()).thenReturn("image/png");

        String uploaderId = "user123";
        String category = "profile";
        String fileCategoryItemId = "item-456";

        // Mock S3ClientService와 FileMetadataRepository
        when(s3ClientService.generateUniqueFileName("unknown-file")).thenReturn("unique-file-name.png");
        when(s3ClientService.generateS3Key(uploaderId, category, "unique-file-name.png"))
                .thenReturn("test-user/profile/unique-file-name.png");
        when(s3ClientService.uploadFile(mockFile, "test-user/profile/unique-file-name.png"))
                .thenReturn("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");

        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(1L);
        mockMetadata.setDisplayName("unknown-file");
        mockMetadata.setFileName("unique-file-name.png");
        mockMetadata.setFilePath("https://s3.amazonaws.com/test-user/profile/unique-file-name.png");
        mockMetadata.setFileCategoryItemId(fileCategoryItemId);

        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(mockMetadata);

        // Act
        FileMetadata savedMetadata = fileService.uploadFile(mockFile, uploaderId, category, fileCategoryItemId);

        // Assert
        assertThat(savedMetadata.getDisplayName()).isEqualTo("unknown-file");
        assertThat(savedMetadata.getFileCategoryItemId()).isEqualTo(fileCategoryItemId);
    }

    @Test
    void testDownloadFile_Success_Image() {
        Long fileId = 1L;
        String uploaderId = "test-user";
        String category = "profile";
        String s3Key = "test-user/profile/test-image.png";

        FileMetadata metadata = new FileMetadata();
        metadata.setId(fileId);
        metadata.setUploaderId(uploaderId);
        metadata.setFileCategory(category);
        metadata.setContentType("image/png");
        metadata.setFileName("test-image.png");

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        when(s3ClientService.generateS3Key(uploaderId, category, "test-image.png")).thenReturn(s3Key);
        when(s3ClientService.downloadImageFileAsBase64(s3Key)).thenReturn("base64encodedimage");

        Object result = fileService.downloadFile(fileId, uploaderId, category);

        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("base64encodedimage");
    }

    @Test
    void testDeleteFile_Success() {
        Long fileId = 1L;
        String uploaderId = "test-user";
        String category = "profile";
        String s3Key = "test-user/profile/test-image.png";

        FileMetadata metadata = new FileMetadata();
        metadata.setId(fileId);
        metadata.setUploaderId(uploaderId);
        metadata.setFileCategory(category);
        metadata.setFileName("test-image.png");

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
        when(s3ClientService.generateS3Key(uploaderId, category, "test-image.png")).thenReturn(s3Key);

        fileService.deleteFile(fileId, uploaderId, category);

        verify(s3ClientService, times(1)).deleteFile(s3Key);
        verify(fileMetadataRepository, times(1)).delete(metadata);
    }

    @Test
    void testDeleteFile_AccessDenied() {
        Long fileId = 1L;
        String uploaderId = "test-user";
        String wrongUploader = "other-user";
        String category = "profile";

        FileMetadata metadata = new FileMetadata();
        metadata.setId(fileId);
        metadata.setUploaderId(wrongUploader);
        metadata.setFileCategory(category);

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> fileService.deleteFile(fileId, uploaderId, category));

        assertThat(exception.getMessage()).contains("Access denied");
    }
}