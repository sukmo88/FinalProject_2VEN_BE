package com.sysmatic2.finalbe.strategy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * 일간 분석 응답 데이터를 담는 DTO 클래스.
 */
@Data
@Builder
public class DailyStatisticsResponseDto {
    private Long dailyStrategicStatisticsId; // 일간 분석 목록 ID
    private LocalDate inputDate;             // 날짜
    private BigDecimal principal;           // 원금
    private BigDecimal depWdPrice;          // 입출금액
    private BigDecimal dailyProfitLoss;     // 일 손익
    private BigDecimal dailyPlRate;         // 일 손익률
    private BigDecimal cumulativeProfitLoss; // 누적손익
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#.####")
    private BigDecimal cumulativeProfitLossRate; // 누적수익률

    /**
     * {@link DailyStatisticsEntity} 엔티티를 DTO로 변환하는 정적 팩토리 메서드.
     *
     * @param entity 변환 대상 엔티티 객체.
     * @return {@link DailyStatisticsResponseDto}로 변환된 객체.
     */
    public static DailyStatisticsResponseDto fromEntity(DailyStatisticsEntity entity) {
        return DailyStatisticsResponseDto.builder()
                .dailyStrategicStatisticsId(entity.getDailyStatisticsId()) // 엔티티 ID를 DTO ID로 매핑
                .inputDate(entity.getDate()) // 날짜
                .principal(entity.getPrincipal()) // 원금
                .depWdPrice(entity.getDepWdPrice()) // 입출금액
                .dailyProfitLoss(entity.getDailyProfitLoss()) // 일 손익
                .dailyPlRate(entity.getDailyPlRate()) // 일 손익률
                .cumulativeProfitLoss(entity.getCumulativeProfitLoss()) // 누적손익
                .cumulativeProfitLossRate(entity.getCumulativeProfitLossRate()) // 누적수익률
                .build();
    }
}