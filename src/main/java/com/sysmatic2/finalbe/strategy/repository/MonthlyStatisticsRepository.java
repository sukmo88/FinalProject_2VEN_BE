package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyStatisticsRepository extends JpaRepository<MonthlyStatisticsEntity, Long> {
  /**
   * 특정 전략 ID에 대한 월간 통계 데이터를 월 기준으로 페이징하여 조회합니다.
   *
   * @param strategyId 조회할 전략의 ID
   * @param pageable   페이징 정보
   * @return 페이징된 월간 통계 데이터 페이지
   */
  Page<MonthlyStatisticsEntity> findByStrategyEntityStrategyIdOrderByAnalysisMonthAsc(Long strategyId, Pageable pageable);

  /**
   * 특정 전략의 월간 통계 데이터를 조회하는 메서드.
   *
   * - 주어진 전략 ID와 분석 월(YearMonth)에 해당하는 데이터를 반환합니다.
   * - 데이터가 존재하지 않을 경우 Optional.empty()를 반환합니다.
   *
   * @param strategyId   전략의 고유 ID
   * @param analysisMonth 분석할 월 (YearMonth 타입)
   * @return 주어진 전략 ID와 월에 해당하는 MonthlyStatisticsEntity를 Optional로 감싸서 반환
   */
  @Query("""
    SELECT m 
    FROM MonthlyStatisticsEntity m 
    WHERE m.strategyEntity.strategyId = :strategyId 
      AND m.analysisMonth = :analysisMonth
""")
  Optional<MonthlyStatisticsEntity> findByStrategyIdAndAnalysisMonth(@Param("strategyId") Long strategyId,
                                                                     @Param("analysisMonth") YearMonth analysisMonth);

  /**
   * 특정 전략의 모든 월간 손익 데이터를 조회하는 메서드.
   *
   * - 월간 분석 테이블에서 해당 전략 ID에 속하는 모든 월 손익 데이터를 조회합니다.
   * - 결과는 BigDecimal 타입의 리스트로 반환됩니다.
   *
   * @param strategyId 전략 ID
   * @return 해당 전략의 모든 월 손익 리스트 (비어 있을 수 있음)
   */
  @Query("""
    SELECT m.monthlyProfitLoss
    FROM MonthlyStatisticsEntity m
    WHERE m.strategyEntity.strategyId = :strategyId
""")
  List<BigDecimal> findAllMonthlyProfitLossByStrategyId(@Param("strategyId") Long strategyId);

  /**
   * 특정 전략의 월간 분석 데이터를 페이징 처리하여 조회.
   *
   * @param strategyId 전략 ID
   * @param pageable   페이징 정보
   * @return 전략의 월간 분석 페이지 결과
   */
  Page<MonthlyStatisticsEntity> findByStrategyEntityStrategyIdOrderByAnalysisMonthDesc(Long strategyId, Pageable pageable);

  // strategy id로 월간통계 데이터 모두 삭제
  void deleteByStrategyEntity(StrategyEntity strategyEntity);
}
