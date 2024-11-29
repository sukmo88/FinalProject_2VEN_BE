//package com.sysmatic2.finalbe.attachment.service;
//
//import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class IconServiceTest {
//
//    @InjectMocks
//    private IconService iconService;
//
//    @Mock
//    private FileService fileService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testUploadIcon_Success() {
//        // Arrange
//        MultipartFile file = mock(MultipartFile.class);
//        FileMetadataDto fileMetadataDto = new FileMetadataDto();
//        fileMetadataDto.setId(1L);
//
//        when(fileService.uploadFile(eq(file), eq("admin"), eq("icon"), isNull()))
//                .thenReturn(fileMetadataDto);
//
//        // Act
//        Map<String, ?> result = iconService.uploadIcon(file);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.get("fileId")).isEqualTo("1");
//        assertThat(result.get("message")).isEqualTo("File successfully uploaded");
//
//        verify(fileService, times(1)).uploadFile(eq(file), eq("admin"), eq("icon"), isNull());
//    }
//
//    @Test
//    void testModifyIcon_Success() {
//        // Arrange
//        MultipartFile file = mock(MultipartFile.class);
//        Long fileId = 1L;
//        FileMetadataDto existingMetadata = new FileMetadataDto();
//        existingMetadata.setId(fileId);
//        existingMetadata.setFileCategory("icon");
//
//        FileMetadataDto updatedMetadata = new FileMetadataDto();
//        updatedMetadata.setId(fileId);
//
//        when(fileService.getFileMetadataByFileIdAndUploaderIdAndCategory(eq(fileId), eq("admin"), eq("icon")))
//                .thenReturn(existingMetadata);
//        when(fileService.modifyFile(eq(file), eq(fileId), eq("admin"), eq("icon")))
//                .thenReturn(updatedMetadata);
//
//        // Act
//        Map<String, ?> result = iconService.modifyIcon(file, fileId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.get("fileId")).isEqualTo("1");
//        assertThat(result.get("message")).isEqualTo("File successfully modified");
//
//        verify(fileService, times(1)).getFileMetadataByFileIdAndUploaderIdAndCategory(eq(fileId), eq("admin"), eq("icon"));
//        verify(fileService, times(1)).modifyFile(eq(file), eq(fileId), eq("admin"), eq("icon"));
//    }
//
//    @Test
//    void testModifyIcon_InvalidCategory() {
//        // Arrange
//        MultipartFile file = mock(MultipartFile.class);
//        Long fileId = 1L;
//        FileMetadataDto existingMetadata = new FileMetadataDto();
//        existingMetadata.setId(fileId);
//        existingMetadata.setFileCategory("otherCategory");
//
//        when(fileService.getFileMetadataByFileIdAndUploaderIdAndCategory(eq(fileId), eq("admin"), eq("icon")))
//                .thenReturn(existingMetadata);
//
//        // Act & Assert
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> iconService.modifyIcon(file, fileId));
//        assertThat(exception.getMessage()).isEqualTo("Invalid category: Expected 'icon', got otherCategory");
//
//        verify(fileService, times(1)).getFileMetadataByFileIdAndUploaderIdAndCategory(eq(fileId), eq("admin"), eq("icon"));
//        verify(fileService, never()).modifyFile(any(), anyLong(), anyString(), anyString());
//    }
//
//    @Test
//    void testDownloadIcon_Success() {
//        // Arrange
//        Long fileId = 1L;
//        String base64Content = "base64EncodedData";
//
//        when(fileService.downloadFile(eq(fileId), eq("admin"), eq("icon"))).thenReturn(base64Content);
//
//        // Act
//        Map<String, ?> result = iconService.downloadIcon(fileId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.get("fileId")).isEqualTo(fileId);
//        assertThat(result.get("message")).isEqualTo("File successfully downloaded");
//        assertThat(result.get("base64Content")).isEqualTo(base64Content);
//
//        verify(fileService, times(1)).downloadFile(eq(fileId), eq("admin"), eq("icon"));
//    }
//
//    @Test
//    void testDeleteIcon_Success() {
//        // Arrange
//        Long fileId = 1L;
//
//        doNothing().when(fileService).deleteFile(eq(fileId), eq("admin"), eq("icon"), eq(true), eq(true));
//
//        // Act
//        Map<String, ?> result = iconService.deleteIcon(fileId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.get("fileId")).isEqualTo(fileId);
//        assertThat(result.get("message")).isEqualTo("File successfully deleted");
//
//        verify(fileService, times(1)).deleteFile(eq(fileId), eq("admin"), eq("icon"), eq(true), eq(true));
//    }
//}