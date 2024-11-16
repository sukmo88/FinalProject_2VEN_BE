package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultationMessageDto {
  private Long id; // 메시지 ID
  private String content; // 메시지 내용
  private String senderNickname; // 발신자 닉네임
  private LocalDateTime sentAt; // 메시지 전송 시간
  private Boolean isRead; // 메시지 읽음 여부
}
