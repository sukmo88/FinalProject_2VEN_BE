package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 상담 목록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationListResponseDto {
  private Long id;
  private String investorName;
  private String investorProfileUrl;
  private String traderName;
  private String traderProfileUrl;

//  private Long strategyId; // 전략 ID 포함
  private String strategyName; // 전략 이름 포함

  private LocalDateTime investmentDate;
  private String title;
  private ConsultationStatus status;
  private LocalDateTime createdAt;
}
