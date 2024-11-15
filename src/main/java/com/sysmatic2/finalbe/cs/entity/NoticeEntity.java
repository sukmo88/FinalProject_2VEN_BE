package com.sysmatic2.finalbe.cs.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notice")
public class NoticeEntity {

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

  @CreatedDate
  @Column(name = "posted_at", nullable = false, updatable = false)
  private LocalDateTime postedAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L; // 기본값 설정

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id", nullable = false)
  private MemberEntity writer; // MemberEntity와 연관 관계 설정

  @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> attachments = new ArrayList<>(); // 첨부파일 연관 관계 설정

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now(); // updatedAt 자동 갱신
  }
}
