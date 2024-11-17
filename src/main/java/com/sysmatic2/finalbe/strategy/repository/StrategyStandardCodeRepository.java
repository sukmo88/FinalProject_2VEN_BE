package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.common.StandardCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyStandardCodeRepository extends JpaRepository<StandardCodeEntity, String> {
}
