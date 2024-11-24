//package com.sysmatic2.finalbe.attachment.service;
//
//import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
//import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//class ProfileServiceTest {
//
//    @Mock
//    private S3ClientService s3ClientService;
//
//    @Mock
//    private FileMetadataRepository fileMetadataRepository;
//
//    @InjectMocks
//    private ProfileService profileService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testUploadProfileFile_Success() {
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getOriginalFilename()).thenReturn("test-image.png");
//        when(mockFile.getContentType()).thenReturn("image/png");
//        when(mockFile.getSize()).thenReturn(1024L);
//
//        String uploaderId = "test-user";
//        String displayName = "Profile Picture";
//        String uniqueFileName = "unique-test-image.png";
//        String s3Key = "test-user/profile/unique-test-image.png";
//        String fileUrl = "https://s3-bucket-url/test-user/profile/unique-test-image.png";
//
//        when(s3ClientService.generateUniqueFileName(anyString())).thenReturn(uniqueFileName);
//        when(s3ClientService.generateS3Key(uploaderId, "profile", uniqueFileName)).thenReturn(s3Key);
//        when(s3ClientService.uploadFile(mockFile, s3Key)).thenReturn(fileUrl);
//
//        FileMetadata metadata = new FileMetadata();
//        metadata.setId(1L);
//        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(metadata);
//
//        FileMetadata result = profileService.uploadProfileFile(mockFile, uploaderId, displayName);
//
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(s3ClientService, times(1)).uploadFile(mockFile, s3Key);
//        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
//    }
//
//    @Test
//    void testDownloadProfileFileAsBase64_Success() {
//        Long fileId = 1L;
//        String uploaderId = "test-user";
//        String s3Key = "test-user/profile/test-image.png";
//
//        FileMetadata metadata = new FileMetadata();
//        metadata.setId(fileId);
//        metadata.setUploaderId(uploaderId);
//        metadata.setFileCategory("profile");
//        metadata.setFileName("test-image.png");
//
//        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
//        when(s3ClientService.generateS3Key(uploaderId, "profile", "test-image.png")).thenReturn(s3Key);
//        when(s3ClientService.downloadImageFileAsBase64(s3Key)).thenReturn("base64ImageData");
//
//        String result = profileService.downloadProfileFileAsBase64(fileId, uploaderId);
//
//        assertThat(result).isEqualTo("base64ImageData");
//        verify(s3ClientService, times(1)).downloadImageFileAsBase64(s3Key);
//    }
//
//    @Test
//    void testDeleteProfileFile_Success() {
//        Long fileId = 1L;
//        String uploaderId = "test-user";
//        String s3Key = "test-user/profile/test-image.png";
//
//        FileMetadata metadata = new FileMetadata();
//        metadata.setId(fileId);
//        metadata.setUploaderId(uploaderId);
//        metadata.setFileCategory("profile");
//        metadata.setFileName("test-image.png");
//
//        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(metadata));
//        when(s3ClientService.generateS3Key(uploaderId, "profile", "test-image.png")).thenReturn(s3Key);
//
//        profileService.deleteProfileFile(fileId, uploaderId);
//
//        verify(s3ClientService, times(1)).deleteFile(s3Key);
//        verify(fileMetadataRepository, times(1)).delete(metadata);
//    }
//
//    @Test
//    void testValidateImageFile_InvalidType() {
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getContentType()).thenReturn("text/plain");
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> profileService.uploadProfileFile(mockFile, "test-user", "Invalid Image"));
//
//        assertThat(exception.getMessage()).isEqualTo("Invalid file type: Only image files are allowed.");
//    }
//}