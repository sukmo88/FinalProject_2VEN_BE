package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyIACRepository extends JpaRepository<StrategyIACEntity, StrategyIACId> {
    //해당 전략 id로 조회
    List<StrategyIACEntity> findByStrategyEntity_StrategyId(Long strategyId);
}
