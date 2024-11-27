package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyStatisticsRepository extends JpaRepository<MonthlyStatisticsEntity, Long> {
  List<MonthlyStatisticsEntity> findByStrategyEntity_StrategyId(Long strategyId);
}
