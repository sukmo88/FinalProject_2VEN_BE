package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateNoticeDto {
  private NoticeStatus noticeStatus; // 공지 상태
  private String title; // 제목
  private String content; // 내용
  private LocalDateTime scheduledAt; // 예약일
  private String writerId; // 작성자 ID
}
