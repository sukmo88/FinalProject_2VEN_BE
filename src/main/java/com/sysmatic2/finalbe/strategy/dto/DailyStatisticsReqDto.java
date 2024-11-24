package com.sysmatic2.finalbe.strategy.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatisticsReqDto {
    private LocalDate date; // 일자
    private BigDecimal depWdPrice; // 입출금
    private BigDecimal dailyProfitLoss; // 일손익
}
