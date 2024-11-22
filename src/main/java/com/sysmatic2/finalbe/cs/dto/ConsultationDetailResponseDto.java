package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private String investorProfileUrl; // 추가
  private String traderId;
  private String traderName;
  private String traderProfileUrl; // 추가

  private Long strategyId; // 전략 ID 포함
  private String strategyName; // 전략 이름 포함

  private double investmentAmount;
  private LocalDateTime investmentDate;
  private String title;
  private String content;
  private ConsultationStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // 추가된 필드: 트레이더의 답변 내용 및 답변 날짜
  private String replyContent;
  private LocalDateTime answerDate;

  // 추가된 필드: 답변 생성일 및 수정일
  private LocalDateTime replyCreatedAt;
  private LocalDateTime replyUpdatedAt;
}
