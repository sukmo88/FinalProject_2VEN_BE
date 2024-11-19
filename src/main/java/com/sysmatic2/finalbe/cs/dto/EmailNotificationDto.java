package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailNotificationDto {
  private String recipientEmail; // 수신자 이메일
  private String subject; // 이메일 제목
  private String body; // 이메일 본문
}
