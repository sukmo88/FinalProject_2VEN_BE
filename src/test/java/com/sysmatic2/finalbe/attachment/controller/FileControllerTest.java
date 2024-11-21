package com.sysmatic2.finalbe.attachment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN", "TRADER", "INVESTOR"})
    void uploadFile_Success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test-file.png", "image/png", "test content".getBytes());

        String fileCategory = "images";
        String uploaderId = "temp-user";
        String fileUrl = "https://s3-bucket/test-file.png";

        // Mock FileService to return a file URL
        Mockito.when(fileService.uploadFile(any(), eq(uploaderId), eq(fileCategory))).thenReturn(fileUrl);

        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile)
                        .param("fileCategory", fileCategory)
                        .param("uploaderId", uploaderId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File successfully uploaded"));
    }


    @Test
    @WithMockUser(roles = {"ADMIN", "TRADER", "INVESTOR"})
    void uploadFile_BadRequest_WhenFileIsInvalid() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "", "application/octet-stream", (byte[]) null);

        String fileCategory = "images";
        String uploaderId = "temp-user";

        mockMvc.perform(multipart("/api/files/upload")
                        .file(invalidFile)
                        .param("fileCategory", fileCategory)
                        .param("uploaderId", uploaderId)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("File cannot be null or empty"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "TRADER", "INVESTOR"})
    void downloadFile_Success() throws Exception {
        // Arrange
        Long fileId = 1L;
        String mockFileName = "test-file.png";
        String mockPresignedUrl = "https://s3-bucket/test-file.png?presigned=true";

        // Mocking file metadata and presigned URL generation
        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setFileName(mockFileName);

        Mockito.when(fileService.getFileMetadata(fileId)).thenReturn(mockMetadata);
        Mockito.when(fileService.generatePresignedUrl(mockFileName)).thenReturn(mockPresignedUrl);

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{id}", fileId) // 컨트롤러 경로와 매핑된 {id} 사용
                        .with(csrf()))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(content().string(mockPresignedUrl)); // Expect the presigned URL in the response body
    }


    @Test
    @WithMockUser(roles = {"ADMIN", "TRADER", "INVESTOR"})
    void getFileMetadata_Success() throws Exception {
        Long fileId = 1L;
        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setId(fileId);
        mockMetadata.setDisplayName("test-file.png");
        mockMetadata.setFileCategory("PROFILE");

        Mockito.when(fileService.getFileMetadata(fileId)).thenReturn(mockMetadata);

        mockMvc.perform(get("/api/files/{fileId}", fileId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId))
                .andExpect(jsonPath("$.displayName").value("test-file.png"))
                .andExpect(jsonPath("$.fileCategory").value("PROFILE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "TRADER", "INVESTOR"})
    void deleteFile_Success() throws Exception {
        Long fileId = 1L;
        String fileCategory = "images";
        String uploaderId = "temp-user";

        // Mock 서비스 계층 동작
        Mockito.doNothing().when(fileService).deleteFile(fileId, uploaderId, fileCategory);

        // 테스트 요청 수행
        mockMvc.perform(delete("/api/files/{fileId}", fileId) // DELETE 메서드
                        .param("fileCategory", fileCategory)  // 요청 파라미터
                        .param("uploaderId", uploaderId)
                        .with(csrf()))                       // CSRF 토큰 포함
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File successfully deleted"));

    }



    @Test
    @WithMockUser(roles = {"ADMIN", "TRADER", "INVESTOR"})
    void getFilesByCategoryAndUploader_Success() throws Exception {
        String fileCategory = "images";
        String uploaderId = "temp-user";

        List<FileMetadata> mockFiles = List.of(
                createFileMetadata(1L, "File1.png", "test-file1.png", "https://s3-bucket/file1.png", 1024L, "image/png", fileCategory, uploaderId),
                createFileMetadata(2L, "File2.png", "test-file2.png", "https://s3-bucket/file2.png", 2048L, "image/png", fileCategory, uploaderId)
        );

        Mockito.when(fileService.getFilesByCategoryAndUploader(eq(fileCategory), eq(uploaderId)))
                .thenReturn(mockFiles);

        mockMvc.perform(get("/api/files/list")
                        .param("fileCategory", fileCategory)
                        .param("uploaderId", uploaderId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(mockFiles.size()))
                .andExpect(jsonPath("$[0].id").value(mockFiles.get(0).getId()))
                .andExpect(jsonPath("$[0].displayName").value(mockFiles.get(0).getDisplayName()))
                .andExpect(jsonPath("$[1].id").value(mockFiles.get(1).getId()))
                .andExpect(jsonPath("$[1].displayName").value(mockFiles.get(1).getDisplayName()));
    }

    private FileMetadata createFileMetadata(Long id, String displayName, String fileName, String filePath, Long fileSize, String contentType, String fileCategory, String uploaderId) {
        FileMetadata metadata = new FileMetadata();
        metadata.setId(id);
        metadata.setDisplayName(displayName);
        metadata.setFileName(fileName);
        metadata.setFilePath(filePath);
        metadata.setFileSize(fileSize);
        metadata.setContentType(contentType);
        metadata.setFileCategory(fileCategory);
        metadata.setUploaderId(uploaderId);
        return metadata;
    }

}
