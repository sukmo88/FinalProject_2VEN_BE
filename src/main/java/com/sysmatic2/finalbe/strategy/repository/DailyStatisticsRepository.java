package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface DailyStatisticsRepository extends JpaRepository<DailyStatisticsEntity, Long> {

    // 가장 최근 데이터 1개 조회 (Pageable 사용)
    @Query("SELECT d FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date DESC")
    List<DailyStatisticsEntity> findLatestByStrategyId(@Param("strategyId") Long strategyId, Pageable pageable);

    // 특정 전략의 일손익 리스트 조회
    @Query("SELECT d.dailyProfitLoss FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<BigDecimal> findDailyProfitLossesByStrategyId(@Param("strategyId") Long strategyId);

    // 최근 1년 전 해당 전략의 잔고 조회
    @Query("SELECT d.balance FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "AND d.date <= :oneYearAgo " +
            "ORDER BY d.date DESC")
    Optional<BigDecimal> findBalanceOneYearAgo(@Param("strategyId") Long strategyId, @Param("oneYearAgo") LocalDate oneYearAgo);

    // 특정 전략의 누적손익 히스토리 조회
    @Query("SELECT d.date, d.cumulativeProfitLoss FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<Object[]> findCumulativeProfitLossHistory(@Param("strategyId") Long strategyId);

    // 모든 전략의 KP-Ratio 조회
    @Query("SELECT d.kpRatio FROM DailyStatisticsEntity d WHERE d.kpRatio IS NOT NULL AND d.kpRatio > 0")
    List<BigDecimal> findAllKpRatios();
}
