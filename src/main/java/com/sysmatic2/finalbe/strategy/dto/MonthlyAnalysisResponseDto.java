package com.sysmatic2.finalbe.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 월간 분석 목록 응답을 위한 DTO.
 */
@Data
@Builder
@AllArgsConstructor
public class MonthlyAnalysisResponseDto {
    private Long strategyMonthlyDataId; // 월간 분석 ID
    private String analysisMonth; // 분석 월 (YYYY-MM)
    private BigDecimal monthlyAveragePrincipal; // 월평균 원금
    private BigDecimal monthlyDepWdAmount; // 월 입출금
    private BigDecimal monthlyPl; // 월 손익
    private BigDecimal monthlyReturn; // 월 손익률
    private BigDecimal monthlyCumulativePl; // 월 누적 손익
    private BigDecimal monthlyCumulativeReturn; // 월 누적 손익률
}