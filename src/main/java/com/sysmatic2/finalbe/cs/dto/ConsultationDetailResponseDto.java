package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 상담 상세 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationDetailResponseDto {
  private Long id;
  private String investorId;
  private String investorName;
  private String traderId;
  private String traderName;

  private Long strategyId; // 전략 ID 포함
  private String strategyName; // 전략 이름 포함

  private double investmentAmount;
  private LocalDateTime investmentDate;
  private String title;
  private String content;
  private ConsultationStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
