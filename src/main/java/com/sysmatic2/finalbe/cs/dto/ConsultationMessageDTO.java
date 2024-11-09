package com.sysmatic2.finalbe.cs.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConsultationMessageDTO {

  private Long senderId;
  private Long receiverId;
  private Long strategyId;
  private String title;
  private String content;
  private LocalDateTime sentAt;
}
