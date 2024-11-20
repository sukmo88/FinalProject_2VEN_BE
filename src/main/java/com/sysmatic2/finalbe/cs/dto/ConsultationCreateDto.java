package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

@Builder
@Data
public class ConsultationCreateDto {
  @NotBlank
  private String investorId;
  @NotBlank
  private String traderId;
  @NotBlank
  private String strategyName;
  private double investmentAmount;
  private LocalDateTime investmentDate;
  private String title;
  private String content;
  private ConsultationStatus status;
}
