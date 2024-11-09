package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDTO;
import com.sysmatic2.finalbe.cs.dto.ConsultationHistoryDTO;
import com.sysmatic2.finalbe.cs.entity.Consultation;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsultationService {

  @Autowired
  private ConsultationRepository consultationRepository;

  public Consultation saveMessage(ConsultationMessageDTO messageDTO) {
    Consultation consultation = Consultation.builder()
            .senderId(messageDTO.getSenderId())
            .receiverId(messageDTO.getReceiverId())
            .strategyId(messageDTO.getStrategyId())
            .title(messageDTO.getTitle())
            .content(messageDTO.getContent())
            .sentAt(LocalDateTime.now())
            .isRead(false)
            .isAnswered(false)
            .build();

    return consultationRepository.save(consultation);
  }

  public List<ConsultationHistoryDTO> getMessageHistory(Long userId) {
    List<Consultation> consultations = consultationRepository.findBySenderIdOrReceiverId(userId, userId);
    return consultations.stream()
            .map(this::convertToHistoryDTO)
            .collect(Collectors.toList());
  }

  private ConsultationHistoryDTO convertToHistoryDTO(Consultation consultation) {
    return ConsultationHistoryDTO.builder()
            .messageId(consultation.getId())
            .senderId(consultation.getSenderId())
            .receiverId(consultation.getReceiverId())
            .strategyId(consultation.getStrategyId())
            .title(consultation.getTitle())
            .content(consultation.getContent())
            .sentAt(consultation.getSentAt())
            .updatedAt(consultation.getUpdatedAt())
            .isRead(consultation.getIsRead())
            .isAnswered(consultation.getIsAnswered())
            .build();
  }

  public void updateReadStatus(Long messageId) {
    Consultation consultation = consultationRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
    consultation.setIsRead(true);
    consultation.setUpdatedAt(LocalDateTime.now());
    consultationRepository.save(consultation);
  }
}
