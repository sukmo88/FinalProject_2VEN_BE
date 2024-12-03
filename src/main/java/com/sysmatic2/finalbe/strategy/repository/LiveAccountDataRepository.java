package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.LiveAccountDataEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveAccountDataRepository extends JpaRepository<LiveAccountDataEntity, Long> {
    Page<LiveAccountDataEntity> findAllByStrategy(StrategyEntity strategy, Pageable pageable);
    List<LiveAccountDataEntity> findAllByStrategy(StrategyEntity strategy);
    List<LiveAccountDataEntity> findAllByLiveAccountIdInAndStrategy(List<Long> ids, StrategyEntity strategy);
}
