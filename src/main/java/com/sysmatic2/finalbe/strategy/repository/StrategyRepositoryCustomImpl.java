package com.sysmatic2.finalbe.strategy.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sysmatic2.finalbe.exception.InvalidFieldNameException;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsChartResponseDto;
import com.sysmatic2.finalbe.strategy.dto.SearchOptionsDto;
import com.sysmatic2.finalbe.strategy.entity.QDailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.QStrategyEntity;
import com.sysmatic2.finalbe.admin.entity.QInvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.QTradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.QTradingTypeEntity;
import com.sysmatic2.finalbe.strategy.entity.QStrategyIACEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public Page<StrategyEntity> findStrategiesByFilters(Integer tradingCycleId, Integer investmentAssetClassesId, Pageable pageable) {
        // QueryDSL에서 사용할 Q클래스 정의
        QStrategyEntity strategy = QStrategyEntity.strategyEntity;
        QTradingCycleEntity tradingCycle = QTradingCycleEntity.tradingCycleEntity;
        QTradingTypeEntity tradingType = QTradingTypeEntity.tradingTypeEntity;
        QStrategyIACEntity strategyIAC = QStrategyIACEntity.strategyIACEntity;
        QInvestmentAssetClassesEntity investmentAsset = QInvestmentAssetClassesEntity.investmentAssetClassesEntity;

        // 5. 총 데이터 개수 조회
        long total = queryFactory
                .select(strategy.count())
                .from(strategy)
                .where(
                        tradingCycleId != null ? tradingCycle.tradingCycleId.eq(tradingCycleId) : null,
                        investmentAssetClassesId != null ? strategy.strategyIACEntities.any().investmentAssetClassesEntity.investmentAssetClassesId.eq(investmentAssetClassesId) : null,
                        strategy.isPosted.eq("Y"),
                        strategy.isApproved.eq("Y")
                )
                .fetchOne();

        // 1. 전략 데이터 조회
        List<StrategyEntity> strategyEntities = queryFactory
                .selectFrom(strategy)
                .leftJoin(strategy.tradingTypeEntity, tradingType)
                .leftJoin(strategy.tradingCycleEntity, tradingCycle)
                .where(
                        tradingCycleId != null ? tradingCycle.tradingCycleId.eq(tradingCycleId) : null,
                        investmentAssetClassesId != null ? strategy.strategyIACEntities.any()
                                .investmentAssetClassesEntity.investmentAssetClassesId.eq(investmentAssetClassesId) : null,
                        strategy.isPosted.eq("Y"),
                        strategy.isApproved.eq("Y")
                )
                .orderBy(strategy.smScore.desc()) // smScore 내림차순 정렬 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if(strategyEntities.isEmpty()) {
            return new PageImpl<>(strategyEntities, pageable, 0L);
        }

        return new PageImpl<>(strategyEntities, pageable, total);

    }

    /**
     * 2. 필터 옵션 Dto를 받아 상세 필터링한 전략 목록 반환(페이지네이션)
     *
     * @param searchOptions 필터링 옵션 객체
     * @param pageable 페이징 정보 (페이지 번호 및 크기)
     * @return 필터링된 전략 목록 (Page 객체 포함)
     */
    @Override
    public Page<StrategyEntity> findStrategiesByDetailSearchOptions(SearchOptionsDto searchOptions, Pageable pageable) {
        //QueryDSL용 Q객체 생성
        QStrategyEntity strategyQ = QStrategyEntity.strategyEntity; //전략
        QStrategyIACEntity strategyIACQ = QStrategyIACEntity.strategyIACEntity; //전략-투자자산분류 관계엔티티
        QDailyStatisticsEntity dailyStatisticsQ = QDailyStatisticsEntity.dailyStatisticsEntity; //일간데이터

        //전략관련 필터 생성
        BooleanBuilder strategyBuilder = new BooleanBuilder();
        //일간데이터 관련 필터 생성
        BooleanBuilder statisticsBuilder = new BooleanBuilder();

        // 1. is_posted = Y 필터
        strategyBuilder.and(strategyQ.isPosted.eq("Y"));

        // 2. is_Approved = Y 필터
        strategyBuilder.and(strategyQ.isApproved.eq("Y"));

        // 3. 최소 운용 가능 금액 필터 - ex) 1000만원 ~ 2000만원
        if (searchOptions.getMinInvestmentAmount() != null) {
            strategyBuilder.and(strategyQ.minInvestmentAmount.eq(searchOptions.getMinInvestmentAmount()));
        }

        // 4. 키워드 검색 필터
        if (searchOptions.getKeyword() != null && !searchOptions.getKeyword().isEmpty()) {
            strategyBuilder.and(strategyQ.strategyTitle.containsIgnoreCase(searchOptions.getKeyword()));
        }

        // 4. 매매 유형 ID 필터 - 자동/반자동/수동(중첩가능)
        if (searchOptions.getTradingTypeIdList() != null && !searchOptions.getTradingTypeIdList().isEmpty()) {
            strategyBuilder.and(strategyQ.tradingTypeEntity.tradingTypeId.in(searchOptions.getTradingTypeIdList()));
        }

        // 5. 전략 상태 코드 필터 - 운용/운용중지(중첩가능)
        if (searchOptions.getStrategyOperationStatusList() != null && !searchOptions.getStrategyOperationStatusList().isEmpty()) {
            strategyBuilder.and(strategyQ.strategyStatusCode.in(searchOptions.getStrategyOperationStatusList()));
        }

        // 6. 매매 주기 ID 필터 - 데이/커스텀(중첩가능)
        if (searchOptions.getTradingCylcleIdList() != null && !searchOptions.getTradingCylcleIdList().isEmpty()) {
            strategyBuilder.and(strategyQ.tradingCycleEntity.tradingCycleId.in(searchOptions.getTradingCylcleIdList()));
        }

        // 7. 투자자산 분류 필터
        if (searchOptions.getInvestmentAssetClassesIdList() != null && !searchOptions.getInvestmentAssetClassesIdList().isEmpty()) {
            strategyBuilder.and(strategyQ.strategyIACEntities.any()
                    .investmentAssetClassesEntity.investmentAssetClassesId
                    .in(searchOptions.getInvestmentAssetClassesIdList()));
        }

        // 8. 운용 기간 필터 - 중첩가능
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
            strategyBuilder.and(dateBuilder);
        }

        // 10. 원금 필터(제일 최근 데이터 기준)
        if (searchOptions.getMinPrincipal() != null || searchOptions.getMaxPrincipal() != null) {
            BooleanBuilder principalBuilder = new BooleanBuilder();
            if (searchOptions.getMinPrincipal() != null) {
                principalBuilder.and(dailyStatisticsQ.principal.goe(searchOptions.getMinPrincipal()));
            }
            if (searchOptions.getMaxPrincipal() != null) {
                principalBuilder.and(dailyStatisticsQ.principal.loe(searchOptions.getMaxPrincipal()));
            }
            statisticsBuilder.and(principalBuilder);
        }

        // 11. SM-Score 필터
        if (searchOptions.getMinSmscore() != null || searchOptions.getMaxSmscore() != null) {
            BooleanBuilder smScoreBuilder = new BooleanBuilder();
            if (searchOptions.getMinSmscore() != null) {
                smScoreBuilder.and(strategyQ.smScore.goe(BigDecimal.valueOf(searchOptions.getMinSmscore())));
            }
            if (searchOptions.getMaxSmscore() != null) {
                smScoreBuilder.and(strategyQ.smScore.loe(BigDecimal.valueOf(searchOptions.getMaxSmscore())));
            }
            strategyBuilder.and(smScoreBuilder);
        }

        // 12. MDD(최대자본인하율) 필터
        if (searchOptions.getMinMdd() != null || searchOptions.getMaxMdd() != null) {
            BooleanBuilder mddBuilder = new BooleanBuilder();
            if (searchOptions.getMinMdd() != null) {
                mddBuilder.and(dailyStatisticsQ.maxDrawdownRate.goe(searchOptions.getMinMdd()));
            }
            if (searchOptions.getMaxMdd() != null) {
                mddBuilder.and(dailyStatisticsQ.maxDrawdownRate.loe(searchOptions.getMaxMdd()));
            }
            statisticsBuilder.and(mddBuilder);
        }

        //13. 날짜 필터링 - 해당기간의 일간데이터
        if (searchOptions.getStartDate() != null && searchOptions.getEndDate() != null) {
            statisticsBuilder.and(dailyStatisticsQ.date.between(searchOptions.getStartDate(), searchOptions.getEndDate()));
        }

        //14. 손익률 필터
        if (searchOptions.getReturnRateList() != null && !searchOptions.getReturnRateList().isEmpty()) {
            BooleanBuilder returnRateBuilder = new BooleanBuilder();
            searchOptions.getReturnRateList().forEach(rate -> {
                switch (rate) {
                    case 1: // 10% 이하
                        returnRateBuilder.or(dailyStatisticsQ.cumulativeProfitLossRate.loe(BigDecimal.valueOf(0.10)));
                        break;
                    case 2: // 10% 초과 ~ 30% 미만
                        returnRateBuilder.or(
                                dailyStatisticsQ.cumulativeProfitLossRate.gt(BigDecimal.valueOf(0.10))
                                        .and(dailyStatisticsQ.cumulativeProfitLossRate.lt(BigDecimal.valueOf(0.30)))
                        );
                        break;
                    case 3: // 30% 이상
                        returnRateBuilder.or(dailyStatisticsQ.cumulativeProfitLossRate.goe(BigDecimal.valueOf(0.30)));
                        break;
                    default:
                        break;
                }
            });
            statisticsBuilder.and(returnRateBuilder);
        }

        //1)서브쿼리 관련 필터 없는경우 메인쿼리만 동작함.
        if (!statisticsBuilder.hasValue()) {
            List<StrategyEntity> allStrategies = queryFactory
                    .selectFrom(strategyQ)
                    .where(strategyBuilder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .distinct()
                    .fetch();

            Long totalCount = queryFactory
                    .select(strategyQ.count())
                    .from(strategyQ)
                    .where(strategyBuilder)
                    .fetchOne();

            return new PageImpl<>(allStrategies, pageable, totalCount);
        }

        //2) 서브쿼리 필터가 있는경우
        //DailyStatistics 관련 서브쿼리
        JPQLQuery<Long> strategyIdsQuery = queryFactory
                .select(dailyStatisticsQ.strategyEntity.strategyId)
                .from(dailyStatisticsQ)
                .where(statisticsBuilder)
                .distinct();

        //서브쿼리의 결과 값들 id 저장
        List<Long> strategyIds = strategyIdsQuery.fetch();

        //전략 관련 메인 쿼리
        //3-1)서브쿼리 결과 없는경우
        if(strategyIds.isEmpty()) {
            List<StrategyEntity> allStrategies = queryFactory
                    .selectFrom(strategyQ)
                    .where(strategyBuilder)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .distinct()
                    .fetch();

            Long totalCount = queryFactory
                    .select(strategyQ.count())
                    .from(strategyQ)
                    .where(strategyBuilder)
                    .fetchOne();

            return new PageImpl<>(allStrategies, pageable, totalCount);
        }

        //3-2)서브쿼리 결과 있는 경우
        List<StrategyEntity> strategyEntities = queryFactory
                .selectFrom(strategyQ)
                .where(strategyBuilder.and(strategyQ.strategyId.in(strategyIds)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        //결과 갯수
        Long resultCnt = queryFactory
                .select(strategyQ.count())
                .from(strategyQ)
                .where(strategyBuilder.and(strategyQ.strategyId.in(strategyIdsQuery)))
                .fetchOne();

        //결과 없거나 빈경우
        if(strategyEntities == null && strategyEntities.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return new PageImpl<>(strategyEntities, pageable, resultCnt);
    }

    /**
     * 특정 전략 ID와 두 가지 데이터 옵션에 해당하는 값을 날짜순으로 조회
     *
     * @param strategyId 전략 ID
     * @param option1 첫 번째 데이터 옵션 (조회할 컬럼 이름)
     * @param option2 두 번째 데이터 옵션 (조회할 컬럼 이름)
     * @return Map<String, List<?>> (선택된 데이터 옵션 이름과 값 리스트)
     */
    @Override
    public Map<String, List<?>> findChartDataByOptions(Long strategyId, String option1, String option2) {
        QDailyStatisticsEntity dailyStatistics = QDailyStatisticsEntity.dailyStatisticsEntity;

        // 유효한 필드 이름 목록
        List<String> validFields = List.of(
                "referencePrice", "balance", "principal", "cumulativeDepWdPrice",
                "depWdPrice", "dailyProfitLoss", "dailyPlRate", "cumulativeProfitLoss",
                "cumulativeProfitLossRate", "currentDrawdownAmount", "currentDrawdownRate",
                "averageProfitLoss", "averageProfitLossRate", "winRate", "profitFactor",
                "roa", "totalProfit", "totalLoss"
        );

        // 필드 검증
        if (!validFields.contains(option1)) {
            throw new InvalidFieldNameException("Invalid field name for option1: " + option1);
        }
        if (!validFields.contains(option2)) {
            throw new InvalidFieldNameException("Invalid field name for option2: " + option2);
        }

        // 동적으로 PathBuilder를 사용하여 컬럼 선택
        PathBuilder<Object> dailyStatisticsPath = new PathBuilder<>(Object.class, dailyStatistics.getMetadata());

        Expression<Object> column1 = dailyStatisticsPath.get(option1, Object.class);

        // 중복 여부에 따라 동일한 컬럼만 조회
        List<Object> option1Values = queryFactory
                .select(column1)
                .from(dailyStatistics)
                .where(dailyStatistics.strategyEntity.strategyId.eq(strategyId))
                .orderBy(dailyStatistics.date.asc())
                .fetch();

        Map<String, List<?>> result = new HashMap<>();
        result.put(option1, option1Values);

        // 두 옵션이 다르면 두 번째 데이터 추가
        if (!option1.equals(option2)) {
            Expression<Object> column2 = dailyStatisticsPath.get(option2, Object.class);
            List<Object> option2Values = queryFactory
                    .select(column2)
                    .from(dailyStatistics)
                    .where(dailyStatistics.strategyEntity.strategyId.eq(strategyId))
                    .orderBy(dailyStatistics.date.asc())
                    .fetch();

            result.put(option2, option2Values);
        }

        return result;
    }
}

