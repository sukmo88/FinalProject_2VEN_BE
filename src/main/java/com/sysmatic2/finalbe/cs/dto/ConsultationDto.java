package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultationDto {
  private Long id; // 상담 스레드 ID
  private Long investorId; // 투자자 ID
  private Long traderId; // 트레이더 ID
  private Long strategyId; // 전략 ID
  private String strategyName; // 전략 이름
  private String consultationTitle; // 상담 제목
  private String initialContent; // 초기 상담 내용
  private LocalDateTime createdAt; // 상담 생성 시간
}
