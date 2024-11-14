package com.sysmatic2.finalbe.cs.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(name = "faq")
@Getter
@Setter
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FAQ {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "faq_id")
  private Long id;

  @Column(name = "writer_id", nullable = false)
  private Long writerId;

  @Column(name = "question", nullable = false, length = 255)
  private String question;

  @Column(name = "answer", columnDefinition = "TEXT")
  private String answer;

  @Column(name = "posted_at", nullable = false)
  private LocalDateTime postedAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;

  @ManyToOne
  @JoinColumn(name = "faq_category_id")
  private FAQCategory faqCategory;

}