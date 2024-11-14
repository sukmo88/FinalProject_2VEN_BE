package com.sysmatic2.finalbe.admin.entity;

import com.sysmatic2.finalbe.StandardCodeEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.Auditable;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_approval_history")
@Getter
@Setter
@ToString
public class StrategyApprovalHistoryEntity extends Auditable {
    @Id
    @Column(name = "strategy_approval_history_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long strategyApprovalHistoryId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member; // 전략을 승인 또는 반랴한 관리자 id

    @ManyToOne
    @JoinColumn(name = "strategy_id", nullable = false)
    private StrategyEntity strategy;

    @Column(name = "request_reason_code", nullable = false)
    private String requestReasonCode;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "approval_reason")
    private String approvalReason;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;

    @Column(name = "status_code", nullable = false)
    private String statusCode;
}