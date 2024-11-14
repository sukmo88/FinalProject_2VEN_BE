package com.sysmatic2.finalbe.cs.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationHistoryDTO {

  private Long messageId;
  private Long senderId;
  private Long receiverId;
  private Long strategyId;
  private String title;
  private String content;
  private LocalDateTime sentAt;
  private LocalDateTime updatedAt;
  private Boolean isRead;
  private Boolean isAnswered;
}
