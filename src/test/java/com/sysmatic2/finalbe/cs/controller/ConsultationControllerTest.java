package com.sysmatic2.finalbe.cs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.cs.NoOpSecurityConfig;
import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.service.ConsultationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(ConsultationController.class)
@Import(NoOpSecurityConfig.class) // Security 비활성화
class ConsultationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ConsultationService consultationService;

  @Autowired
  private ObjectMapper objectMapper;

  private ConsultationMessageDto messageDto;
  private ConsultationSummaryDto summaryDto;

  @BeforeEach
  void setUp() {
    // Mock Message DTO
    messageDto = new ConsultationMessageDto();
    messageDto.setId(1L);
    messageDto.setContent("Test Message");
    messageDto.setSenderNickname("Investor");
    messageDto.setSentAt(LocalDateTime.now());
    messageDto.setIsRead(false);

    // Mock Summary DTO
    summaryDto = new ConsultationSummaryDto();
    summaryDto.setId(1L);
    summaryDto.setConsultationTitle("Test Consultation");
    summaryDto.setInvestorNickname("Investor");
    summaryDto.setTraderNickname("Trader");
    summaryDto.setCreatedAt(LocalDateTime.now());
    summaryDto.setIsRead(false);
  }

  @Test
  void createConsultation_ShouldReturnConsultation() throws Exception {
    ConsultationDto consultationDto = new ConsultationDto();
    consultationDto.setId(1L);
    consultationDto.setConsultationTitle("Test Consultation");

    Mockito.when(consultationService.createConsultation(
                    anyString(), anyString(), anyLong(), anyString(), anyString()))
            .thenReturn(consultationDto);

    mockMvc.perform(post("/api/consultations")
                    .param("investorId", "investor-001")
                    .param("traderId", "trader-001")
                    .param("strategyId", "1")
                    .param("consultationTitle", "Test Consultation")
                    .param("content", "Test Content"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.consultationTitle").value("Test Consultation"));
  }

  @Test
  void sendMessage_ShouldReturnMessage() throws Exception {
    SendMessageDto sendMessageDto = new SendMessageDto();
    sendMessageDto.setThreadId(1L);
    sendMessageDto.setContent("Test Message");

    Mockito.when(consultationService.sendMessage(
                    anyString(), any(SendMessageDto.class)))
            .thenReturn(messageDto);

    mockMvc.perform(post("/api/consultations/messages")
                    .param("senderId", "investor-001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sendMessageDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.content").value("Test Message"))
            .andExpect(jsonPath("$.senderNickname").value("Investor"))
            .andExpect(jsonPath("$.isRead").value(false));
  }

  @Test
  void getUserMessages_ShouldReturnPageOfMessages() throws Exception {
    Page<ConsultationMessageDto> page = new PageImpl<>(
            Collections.singletonList(messageDto),
            PageRequest.of(0, 10),
            1);

    Mockito.when(consultationService.getUserMessages(
                    eq("investor-001"), eq(true), any(PageRequest.class)))
            .thenReturn(page);

    mockMvc.perform(get("/api/consultations/messages")
                    .param("userId", "investor-001")
                    .param("sent", "true")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].content").value("Test Message"));
  }

  @Test
  void searchMessagesByKeyword_ShouldReturnPageOfMessages() throws Exception {
    Page<ConsultationMessageDto> page = new PageImpl<>(
            Collections.singletonList(messageDto),
            PageRequest.of(0, 10),
            1);

    Mockito.when(consultationService.searchMessagesByKeyword(
                    eq("Test"), any(PageRequest.class)))
            .thenReturn(page);

    mockMvc.perform(get("/api/consultations/messages/search")
                    .param("keyword", "Test")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].content").value("Test Message"));
  }

  @Test
  void getUserConsultations_ShouldReturnSummaryList() throws Exception {
    Mockito.when(consultationService.getUserConsultations(
                    eq("investor-001")))
            .thenReturn(Collections.singletonList(summaryDto));

    mockMvc.perform(get("/api/consultations/summary")
                    .param("userId", "investor-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].consultationTitle").value("Test Consultation"));
  }
}
