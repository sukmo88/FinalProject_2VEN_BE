package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StrategyProposalRepository extends JpaRepository<StrategyProposalEntity, Long> {
    Optional<StrategyProposalEntity> findByStrategyAndWriterId(StrategyEntity strategy, String writerId);

    Optional<StrategyProposalEntity> findByStrategy(StrategyEntity strategy);

    Optional<StrategyProposalEntity> findByFileLink(String fileLink);

}
