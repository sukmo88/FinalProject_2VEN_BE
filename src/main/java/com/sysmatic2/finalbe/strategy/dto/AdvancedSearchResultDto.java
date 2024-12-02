package com.sysmatic2.finalbe.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AdvancedSearchResultDto {
    private Long strategyId; // 전략 ID
    private String tradingTypeIcon; // 매매유형 아이콘
    private String tradingCycleIcon; // 주기 아이콘
    private List<String> investmentAssetClassesIcons; // 투자자산 분류 아이콘 리스트
    private String strategyTitle; // 전략명

    private BigDecimal cumulativeProfitLossRate; //누적손익률 - dailystatistics
    private BigDecimal recentOneYearReturn;      //최근1년손익률 - dailystatistics
    private BigDecimal Mdd;                      //Mdd(최대자본인하율) - dailystatistics(maxDrawDownRate)

    private BigDecimal smScore;                  //sm-score - strategy
    private Long followersCount;                 // 팔로워 수 - strategy

    private List<Double> cumulativeProfitLossRateList; // 누적 수익률 전체 데이터 - dailystatistics
}