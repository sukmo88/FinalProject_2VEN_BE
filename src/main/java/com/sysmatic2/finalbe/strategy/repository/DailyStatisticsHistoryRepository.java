package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyStatisticsHistoryRepository extends JpaRepository<DailyStatisticsHistoryEntity, Long> {
}
