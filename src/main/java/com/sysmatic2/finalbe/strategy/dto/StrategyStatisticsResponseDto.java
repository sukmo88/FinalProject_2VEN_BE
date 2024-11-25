package com.sysmatic2.finalbe.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class StrategyStatisticsResponseDto {

    private BigDecimal balance;
    private BigDecimal cumulativeDepWdPrice;
    private BigDecimal principal;
    private Integer operationPeriod;
    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal cumulativeProfitLoss;
    private BigDecimal cumulativeProfitLossRate;
    private BigDecimal maxCumulativeProfitLoss;
    private BigDecimal maxCumulativeProfitLossRatio;

    private BigDecimal currentDrawdownAmount;
    private BigDecimal currentDrawdownRate;
    private BigDecimal maxDrawdownAmount;
    private BigDecimal maxDrawdownRate;

    private BigDecimal unrealizedProfitLoss;
    private BigDecimal averageProfitLossRate;
    private BigDecimal maxDailyProfit;
    private BigDecimal maxDailyProfitRate;
    private BigDecimal maxDailyLoss;
    private BigDecimal maxDailyLossRate;

    private Integer tradingDays;
    private Integer totalProfitDays;
    private Integer totalLossDays;
    private Integer currentConsecutivePlDays;
    private Integer maxConsecutiveProfitDays;
    private Integer maxConsecutiveLossDays;

    private BigDecimal winRate;
    private Integer daysSincePeak;
    private BigDecimal profitFactor;
    private BigDecimal roa;
}