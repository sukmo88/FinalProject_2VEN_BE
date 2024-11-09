package com.sysmatic2.finalbe.cs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notice")
public class Notice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notice_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "notice_status", nullable = false)
  private NoticeStatus noticeStatus;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "posted_at", nullable = false)
  private LocalDateTime postedAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "view_count")
  private Long viewCount;

  @Column(name = "writer_id", nullable = false)
  private Long writerId;
}
