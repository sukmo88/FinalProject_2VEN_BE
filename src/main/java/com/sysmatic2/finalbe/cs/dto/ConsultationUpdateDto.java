package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConsultationUpdateDto {
  private String title;
  private String content;
  private String strategyName;
  private double investmentAmount;
  private LocalDateTime investmentDate;
  private ConsultationStatus status;
}