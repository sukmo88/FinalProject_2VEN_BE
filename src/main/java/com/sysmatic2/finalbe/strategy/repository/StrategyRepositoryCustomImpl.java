package com.sysmatic2.finalbe.strategy.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
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

        // 1. 기본 데이터 조회 (투자자산 필터링 포함)
        List<Tuple> tuples = queryFactory
                .select(
                        strategy.strategyId, // 전략 ID (후처리용)
                        tradingType.tradingTypeIcon, // 매매유형 아이콘
                        tradingCycle.tradingCycleIcon, // 매매주기 아이콘
                        strategy.strategyTitle, // 전략명
                        strategy.followersCount // 팔로워 수
                )
                .from(strategy) // 전략 테이블에서 시작
                .leftJoin(strategy.tradingCycleEntity, tradingCycle) // 매매주기와 LEFT JOIN
                .leftJoin(strategy.tradingTypeEntity, tradingType) // 매매유형과 LEFT JOIN
                .leftJoin(strategy.strategyIACEntities, strategyIAC) // 전략과 투자자산 관계 LEFT JOIN
                .leftJoin(strategyIAC.investmentAssetClassesEntity, investmentAsset) // 투자자산과 LEFT JOIN
                .where(
                        tradingCycleId != null ? tradingCycle.tradingCycleId.eq(tradingCycleId) : null, // 매매주기 ID 필터
                        investmentAssetClassesId != null ? investmentAsset.investmentAssetClassesId.eq(investmentAssetClassesId) : null // 투자자산 ID 필터
                )
                .offset(pageable.getOffset()) // 페이지 시작 위치 설정
                .limit(pageable.getPageSize()) // 페이지 크기 설정
                .fetch(); // 쿼리 실행 및 결과 반환

        // 2. 투자자산 아이콘 리스트 조회 및 DTO 생성
        List<StrategyListDto> results = tuples.stream()
                .map(tuple -> {
                    Long strategyId = tuple.get(strategy.strategyId);

                    // 서브쿼리로 투자자산 아이콘 리스트 조회
                    List<String> investmentAssetIcons = queryFactory
                            .select(investmentAsset.investmentAssetClassesIcon)
                            .from(strategyIAC)
                            .join(strategyIAC.investmentAssetClassesEntity, investmentAsset)
                            .where(strategyIAC.strategyEntity.strategyId.eq(strategyId)) // 동일한 전략 ID로 필터링
                            .fetch();

                    // DTO 생성
                    return new StrategyListDto(
                            tuple.get(tradingType.tradingTypeIcon), // 매매유형 아이콘
                            tuple.get(tradingCycle.tradingCycleIcon), // 매매주기 아이콘
                            investmentAssetIcons, // 투자자산 아이콘 리스트
                            tuple.get(strategy.strategyTitle), // 전략명
                            tuple.get(strategy.followersCount) // 팔로워 수
                    );
                })
                .toList();

        // 3. 총 데이터 개수 조회
        long total = queryFactory
                .select(strategy.count())
                .from(strategy)
                .leftJoin(strategy.tradingCycleEntity, tradingCycle)
                .leftJoin(strategy.tradingTypeEntity, tradingType)
                .leftJoin(strategy.strategyIACEntities, strategyIAC)
                .leftJoin(strategyIAC.investmentAssetClassesEntity, investmentAsset)
                .where(
                        tradingCycleId != null ? tradingCycle.tradingCycleId.eq(tradingCycleId) : null,
                        investmentAssetClassesId != null ? investmentAsset.investmentAssetClassesId.eq(investmentAssetClassesId) : null
                )
                .fetchOne();

        // 4. Page 객체 생성 및 반환
        return new PageImpl<>(results, pageable, total);
    }
}