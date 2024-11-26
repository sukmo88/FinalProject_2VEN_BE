//package com.sysmatic2.finalbe.attachment.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
//import com.sysmatic2.finalbe.attachment.service.ProfileService;
//import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
//import com.sysmatic2.finalbe.member.entity.MemberEntity;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Optional;
//
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ProfileController.class)
//@ExtendWith(MockitoExtension.class)
//class ProfileControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Mock
//    private ProfileService profileService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private CustomUserDetails userDetails;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // CustomUserDetails를 사용한 Authentication 설정
//        userDetails = new CustomUserDetails(Optional.empty());
//        userDetails.setUsername("testUser");
//        userDetails.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
//
//        // SecurityContext에 Authentication 설정
//        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
//    }
//
//    @Test
//    void uploadProfileFile_ShouldReturnCreatedResponse() throws Exception {
//        // Arrange
//        FileMetadataDto fileMetadataDto = new FileMetadataDto();
//        fileMetadataDto.setId(1L);
//        fileMetadataDto.setFilePath("http://example.com/file");
//        fileMetadataDto.setDisplayName("fileName");
//
//        when(profileService.uploadOrUpdateProfileFile(any(), eq("user123"))).thenReturn(fileMetadataDto);
//
//        // Act & Assert
//        mockMvc.perform(multipart("/api/files/profile")
//                        .file("file", "test data".getBytes())
//                        .principal(userDetails))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.fileId", is(1)))
//                .andExpect(jsonPath("$.fileUrl", is("http://example.com/file")))
//                .andExpect(jsonPath("$.displayName", is("fileName")))
//                .andExpect(jsonPath("$.message", is("File successfully uploaded")));
//
//        verify(profileService, times(1)).uploadOrUpdateProfileFile(any(), eq("user123"));
//    }
//
//    @Test
//    void deleteProfileFile_ShouldReturnOkResponse() throws Exception {
//        // Arrange
//        Long fileId = 1L;
//        doNothing().when(profileService).deleteProfileFile(fileId, "user123");
//
//        // Act & Assert
//        mockMvc.perform(delete("/api/files/profile/{fileId}", fileId)
//                        .principal(userDetails))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.fileId", is(1)))
//                .andExpect(jsonPath("$.message", is("File successfully deleted")));
//
//        verify(profileService, times(1)).deleteProfileFile(fileId, "user123");
//    }
//
//    @Test
//    void getProfileUrl_ShouldReturnProfileMetadata() throws Exception {
//        // Arrange
//        FileMetadataDto fileMetadataDto = new FileMetadataDto();
//        fileMetadataDto.setFilePath("http://example.com/profile");
//        fileMetadataDto.setDisplayName("profile.jpg");
//        fileMetadataDto.setFileCategory("profile");
//
//        when(profileService.getProfileUrl("user123")).thenReturn(fileMetadataDto);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/files/profile/url")
//                        .principal(userDetails))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.fileUrl", is("http://example.com/profile")))
//                .andExpect(jsonPath("$.displayName", is("profile.jpg")))
//                .andExpect(jsonPath("$.category", is("profile")))
//                .andExpect(jsonPath("$.message", is("File url retrieved successfully")));
//
//        verify(profileService, times(1)).getProfileUrl("user123");
//    }
//}