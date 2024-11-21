package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * 상담 업데이트 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationUpdateDto {
  private String title;
  private String content;

  private Long strategyId; // 전략 ID 사용 (선택적)
  private String strategyName; // 전략 이름 사용 (선택적)

  @Positive(message = "투자 금액은 양수여야 합니다.")
  private Double investmentAmount;

  private LocalDateTime investmentDate;
  private ConsultationStatus status;
}
