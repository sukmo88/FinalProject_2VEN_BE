package com.sysmatic2.finalbe.strategy.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeleteDailyStatisticsRequestDto {
    private List<Long> dailyStatisticsId; // 날짜순 오름차순 리스트
}
