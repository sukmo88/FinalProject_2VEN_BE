package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultationSummaryDto {
  private Long id; // 상담 스레드 ID
  private String consultationTitle; // 상담 제목
  private String investorNickname; // 투자자 닉네임
  private String traderNickname; // 트레이더 닉네임
  private LocalDateTime createdAt; // 상담 생성 시간
  private Boolean isRead; // 상담 읽음 여부 (최신 메시지 기준)
}
