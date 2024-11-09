package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDTO;
import com.sysmatic2.finalbe.cs.dto.ConsultationHistoryDTO;
import com.sysmatic2.finalbe.cs.entity.Consultation;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ConsultationServiceTest {

  @Mock
  private ConsultationRepository consultationRepository;

  @InjectMocks
  private ConsultationService consultationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSaveMessage() {
    ConsultationMessageDTO messageDTO = new ConsultationMessageDTO();
    messageDTO.setSenderId(1L);
    messageDTO.setReceiverId(2L);
    messageDTO.setStrategyId(1L);
    messageDTO.setTitle("Test Title");
    messageDTO.setContent("Test Content");

    Consultation consultation = new Consultation();
    consultation.setId(1L);
    when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

    Consultation result = consultationService.saveMessage(messageDTO);

    assertNotNull(result);
    assertEquals(consultation.getId(), result.getId());
    verify(consultationRepository, times(1)).save(any(Consultation.class));
  }

  @Test
  void testGetMessageHistory() {
    Consultation consultation = new Consultation();
    consultation.setId(1L);
    consultation.setSenderId(1L);
    consultation.setReceiverId(2L);
    consultation.setStrategyId(1L);
    consultation.setTitle("Test Title");

    when(consultationRepository.findBySenderIdOrReceiverId(1L, 1L))
            .thenReturn(List.of(consultation));

    List<ConsultationHistoryDTO> history = consultationService.getMessageHistory(1L);

    assertNotNull(history);
    assertEquals(1, history.size());
    assertEquals(consultation.getTitle(), history.get(0).getTitle());
    verify(consultationRepository, times(1)).findBySenderIdOrReceiverId(1L, 1L);
  }

  @Test
  void testUpdateReadStatus() {
    Consultation consultation = new Consultation();
    consultation.setId(1L);
    consultation.setIsRead(false);

    when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

    consultationService.updateReadStatus(1L);

    assertTrue(consultation.getIsRead());
    verify(consultationRepository, times(1)).save(consultation);
  }
}
