package com.sysmatic2.finalbe.cs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "faq")
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

  @Column(name = "posted_at")
  private LocalDateTime postedAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_active")
  private Boolean isActive;

  @ManyToOne
  @JoinColumn(name = "faq_category_id", nullable = false)
  private FAQCategory faqCategory;
}