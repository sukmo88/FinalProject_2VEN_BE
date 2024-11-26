package com.sysmatic2.finalbe.strategy.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sysmatic2.finalbe.strategy.dto.StrategyListDto;
import com.sysmatic2.finalbe.strategy.entity.QStrategyEntity;
import com.sysmatic2.finalbe.admin.entity.QInvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.QTradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.QTradingTypeEntity;
import com.sysmatic2.finalbe.strategy.entity.QStrategyIACEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * QueryDSL을 사용한 커스텀 리포지토리 구현체
 */
public class StrategyRepositoryCustomImpl implements StrategyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * JPAQueryFactory 초기화
     *
     * @param entityManager 엔티티 매니저
     */
    public StrategyRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    /**
     * 필터 조건에 따라 전략 목록을 조회하고 페이징 처리합니다.
     *
     * @param tradingCycleId 투자주기 ID (nullable)
     * @param investmentAssetClassesId 투자자산 분류 ID (nullable)
     * @param pageable 페이징 정보 (페이지 번호 및 크기)
     * @return 필터링된 전략 목록 (Page 객체 포함)
     */
    @Override
    public Page<StrategyListDto> findStrategiesByFilters(Integer tradingCycleId, Integer investmentAssetClassesId, Pageable pageable) {
        // QueryDSL에서 사용할 Q클래스 정의
        QStrategyEntity strategy = QStrategyEntity.strategyEntity;
        QTradingCycleEntity tradingCycle = QTradingCycleEntity.tradingCycleEntity;
        QTradingTypeEntity tradingType = QTradingTypeEntity.tradingTypeEntity;
        QStrategyIACEntity strategyIAC = QStrategyIACEntity.strategyIACEntity;
        QInvestmentAssetClassesEntity investmentAsset = QInvestmentAssetClassesEntity.investmentAssetClassesEntity;

        // 1. 전략 데이터 조회 (중복 방지 및 투자자산 아이콘 제외)
        List<Long> strategyIds = queryFactory
                .select(strategy.strategyId)
                .from(strategy)
                .leftJoin(strategy.tradingCycleEntity, tradingCycle)
                .leftJoin(strategy.tradingTypeEntity, tradingType)
                .where(
                        tradingCycleId != null ? tradingCycle.tradingCycleId.eq(tradingCycleId) : null,
                        investmentAssetClassesId != null ? strategy.strategyIACEntities.any().investmentAssetClassesEntity.investmentAssetClassesId.eq(investmentAssetClassesId) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct() // 중복 제거
                .fetch();

        if (strategyIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. 전략 데이터와 매매 유형, 매매 주기 정보 조회
        List<Tuple> tuples = queryFactory
                .select(
                        strategy.strategyId,
                        tradingType.tradingTypeIcon,
                        tradingCycle.tradingCycleIcon,
                        strategy.strategyTitle
                )
                .from(strategy)
                .leftJoin(strategy.tradingCycleEntity, tradingCycle)
                .leftJoin(strategy.tradingTypeEntity, tradingType)
                .where(strategy.strategyId.in(strategyIds)) // 필터링된 전략 ID에 해당하는 데이터만 조회
                .fetch();

        // 3. 투자자산 아이콘 리스트 조회
        Map<Long, List<String>> strategyAssetIconsMap = queryFactory
                .select(
                        strategyIAC.strategyEntity.strategyId,
                        investmentAsset.investmentAssetClassesIcon
                )
                .from(strategyIAC)
                .join(strategyIAC.investmentAssetClassesEntity, investmentAsset)
                .where(strategyIAC.strategyEntity.strategyId.in(strategyIds))
                .fetch()
                .stream()
                .collect(
                        // Map<StrategyId, List<InvestmentAssetIcons>>
                        Collectors.groupingBy(
                                tuple -> tuple.get(strategyIAC.strategyEntity.strategyId),
                                Collectors.mapping(tuple -> tuple.get(investmentAsset.investmentAssetClassesIcon), Collectors.toList())
                        )
                );

        // 4. DTO 생성
        List<StrategyListDto> results = tuples.stream()
                .map(tuple -> new StrategyListDto(
                        tuple.get(strategy.strategyId), // 전략 ID
                        tuple.get(tradingType.tradingTypeIcon), // 매매유형 아이콘
                        tuple.get(tradingCycle.tradingCycleIcon), // 매매주기 아이콘
                        strategyAssetIconsMap.getOrDefault(tuple.get(strategy.strategyId), List.of()), // 투자자산 아이콘 리스트
                        tuple.get(strategy.strategyTitle) // 전략명
                ))
                .toList();

        // 5. 총 데이터 개수 조회
        long total = queryFactory
                .select(strategy.count())
                .from(strategy)
                .leftJoin(strategy.tradingCycleEntity, tradingCycle)
                .leftJoin(strategy.tradingTypeEntity, tradingType)
                .where(
                        tradingCycleId != null ? tradingCycle.tradingCycleId.eq(tradingCycleId) : null,
                        investmentAssetClassesId != null ? strategy.strategyIACEntities.any().investmentAssetClassesEntity.investmentAssetClassesId.eq(investmentAssetClassesId) : null
                )
                .fetchOne();

        // 6. Page 객체 생성 및 반환
        return new PageImpl<>(results, pageable, total);
    }
}