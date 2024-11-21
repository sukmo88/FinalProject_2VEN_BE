package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FileServiceTest {

    private FileMetadataRepository fileMetadataRepository;
    private S3ClientService s3ClientService;
    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileMetadataRepository = mock(FileMetadataRepository.class);
        s3ClientService = mock(S3ClientService.class);
        fileService = new FileService(s3ClientService, fileMetadataRepository);
    }

    @Test
    void testGeneratePresignedUrl_success() {
        // Arrange
        String fileName = "test-file.png";
        String uploaderId = "user123";
        String fileCategory = "images";
        String displayName = "Test File";
        String s3Key = "user123/images/test-file.png";
        String presignedUrl = "https://s3.presigned-url.com/test-file.png";

        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setFileName(fileName);
        mockMetadata.setDisplayName(displayName);
        mockMetadata.setUploaderId(uploaderId);
        mockMetadata.setFileCategory(fileCategory);

        // Mock repository and S3 client behavior
        when(fileMetadataRepository.findByFileNameAndUploaderIdAndFileCategory(fileName, uploaderId, fileCategory))
                .thenReturn(Optional.of(mockMetadata)); // Mock metadata repository
        when(s3ClientService.generateS3Key(uploaderId, fileCategory, fileName))
                .thenReturn(s3Key); // Mock S3 key generation
        when(s3ClientService.generatePresignedUrl(s3Key, displayName))
                .thenReturn(presignedUrl); // Mock Presigned URL generation

        // Act
        String result = fileService.generatePresignedUrl(fileName, uploaderId, fileCategory);

        // Assert
        assertThat(result).isEqualTo(presignedUrl); // Validate the returned URL
        verify(fileMetadataRepository, times(1))
                .findByFileNameAndUploaderIdAndFileCategory(fileName, uploaderId, fileCategory);
        verify(s3ClientService, times(1)).generateS3Key(uploaderId, fileCategory, fileName);
        verify(s3ClientService, times(1)).generatePresignedUrl(s3Key, displayName);
    }




    @Test
    void testGeneratePresignedUrl_fileNotFound() {
        // Arrange
        String fileName = "non-existent-file.png";
        String uploaderId = "user123";
        String fileCategory = "images";

        // Mock repository behavior for "not found"
        when(fileMetadataRepository.findByFileNameAndUploaderIdAndFileCategory(fileName, uploaderId, fileCategory))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> fileService.generatePresignedUrl(fileName, uploaderId, fileCategory));

        // Verify repository interaction
        verify(fileMetadataRepository, times(1))
                .findByFileNameAndUploaderIdAndFileCategory(fileName, uploaderId, fileCategory);

        // Verify S3ClientService is not called
        verifyNoInteractions(s3ClientService);
    }



    @Test
    void testUploadFile_success() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String uploaderId = "user123";
        String category = "images";
        String originalFileName = "test-file.png";
        String uniqueFileName = "unique-file-name.png";
        String fileUrl = "https://s3.bucket.com/user123/images/unique-file-name.png";
        Long generatedId = 1L;

        // Mock 파일 정보
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getSize()).thenReturn(1024L);

        // Mock S3 업로드 결과
        when(s3ClientService.uploadFile(mockFile, uploaderId, category))
                .thenReturn(Map.of("fileUrl", fileUrl, "fileName", uniqueFileName));

        // Mock 저장 결과
        FileMetadata savedMetadata = new FileMetadata();
        savedMetadata.setId(generatedId);
        savedMetadata.setDisplayName(originalFileName);
        savedMetadata.setFileName(uniqueFileName);
        savedMetadata.setFilePath(fileUrl);
        savedMetadata.setFileSize(1024L);
        savedMetadata.setContentType("image/png");
        savedMetadata.setFileCategory(category);
        savedMetadata.setUploaderId(uploaderId);

        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(savedMetadata);

        // Act
        String result = fileService.uploadFile(mockFile, uploaderId, category);

        // Assert
        assertThat(result).isEqualTo(generatedId.toString());
        verify(s3ClientService, times(1)).uploadFile(mockFile, uploaderId, category);
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
    }


    @Test
    void testDeleteFile_success() {
        // Arrange
        Long fileId = 1L;
        String uploaderId = "temp-user";
        String fileCategory = "PROFILE";
        String fileName = "test-file.png";
        String s3Key = String.format("%s/%s/%s", uploaderId, fileCategory, fileName); // 올바른 S3 키 경로

        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(fileId);
        mockMetadata.setFileName(fileName);
        mockMetadata.setUploaderId(uploaderId);
        mockMetadata.setFileCategory(fileCategory);

        // Mock repository behavior
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(mockMetadata));
        when(fileMetadataRepository.existsByIdAndFileCategoryAndUploaderId(fileId, fileCategory, uploaderId)).thenReturn(true);


        // Mock S3 key generation
        when(s3ClientService.generateS3Key(uploaderId, fileCategory, fileName)).thenReturn(s3Key);

        // Act
        fileService.deleteFile(fileId, uploaderId, fileCategory);

        // Assert
        verify(s3ClientService, times(1)).deleteFile(eq(s3Key)); // S3ClientService에 deleteFile 호출 확인
        verify(fileMetadataRepository, times(1)).delete(mockMetadata); // Repository에 삭제 호출 확인
    }


    @Test
    void testDeleteFile_fileNotFound() {
        // Arrange
        Long fileId = 1L;
        String uploaderId = "temp-user";
        String fileCategory = "PROFILE";
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fileService.deleteFile(fileId, uploaderId, fileCategory));
        verify(fileMetadataRepository, times(1)).findById(fileId);
        verifyNoInteractions(s3ClientService);
    }

    @Test
    void testGetFileNameById_success() {
        // Arrange
        Long fileId = 1L;
        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(fileId);
        mockMetadata.setFileName("test-file.png");

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(mockMetadata));

        // Act
        String result = fileService.getFileNameById(fileId);

        // Assert
        assertThat(result).isEqualTo("test-file.png");
        verify(fileMetadataRepository, times(1)).findById(fileId);
    }

    @Test
    void testGetFilesByCategoryAndUploader_success() {
        // Arrange
        String category = "images";
        String uploaderId = "user123";
        FileMetadata mockMetadata1 = new FileMetadata();
        mockMetadata1.setFileName("file1.png");
        FileMetadata mockMetadata2 = new FileMetadata();
        mockMetadata2.setFileName("file2.png");

        when(fileMetadataRepository.findByFileCategoryAndUploaderId(category, uploaderId))
                .thenReturn(List.of(mockMetadata1, mockMetadata2));

        // Act
        List<FileMetadata> result = fileService.getFilesByCategoryAndUploader(category, uploaderId);

        // Assert
        assertThat(result).hasSize(2);
        verify(fileMetadataRepository, times(1)).findByFileCategoryAndUploaderId(category, uploaderId);
    }
}
