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

class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private FileService fileService;

    @Mock
    private S3ClientService s3ClientService;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadOrUpdateProfileFile_NewFileUpload() {
        MultipartFile file = mock(MultipartFile.class);
        String uploaderId = "testUploader";
        String category = "profile";
        FileMetadataDto expectedMetadataDto = new FileMetadataDto();

        when(fileService.getFileMetadataByUploaderIdAndCategory(eq(uploaderId), eq(category)))
                .thenReturn(Optional.empty());
        when(fileService.uploadFile(eq(file), eq(uploaderId), eq(category), isNull()))
                .thenReturn(expectedMetadataDto);

        FileMetadataDto result = profileService.uploadOrUpdateProfileFile(file, uploaderId);

        assertThat(result).isEqualTo(expectedMetadataDto);
        verify(fileService, times(1)).uploadFile(file, uploaderId, category, null);
        verify(fileService, never()).modifyFile(any(), anyLong(), anyString(), anyString());
    }

    @Test
    void testUploadOrUpdateProfileFile_UpdateExistingFile() {
        MultipartFile file = mock(MultipartFile.class);
        String uploaderId = "testUploader";
        String category = "profile";
        FileMetadataDto existingMetadataDto = new FileMetadataDto();
        existingMetadataDto.setId(1L);

        FileMetadataDto updatedMetadataDto = new FileMetadataDto();

        when(fileService.getFileMetadataByUploaderIdAndCategory(eq(uploaderId), eq(category)))
                .thenReturn(Optional.of(existingMetadataDto));
        when(fileService.modifyFile(eq(file), eq(1L), eq(uploaderId), eq(category)))
                .thenReturn(updatedMetadataDto);

        FileMetadataDto result = profileService.uploadOrUpdateProfileFile(file, uploaderId);

        assertThat(result).isEqualTo(updatedMetadataDto);
        verify(fileService, times(1)).modifyFile(file, 1L, uploaderId, category);
        verify(fileService, never()).uploadFile(any(), anyString(), anyString(), any());
    }

    @Test
    void testDownloadProfileFileAsBase64() {
        Long fileId = 1L;
        String uploaderId = "testUploader";
        String base64Data = "base64EncodedString";

        when(fileService.downloadFile(eq(fileId), eq(uploaderId), eq("profile"))).thenReturn(base64Data);

        Object result = profileService.downloadProfileFileAsBase64(fileId, uploaderId);

        assertThat(result).isEqualTo(base64Data);
        verify(fileService, times(1)).downloadFile(fileId, uploaderId, "profile");
    }

    @Test
    void testDeleteProfileFile() {
        Long fileId = 1L;
        String uploaderId = "testUploader";

        doNothing().when(fileService).deleteFile(eq(fileId), eq(uploaderId), eq("profile"), eq(true), eq(true));

        profileService.deleteProfileFile(fileId, uploaderId);

        verify(fileService, times(1)).deleteFile(fileId, uploaderId, "profile", true, true);
    }

    @Test
    void testGetProfileFileMetadata() {
        Long fileId = 1L;
        String uploaderId = "testUploader";
        FileMetadataDto expectedMetadataDto = new FileMetadataDto();

        when(fileService.getFileMetadataByFileIdAndUploaderIdAndCategory(eq(fileId), eq(uploaderId), eq("profile")))
                .thenReturn(expectedMetadataDto);

        FileMetadataDto result = profileService.getProfileFileMetadata(fileId, uploaderId);

        assertThat(result).isEqualTo(expectedMetadataDto);
        verify(fileService, times(1)).getFileMetadataByFileIdAndUploaderIdAndCategory(fileId, uploaderId, "profile");
    }

    @Test
    void testCreateDefaultFileMetadataForMember() {
        String uploaderId = "testUploader";
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(1L);
        fileMetadata.setUploaderId(uploaderId);
        fileMetadata.setFileCategory("profile");

        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(fileMetadata);

        String result = profileService.createDefaultFileMetadataForMember(uploaderId);

        assertThat(result).isEqualTo("1");
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
    }
}