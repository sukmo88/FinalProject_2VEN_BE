package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.TradingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TradingTypeRepository extends JpaRepository<TradingType, Integer> {
    TradingType findByTradingTypeName(String tradingTypeName);
    Page<TradingType> findByIsActive(String isActive, Pageable pageable); // 활성 상태에 따른 페이지네이션 조회
    Optional<TradingType> findByTradingTypeOrder(Integer tradingTypeOrder);
    @Query("SELECT MAX(t.tradingTypeOrder) FROM TradingType t")
    Optional<Integer> findMaxTradingTypeOrder();
}
