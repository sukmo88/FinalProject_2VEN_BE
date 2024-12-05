package com.sysmatic2.finalbe.strategy.entity;

import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@Table(name = "strategy")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = {"strategyIACEntities", "tradingTypeEntity", "tradingCycleEntity", "writerId", "updaterId"})
public class StrategyEntity extends Auditable {
    @Id
    @Column(name = "strategy_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long strategyId; // 전략 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_type_id")
    private TradingTypeEntity tradingTypeEntity; // 매매유형 ID

    @Column(name = "strategy_status_code", nullable = false)
    private String strategyStatusCode; // 전략상태코드(공통 코드) - 운용중/운용종료

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_cycle_id", nullable = false)
    private TradingCycleEntity tradingCycleEntity; // 매매주기 ID

    @Column(name = "min_investment_amount", nullable = false)
    private String minInvestmentAmount; //최소운용가능금액

    @Column(name = "strategy_title", nullable = false)
    private String strategyTitle; // 전략명

    @CreatedBy
    @Column(name = "writer_id", updatable = false, nullable = false)
    private String writerId; // 작성자 ID

    @Column(name = "is_posted", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "isPosted 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isPosted; // 공개여부

    @Column(name = "is_approved", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N|P", message = "isApproved 필드는 'Y','N','P'만 허용됩니다.")
    private String isApproved = "N"; // 승인여부 default = N, 승인 요청을 보내면 P, 승인되면 Y

    @CreatedDate
    @Column(name="writed_at", updatable = false, nullable = false)
    private LocalDateTime writedAt; // 작성일시

    @Column(name = "strategy_overview", length = 3000)
    private String strategyOverview; // 전략소개 1000자

    @LastModifiedBy
    @Column(name="updater_id")
    private String updaterId; // 수정자 ID

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt; // 수정일시

    @Column(name = "exit_date")
    private LocalDateTime exitDate; // 전략종료일시

    @Column(name = "kp_ratio", nullable = false, precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000")
    private BigDecimal kpRatio = BigDecimal.ZERO; // KP-Ratio

    @Column(name = "sm_score", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000")
    private BigDecimal smScore = BigDecimal.ZERO; // SM-Score

    @Column(name = "followers_count", nullable = false)
    private Long followersCount = 0L; // 팔로워수 default = 0

    //전략(1) : 관계(N)
    @OneToMany(mappedBy = "strategyEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StrategyIACEntity> strategyIACEntities;

    public void incrementFollowersCount() {
        this.followersCount++;
    }

    // 팔로우 감소 (followersCount가 음수가 되지 않도록 보호)
    public void decrementFollowersCount() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }
}