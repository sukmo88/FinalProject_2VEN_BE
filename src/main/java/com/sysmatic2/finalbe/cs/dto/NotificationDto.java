package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
  private String type; // 알림 유형 (예: "message_received")
  private Long messageId; // 메시지 ID
  private String senderNickname; // 발신자 닉네임
  private String content; // 메시지 내용
  private LocalDateTime sentAt; // 메시지 전송 시간
}
