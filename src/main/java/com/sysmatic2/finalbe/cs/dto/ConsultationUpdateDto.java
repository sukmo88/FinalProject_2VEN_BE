package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
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
  @DecimalMax(value = "1000000000.00", message = "투자 금액은 최대 100억을 초과할 수 없습니다.")
  private BigDecimal investmentAmount;

  private LocalDateTime investmentDate;
  private ConsultationStatus status;
}
