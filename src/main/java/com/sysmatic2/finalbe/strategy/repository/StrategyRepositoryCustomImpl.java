package com.sysmatic2.finalbe.strategy.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sysmatic2.finalbe.strategy.dto.AdvancedSearchResultDto;
import com.sysmatic2.finalbe.strategy.dto.SearchOptionsDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyListDto;
import com.sysmatic2.finalbe.strategy.entity.QDailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.QStrategyEntity;
import com.sysmatic2.finalbe.admin.entity.QInvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.QTradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.QTradingTypeEntity;
import com.sysmatic2.finalbe.strategy.entity.QStrategyIACEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * QueryDSL을 사용한 커스텀 리포지토리 구현체
 */
@Repository
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
     * 1. 투자주기, 투자자산 분류 id로 필터링(페이지네이션)
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

    /**
     * 2. 필터 옵션 Dto를 받아 상세 필터링한 전략 목록 반환(페이지네이션)
     *
     * @param searchOptions 필터링 옵션 객체
     * @param pageable 페이징 정보 (페이지 번호 및 크기)
     * @return 필터링된 전략 목록 (Page 객체 포함)
     */
    @Override
    public Page<AdvancedSearchResultDto> findStrategiesByDetailSearchOptions(SearchOptionsDto searchOptions, Pageable pageable) {
        //QueryDSL용 Q객체 생성
        QStrategyEntity strategyQ = QStrategyEntity.strategyEntity;
        QStrategyIACEntity strategyIACQ = QStrategyIACEntity.strategyIACEntity;
        QInvestmentAssetClassesEntity iacQ = QInvestmentAssetClassesEntity.investmentAssetClassesEntity;
        QDailyStatisticsEntity dailyStatisticsQ = QDailyStatisticsEntity.dailyStatisticsEntity;

        BooleanBuilder builder = new BooleanBuilder();

        // 1. 투자자산 분류 필터
        if (searchOptions.getInvestmentAssetClassesIdList() != null && !searchOptions.getInvestmentAssetClassesIdList().isEmpty()) {
            builder.and(strategyIACQ.investmentAssetClassesEntity.investmentAssetClassesId.in(searchOptions.getInvestmentAssetClassesIdList()));
        }

        // 2. 전략 상태 코드 필터 - 운용/운용중지
        if (searchOptions.getStrategyOperationStatusList() != null && !searchOptions.getStrategyOperationStatusList().isEmpty()) {
            builder.and(strategyQ.strategyStatusCode.in(searchOptions.getStrategyOperationStatusList()));
        }

        // 3. 매매 유형 ID 필터 - 자동/반자동/수동
        if (searchOptions.getTradingTypeIdList() != null && !searchOptions.getTradingTypeIdList().isEmpty()) {
            builder.and(strategyQ.tradingTypeEntity.tradingTypeId.in(searchOptions.getTradingTypeIdList()));
        }

        // 4. 운용 기간 필터
        if (searchOptions.getOperationDaysList() != null && !searchOptions.getOperationDaysList().isEmpty()) {
            BooleanBuilder dateBuilder = new BooleanBuilder();
            LocalDateTime now = LocalDateTime.now();
            for (Integer days : searchOptions.getOperationDaysList()) {
                switch (days) {
                    case 0: // 1년 미만
                        dateBuilder.or(strategyQ.writedAt.after(now.minus(1, ChronoUnit.YEARS)));
                        break;
                    case 1: // 1년 ~ 2년
                        dateBuilder.or(strategyQ.writedAt.between(now.minus(2, ChronoUnit.YEARS), now.minus(1, ChronoUnit.YEARS)));
                        break;
                    case 2: // 2년 ~ 3년
                        dateBuilder.or(strategyQ.writedAt.between(now.minus(3, ChronoUnit.YEARS), now.minus(2, ChronoUnit.YEARS)));
                        break;
                    case 3: // 3년 이상
                        dateBuilder.or(strategyQ.writedAt.before(now.minus(3, ChronoUnit.YEARS)));
                        break;
                }
            }
            builder.and(dateBuilder);
        }

        // 5. 매매 주기 ID 필터 - 데이/커스텀
        if (searchOptions.getTradingCylcleIdList() != null && !searchOptions.getTradingCylcleIdList().isEmpty()) {
            builder.and(strategyQ.tradingCycleEntity.tradingCycleId.in(searchOptions.getTradingCylcleIdList()));
        }

        // 6. 최소 운용 가능 금액 필터
        if (searchOptions.getMinInvestmentAmount() != null) {
            builder.and(strategyQ.minInvestmentAmount.eq(searchOptions.getMinInvestmentAmount()));
        }

        // 7. 원금 필터(제일 최근 데이터 기준)
        if (searchOptions.getMinPrincipal() != null || searchOptions.getMaxPrincipal() != null) {
            BooleanBuilder principalBuilder = new BooleanBuilder();
            if (searchOptions.getMinPrincipal() != null) {
                principalBuilder.and(dailyStatisticsQ.principal.goe(searchOptions.getMinPrincipal()));
            }
            if (searchOptions.getMaxPrincipal() != null) {
                principalBuilder.and(dailyStatisticsQ.principal.loe(searchOptions.getMaxPrincipal()));
            }
            builder.and(principalBuilder);
        }

        // 8. SM-Score 필터
        //TODO)
//        if (searchOptions.getMinSmscore() != null || searchOptions.getMaxSmscore() != null) {
//            BooleanBuilder smScoreBuilder = new BooleanBuilder();
//            if (searchOptions.getMinSmscore() != null) {
//                smScoreBuilder.and(dailyStatisticsQ.sharpRatio.goe(BigDecimal.valueOf(searchOptions.getMinSmscore())));
//            }
//            if (searchOptions.getMaxSmscore() != null) {
//                smScoreBuilder.and(dailyStatisticsQ.sharpRatio.loe(BigDecimal.valueOf(searchOptions.getMaxSmscore())));
//            }
//            builder.and(smScoreBuilder);
//        }

        // 9. MDD(최대자본인하율) 필터
        if (searchOptions.getMinMdd() != null || searchOptions.getMaxMdd() != null) {
            BooleanBuilder mddBuilder = new BooleanBuilder();
            if (searchOptions.getMinMdd() != null) {
                mddBuilder.and(dailyStatisticsQ.maxDrawdownRate.goe(searchOptions.getMinMdd()));
            }
            if (searchOptions.getMaxMdd() != null) {
                mddBuilder.and(dailyStatisticsQ.maxDrawdownRate.loe(searchOptions.getMaxMdd()));
            }
            builder.and(mddBuilder);
        }

        // 10. 수익률 기준 일자 필터(종료일 기준가 / 시작일 기준가 -1)
        if (searchOptions.getReturnRateList() != null && !searchOptions.getReturnRateList().isEmpty()) {
            BooleanBuilder returnRateBuilder = new BooleanBuilder();

            for (Integer rate : searchOptions.getReturnRateList()) {
                BigDecimal rateValue = BigDecimal.valueOf(rate).divide(BigDecimal.valueOf(100)); // 10, 20, 30%를 0.1, 0.2, 0.3으로 변환
                returnRateBuilder.or(dailyStatisticsQ.referencePrice.goe(rateValue)); // 최근 1년 수익률 기준 필터
            }

            // 기간 필터와 결합
            if (searchOptions.getStartDate() != null && searchOptions.getEndDate() != null) {
                returnRateBuilder.and(dailyStatisticsQ.date.between(searchOptions.getStartDate(), searchOptions.getEndDate()));
            }

            builder.and(returnRateBuilder);
        }

        // 12. 전체 데이터 개수 (페이지네이션 전)
        long totalCnt = queryFactory
                .select(strategyQ.count())
                .from(strategyQ)
                .leftJoin(strategyQ.strategyIACEntities, strategyIACQ)
                .where(builder)
                .fetchOne();

        if (totalCnt == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 13. 전략 ID 조회 (필터, 페이징 적용)
        List<Long> strategyIds = queryFactory
                .select(strategyQ.strategyId)
                .from(strategyQ)
                .leftJoin(strategyQ.strategyIACEntities, strategyIACQ)
                .where(builder)
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (strategyIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 14. 해당 id의 전략의 정보들 가져옴
        List<Tuple> tuples = queryFactory
                .select(
                        strategyQ.strategyId,
                        strategyQ.tradingTypeEntity.tradingTypeIcon,
                        strategyQ.tradingCycleEntity.tradingCycleIcon,
                        strategyQ.strategyTitle,
                        strategyQ.followersCount
                )
                .from(strategyQ)
                .where(strategyQ.strategyId.in(strategyIds))
                .fetch();

        // 15. 투자자산 분류 아이콘 가져오기
        Map<Long, List<String>> assetIconsMap = queryFactory
                .select(strategyIACQ.strategyEntity.strategyId, iacQ.investmentAssetClassesIcon)
                .from(strategyIACQ)
                .join(strategyIACQ.investmentAssetClassesEntity, iacQ)
                .where(strategyIACQ.strategyEntity.strategyId.in(strategyIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(strategyIACQ.strategyEntity.strategyId),
                        Collectors.mapping(tuple -> tuple.get(iacQ.investmentAssetClassesIcon), Collectors.toList())
                ));

        // 16. DTO 리스트 생성
        List<AdvancedSearchResultDto> results = tuples.stream()
                .map(tuple -> new AdvancedSearchResultDto(
                        tuple.get(strategyQ.strategyId),
                        tuple.get(strategyQ.tradingTypeEntity.tradingTypeIcon),
                        tuple.get(strategyQ.tradingCycleEntity.tradingCycleIcon),
                        assetIconsMap.getOrDefault(tuple.get(strategyQ.strategyId), List.of()),
                        tuple.get(strategyQ.strategyTitle),
                        tuple.get(strategyQ.followersCount)
                ))
                .toList();

        return new PageImpl<>(results, pageable, totalCnt);
    }
}
