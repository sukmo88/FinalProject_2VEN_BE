package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyIACRepository extends JpaRepository<StrategyIACEntity, StrategyIACId> {
    //해당 전략 id로 조회
    List<StrategyIACEntity> findByStrategyEntity_StrategyId(Long strategyId);
    //해당 엔티티로 조회
    List<StrategyIACEntity> findByInvestmentAssetClassesEntity(InvestmentAssetClassesEntity investmentAssetClassesEntity);

    // 전략으로 해당하는 데이터 모두 삭제
    void deleteAllByStrategyEntity(StrategyEntity strategy);
}
