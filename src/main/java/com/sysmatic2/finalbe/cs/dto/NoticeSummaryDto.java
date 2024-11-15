package com.sysmatic2.finalbe.cs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeSummaryDto {
  private Long id; // 공지사항 ID
  private String title; // 제목
  private LocalDateTime postedAt; // 생성일
  private Long viewCount; // 조회수
}
