package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.entity.FollowingStrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowingStrategyRepository extends JpaRepository<FollowingStrategyEntity, Long> {
    void deleteAllByStrategy(StrategyEntity strategy);
}
