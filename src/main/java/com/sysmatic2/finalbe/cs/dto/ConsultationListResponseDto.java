package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultationListResponseDto {
  private Long id;
  private String investorName;
  private String traderName;
  private String strategyName;
  private LocalDateTime investmentDate;
  private String title;
  private ConsultationStatus status;
  private LocalDateTime createdAt;
}