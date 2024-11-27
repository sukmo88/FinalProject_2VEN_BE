package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.dto.AdvancedSearchResultDto;
import com.sysmatic2.finalbe.strategy.dto.SearchOptionsDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyListDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    Page<StrategyListDto> findStrategiesByFilters(Integer tradingCycleId, Integer investmentAssetClassesId, Pageable pageable);

    /**
     * 필터 객체에 따라 전략 목록을 필터링(페이지네이션).
     *
     * @param searchOptions 필터링 옵션 객체
     * @param pageable 페이징 정보 (페이지 번호 및 크기)
     * @return 필터링된 전략 엔티티 목록 (Page 객체 포함)
     */
    Page<AdvancedSearchResultDto> findStrategiesByDetailSearchOptions(SearchOptionsDto searchOptions, Pageable pageable);
}