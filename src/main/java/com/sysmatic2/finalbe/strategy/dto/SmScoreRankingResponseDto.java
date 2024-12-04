package com.sysmatic2.finalbe.strategy.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SmScoreRankingResponseDto {
    private Long strategyId; // 전략 ID
    private String strategyTitle; // 전략명
    private String profilePath;  // 프로필 이미지 링크
    private String nickname; // 닉네임
    private List<BigDecimal> cumulativeProfitLossRateList; // 누적 수익률 전체 데이터 - dailystatistics
    private BigDecimal dailyPlRate; // 일손익률
}