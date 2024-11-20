package com.sysmatic2.finalbe.cs.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 상담 엔티티
 * 단일 메시지 기반의 상담을 표현
 */
@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 상담을 요청한 투자자
  @ManyToOne
  @JoinColumn(name = "investor_id", nullable = false)
  private MemberEntity investor;

  // 상담을 제공하는 트레이더
  @ManyToOne
  @JoinColumn(name = "trader_id", nullable = false)
  private MemberEntity trader;

  // 투자 전략
  @ManyToOne
  @JoinColumn(name = "strategy_id", nullable = true)
  private StrategyEntity strategyName;

  // 투자 금액
  @Column(nullable = false)
  private double investmentAmount;

  // 투자 시점
  @Column(nullable = false)
  private LocalDateTime investmentDate;

  // 상담 제목
  @Column(nullable = false, length = 255) // 제목 길이 제한
  private String title;

  // 상담 내용
  @Column(nullable = false, length = 1000)
  private String content;

  // 상담 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConsultationStatus status;

  // 생성일
  @Column(nullable = false)
  private LocalDateTime createdAt;

  // 수정일
  @Column(nullable = false)
  private LocalDateTime updatedAt;

}
