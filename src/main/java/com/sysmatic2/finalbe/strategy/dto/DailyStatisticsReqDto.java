package com.sysmatic2.finalbe.strategy.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
public class DailyStatisticsReqDto {
    private Long strategyId; // 전략 ID
    private LocalDate date; // 일자
    private BigDecimal depWdPrice; // 입출금
    private BigDecimal dailyProfitLoss; // 일손익
}
