package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.StandardCodeEntity;
import com.sysmatic2.finalbe.strategy.entity.TradingTypeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class StrategyRequestDto {
    private TradingTypeEntity tradingTypeEntity; // 매매유형 ID
    private StandardCodeEntity tradingCycleCode; // 매매주기코드(공통 코드)
    private String strategyTitle; // 전략명
    private String isPosted; // 공개여부
    private String strategyOverview; // 전략소개
    private BigDecimal principal; // 원금
    private BigDecimal currentPortfolioValue; // 평가금액
}