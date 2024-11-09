package com.sysmatic2.finalbe.cs.dto;

import java.time.LocalDateTime;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import lombok.Data;

@Data
public class NoticeDTO {
  private Long id;
  private NoticeStatus noticeStatus;
  private String title;
  private String content;
  private LocalDateTime postedAt;
  private LocalDateTime updatedAt;
  private LocalDateTime scheduledAt;
  private Long viewCount;
  private Long writerId;
}
