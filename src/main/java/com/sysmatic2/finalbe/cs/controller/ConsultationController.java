package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDTO;
import com.sysmatic2.finalbe.cs.dto.ConsultationHistoryDTO;
import com.sysmatic2.finalbe.cs.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

  @Autowired
  private ConsultationService consultationService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @PostMapping("/send")
  public ConsultationMessageDTO sendMessage(@RequestBody ConsultationMessageDTO messageDTO) {
    consultationService.saveMessage(messageDTO);
    return messageDTO;
  }

  @GetMapping("/history/{userId}")
  public List<ConsultationHistoryDTO> getMessageHistory(@PathVariable Long userId) {
    return consultationService.getMessageHistory(userId);
  }

  @PutMapping("/read/{messageId}")
  public void updateReadStatus(@PathVariable Long messageId) {
    consultationService.updateReadStatus(messageId);
  }

  @MessageMapping("/sendMessage")
  @SendTo("/topic/messages")
  public ConsultationMessageDTO sendWebSocketMessage(ConsultationMessageDTO messageDTO) {
    consultationService.saveMessage(messageDTO);
    return messageDTO;
  }

  public void notifyReceiver(Long receiverId, ConsultationMessageDTO messageDTO) {
    messagingTemplate.convertAndSend("/topic/user/" + receiverId, messageDTO);
  }
}
