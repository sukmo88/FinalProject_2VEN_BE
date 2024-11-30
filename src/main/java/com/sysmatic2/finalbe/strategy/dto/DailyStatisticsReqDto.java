package com.sysmatic2.finalbe.strategy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "날짜는 필수 입력값입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd") // 날짜 형식 명시
    private LocalDate date; // 일자

    @NotNull(message = "입출금 금액은 필수 입력값입니다.")
    private BigDecimal depWdPrice; // 입출금 (양수: 입금, 음수: 출금)

    @NotNull(message = "일손익은 필수 입력값입니다.")
    private BigDecimal dailyProfitLoss; // 일손익 (양수: 이익, 음수: 손실)
}
