package com.sysmatic2.finalbe.cs.controlleer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.cs.dto.AdminFAQDto;
import com.sysmatic2.finalbe.cs.dto.FAQResponse;
import com.sysmatic2.finalbe.cs.dto.UserFAQDto;
import com.sysmatic2.finalbe.cs.entity.FAQ;
import com.sysmatic2.finalbe.cs.entity.FAQCategory;
import com.sysmatic2.finalbe.cs.service.FAQService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FAQController.class)
class FAQControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FAQService faqService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Get all FAQs - Success for Admin")
    void getAllFAQs_AdminSuccess() throws Exception {
        // given
        FAQCategory category = new FAQCategory(1L, "general", "this is general");
        AdminFAQDto adminFAQ1 = new AdminFAQDto(1L, "Admin Question 1?", "Admin Answer 1", 1L, LocalDateTime.now(), null, true, category);
        AdminFAQDto adminFAQ2 = new AdminFAQDto(2L, "Admin Question 2?", "Admin Answer 2", 2L, LocalDateTime.now(), null, true, category);

        List<FAQResponse> faqs = List.of(adminFAQ1, adminFAQ2);

        // when `faqService.getAllFAQs("ADMIN")`가 호출되면 `faqs` 반환
        given(faqService.getAllFAQs(any(String.class))).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/faqs")
                        .param("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(faqs.size()))
                .andExpect(jsonPath("$[0].question").value("Admin Question 1?"))
                .andExpect(jsonPath("$[0].answer").value("Admin Answer 1"))
                .andExpect(jsonPath("$[0].faqCategory.name").value("general"))
                .andExpect(jsonPath("$[1].question").value("Admin Question 2?"))
                .andExpect(jsonPath("$[1].answer").value("Admin Answer 2"))
                .andExpect(jsonPath("$[1].faqCategory.name").value("general"));
    }



    @Test
    @DisplayName("Update FAQ - Success for Admin")
    void updateFAQ_AdminSuccess() throws Exception {
        // given
        FAQ faq = new FAQ();
        faq.setQuestion("Updated Question?");
        faq.setAnswer("Updated Answer");
        AdminFAQDto updatedFAQ = new AdminFAQDto(1L, "Updated Question?", "Updated Answer", 1L, LocalDateTime.now(), null, true, new FAQCategory(1L, "general", "this is general"));
        given(faqService.updateFAQ(eq(1L), any(FAQ.class), eq("ADMIN"))).willReturn(updatedFAQ);

        // when & then
        mockMvc.perform(put("/api/faqs/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("role", "ADMIN")
                        .content(objectMapper.writeValueAsString(faq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedFAQ.getId()))
                .andExpect(jsonPath("$.question").value(updatedFAQ.getQuestion()))
                .andExpect(jsonPath("$.answer").value(updatedFAQ.getAnswer()));
    }

    @Test
    @DisplayName("Delete FAQ - Success for Admin")
    void deleteFAQ_AdminSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/faqs/{id}", 1)
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("FAQ successfully deleted"));
    }


    @Test
    @DisplayName("Delete FAQ - Forbidden")
    void deleteFAQ_Forbidden() throws Exception {
        // given
        doThrow(new SecurityException("Access Denied")).when(faqService).deleteFAQ(eq(1L), eq("USER"));

        // when & then
        mockMvc.perform(delete("/api/faqs/{id}", 1)
                        .param("role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("DELETE_FAILED"))
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @DisplayName("Delete FAQ - Not Found")
    void deleteFAQ_NotFound() throws Exception {
        // given
        doThrow(new IllegalArgumentException("FAQ with id 1 not found")).when(faqService).deleteFAQ(eq(1L), eq("ADMIN"));

        // when & then
        mockMvc.perform(delete("/api/faqs/{id}", 1)
                        .param("role", "ADMIN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DELETE_FAILED"))
                .andExpect(jsonPath("$.message").value("FAQ with id 1 not found"));
    }

    @Test
    @DisplayName("Search FAQs - Success")
    void searchFAQs_Success() throws Exception {
        // given
        FAQCategory category = new FAQCategory(1L, "general", "this is general");
        UserFAQDto faq1 = new UserFAQDto(1L, "Question 1?", "Answer 1", category);
        UserFAQDto faq2 = new UserFAQDto(2L, "Question 2?", "Answer 2", category);

        List<UserFAQDto> faqList = List.of(faq1, faq2);
        Page<UserFAQDto> faqPage = new PageImpl<>(faqList, PageRequest.of(0, 10), faqList.size());

        // faqService.searchFAQs 호출 시 faqPage 반환 설정
        given(faqService.searchFAQs(eq("Question"), any(Pageable.class))).willReturn(faqPage);

        // when & then
        mockMvc.perform(get("/api/faqs/search")
                        .param("keyword", "Question")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(faqList.size()))
                .andExpect(jsonPath("$.content[0].question").value("Question 1?"))
                .andExpect(jsonPath("$.content[0].answer").value("Answer 1"))
                .andExpect(jsonPath("$.content[0].faqCategory.name").value("general"))
                .andExpect(jsonPath("$.content[1].question").value("Question 2?"))
                .andExpect(jsonPath("$.content[1].answer").value("Answer 2"))
                .andExpect(jsonPath("$.content[1].faqCategory.name").value("general"));
    }
}
