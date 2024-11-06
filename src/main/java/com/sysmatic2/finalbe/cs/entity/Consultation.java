package com.sysmatic2.finalbe.cs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "consultation")
public class Consultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "consultation_id")
  private Long id;

  @Column(name = "sender_id", nullable = false)
  private Long senderId;

  @Column(name = "receiver_id", nullable = false)
  private Long receiverId;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "sent_at", nullable = false)
  private LocalDateTime sentAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_read")
  private Boolean isRead;

  @Column(name = "is_answered", nullable = false)
  private Boolean isAnswered;

  @Column(name = "answered_at")
  private LocalDateTime answeredAt;
}
