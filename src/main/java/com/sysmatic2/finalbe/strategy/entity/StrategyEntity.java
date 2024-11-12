package com.sysmatic2.finalbe.strategy.entity;

import com.sysmatic2.finalbe.StandardCodeEntity;
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

@Entity
@Table(name = "strategy")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
public class StrategyEntity extends Auditable {
    @Id
    @Column(name = "strategy_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long strategyId; // 전략 ID

    @OneToOne
    @JoinColumn(name = "trading_type_id")
    private TradingTypeEntity tradingTypeEntity; // 매매유형 ID

    @ManyToOne
    @JoinColumn(name = "strategy_status_code", nullable = false)
    private StandardCodeEntity strategyStatusCode; // 전략상태코드(공통 코드)

    @ManyToOne
    @JoinColumn(name = "trading_cycle_code", nullable = false)
    private StandardCodeEntity tradingCycleCode; // 매매주기코드(공통 코드)

    @Column(name = "followers_count", nullable = false)
    private Long followersCount; // 팔로워수

    @Column(name = "strategy_title", nullable = false)
    private String strategyTitle; // 전략명

    @Column(name = "is_posted", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "isActive 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isPosted; // 공개여부

    @Column(name = "is_granted", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "isActive 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isGranted; // 승인여부

    @Column(name = "cumulative_return", nullable = false)
    private BigDecimal cumulativeReturn; // 누적 수익률

    @Column(name = "one_year_return", nullable = false)
    private BigDecimal oneYearReturn; // 최근 1년 수익률

    @Column(name = "mdd", nullable = false)
    private BigDecimal mdd; // MDD

    @Column(name = "sm_score", nullable = false)
    private BigDecimal smScore; // SM-Score

    @Column(name = "strategy_overview", nullable = false, length = 1000)
    private String strategyOverview; // 전략소개

    @Column(name="strategy_operation_period", nullable = false)
    private Integer strategyOperationPeriod; // 총전략운용일수

    @Column(name = "exit_date")
    private LocalDateTime exitDate; // 전략종료일시

    @CreatedBy
    @Column(name = "writer_id", updatable = false, nullable = false)
    private Long writerId; // 작성자 ID

    @LastModifiedBy
    @Column(name="updater_id")
    private Long updaterId; // 수정자 ID

    @CreatedDate
    @Column(name="writed_at", updatable = false, nullable = false)
    private LocalDateTime writedAt; // 작성일시

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt; // 수정일시

    public void updateOperationPeriod() {
        // 전략 상태가 "STRATEGY_STATUS_ACTIVE"인 경우에만 운용 기간을 계산합니다.
        // 운용 기간은 작성 일자(writedAt)로부터 현재 날짜까지의 일 수로 계산됩니다.
        // 전략이 처음 저장되거나 업데이트될 때마다 이 메서드가 호출되어 운용 기간이 갱신됩니다.
        if ("STRATEGY_STATUS_ACTIVE".equals(this.strategyStatusCode.getCode()) && this.writedAt != null) {
            this.strategyOperationPeriod = (int) ChronoUnit.DAYS.between(this.writedAt, LocalDateTime.now());
        }
    }

    public void setStrategyStatusCode(StandardCodeEntity newStatus) {
        // 상태가 INACTIVE로 변경된 경우에만 종료일 설정
        if (this.strategyStatusCode != null && "STRATEGY_STATUS_INACTIVE".equals(newStatus.getCode()) &&
                !"STRATEGY_STATUS_INACTIVE".equals(this.strategyStatusCode.getCode())) {
            this.exitDate = LocalDateTime.now();
        }
        this.strategyStatusCode = newStatus; // 상태 업데이트
    }
}