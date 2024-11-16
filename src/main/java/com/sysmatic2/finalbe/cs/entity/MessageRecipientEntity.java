package com.sysmatic2.finalbe.cs.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_recipient")
public class MessageRecipientEntity {

  @EmbeddedId
  private MessageRecipientId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("messageId")
  @JoinColumn(name = "message_id")
  private ConsultationMessageEntity message;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("recipientId")
  @JoinColumn(name = "recipient_id")
  private MemberEntity recipient;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}


