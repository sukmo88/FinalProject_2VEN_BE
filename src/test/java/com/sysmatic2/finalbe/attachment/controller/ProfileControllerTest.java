//package com.sysmatic2.finalbe.attachment.controller;
//
//import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
//import com.sysmatic2.finalbe.attachment.service.ProfileService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ProfileController.class)
//class ProfileControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ProfileService profileService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
//    void testUploadProfileFile_Success() throws Exception {
//        FileMetadata mockMetadata = new FileMetadata();
//        mockMetadata.setId(1L);
//        mockMetadata.setFilePath("https://example.com/test-user/profile/test-image.png");
//        mockMetadata.setDisplayName("Test Image");
//
//        when(profileService.uploadProfileFile(any(), anyString(), anyString())).thenReturn(mockMetadata);
//
//        MockMultipartFile mockFile = new MockMultipartFile(
//                "file",
//                "test-image.png",
//                MediaType.IMAGE_PNG_VALUE,
//                "test-image-content".getBytes()
//        );
//
//        mockMvc.perform(multipart("/api/files/profile/upload")
//                        .file(mockFile)
//                        .param("uploaderId", "test-user")
//                        .param("displayName", "Test Image")
//                        .with(csrf())) // CSRF 토큰 추가
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.fileId", is(1)))
//                .andExpect(jsonPath("$.fileUrl", is("https://example.com/test-user/profile/test-image.png")))
//                .andExpect(jsonPath("$.displayName", is("Test Image")))
//                .andExpect(jsonPath("$.message", is("File successfully uploaded")));
//
//        verify(profileService, times(1)).uploadProfileFile(any(), eq("test-user"), eq("Test Image"));
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
//    void testDownloadProfileFile_Success() throws Exception {
//        String base64Content = "base64ImageData";
//
//        when(profileService.downloadProfileFileAsBase64(anyLong(), anyString())).thenReturn(base64Content);
//
//        mockMvc.perform(get("/api/files/profile/download/1")
//                        .param("uploaderId", "test-user")
//                        .with(csrf())) // CSRF 토큰 추가
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.fileId", is(1)))
//                .andExpect(jsonPath("$.message", is("File successfully retrieved")))
//                .andExpect(jsonPath("$.base64Content", is(base64Content)));
//
//        verify(profileService, times(1)).downloadProfileFileAsBase64(1L, "test-user");
//    }
//
//    @Test
//    @WithMockUser(username = "test-user", roles = {"ADMIN", "TRADER", "INVESTOR"})
//    void testDeleteProfileFile_Success() throws Exception {
//        doNothing().when(profileService).deleteProfileFile(anyLong(), anyString());
//
//        mockMvc.perform(delete("/api/files/profile/delete/1")
//                        .param("uploaderId", "test-user")
//                        .with(csrf())) // CSRF 토큰 추가
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.fileId", is(1)))
//                .andExpect(jsonPath("$.message", is("File successfully deleted")));
//
//        verify(profileService, times(1)).deleteProfileFile(1L, "test-user");
//    }
//}