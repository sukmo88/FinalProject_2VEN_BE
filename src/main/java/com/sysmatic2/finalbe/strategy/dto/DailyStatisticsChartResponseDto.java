package com.sysmatic2.finalbe.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 전략 상세 차트 데이터를 반환하는 DTO
 */
@Data
@AllArgsConstructor
public class DailyStatisticsChartResponseDto {

    private Map<String, List<?>> data; // 데이터 맵 (옵션 이름: 값 리스트)
    private String timestamp;
}