package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.TradingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyRepository extends JpaRepository<StrategyEntity, Long> {
}
