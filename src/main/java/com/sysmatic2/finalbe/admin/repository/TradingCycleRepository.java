package com.sysmatic2.finalbe.admin.repository;

import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradingCycleRepository extends JpaRepository<TradingCycleEntity, Integer> {
    // 투자주기명으로 조회
    TradingCycleEntity findByTradingCycleName(String tradingCycleName);

    // 활성 상태에 따른 페이지네이션 조회
    Page<TradingCycleEntity> findByIsActive(String isActive, Pageable pageable);

    // 활성 상태에 따른 순서별 정렬 조회
    List<TradingCycleEntity> findByIsActiveOrderByTradingCycleOrderAsc(String isActive);

    // 투자주기 순서로 조회
    Optional<TradingCycleEntity> findByTradingCycleOrder(Integer tradingCycleOrder);

    // 최대 투자주기 순서 조회
    @Query("SELECT MAX(t.tradingCycleOrder) FROM TradingCycleEntity t")
    Optional<Integer> findMaxTradingCycleOrder();
}
