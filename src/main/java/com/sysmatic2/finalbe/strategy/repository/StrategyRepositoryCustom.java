package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.dto.SearchOptionsDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.validation.constraints.DecimalMax;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * QueryDSL 기반 커스텀 쿼리를 위한 인터페이스
 */
public interface StrategyRepositoryCustom {

    /**
     * 필터 조건에 따라 전략 목록을 조회하고 페이징 처리합니다.
     *
     * @param tradingCycleId 투자주기 ID (nullable)
     * @param investmentAssetClassesId 투자자산 분류 ID (nullable)
     * @param pageable 페이징 정보 (페이지 번호 및 크기)
     * @return 필터링된 전략 목록 (Page 객체 포함)
     */
    Page<StrategyEntity> findStrategiesByFilters(Integer tradingCycleId, Integer investmentAssetClassesId, Pageable pageable);

    /**
     * 필터 객체에 따라 전략 목록을 필터링(페이지네이션).
     *
     * @param searchOptions 필터링 옵션 객체
     * @param pageable 페이징 정보 (페이지 번호 및 크기)
     * @return 필터링된 전략 엔티티 목록 (Page 객체 포함)
     */
    Page<StrategyEntity> findStrategiesByDetailSearchOptions(SearchOptionsDto searchOptions, Pageable pageable);

    /**
     * 특정 전략 ID와 두 가지 데이터 옵션에 해당하는 값을 날짜순으로 조회
     *
     * <p>동적 쿼리를 통해 전달받은 두 개의 옵션 필드(컬럼 이름)에 대한 데이터를 날짜순 오름차순으로 조회하여 반환합니다.</p>
     *
     * @param strategyId 전략 ID (필수)
     * @param option1 첫 번째 데이터 옵션 (DailyStatisticsEntity의 필드 이름)
     * @param option2 두 번째 데이터 옵션 (DailyStatisticsEntity의 필드 이름)
     * @return Map<String, List<?>> (옵션 이름과 해당 데이터 리스트)
     *         - Key: option1, option2의 이름
     *         - Value: 각 옵션에 해당하는 값 리스트 (날짜 오름차순)
     * @throws IllegalArgumentException 옵션이 유효하지 않거나 컬럼이 존재하지 않을 경우 예외 발생
     */
    Map<String, List<?>> findChartDataByOptions(Long strategyId, String option1, String option2);
}