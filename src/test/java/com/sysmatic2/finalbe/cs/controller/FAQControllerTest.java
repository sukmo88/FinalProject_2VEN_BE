package com.sysmatic2.finalbe.cs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.cs.dto.AdminFAQDto;
import com.sysmatic2.finalbe.cs.dto.FAQResponse;
import com.sysmatic2.finalbe.cs.dto.UserFAQDto;
import com.sysmatic2.finalbe.cs.entity.FAQ;
import com.sysmatic2.finalbe.cs.entity.FAQCategory;
import com.sysmatic2.finalbe.cs.repository.FAQCategoryRepository;
import com.sysmatic2.finalbe.cs.repository.FAQRepository;
import com.sysmatic2.finalbe.cs.service.FAQService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.eq;


@WebMvcTest(FAQController.class)
class FAQControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FAQService faqService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Get All FAQs - Success")
    @WithMockUser(roles = {"ADMIN"})
    void getAllFAQs_Success() throws Exception {
        given(faqService.getAllFAQs(0, 10, "ROLE_ADMIN")).willReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/faqs")
                        .param("page", "0")
                        .param("pageSize", "10")
                        .header("Authorization", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("test"));
    }

//    @Test
//    @DisplayName("Get All FAQs - Unauthorized")
//    void getAllFAQs_Unauthorized() throws Exception {
//        mockMvc.perform(get("/api/faqs")
//                        .param("page", "0")
//                        .param("pageSize", "10"))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
//                .andExpect(jsonPath("$.message").value("User is not authenticated"));
//    }

    @Test
    @DisplayName("Create FAQ - Success")
    @WithMockUser(roles = {"ADMIN"})
    void createFAQ_Success() throws Exception {
        AdminFAQDto faq = new AdminFAQDto();
        faq.setWriterId("writer");
        faq.setQuestion("What is this?");
        faq.setAnswer("This is a FAQ");

        given(faqService.createFAQ(any(AdminFAQDto.class))).willReturn(faq);

        mockMvc.perform(post("/api/faqs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faq))
                        .header("Authorization", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.writerId").value("writer"))
                .andExpect(jsonPath("$.question").value("What is this?"))
                .andExpect(jsonPath("$.answer").value("This is a FAQ"));
    }

    @Test
    @DisplayName("Create FAQ - Forbidden for non-admin user")
    @WithMockUser(roles = {"USER"})
    void createFAQ_Forbidden() throws Exception {
        AdminFAQDto faq = new AdminFAQDto();
        faq.setWriterId("writer");
        faq.setQuestion("What is this?");
        faq.setAnswer("This is a FAQ");

        mockMvc.perform(post("/api/faqs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faq))
                        .header("Authorization", "ROLE_USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access Denied: User does not have sufficient permissions"));
    }

    @Test
    @DisplayName("Update FAQ - Success")
    @WithMockUser(roles = {"ADMIN"})
    void updateFAQ_Success() throws Exception {
        AdminFAQDto faq = new AdminFAQDto();
        faq.setWriterId("writer");
        faq.setQuestion("Updated Question");
        faq.setAnswer("Updated Answer");

        given(faqService.updateFAQ(eq(1L), any(AdminFAQDto.class), eq("ROLE_ADMIN"))).willReturn(faq);

        mockMvc.perform(put("/api/faqs/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faq))
                        .header("Authorization", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.writerId").value("writer"))
                .andExpect(jsonPath("$.question").value("Updated Question"))
                .andExpect(jsonPath("$.answer").value("Updated Answer"));
    }

    @Test
    @DisplayName("Update FAQ - Bad Request")
    @WithMockUser(roles = {"ADMIN"})
    void updateFAQ_BadRequest() throws Exception {
        AdminFAQDto faq = new AdminFAQDto(); // Missing required fields

        mockMvc.perform(put("/api/faqs/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faq))
                        .header("Authorization", "ROLE_ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid input: Missing required fields"));
    }

    @Test
    @DisplayName("Delete FAQ - Success")
    @WithMockUser(roles = {"ADMIN"})
    void deleteFAQ_Success() throws Exception {
        willDoNothing().given(faqService).deleteFAQ(eq(1L), eq("ROLE_ADMIN"));

        mockMvc.perform(delete("/api/faqs/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("FAQ successfully deleted"));
    }

    @Test
    @DisplayName("Delete FAQ - Forbidden for non-admin user")
    @WithMockUser(roles = {"USER"})
    void deleteFAQ_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/faqs/{id}", 1L)
                        .with(csrf())
                        .header("Authorization", "ROLE_USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access Denied: User does not have sufficient permissions"));
    }

    @Test
    @DisplayName("Search FAQs - Success")
    @WithMockUser(roles = {"USER"})
    void searchFAQs_Success() throws Exception {
        given(faqService.searchFAQs(0, 10, "test")).willReturn(Map.of("results", "search data"));

        mockMvc.perform(get("/api/faqs/search")
                        .param("page", "0")
                        .param("pageSize", "10")
                        .param("keyword", "test")
                        .header("Authorization", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").value("search data"));
    }




}