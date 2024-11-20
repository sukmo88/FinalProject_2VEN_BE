package com.sysmatic2.finalbe.cs.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "consultation_message")
public class ConsultationMessageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id")
  private Long id; // 메시지 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "thread_id", nullable = false)
  private ConsultationThreadEntity thread; // 상담 스레드 참조

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private MemberEntity sender; // 메시지 발신자

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content; // 메시지 내용

  @Column(name = "sent_at", nullable = false)
  private LocalDateTime sentAt; // 메시지 전송 시간

  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false; // 메시지 읽음 여부

  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MessageRecipientEntity> recipients = new ArrayList<>(); // 수신자 목록

}
