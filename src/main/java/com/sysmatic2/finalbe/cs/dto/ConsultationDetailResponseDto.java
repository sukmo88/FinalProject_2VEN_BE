package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsultationDetailResponseDto {
  private Long id;
  private String investorId;
  private String investorName;
  private String traderId;
  private String traderName;
  private String strategyName;
  private double investmentAmount;
  private LocalDateTime investmentDate;
  private String title;
  private String content;
  private ConsultationStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}