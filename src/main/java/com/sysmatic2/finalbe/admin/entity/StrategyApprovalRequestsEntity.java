package com.sysmatic2.finalbe.admin.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.common.Auditable;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_approval_requests")
@Getter
@Setter
@ToString
public class StrategyApprovalRequestsEntity extends Auditable {
    @Id
    @Column(name = "strategy_approval_requests_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long strategyApprovalRequestsId;

    @Column(name = "request_datetime", nullable = false)
    private LocalDateTime requestDatetime; //승인 요청 일시

    @Column(name = "is_approved")
    private String isApproved; //승인여부(결과)

    @ManyToOne
    @JoinColumn(name = "strategy_id", nullable = false)
    private StrategyEntity strategy; //전략 ID

    @Column(name = "is_posted", nullable = false)
    private String isPosted; //공개 여부

    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private MemberEntity applicant; // 요청자 ID

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private MemberEntity admin; // 관리자 ID

    @Column(name = "approval_datetime")
    private LocalDateTime approvalDatetime; // 승인 일시

    @Column(name = "rejection_reason")
    private String rejectionReason; //거부 사유

    @Column(name = "rejection_datetime")
    private LocalDateTime rejectionDatetime; //거부 일시
}