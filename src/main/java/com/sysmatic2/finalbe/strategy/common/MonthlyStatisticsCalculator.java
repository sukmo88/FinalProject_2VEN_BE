package com.sysmatic2.finalbe.strategy.common;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MonthlyStatisticsCalculator {

    /**
     * 현재 월 데이터를 가져오거나 새로 생성하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월 (YearMonth)
     * @param msp             월간 통계 레포지토리
     * @return 존재하는 월간 통계 데이터 또는 새로 생성된 데이터
     */
    public static MonthlyStatisticsEntity getOrCreateMonthlyStatistics(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                                       YearMonth currentMonth, MonthlyStatisticsRepository msp) {
        // 월간 통계 데이터가 이미 존재하면 반환, 없으면 새로운 데이터 생성
        return msp.findByStrategyIdAndAnalysisMonth(strategyId, currentMonth)
                .orElseGet(() -> MonthlyStatisticsEntity.builder()
                        .strategyEntity(dailyStatistics.getStrategyEntity()) // 전략 엔티티 설정
                        .analysisMonth(currentMonth) // 현재 월 설정
                        .monthlyAvgPrincipal(BigDecimal.ZERO) // 초기 월평균 원금
                        .monthlyDepWdAmount(BigDecimal.ZERO) // 초기 월 입출금 총액
                        .monthlyProfitLoss(BigDecimal.ZERO) // 초기 월 손익
                        .monthlyReturn(BigDecimal.ZERO) // 초기 월 손익률
                        .monthlyCumulativeProfitLoss(BigDecimal.ZERO) // 초기 월 누적 손익
                        .monthlyCumulativeReturn(BigDecimal.ZERO) // 초기 월 누적 손익률
                        .build());
    }

    /**
     * 월평균 원금을 계산하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월 (YearMonth)
     * @param dsp             일간 통계 레포지토리
     * @return 월평균 원금
     */
    public static BigDecimal calculateMonthlyAveragePrincipal(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                              YearMonth currentMonth, DailyStatisticsRepository dsp) {
        // 해당 월의 모든 일간 원금을 조회
        List<BigDecimal> dailyPrincipals = dsp.findDailyPrincipalsByStrategyIdAndMonth(
                strategyId, currentMonth.getYear(), currentMonth.getMonthValue());
        // 데이터가 null인 경우 빈 리스트로 초기화
        if (dailyPrincipals == null) {
            dailyPrincipals = new ArrayList<>();
        }
        // 현재 원금을 리스트에 추가
        dailyPrincipals.add(dailyStatistics.getPrincipal());

        // 원금 리스트가 비어 있으면 0 반환, 그렇지 않으면 평균 계산
        return dailyPrincipals.isEmpty()
                ? BigDecimal.ZERO
                : dailyPrincipals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add) // 원금 합산
                .divide(BigDecimal.valueOf(dailyPrincipals.size()), 4, RoundingMode.HALF_UP); // 평균 계산 (소수점 4자리 반올림)
    }

    /**
     * 월 입출금 총액을 계산하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월 (YearMonth)
     * @param dsp             일간 통계 레포지토리
     * @return 월 입출금 총액
     */
    public static BigDecimal calculateTotalDepWdAmount(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                       YearMonth currentMonth, DailyStatisticsRepository dsp) {
        // 해당 월의 모든 입출금 데이터를 조회
        List<BigDecimal> depWdAmounts = dsp.findDailyDepWdAmountsByStrategyIdAndMonth(
                strategyId, currentMonth.getYear(), currentMonth.getMonthValue());
        // 데이터가 null인 경우 빈 리스트로 초기화
        if (depWdAmounts == null) {
            depWdAmounts = new ArrayList<>();
        }
        // 현재 입출금 금액을 리스트에 추가
        depWdAmounts.add(dailyStatistics.getDepWdPrice());

        // 입출금 데이터가 비어 있으면 0 반환, 그렇지 않으면 총합 계산
        return depWdAmounts.isEmpty()
                ? BigDecimal.ZERO
                : depWdAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add); // 총합 계산
    }

    /**
     * 월 손익을 계산하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월 (YearMonth)
     * @param dsp             일간 통계 레포지토리
     * @return 월 손익
     */
    public static BigDecimal calculateTotalProfitLoss(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                      YearMonth currentMonth, DailyStatisticsRepository dsp) {
        // 해당 월의 모든 일간 손익 데이터를 조회
        List<BigDecimal> profitLosses = dsp.findDailyProfitLossesByStrategyIdAndMonth(
                strategyId, currentMonth.getYear(), currentMonth.getMonthValue());
        // 데이터가 null인 경우 빈 리스트로 초기화
        if (profitLosses == null) {
            profitLosses = new ArrayList<>();
        }
        // 현재 일간 손익을 리스트에 추가
        profitLosses.add(dailyStatistics.getDailyProfitLoss());

        // 손익 데이터가 비어 있으면 0 반환, 그렇지 않으면 총합 계산
        return profitLosses.isEmpty()
                ? BigDecimal.ZERO
                : profitLosses.stream().reduce(BigDecimal.ZERO, BigDecimal::add); // 총합 계산
    }

    /**
     * 월 손익률을 계산하는 메서드.
     *
     * @param strategyId   전략 ID
     * @param currentMonth 현재 월 (YearMonth)
     * @param dsp          일간 통계 레포지토리
     * @return 월 손익률
     */
    public static BigDecimal calculateMonthlyReturn(Long strategyId, YearMonth currentMonth,
                                                    DailyStatisticsRepository dsp) {
        // 이전 월 계산
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // 이전 월 마지막 기준가 가져오기
        BigDecimal previousReferencePrice = getPreviousMonthLastReferencePrice(strategyId, previousMonth, dsp);

        // 이전 기준가 검증: 0 이하일 경우 예외 처리
        if (previousReferencePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 현재 월 마지막 기준가 가져오기
        List<BigDecimal> lastReferencePrices = dsp.findLastReferencePriceByStrategyIdAndMonth(
                strategyId, currentMonth.getYear(), currentMonth.getMonthValue(), Pageable.ofSize(1));

        // 현재 기준가 검증: 데이터가 없으면 0 설정
        BigDecimal currentReferencePrice = lastReferencePrices == null || lastReferencePrices.isEmpty()
                ? BigDecimal.ZERO
                : lastReferencePrices.get(0);

        // 현재 기준가가 0 이하인 경우 월 손익률 0 반환
        if (currentReferencePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 월 손익률 = (현재 기준가 - 이전 기준가) / 이전 기준가
        return currentReferencePrice.subtract(previousReferencePrice)
                .divide(previousReferencePrice, 4, RoundingMode.HALF_UP);
    }

    /**
     * 월 누적 손익을 계산하는 메서드.
     *
     * - 이전 월까지의 모든 월간 손익 데이터를 합산하여 월 누적 손익을 계산합니다.
     * - 현재 일간 손익도 포함하여 계산합니다.
     *
     * @param strategyId       전략 ID
     * @param currentProfitLoss 현재 일간 손익
     * @param msp              월간 통계 레포지토리
     * @return 월 누적 손익
     */
    public static BigDecimal calculateCumulativeProfitLoss(Long strategyId, BigDecimal currentProfitLoss,
                                                           MonthlyStatisticsRepository msp) {
        // 이전 월까지의 모든 월간 손익 데이터를 조회
        List<BigDecimal> monthlyProfitLosses = msp.findAllMonthlyProfitLossByStrategyId(strategyId);

        // 월간 손익 데이터를 합산 (데이터가 없으면 기본값 0으로 설정)
        BigDecimal totalProfitLoss = monthlyProfitLosses == null || monthlyProfitLosses.isEmpty()
                ? BigDecimal.ZERO
                : monthlyProfitLosses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // 현재 일간 손익 추가
        return totalProfitLoss.add(currentProfitLoss);
    }

    /**
     * 월 누적 손익률을 계산하는 메서드.
     *
     * - 현재 월의 마지막 기준가를 기준으로 초기 기준가와 비교하여 월 누적 손익률을 계산합니다.
     *
     * @param strategyId   전략 ID
     * @param currentMonth 현재 월 (YearMonth)
     * @param dsp          일간 통계 레포지토리
     * @return 월 누적 손익률
     */
    public static BigDecimal calculateCumulativeReturn(Long strategyId, YearMonth currentMonth,
                                                       DailyStatisticsRepository dsp) {
        // 현재 월의 마지막 기준가를 조회
        List<BigDecimal> lastReferencePrices = dsp.findLastReferencePriceByStrategyIdAndMonth(
                strategyId, currentMonth.getYear(), currentMonth.getMonthValue(), Pageable.ofSize(1));

        // 마지막 기준가 설정 (데이터가 없으면 기본값 0으로 설정)
        BigDecimal lastReferencePrice = lastReferencePrices == null || lastReferencePrices.isEmpty()
                ? BigDecimal.ZERO
                : lastReferencePrices.get(0);

        // 기준가가 유효한 경우 손익률 계산, 그렇지 않으면 기본값 0 반환
        return lastReferencePrice.compareTo(BigDecimal.ZERO) > 0
                ? lastReferencePrice.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE)
                : BigDecimal.ZERO;
    }

    /**
     * 이전 월의 마지막 기준가를 가져오는 메서드.
     *
     * @param strategyId    전략 ID
     * @param previousMonth 이전 월 (YearMonth)
     * @param dsp           일간 통계 레포지토리
     * @return 이전 월의 마지막 기준가
     */
    public static BigDecimal getPreviousMonthLastReferencePrice(Long strategyId, YearMonth previousMonth,
                                                                DailyStatisticsRepository dsp) {
        // 이전 월의 마지막 기준가를 조회
        List<BigDecimal> referencePrices = dsp.findLastReferencePriceByStrategyIdAndMonth(
                strategyId, previousMonth.getYear(), previousMonth.getMonthValue(), Pageable.ofSize(1));

        // 리스트가 비어 있으면 0 반환, 그렇지 않으면 첫 번째 값 반환
        return referencePrices == null || referencePrices.isEmpty() ? BigDecimal.ZERO : referencePrices.get(0);
    }
}