package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDTO;
import com.sysmatic2.finalbe.cs.dto.ConsultationHistoryDTO;
import com.sysmatic2.finalbe.cs.service.ConsultationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ConsultationControllerTest {

  private MockMvc mockMvc;

  @Mock
  private ConsultationService consultationService;

  @InjectMocks
  private ConsultationController consultationController;

  @Test
  void testSendMessage() throws Exception {
    ConsultationMessageDTO messageDTO = new ConsultationMessageDTO();
    messageDTO.setSenderId(1L);
    messageDTO.setReceiverId(2L);
    messageDTO.setStrategyId(1L);
    messageDTO.setTitle("Test Title");

    mockMvc = MockMvcBuilders.standaloneSetup(consultationController).build();

    mockMvc.perform(post("/api/consultations/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"senderId\":1,\"receiverId\":2,\"strategyId\":1,\"title\":\"Test Title\",\"content\":\"Test Content\"}"))
            .andExpect(status().isOk());

    verify(consultationService, times(1)).saveMessage(any(ConsultationMessageDTO.class));
  }

  @Test
  void testGetMessageHistory() throws Exception {
    ConsultationHistoryDTO historyDTO = new ConsultationHistoryDTO();
    historyDTO.setMessageId(1L);
    historyDTO.setTitle("Test Title");

    when(consultationService.getMessageHistory(1L)).thenReturn(List.of(historyDTO));

    mockMvc = MockMvcBuilders.standaloneSetup(consultationController).build();

    mockMvc.perform(get("/api/consultations/history/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Test Title"));

    verify(consultationService, times(1)).getMessageHistory(1L);
  }

  @Test
  void testUpdateReadStatus() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(consultationController).build();

    mockMvc.perform(put("/api/consultations/read/1"))
            .andExpect(status().isOk());

    verify(consultationService, times(1)).updateReadStatus(1L);
  }
}
