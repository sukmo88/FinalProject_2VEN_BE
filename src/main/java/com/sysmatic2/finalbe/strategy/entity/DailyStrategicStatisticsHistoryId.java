package com.sysmatic2.finalbe.strategy.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DailyStrategicStatisticsHistoryId implements Serializable {
    private Long strategyId; // 전략 ID
    private Long dailyStrategicStatisticsId; // 전략 일간 통계 ID
    private Long dailyStrategicStatisticsHistoryId; // 전략 일간 통계 이력 ID
}
