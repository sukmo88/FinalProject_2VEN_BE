package com.sysmatic2.finalbe.strategy.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class StrategySmScoreDto {
    private Long strategyId; // 전략 ID
    private BigDecimal smScore; // SM-SCORE
}