package com.sysmatic2.finalbe.cs.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
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
@Table(name = "consultation_thread")
public class ConsultationThreadEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "thread_id")
  private Long id; // 상담 스레드 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "investor_id", nullable = false)
  private MemberEntity investor; // 투자자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trader_id", nullable = false)
  private MemberEntity trader; // 트레이더

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "strategy_id", nullable = false)
  private StrategyEntity strategy; // 관련된 투자 전략

  @Column(name = "consultation_title", nullable = false, length = 255)
  private String consultationTitle; // 상담 제목

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt; // 스레드 생성 시간

  @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ConsultationMessageEntity> messages = new ArrayList<>(); // 메시지 목록

  @PrePersist
  protected void onCreate() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }
}
