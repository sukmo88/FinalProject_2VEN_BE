package com.sysmatic2.finalbe.admin.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.common.Auditable;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
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

    @Column(name = "request_datetime", updatable = false, nullable = false)
    private LocalDateTime requestDatetime; //승인 요청 일시

    @Column(name = "is_approved")
    @Pattern(regexp = "Y|N|P", message = "isApproved 필드는 'Y','N','P'만 허용됩니다.")
    private String isApproved; //승인여부(결과) - 등록하면 P(대기), 거부되면 N(거절), 승인되면 Y(승인)

    @ManyToOne
    @JoinColumn(name = "strategy_id", nullable = false)
    private StrategyEntity strategy; //전략 ID

    @Column(name = "is_posted", nullable = false)
    @Pattern(regexp = "Y|N", message = "isPosted 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isPosted; //공개 여부

    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private MemberEntity applicant; // 요청자 ID

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private MemberEntity admin; // 관리자 ID

    @Column(name = "approval_datetime")
    private LocalDateTime approvalDatetime; // 승인 일시

    @Column(name = "rejection_reason", length = 3000)
    private String rejectionReason; //거부 사유

    @Column(name = "rejection_datetime")
    private LocalDateTime rejectionDatetime; //거부 일시
}