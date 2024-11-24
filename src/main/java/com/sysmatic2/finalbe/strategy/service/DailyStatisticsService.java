package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.util.StatisticsCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyStatisticsService {

    private final DailyStatisticsRepository dssp;
    private final DailyStatisticsHistoryRepository dsshp;

    @Transactional
    public void processDailyStatistics(DailyStatisticsReqDto reqDto) {
        // 1. 이전 데이터 가져오기 (최신 데이터 1개)
        List<DailyStatisticsEntity> previousStates = dssp.findLatestByStrategyId(reqDto.getStrategyId(), PageRequest.of(0, 1));
        Optional<DailyStatisticsEntity> previousState = previousStates.isEmpty() ? Optional.empty() : Optional.of(previousStates.get(0));

        // 2. 일일 통계 계산
        DailyStatisticsEntity dailyStatistics = calculateDailyStatistics(reqDto, previousState);

        // 3. 저장 처리
        dssp.save(dailyStatistics);
    }

    @Transactional
    public DailyStatisticsEntity calculateDailyStatistics(DailyStatisticsReqDto reqDto, Optional<DailyStatisticsEntity> previousState) {
        // 이전 상태 가져오기 (기본값 처리 포함)
        BigDecimal previousBalance = previousState.map(DailyStatisticsEntity::getBalance).orElse(BigDecimal.ZERO);
        BigDecimal previousCumulativeProfitLoss = previousState.map(DailyStatisticsEntity::getCumulativeProfitLoss).orElse(BigDecimal.ZERO);
        BigDecimal previousPrincipal = previousState.map(DailyStatisticsEntity::getPrincipal).orElse(BigDecimal.ZERO);
        BigDecimal previousReferencePrice = previousState.map(DailyStatisticsEntity::getReferencePrice).orElse(BigDecimal.ZERO);
        BigDecimal previousMaxCumulativeProfitLoss = previousState.map(DailyStatisticsEntity::getMaxCumulativeProfitLoss).orElse(BigDecimal.ZERO);
        BigDecimal previousMaxCumulativeProfitLossRate = previousState.map(DailyStatisticsEntity::getMaxCumulativeProfitLossRate).orElse(BigDecimal.ZERO);
        BigDecimal previousMaxDrawdownAmount = previousState.map(DailyStatisticsEntity::getMaxDrawdownAmount).orElse(BigDecimal.ZERO);
        BigDecimal previousMaxDrawdownRate = previousState.map(DailyStatisticsEntity::getMaxDrawdownRate).orElse(BigDecimal.ZERO);
        int previousTradingDays = previousState.map(DailyStatisticsEntity::getTradingDays).orElse(0);
        int previousProfitDays = previousState.map(DailyStatisticsEntity::getTotalProfitDays).orElse(0);
        int previousLossDays = previousState.map(DailyStatisticsEntity::getTotalLossDays).orElse(0);
        BigDecimal previousTotalProfit = previousState.map(DailyStatisticsEntity::getTotalProfit).orElse(BigDecimal.ZERO);
        BigDecimal previousTotalLoss = previousState.map(DailyStatisticsEntity::getTotalLoss).orElse(BigDecimal.ZERO);
        int previousStrategyOperationDays = previousState.map(DailyStatisticsEntity::getStrategyOperationDays).orElse(0);
        int previousCurrentConsecutivePlDays = previousState.map(DailyStatisticsEntity::getCurrentConsecutivePlDays).orElse(0);
        int previousMaxConsecutiveProfitDays = previousState.map(DailyStatisticsEntity::getMaxConsecutiveProfitDays).orElse(0);
        int previousMaxConsecutiveLossDays = previousState.map(DailyStatisticsEntity::getMaxConsecutiveLossDays).orElse(0);

        // 사용자 입력값
        BigDecimal dailyProfitLoss = reqDto.getDailyProfitLoss();
        BigDecimal depWdPrice = reqDto.getDepWdPrice();

        // 계산된 지표들
        BigDecimal balance = StatisticsCalculator.calculateBalance(previousBalance, dailyProfitLoss, depWdPrice); // 잔고 = 이전 잔고 + 일손익 + 입출금
        BigDecimal principal = StatisticsCalculator.calculatePrincipal(previousPrincipal, depWdPrice); // 원금 = 이전 원금 + 입출금
        BigDecimal cumulativeProfitLoss = StatisticsCalculator.calculateCumulativeProfitLoss(previousCumulativeProfitLoss, dailyProfitLoss); // 누적손익 = 이전 누적손익 + 일손익
        int tradingDays = StatisticsCalculator.calculateTradingDays(previousTradingDays, dailyProfitLoss); // 거래일수 = 일손익이 0이 아닌 경우 1 증가
        int totalLossDays = previousLossDays + (dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? 1 : 0); // 손실일수 = 일손익이 음수인 경우 증가
        int totalProfitDays = previousProfitDays + (dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0); // 이익일수 = 일손익이 양수인 경우 증가
        BigDecimal totalLoss = previousTotalLoss.add(dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? dailyProfitLoss : BigDecimal.ZERO); // 총손실 = 이전 총손실 + 음수인 일손익
        BigDecimal totalProfit = previousTotalProfit.add(dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0 ? dailyProfitLoss : BigDecimal.ZERO); // 총이익 = 이전 총이익 + 양수인 일손익
        BigDecimal averageLoss = StatisticsCalculator.calculateAverageLoss(totalLoss, totalLossDays); // 평균손실 = 총손실 / 손실일수
        BigDecimal averageProfit = StatisticsCalculator.calculateAverageProfit(totalProfit, totalProfitDays); // 평균이익 = 총이익 / 이익일수
        BigDecimal referencePrice = StatisticsCalculator.calculateReferencePrice(balance, principal); // 기준가 = (잔고 / 원금) * 1000
        BigDecimal dailyPlRate = StatisticsCalculator.calculateDailyPlRate(referencePrice, previousReferencePrice); // 일손익률 = (오늘 기준가 - 이전 기준가) / 이전 기준가
        BigDecimal cumulativeProfitLossRate = StatisticsCalculator.calculateCumulativeProfitLossRate(referencePrice); // 누적손익률 = (기준가 / 1000) - 1
        BigDecimal maxCumulativeProfitLoss = StatisticsCalculator.calculateMaxCumulativeProfitLoss(cumulativeProfitLoss, previousMaxCumulativeProfitLoss); // 최대누적손익 = max(현재 누적손익, 이전 최대누적손익)
        BigDecimal maxCumulativeProfitLossRate = cumulativeProfitLossRate.max(previousMaxCumulativeProfitLossRate); // 최대누적손익률 = max(현재 누적손익률, 이전 최대누적손익률)
        BigDecimal currentDrawdownAmount = cumulativeProfitLoss.subtract(maxCumulativeProfitLoss).max(BigDecimal.ZERO); // 현재 자본인하 금액 = max(누적손익 - 최대누적손익, 0)
        BigDecimal maxDrawdownAmount = currentDrawdownAmount.min(previousMaxDrawdownAmount); // 최대 자본인하 금액 = min(현재 자본인하 금액, 이전 최대 자본인하 금액)
        BigDecimal currentDrawdownRate = BigDecimal.ZERO;
        if (referencePrice.compareTo(BigDecimal.ZERO) > 0) {
            currentDrawdownRate = referencePrice.subtract(previousReferencePrice.max(referencePrice))
                    .divide(referencePrice, 4, BigDecimal.ROUND_HALF_UP);
        } // 현재 자본인하율 = (기준가 - max(이전 기준가, 현재 기준가)) / 기준가
        BigDecimal maxDrawdownRate = currentDrawdownRate.min(previousMaxDrawdownRate); // 최대 자본인하율 = min(현재 자본인하율, 이전 최대 자본인하율)
        BigDecimal winRate = StatisticsCalculator.calculateWinRate(totalProfitDays, tradingDays); // 승률 = 이익일수 / 거래일수
        BigDecimal profitFactor = StatisticsCalculator.calculateProfitFactor(totalProfit, totalLoss); // Profit Factor = 총이익 / |총손실|
        BigDecimal roa = StatisticsCalculator.calculateROA(cumulativeProfitLoss, maxDrawdownAmount); // ROA = - 누적손익 / |최대 자본인하 금액|
        BigDecimal averageProfitLossRatio = averageLoss.compareTo(BigDecimal.ZERO) > 0
                ? averageProfit.divide(averageLoss.abs(), 4, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO; // 평균손익비 = 평균이익 / |평균손실|
        BigDecimal unrealizedProfitLoss = StatisticsCalculator.calculateUnrealizedProfitLoss(principal, balance); // 평가손익 = 원금 - 잔고
        int daysSincePeak = StatisticsCalculator.calculateDaysSincePeak(maxCumulativeProfitLoss, previousMaxCumulativeProfitLoss, previousState.map(DailyStatisticsEntity::getDaysSincePeak).orElse(0)); // 고점 후 경과일 = 현재 고점과 이전 고점 비교

        // 변동계수 및 Sharp Ratio 계산
        List<BigDecimal> dailyProfitLosses = dssp.findDailyProfitLossesByStrategyId(reqDto.getStrategyId());
        BigDecimal coefficientOfVariation = StatisticsCalculator.calculateCoefficientOfVariation(dailyProfitLosses, averageProfit); // 변동계수 = 표준편차 / 평균손익
        BigDecimal stdDevProfitLoss = StatisticsCalculator.calculateStdDev(dailyProfitLosses); // 표준편차
        BigDecimal sharpRatio = StatisticsCalculator.calculateSharpRatio(averageProfit, stdDevProfitLoss); // Sharp Ratio = 평균손익 / 표준편차

        BigDecimal maxDailyProfit = previousState.map(DailyStatisticsEntity::getMaxDailyProfit).orElse(BigDecimal.ZERO).max(dailyProfitLoss); // 최대일이익
        BigDecimal maxDailyProfitRate = principal.compareTo(BigDecimal.ZERO) > 0
                ? maxDailyProfit.divide(principal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO; // 최대일이익률 = (최대일이익 / 원금) * 100
        BigDecimal maxDailyLoss = previousState.map(DailyStatisticsEntity::getMaxDailyLoss).orElse(BigDecimal.ZERO).min(dailyProfitLoss); // 최대일손실
        BigDecimal maxDailyLossRate = principal.compareTo(BigDecimal.ZERO) > 0
                ? maxDailyLoss.divide(principal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO; // 최대일손실률 = (최대일손실 / 원금) * 100
        BigDecimal averageProfitLoss = tradingDays > 0
                ? totalProfit.add(totalLoss).divide(BigDecimal.valueOf(tradingDays), 4, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO; // 평균손익 = (총이익 + 총손실) / 거래일수
        BigDecimal averageProfitLossRate = principal.compareTo(BigDecimal.ZERO) > 0
                ? averageProfitLoss.divide(principal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO; // 평균손익률 = (평균손익 / 원금) * 100
        int currentConsecutivePlDays = dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0
                ? previousCurrentConsecutivePlDays + 1
                : (dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? previousCurrentConsecutivePlDays - 1 : 0); // 현재 연속 손익일수
        int maxConsecutiveProfitDays = currentConsecutivePlDays > 0
                ? Math.max(previousMaxConsecutiveProfitDays, currentConsecutivePlDays)
                : previousMaxConsecutiveProfitDays; // 최대 연속 수익일수
        int maxConsecutiveLossDays = currentConsecutivePlDays < 0
                ? Math.max(previousMaxConsecutiveLossDays, Math.abs(currentConsecutivePlDays))
                : previousMaxConsecutiveLossDays; // 최대 연속 손실일수
        int strategyOperationDays = previousStrategyOperationDays + 1; // 총 전략 운용일수

        BigDecimal cumulativeProfitLossRateLn = cumulativeProfitLossRate.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(Math.log(cumulativeProfitLossRate.doubleValue()))
                : BigDecimal.ZERO;

        BigDecimal currentDrawdownRateLn = currentDrawdownRate.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(Math.log(currentDrawdownRate.doubleValue()))
                : BigDecimal.ZERO;

        // 누적 입출금, 입금, 출금 계산
        BigDecimal cumulativeDepWdPrice = StatisticsCalculator.calculateCumulativeDepWd(
                previousState.map(DailyStatisticsEntity::getCumulativeDepWdPrice).orElse(BigDecimal.ZERO),
                depWdPrice);

        BigDecimal depositAmount = StatisticsCalculator.calculateDepositAmount(depWdPrice);
        BigDecimal cumulativeDepositAmount = StatisticsCalculator.calculateCumulativeDeposit(
                previousState.map(DailyStatisticsEntity::getCumulativeDepositAmount).orElse(BigDecimal.ZERO),
                depositAmount);

        BigDecimal withdrawAmount = StatisticsCalculator.calculateWithdrawAmount(depWdPrice);
        BigDecimal cumulativeWithdrawAmount = StatisticsCalculator.calculateCumulativeWithdraw(
                previousState.map(DailyStatisticsEntity::getCumulativeWithdrawAmount).orElse(BigDecimal.ZERO),
                withdrawAmount);

        // 최근 1년 수익률 계산
        Optional<BigDecimal> optionalOneYearAgoBalance = dssp.findBalanceOneYearAgo(reqDto.getStrategyId(), reqDto.getDate().minusYears(1));
        BigDecimal recentOneYearReturn = optionalOneYearAgoBalance
                .filter(balanceOneYearAgo -> balanceOneYearAgo.compareTo(BigDecimal.ZERO) > 0)
                .map(balanceOneYearAgo -> balance.divide(balanceOneYearAgo, 4, BigDecimal.ROUND_HALF_UP)
                        .subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100)))
                .orElse(BigDecimal.ZERO); // 최근 1년 수익률

        return DailyStatisticsEntity.builder()
                .date(reqDto.getDate())
                .depWdPrice(depWdPrice)
                .dailyProfitLoss(dailyProfitLoss)
                .tradingDays(tradingDays)
                .balance(balance)
                .principal(principal)
                .cumulativeProfitLoss(cumulativeProfitLoss)
                .unrealizedProfitLoss(unrealizedProfitLoss)
                .referencePrice(referencePrice)
                .dailyPlRate(dailyPlRate)
                .cumulativeProfitLossRate(cumulativeProfitLossRate)
                .maxCumulativeProfitLoss(maxCumulativeProfitLoss)
                .maxCumulativeProfitLossRate(maxCumulativeProfitLossRate)
                .currentDrawdownAmount(currentDrawdownAmount)
                .maxDrawdownAmount(maxDrawdownAmount)
                .currentDrawdownRate(currentDrawdownRate)
                .maxDrawdownRate(maxDrawdownRate)
                .winRate(winRate)
                .profitFactor(profitFactor)
                .roa(roa)
                .totalProfit(totalProfit)
                .totalProfitDays(totalProfitDays)
                .averageProfit(averageProfit)
                .totalLoss(totalLoss)
                .totalLossDays(totalLossDays)
                .averageLoss(averageLoss)
                .averageProfitLossRatio(averageProfitLossRatio)
                .peak(maxCumulativeProfitLoss)
                .peakRate(maxCumulativeProfitLossRate)
                .daysSincePeak(daysSincePeak)
                .coefficientOfVariation(coefficientOfVariation)
                .sharpRatio(sharpRatio)
                .maxDailyProfit(maxDailyProfit)
                .maxDailyProfitRate(maxDailyProfitRate)
                .maxDailyLoss(maxDailyLoss)
                .maxDailyLossRate(maxDailyLossRate)
                .averageProfitLoss(averageProfitLoss)
                .averageProfitLossRate(averageProfitLossRate)
                .currentConsecutivePlDays(currentConsecutivePlDays)
                .maxConsecutiveProfitDays(maxConsecutiveProfitDays)
                .maxConsecutiveLossDays(maxConsecutiveLossDays)
                .recentOneYearReturn(recentOneYearReturn)
                .strategyOperationDays(strategyOperationDays)
                .cumulativeDepWdPrice(cumulativeDepWdPrice)
                .depositAmount(depositAmount)
                .cumulativeDepositAmount(cumulativeDepositAmount)
                .withdrawAmount(withdrawAmount)
                .cumulativeWithdrawAmount(cumulativeWithdrawAmount)
                .build();
    }
}