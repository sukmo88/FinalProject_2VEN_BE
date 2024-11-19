package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;

@Data
public class SendMessageDto {
  private Long threadId; // 상담 스레드 ID
  private String content; // 메시지 내용
}
