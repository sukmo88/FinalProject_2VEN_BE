package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

/**
 * 상담 생성 요청 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultationCreateDto {
  @NotBlank(message = "투자자 ID는 필수입니다.")
  private String investorId;

  @NotBlank(message = "트레이더 ID는 필수입니다.")
  private String traderId;

  @NotNull(message = "전략 ID는 필수입니다.")
  private Long strategyId; // 전략 ID 사용

  @NotBlank(message = "전략 이름은 필수입니다.")
  private String strategyName; // 전략 이름 사용

  @NotNull(message = "투자 금액은 필수입니다.")
  @Positive(message = "투자 금액은 양수여야 합니다.")
  @DecimalMax(value = "1000000000.00", message = "투자 금액은 최대 100억을 초과할 수 없습니다.")
  private BigDecimal investmentAmount;

  @NotNull(message = "투자 시점은 필수입니다.")
  private LocalDateTime investmentDate;

  @NotBlank(message = "상담 제목은 필수입니다.")
  private String title;

  @NotBlank(message = "상담 내용은 필수입니다.")
  private String content;

  @NotNull(message = "상담 상태는 필수입니다.")
  @Builder.Default
  private ConsultationStatus status = ConsultationStatus.PENDING; // 기본값 설정
}