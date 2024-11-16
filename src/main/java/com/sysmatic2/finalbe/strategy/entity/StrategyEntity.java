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
@ToString(exclude = "strategyIACEntities")
public class StrategyEntity extends Auditable {
    @Id
    @Column(name = "strategy_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long strategyId; // 전략 ID


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_type_id", nullable = false)
    private TradingTypeEntity tradingTypeEntity; // 매매유형 ID

    @Column(name = "strategy_status_code", nullable = false)
    private String strategyStatusCode; // 전략상태코드(공통 코드)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trading_cycle_id", nullable = false)
    private TradingCycleEntity tradingCycleEntity; // 매매주기 ID

    @Column(name = "min_investment_amount", nullable = false)
    private String minInvestmentAmount; //최소운용가능금액

    @Column(name = "followers_count", nullable = false)
    private Long followersCount = 0L; // 팔로워수 default = 0

    @Column(name = "strategy_title", nullable = false)
    private String strategyTitle; // 전략명

    @CreatedBy
    @Column(name = "writer_id", updatable = false, nullable = false)
    private Long writerId; // 작성자 ID

    @Column(name = "is_posted", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "isActive 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isPosted; // 공개여부

    @Column(name = "is_granted", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "isActive 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isGranted = "N"; // 승인여부 default = N

    @CreatedDate
    @Column(name="writed_at", updatable = false, nullable = false)
    private LocalDateTime writedAt; // 작성일시

    @Column(name = "strategy_overview", length = 1000)
    private String strategyOverview; // 전략소개

    @LastModifiedBy
    @Column(name="updater_id")
    private Long updaterId; // 수정자 ID

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt; // 수정일시

    @Column(name = "exit_date")
    private LocalDateTime exitDate; // 전략종료일시

    //전략(1) : 관계(N)
    @OneToMany(mappedBy = "strategyEntity")
    private List<StrategyIACEntity> strategyIACEntities;

//    public void updateOperationPeriod() {
//        // 전략 상태가 "STRATEGY_STATUS_ACTIVE"인 경우에만 운용 기간을 계산합니다.
//        // 운용 기간은 작성 일자(writedAt)로부터 현재 날짜까지의 일 수로 계산됩니다.
//        // 전략이 처음 저장되거나 업데이트될 때마다 이 메서드가 호출되어 운용 기간이 갱신됩니다.
//        if ("STRATEGY_STATUS_ACTIVE".equals(this.strategyStatusCode) && this.writedAt != null) {
//            this.strategyOperationDays = (int) ChronoUnit.DAYS.between(this.writedAt, LocalDateTime.now());
//        }
//    }

    public void setStrategyStatusCode(String newStatus) {
        // 상태가 INACTIVE로 변경된 경우에만 종료일 설정
        if (this.strategyStatusCode != null && "STRATEGY_STATUS_INACTIVE".equals(newStatus) &&
                !"STRATEGY_STATUS_INACTIVE".equals(this.strategyStatusCode)) {
            this.exitDate = LocalDateTime.now();
        }
        this.strategyStatusCode = newStatus; // 상태 업데이트
    }
}