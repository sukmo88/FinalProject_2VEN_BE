package com.sysmatic2.finalbe.strategy.entity;

import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "monthly_statistics")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatisticsEntity extends Auditable {
    @ManyToOne
    @JoinColumn(name = "strategy_id", nullable = false)
    private StrategyEntity strategyEntity; // 전략 FK

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_statistics_id", nullable = false)
    private Long monthlyStatisticsId; // 전략 월간 통계 ID

    @Column(name = "analysis_month", nullable = false)
    private YearMonth analysisMonth; // 년월 - @converter필요

    @Column(name = "monthly_average_principal", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyAvgPrincipal; //월평균 원금 - 해당 월의 원금들의 평균값

    @Column(name = "monthly_dep_wd_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyDepWdAmount; // 월 입출금 총액 - 해당 월의 입출금액 총합

    @Column(name = "monthly_profit_loss", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyProfitLoss; // 월손익 - 해당 월의 일손익 합산

    @Column(name = "monthly_return", nullable = false, precision = 10, scale = 4)
    private BigDecimal monthlyReturn; // 월 손익률 - 해당월 마지막 기준가 / 해당월 첫번째 기준가 -1

    @Column(name = "monthly_cumulative_profit_loss", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyCumulativeProfitLoss; // 월누적손익 - 해당월까지의 일손익 합산

    @Column(name = "monthly_cumulative_return", nullable = false, precision = 10, scale = 4)
    private BigDecimal monthlyCumulativeReturn; // 월누적손익률(%) - 해당월 마지막 기준가 / 1000 - 1

    @Column(name = "monthly_average_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyAvgBalance; // 월평균 잔고 - 해당 월의 잔고들의 평균값
}
