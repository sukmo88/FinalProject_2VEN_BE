package com.sysmatic2.finalbe.strategy.entity;

import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class StrategyHistoryEntity extends Auditable {
    @Column(name = "strategy_id", nullable = false)
    private Long strategyId; //전략 ID

    @Id
    @Column(name = "strategy_history_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //전략 이력 ID

    @Column(name = "trading_type_id", nullable = false)
    private Integer tradingTypeId; //매매 유형 ID

    @Column(name = "trading_cycle", nullable = false)
    private Integer tradingCycle; //전략 주기

    @Column(name = "strategy_status_code", nullable = false)
    private String strategyStatusCode; // 전략상태코드(공통 코드) - 운용중/운용종료

    @Column(name = "strategy_history_status_code", nullable = false)
    private String strategyHistoryStatusCode; //전략 이력 공통코드(게시/삭제/수정)

    @Column(name = "min_investment_amount", nullable = false)
    private String minInvestmentAmount; //최소운용가능금액

    @Column(name = "followers_count", nullable = false)
    private Long followersCount; //팔로워수

    @Column(name = "strategy_title", length = 300, nullable = false)
    private String strategyTitle; //전략명

    @Column(name = "kp_ratio", nullable = true, precision = 19, scale = 4)
    private BigDecimal kpRatio; // KP-Ratio

    @Column(name = "sm_score", nullable = true, precision = 10, scale = 2)
    private BigDecimal smScore; // SM-Score

    @Column(name = "writer_id", nullable = false)
    private String writerId; //작성자 ID

    @Column(name = "is_posted", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "isPosted 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isPosted; //공개여부

    @Column(name = "is_approved", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N|P", message = "isApproved 필드는 'Y','N','P'만 허용됩니다.")
    private String isApproved; //승인여부

    @Column(name = "writed_at", nullable = false)
    private LocalDateTime writedAt; //작성일시

    @Column(name = "strategy_overview", length = 3000)
    private String strategyOverview; //전략소개

    @Column(name = "updater_id")
    private String updaterId; //수정자 ID

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시

    @Column(name = "exit_date")
    private LocalDateTime exitDate; //종료일시

    @Column(name = "change_start_date", nullable = false)
    private LocalDateTime changeStartDate; //변경시작일시

    @Column(name = "change_end_date", nullable = false)
    private LocalDateTime changeEndDate; //변경종료일시

    //전략엔티티를 받아서 전략 이력 엔티티를 만드는 생성자
    public StrategyHistoryEntity(StrategyEntity strategyEntity, String statusHistoryCode, LocalDateTime startDatetime){
        this.strategyId = strategyEntity.getStrategyId();
        this.tradingTypeId = strategyEntity.getTradingTypeEntity().getTradingTypeId();
        this.tradingCycle = strategyEntity.getTradingCycleEntity().getTradingCycleId();
        this.strategyStatusCode = strategyEntity.getStrategyStatusCode();
        this.strategyHistoryStatusCode = statusHistoryCode;
        this.minInvestmentAmount = strategyEntity.getMinInvestmentAmount();
        this.followersCount = strategyEntity.getFollowersCount();
        this.strategyTitle = strategyEntity.getStrategyTitle();
        this.writerId = strategyEntity.getWriterId();
        this.writedAt = strategyEntity.getWritedAt();
        this.isPosted = strategyEntity.getIsPosted();
        this.isApproved = strategyEntity.getIsApproved();
        this.updaterId = strategyEntity.getUpdaterId();
        this.updatedAt = strategyEntity.getUpdatedAt();
        this.exitDate = strategyEntity.getExitDate();
        this.changeStartDate = startDatetime;
        this.changeEndDate = LocalDateTime.now();
    }
}
