package com.sysmatic2.finalbe.attachment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testUploadProfileFile_Success() throws Exception {
        // Arrange
        byte[] fileContent = "dummy file content".getBytes();
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getBytes()).thenReturn(fileContent);

        FileMetadataDto fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setId(1L);
        fileMetadataDto.setFilePath("http://example.com/test.jpg");
        fileMetadataDto.setDisplayName("test.jpg");

        when(profileService.uploadOrUpdateProfileFile(any(MultipartFile.class), eq("uploaderId")))
                .thenReturn(fileMetadataDto);

        // Act & Assert
        mockMvc.perform(multipart("/api/files/profile")
                        .file("file", fileContent)
                        .param("uploaderId", "uploaderId")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileId").value(1))
                .andExpect(jsonPath("$.fileUrl").value("http://example.com/test.jpg"))
                .andExpect(jsonPath("$.displayName").value("test.jpg"))
                .andExpect(jsonPath("$.message").value("File successfully uploaded"));

        verify(profileService, times(1)).uploadOrUpdateProfileFile(any(MultipartFile.class), eq("uploaderId"));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testDownloadProfileFile_Success() throws Exception {
        Long fileId = 1L;
        String base64Content = "base64EncodedContent";
        FileMetadataDto fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setDisplayName("test.jpg");

        when(profileService.getProfileFileMetadata(eq(fileId), eq("uploaderId"))).thenReturn(fileMetadataDto);
        when(profileService.downloadProfileFileAsBase64(eq(fileId), eq("uploaderId"))).thenReturn(base64Content);

        mockMvc.perform(get("/api/files/profile/{fileId}", fileId)
                        .param("uploaderId", "uploaderId")
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(1))
                .andExpect(jsonPath("$.displayName").value("test.jpg"))
                .andExpect(jsonPath("$.base64Content").value("base64EncodedContent"))
                .andExpect(jsonPath("$.message").value("File successfully retrieved"));

        verify(profileService, times(1)).getProfileFileMetadata(eq(fileId), eq("uploaderId"));
        verify(profileService, times(1)).downloadProfileFileAsBase64(eq(fileId), eq("uploaderId"));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testDeleteProfileFile_Success() throws Exception {
        Long fileId = 1L;

        doNothing().when(profileService).deleteProfileFile(eq(fileId), eq("uploaderId"));

        mockMvc.perform(delete("/api/files/profile/{fileId}", fileId)
                        .param("uploaderId", "uploaderId")
                        .with(csrf())) // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(1))
                .andExpect(jsonPath("$.message").value("File successfully deleted"));

        verify(profileService, times(1)).deleteProfileFile(eq(fileId), eq("uploaderId"));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testGetProfileFileMetadata_Success() throws Exception {
        // Arrange
        Long fileId = 1L;
        String uploaderId = "uploaderId";

        FileMetadataDto fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setId(fileId);
        fileMetadataDto.setFilePath("http://example.com/test.jpg");
        fileMetadataDto.setDisplayName("test.jpg");
        fileMetadataDto.setFileSize(1024L);
        fileMetadataDto.setContentType("image/jpeg");
        fileMetadataDto.setUploaderId(uploaderId);
        fileMetadataDto.setFileCategory("profile");
        fileMetadataDto.setUploadedAt(LocalDateTime.now());

        when(profileService.getProfileFileMetadata(eq(fileId), eq(uploaderId))).thenReturn(fileMetadataDto);

        // Act & Assert
        mockMvc.perform(get("/api/files/profile/{fileId}/metadata", fileId)
                        .param("uploaderId", uploaderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(fileId))
                .andExpect(jsonPath("$.fileUrl").value("http://example.com/test.jpg"))
                .andExpect(jsonPath("$.displayName").value("test.jpg"))
                .andExpect(jsonPath("$.fileSize").value(1024))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.uploaderId").value(uploaderId))
                .andExpect(jsonPath("$.category").value("profile"))
                .andExpect(jsonPath("$.message").value("File metadata retrieved successfully"));

        verify(profileService, times(1)).getProfileFileMetadata(eq(fileId), eq(uploaderId));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testGetProfileFileMetadata_NotFound() throws Exception {
        // Arrange
        Long fileId = 1L;
        String uploaderId = "uploaderId";

        when(profileService.getProfileFileMetadata(eq(fileId), eq(uploaderId)))
                .thenThrow(new NoSuchElementException("File metadata not found."));

        // Act & Assert
        mockMvc.perform(get("/api/files/profile/{fileId}/metadata", fileId)
                        .param("uploaderId", uploaderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("FILE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("File metadata not found."));

        verify(profileService, times(1)).getProfileFileMetadata(eq(fileId), eq(uploaderId));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testGetProfileFileMetadata_InvalidInput() throws Exception {
        // Arrange
        Long fileId = 1L;
        String uploaderId = "uploaderId";

        when(profileService.getProfileFileMetadata(eq(fileId), eq(uploaderId)))
                .thenThrow(new IllegalArgumentException("Invalid file ID or uploader ID."));

        // Act & Assert
        mockMvc.perform(get("/api/files/profile/{fileId}/metadata", fileId)
                        .param("uploaderId", uploaderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid input: Invalid file ID or uploader ID."));

        verify(profileService, times(1)).getProfileFileMetadata(eq(fileId), eq(uploaderId));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
    void testGetProfileFileMetadata_InternalServerError() throws Exception {
        // Arrange
        Long fileId = 1L;
        String uploaderId = "uploaderId";

        when(profileService.getProfileFileMetadata(eq(fileId), eq(uploaderId)))
                .thenThrow(new RuntimeException("Unexpected server error."));

        // Act & Assert
        mockMvc.perform(get("/api/files/profile/{fileId}/metadata", fileId)
                        .param("uploaderId", uploaderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));

        verify(profileService, times(1)).getProfileFileMetadata(eq(fileId), eq(uploaderId));
    }
}