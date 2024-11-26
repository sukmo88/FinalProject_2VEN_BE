//package com.sysmatic2.finalbe.attachment.service;
//
//import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
//import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
//import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProfileServiceTest {
//
//    @InjectMocks
//    private ProfileService profileService;
//
//    @Mock
//    private FileService fileService;
//
//    @Mock
//    private S3ClientService s3ClientService;
//
//    @Mock
//    private FileMetadataRepository fileMetadataRepository;
//
//    @Test
//    void uploadOrUpdateProfileFile_WhenExistingFile_ShouldModifyFile() {
//        // Arrange
//        String uploaderId = "user123";
//        String category = "profile";
//        MultipartFile file = mock(MultipartFile.class);
//
//        FileMetadataDto existingMetadata = new FileMetadataDto();
//        existingMetadata.setId(1L);
//        existingMetadata.setFileName("oldFileName");
//        existingMetadata.setFilePath("oldFileUrl");
//        existingMetadata.setUploaderId(uploaderId);
//        existingMetadata.setFileCategory(category);
//
//        FileMetadataDto updatedMetadata = new FileMetadataDto();
//        updatedMetadata.setId(1L);
//        updatedMetadata.setFileName("newFileName");
//        updatedMetadata.setFilePath("newFileUrl");
//        updatedMetadata.setUploaderId(uploaderId);
//        updatedMetadata.setFileCategory(category);
//
//        // Mock: 기존 파일 메타데이터 존재
//        when(fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, category))
//                .thenReturn(Optional.of(existingMetadata));
//        // Mock: 기존 파일 수정 동작
//        when(fileService.modifyFile(file, existingMetadata.getId(), uploaderId, category))
//                .thenReturn(updatedMetadata);
//
//        // Act
//        FileMetadataDto result = profileService.uploadOrUpdateProfileFile(file, uploaderId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("newFileName", result.getFileName());
//        verify(fileService, times(1)).modifyFile(file, existingMetadata.getId(), uploaderId, category);
//        verify(fileService, never()).uploadFile(any(), anyString(), anyString(), any());
//    }
//
//    @Test
//    void uploadOrUpdateProfileFile_WhenNoExistingFile_ShouldUploadNewFile() {
//        // Arrange
//        String uploaderId = "user123";
//        String category = "profile";
//        MultipartFile file = mock(MultipartFile.class);
//
//        FileMetadataDto newMetadata = new FileMetadataDto();
//        newMetadata.setId(1L);
//        newMetadata.setFileName("fileName");
//        newMetadata.setFilePath("url");
//        newMetadata.setUploaderId(uploaderId);
//        newMetadata.setFileCategory(category);
//
//        // Mock: 기존 파일이 없음
//        when(fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, category))
//                .thenReturn(Optional.empty());
//        // Mock: 새 파일 업로드
//        when(fileService.uploadFile(file, uploaderId, category, null)).thenReturn(newMetadata);
//
//        // Act
//        FileMetadataDto result = profileService.uploadOrUpdateProfileFile(file, uploaderId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("fileName", result.getFileName());
//        verify(fileService, times(1)).uploadFile(file, uploaderId, category, null);
//        verify(fileService, never()).modifyFile(any(), anyLong(), anyString(), anyString());
//    }
//
//    @Test
//    void deleteProfileFile_ShouldCallDeleteFile() {
//        // Arrange
//        Long fileId = 1L;
//        String uploaderId = "user123";
//        String category = "profile";
//
//        // Mock: deleteFile 동작 설정 필요 없음
//        doNothing().when(fileService).deleteFile(fileId, uploaderId, category, true, true);
//
//        // Act
//        profileService.deleteProfileFile(fileId, uploaderId);
//
//        // Assert
//        verify(fileService, times(1)).deleteFile(fileId, uploaderId, category, true, true);
//    }
//
//    @Test
//    void getProfileUrl_WhenFileExists_ShouldReturnMetadata() {
//        // Arrange
//        String uploaderId = "user123";
//        String category = "profile";
//
//        FileMetadataDto metadata = new FileMetadataDto();
//        metadata.setId(1L);
//        metadata.setFileName("fileName");
//        metadata.setFilePath("url");
//        metadata.setUploaderId(uploaderId);
//        metadata.setFileCategory(category);
//
//        // Mock: 파일 메타데이터가 존재
//        when(fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, category))
//                .thenReturn(Optional.of(metadata));
//
//        // Act
//        FileMetadataDto result = profileService.getProfileUrl(uploaderId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("fileName", result.getFileName());
//        verify(fileService, times(1)).getFileMetadataByUploaderIdAndCategory(uploaderId, category);
//    }
//
//    @Test
//    void getProfileUrl_WhenFileNotExists_ShouldReturnNull() {
//        // Arrange
//        String uploaderId = "user123";
//        String category = "profile";
//
//        // Mock: 파일 메타데이터가 존재하지 않음
//        when(fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, category))
//                .thenReturn(Optional.empty());
//
//        // Act
//        FileMetadataDto result = profileService.getProfileUrl(uploaderId);
//
//        // Assert
//        assertNull(result);
//        verify(fileService, times(1)).getFileMetadataByUploaderIdAndCategory(uploaderId, category);
//    }
//
//    @Test
//    void createDefaultFileMetadataForMember_ShouldCreateAndReturnId() {
//        // Arrange
//        String uploaderId = "user123";
//
//        FileMetadata fileMetadata = new FileMetadata();
//        fileMetadata.setId(1L);
//        fileMetadata.setFileCategory("profile");
//        fileMetadata.setUploaderId(uploaderId);
//
//        // Mock: 메타데이터 저장
//        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(fileMetadata);
//
//        // Act
//        String result = profileService.createDefaultFileMetadataForMember(uploaderId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("1", result);
//        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
//    }
//}