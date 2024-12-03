package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategyReviewRepository extends JpaRepository<StrategyReviewEntity, Long> {
    Page<StrategyReviewEntity> findAllByStrategyOrderByWritedAtDesc(StrategyEntity strategy, Pageable pageable);
    List<StrategyReviewEntity> findAllByStrategy(StrategyEntity strategy);
}
