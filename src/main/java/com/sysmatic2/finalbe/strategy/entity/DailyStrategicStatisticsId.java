package com.sysmatic2.finalbe.strategy.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DailyStrategicStatisticsId implements Serializable {
    private Long strategyId; // 전략 ID
    private Long dailyStrategicStatisticsId; // 전략 일간 통계 ID
}
