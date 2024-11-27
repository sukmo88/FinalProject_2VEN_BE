package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
