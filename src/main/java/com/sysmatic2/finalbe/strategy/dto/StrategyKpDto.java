package com.sysmatic2.finalbe.strategy.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StrategyKpDto {
    private Long strategyId;      // 전략 ID
    private BigDecimal kpRatio;  // KP-RATIO
}