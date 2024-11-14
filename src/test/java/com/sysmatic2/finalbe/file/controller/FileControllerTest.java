package com.sysmatic2.finalbe.file.controller;

import com.sysmatic2.finalbe.file.entity.File;
import com.sysmatic2.finalbe.file.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    public void testUploadFile_Success() throws Exception {
        // 가짜 파일 데이터 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test File Content".getBytes()
        );

        // 파일 업로드 결과로 반환될 엔티티 생성
        File mockFileEntity = new File();
        mockFileEntity.setFileId(1L);
        mockFileEntity.setMemberId(1L);
        mockFileEntity.setFileName("test-image.jpg");
        mockFileEntity.setFileType("PROFILE");
        mockFileEntity.setContentType(MediaType.IMAGE_JPEG_VALUE);
        mockFileEntity.setFileSize((long) mockFile.getSize());
        mockFileEntity.setFilePath("/Users/amyou/uploads/test-image.jpg");
        mockFileEntity.setUploadedAt(LocalDateTime.now());
        mockFileEntity.setRelatedEntity("USER");
        mockFileEntity.setRelatedEntityId(1L);

        // Service 모킹 - 가짜 파일 업로드 결과 반환
        when(fileService.uploadFile(any(MockMultipartFile.class), eq(1L), eq("PROFILE"), eq("USER"), eq(1L)))
                .thenReturn(mockFileEntity);

        // MockMvc를 사용하여 요청 수행
        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile)
                        .param("memberId", "1")
                        .param("fileType", "PROFILE")
                        .param("relatedEntity", "USER")
                        .param("relatedEntityId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.fileType").value("PROFILE"))
                .andExpect(jsonPath("$.contentType").value(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(jsonPath("$.relatedEntity").value("USER"))
                .andExpect(jsonPath("$.relatedEntityId").value(1));
    }

    @Test
    public void testUploadFile_Failure() throws Exception {
        // 가짜 파일 데이터 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Test File Content".getBytes()
        );

        // Service에서 예외 발생하도록 모킹
        when(fileService.uploadFile(any(MockMultipartFile.class), eq(1L), eq("PROFILE"), eq("USER"), eq(1L)))
                .thenThrow(new IOException("File upload failed due to IO error"));

        // MockMvc를 사용하여 요청 수행 및 예외 처리 테스트
        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile)
                        .param("memberId", "1")
                        .param("fileType", "PROFILE")
                        .param("relatedEntity", "USER")
                        .param("relatedEntityId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("File upload failed: File upload failed due to IO error"));
    }
}
