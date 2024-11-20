package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 공지사항 업데이트 요청 DTO
 * 클라이언트가 공지사항을 업데이트할 때 필요한 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeUpdateDto {

  private String title; // 공지사항 제목 (선택사항)
  private String content; // 공지사항 내용 (선택사항)
  private NoticeStatus noticeStatus; // 공지 상태 (DRAFT, SCHEDULED, PUBLISHED, ARCHIVED)
  private LocalDateTime scheduledAt; // 공지사항 예약 게시 시간 (선택사항)
}
