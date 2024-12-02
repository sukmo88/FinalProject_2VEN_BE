package com.sysmatic2.finalbe.strategy.common;

import com.sysmatic2.finalbe.strategy.dto.DdDayAndMaxDdInRate;
import com.sysmatic2.finalbe.strategy.dto.StrategyKpDto;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DailyStatisticsCalculator {
    private DailyStatisticsCalculator() {
        // 유틸리티 클래스는 인스턴스화 금지
    }

    /**
     * 변동계수(Coefficient of Variation, CV)를 계산하는 메서드.
     *
     * @param dailyProfitLosses 오늘까지의 모든 일손익 데이터 리스트
     * @param averageProfitLoss 기준일까지의 평균손익
     * @return 변동계수 (단위: %, 소수점 10자리까지 표시)
     */
    public static BigDecimal calculateCoefficientOfVariation(List<BigDecimal> dailyProfitLosses, BigDecimal averageProfitLoss) {
        if (dailyProfitLosses == null || dailyProfitLosses.isEmpty()) {
            throw new IllegalArgumentException("일손익 데이터 리스트는 비어 있을 수 없습니다.");
        }
        if (averageProfitLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 표준편차 계산
        BigDecimal mean = dailyProfitLosses.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyProfitLosses.size()), 11, RoundingMode.HALF_UP); // 중간 계산은 11자리

        BigDecimal variance = dailyProfitLosses.stream()
                .map(profitLoss -> profitLoss.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyProfitLosses.size()), 11, RoundingMode.HALF_UP); // 중간 계산은 11자리

        BigDecimal stdDevProfitLoss = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(10, RoundingMode.HALF_UP); // 표준편차 계산 후 소수점 10자리 반올림

        // 변동계수 계산
        return stdDevProfitLoss.divide(averageProfitLoss, 11, RoundingMode.HALF_UP) // 비율 계산 중간 단계는 11자리
                .multiply(BigDecimal.valueOf(100)) // 백분율로 변환
                .setScale(10, RoundingMode.HALF_UP); // 최종 결과는 10자리
    }

    /**
     * 승률 계산.
     * @param totalProfitDays 총 이익일수
     * @param tradingDays     총 거래일수
     * @return 승률 (단위: %)
     */
    public static BigDecimal calculateWinRate(int totalProfitDays, int tradingDays) {
        return tradingDays > 0
                ? BigDecimal.valueOf(totalProfitDays)
                .divide(BigDecimal.valueOf(tradingDays), 4, RoundingMode.HALF_UP) // 비율 계산
                .multiply(BigDecimal.valueOf(100)) // 백분율로 변환
                .setScale(2, RoundingMode.HALF_UP) // 소수점 둘째 자리까지 표현
                : BigDecimal.ZERO;
    }

    /**
     * Profit Factor 계산.
     * @param totalProfit 총이익
     * @param totalLoss   총손실
     * @return Profit Factor
     */
    public static BigDecimal calculateProfitFactor(BigDecimal totalProfit, BigDecimal totalLoss) {
        return totalLoss.compareTo(BigDecimal.ZERO) < 0
                ? totalProfit.divide(totalLoss.abs(), 4, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
    }

    /**
     * ROA 계산.
     * @param cumulativeProfitLoss 누적손익
     * @param maxDrawdownAmount    최대 자본인하 금액
     * @return ROA (단위: %)
     */
    public static BigDecimal calculateROA(BigDecimal cumulativeProfitLoss, BigDecimal maxDrawdownAmount) {
        // 최대 자본인하 금액이 0이면 0 반환
        if (maxDrawdownAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // ROA 계산: (누적손익 / 최대 자본인하 금액) * -1
        return cumulativeProfitLoss.divide(maxDrawdownAmount, 10, RoundingMode.HALF_UP) // 소수점 10자리까지 반올림
                .multiply(BigDecimal.valueOf(-1)); // 음수 변환
    }

    /**
     * Sharp Ratio 계산 (평균손익 / 일손익 합산의 표준편차).
     *
     * @param dailyProfitLosses 일손익 리스트
     * @param averageProfitLoss 평균손익
     * @return Sharp Ratio (소수점 10자리까지 표시)
     */
    public static BigDecimal calculateSharpRatio(List<BigDecimal> dailyProfitLosses, BigDecimal averageProfitLoss) {
        if (dailyProfitLosses == null || dailyProfitLosses.isEmpty()) {
            throw new IllegalArgumentException("일손익 데이터 리스트는 비어 있을 수 없습니다.");
        }

        if (averageProfitLoss == null || averageProfitLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 평균손익이 0이면 Sharp Ratio는 0
        }

        // 분산 계산
        BigDecimal mean = dailyProfitLosses.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyProfitLosses.size()), 11, RoundingMode.HALF_UP); // 평균 계산

        BigDecimal variance = dailyProfitLosses.stream()
                .map(value -> value.subtract(mean).pow(2)) // (X - 평균)^2
                .reduce(BigDecimal.ZERO, BigDecimal::add) // 분산 합산
                .divide(BigDecimal.valueOf(dailyProfitLosses.size()), 11, RoundingMode.HALF_UP); // 분산 평균

        // 표준편차 계산 (분산의 제곱근)
        BigDecimal stdDevProfitLoss = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(10, RoundingMode.HALF_UP);

        // Sharp Ratio 계산: 평균손익 / 표준편차
        return stdDevProfitLoss.compareTo(BigDecimal.ZERO) > 0
                ? averageProfitLoss.divide(stdDevProfitLoss, 11, RoundingMode.HALF_UP) // Sharp Ratio 계산
                .setScale(10, RoundingMode.HALF_UP) // 최종 결과 소수점 10자리
                : BigDecimal.ZERO;
    }

    /**
     * 최대누적손익 계산.
     * @param cumulativeProfitLoss 현재 누적손익
     * @param previousMaxCumulativeProfitLoss 이전 최대누적손익
     * @return 최대누적손익
     */
    public static BigDecimal calculateMaxCumulativeProfitLoss(BigDecimal cumulativeProfitLoss, BigDecimal previousMaxCumulativeProfitLoss) {
        return cumulativeProfitLoss.max(previousMaxCumulativeProfitLoss);
    }

    /**
     * 평가손익 계산.
     * @param principal 원금
     * @param balance   잔고
     * @return 평가손익
     */
    public static BigDecimal calculateUnrealizedProfitLoss(BigDecimal principal, BigDecimal balance) {
        return principal.subtract(balance);
    }

    /**
     * 고점 후 경과일 계산.
     * @param currentPeak         현재 고점
     * @param previousPeak        이전 고점
     * @param previousDaysSincePeak 이전 고점 후 경과일
     * @return 고점 후 경과일
     */
    public static int calculateDaysSincePeak(BigDecimal currentPeak, BigDecimal previousPeak, int previousDaysSincePeak) {
        return currentPeak.equals(previousPeak) && currentPeak.compareTo(BigDecimal.ZERO) > 0
                ? previousDaysSincePeak + 1
                : 0;
    }

    /**
     * 입금 계산.
     * @param depWdPrice 입출금
     * @param isFirstEntry 첫입력여부
     * @return 입금액
     */
    public static BigDecimal calculateDepositAmount(BigDecimal depWdPrice, boolean isFirstEntry) {
        // 첫 번째 입력이면 0 반환
        if (isFirstEntry) {
            return BigDecimal.ZERO;
        }
        return depWdPrice.compareTo(BigDecimal.ZERO) > 0 ? depWdPrice : BigDecimal.ZERO;
    }

    /**
     * 출금 계산.
     * @param depWdPrice 입출금
     * @param isFirstEntry 첫입력여부
     * @return 출금액
     */
    public static BigDecimal calculateWithdrawAmount(BigDecimal depWdPrice, boolean isFirstEntry) {
        // 첫 번째 입력이면 0 반환
        if (isFirstEntry) {
            return BigDecimal.ZERO;
        }
        return depWdPrice.compareTo(BigDecimal.ZERO) < 0 ? depWdPrice.abs() : BigDecimal.ZERO;
    }

    /**
     * 잔고 계산.
     * @param previousBalance 이전 잔고
     * @param dailyProfitLoss 오늘의 일손익
     * @param depWdPrice 오늘의 입출금
     * @return 계산된 잔고
     */
    public static BigDecimal calculateBalance(BigDecimal previousBalance, BigDecimal dailyProfitLoss, BigDecimal depWdPrice) {
        return previousBalance.add(dailyProfitLoss).add(depWdPrice);
    }

    /**
     * 원금 계산.
     * @param previousPrincipal 이전 원금
     * @param depWdPrice 오늘의 입출금
     * @param previousBalance 이전 잔고
     * @return 소수점 첫째자리에서 반올림된 정수부 원금
     */
    public static BigDecimal calculatePrincipal(BigDecimal previousPrincipal, BigDecimal depWdPrice, BigDecimal previousBalance) {
        if (previousPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            // 이전 원금이 0일 경우 입출금 금액으로 원금을 설정
            return depWdPrice.setScale(0, RoundingMode.HALF_UP);
        }

        if (previousBalance.compareTo(BigDecimal.ZERO) == 0) {
            // 이전 잔고가 0일 경우 입출금 금액만 반영
            return previousPrincipal.add(depWdPrice).setScale(0, RoundingMode.HALF_UP); // 소수점 반올림
        }

        // 원금 = 이전 원금 + (입출금 금액 / (이전 잔고 / 이전 원금))
        BigDecimal adjustmentFactor = previousBalance.divide(previousPrincipal, 10, RoundingMode.HALF_UP); // 이전 잔고/이전 원금
        BigDecimal adjustedDepWd = depWdPrice.divide(adjustmentFactor, 10, RoundingMode.HALF_UP); // 입출금 비율 조정
        return previousPrincipal.add(adjustedDepWd).setScale(0, RoundingMode.HALF_UP); // 소수점 첫째자리에서 반올림 후 정수부 반환
    }

    /**
     * 누적손익 계산.
     * @param previousCumulativeProfitLoss 이전 누적손익
     * @param dailyProfitLoss 오늘의 일손익
     * @return 계산된 누적손익
     */
    public static BigDecimal calculateCumulativeProfitLoss(BigDecimal previousCumulativeProfitLoss, BigDecimal dailyProfitLoss) {
        return previousCumulativeProfitLoss.add(dailyProfitLoss);
    }

    /**
     * 거래일수 계산.
     * @param previousTradingDays 이전 거래일수
     * @param dailyProfitLoss 오늘의 일손익
     * @return 계산된 거래일수
     */
    public static int calculateTradingDays(int previousTradingDays, BigDecimal dailyProfitLoss) {
        return dailyProfitLoss.compareTo(BigDecimal.ZERO) != 0 ? previousTradingDays + 1 : previousTradingDays;
    }

    /**
     * 평균손실 계산.
     * @param totalLoss 총손실 (음수)
     * @param totalLossDays 손실일수
     * @return 계산된 평균손실 (항상 음수, 소수점 첫째 자리에서 반올림하여 정수만 반환)
     */
    public static BigDecimal calculateAverageLoss(BigDecimal totalLoss, int totalLossDays) {
        return totalLossDays > 0
                ? totalLoss.divide(BigDecimal.valueOf(totalLossDays), 0, RoundingMode.HALF_UP) // 소수점 첫째 자리에서 반올림 후 정수만 반환
                : BigDecimal.ZERO;
    }

    /**
     * 평균이익 계산.
     * @param totalProfit 총이익
     * @param totalProfitDays 이익일수
     * @return 계산된 평균이익 (소수점 첫째 자리에서 반올림하여 정수만 반환)
     */
    public static BigDecimal calculateAverageProfit(BigDecimal totalProfit, int totalProfitDays) {
        return totalProfitDays > 0
                ? totalProfit.divide(BigDecimal.valueOf(totalProfitDays), 0, RoundingMode.HALF_UP) // 소수점 첫째 자리에서 반올림 후 정수 반환
                : BigDecimal.ZERO;
    }

    /**
     * 기준가 계산.
     * @param balance 잔고
     * @param principal 원금
     * @return 계산된 기준가 (셋째 자리에서 반올림하여 소수점 둘째 자리까지 표현)
     */
    public static BigDecimal calculateReferencePrice(BigDecimal balance, BigDecimal principal) {
        return principal.compareTo(BigDecimal.ZERO) > 0
                ? balance.divide(principal, 10, RoundingMode.DOWN) // 10자리까지 계산 후
                .multiply(BigDecimal.valueOf(1000))
                .setScale(2, RoundingMode.HALF_UP) // 소수점 셋째 자리에서 반올림, 둘째 자리까지 표현
                : BigDecimal.ZERO;
    }

    /**
     * 누적손익률 계산.
     * @param referencePrice 기준가
     * @return 계산된 누적손익률 (단위: %)
     */
    public static BigDecimal calculateCumulativeProfitLossRate(BigDecimal referencePrice) {
        return referencePrice.compareTo(BigDecimal.ZERO) > 0
                ? referencePrice
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP) // 중간 계산: 소수점 10자리까지
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100)) // 백분율 변환
                .setScale(4, RoundingMode.HALF_UP) // 최종적으로 소수점 4자리로 반올림
                : BigDecimal.ZERO;
    }

    /**
     * 누적 입금 계산.
     * @param previousCumulativeDeposit 이전 누적 입금
     * @param depositAmount 오늘 입금 금액
     * @return 누적 입금
     */
    public static BigDecimal calculateCumulativeDeposit(BigDecimal previousCumulativeDeposit, BigDecimal depositAmount) {
        return previousCumulativeDeposit.add(depositAmount);
    }

    /**
     * 누적 출금 계산.
     * @param previousCumulativeWithdraw 이전 누적 출금
     * @param withdrawAmount 오늘 출금 금액
     * @return 누적 출금
     */
    public static BigDecimal calculateCumulativeWithdraw(BigDecimal previousCumulativeWithdraw, BigDecimal withdrawAmount) {
        return previousCumulativeWithdraw.add(withdrawAmount);
    }

    /**
     * 현재 자본인하율 계산 (백분율, 소수점 4자리까지).
     *
     * @param referencePrice       현재 기준가
     * @param allReferencePrices   지금까지의 모든 기준가 리스트 (등록된 순서대로 정렬)
     * @return 현재 자본인하율 (백분율, 소수점 4자리까지)
     */
    public static BigDecimal calculateCurrentDrawdownRate(BigDecimal referencePrice, List<BigDecimal> allReferencePrices) {
        // 기준가가 0 이하인 경우 계산하지 않음
        if (referencePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 기준가 데이터가 없을 경우
        if (allReferencePrices == null || allReferencePrices.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 지금까지의 최대 기준가 계산
        BigDecimal maxReferencePriceSoFar = allReferencePrices.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO); // 최대값 없으면 0 반환

        // 기준가 - 1000이 0 이하인 경우
        if (referencePrice.subtract(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 현재 자본인하율 계산: ((기준가 - max(이전 기준가, 현재 기준가)) / 기준가) * 100
        return referencePrice.subtract(maxReferencePriceSoFar)
                .divide(referencePrice, 10, RoundingMode.HALF_UP) // 높은 정밀도로 중간 계산
                .multiply(BigDecimal.valueOf(100)) // 백분율로 변환
                .setScale(4, RoundingMode.HALF_UP); // 소수점 4자리까지 반올림
    }

    /**
     * 일손익률 계산 (백분율로 반환, 계산은 소수점 10자리, 반올림은 소수점 5번째 자리).
     *
     * 첫 번째 등록일인 경우: (오늘 기준가 - 1000) / 1000 * 100
     * 두 번째 이후 등록일인 경우: (오늘 기준가 - 이전 기준가) / 이전 기준가 * 100
     *
     * @param referencePrice 오늘의 기준가
     * @param previousReferencePrice 이전 기준가
     * @param isFirstEntry 첫 번째 등록 여부
     * @return 계산된 일손익률 (백분율)
     */
    public static BigDecimal calculateDailyPlRate(BigDecimal referencePrice, BigDecimal previousReferencePrice, boolean isFirstEntry) {
        MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP); // 계산은 소수점 10자리 정밀도 유지

        if (isFirstEntry) {
            // 첫 번째 등록일 계산: (기준가 - 1000) / 1000 * 100
            return referencePrice.subtract(BigDecimal.valueOf(1000), mathContext)
                    .divide(BigDecimal.valueOf(1000), mathContext)
                    .multiply(BigDecimal.valueOf(100), mathContext)
                    .setScale(5, RoundingMode.HALF_UP); // 최종 결과는 소수점 5번째 자리에서 반올림
        } else if (previousReferencePrice.compareTo(BigDecimal.ZERO) > 0) {
            // 두 번째 이후 등록일 계산: (오늘 기준가 - 이전 기준가) / 이전 기준가 * 100
            return referencePrice.subtract(previousReferencePrice, mathContext)
                    .divide(previousReferencePrice, mathContext)
                    .multiply(BigDecimal.valueOf(100), mathContext)
                    .setScale(5, RoundingMode.HALF_UP); // 최종 결과는 소수점 5번째 자리에서 반올림
        } else {
            // 이전 기준가가 0 이하인 경우, 0 반환
            return BigDecimal.ZERO.setScale(5, RoundingMode.HALF_UP);
        }
    }

    /**
     * 누적 입출금 계산.
     * @param depWdHistory 오늘까지의 모든 입출금 내역 리스트 (등록된 순서대로 정렬)
     * @param todayDepWd 오늘의 입출금 금액
     * @return 누적 입출금 (첫 번째 입출금 이후의 모든 입출금 합계)
     */
    public static BigDecimal calculateCumulativeDepWd(List<BigDecimal> depWdHistory, BigDecimal todayDepWd) {
        if (depWdHistory == null || depWdHistory.isEmpty()) {
            return BigDecimal.ZERO; // 첫 번째 등록일 경우 0 반환
        }

        // 첫 번째 입출금 금액
        BigDecimal firstDepWd = depWdHistory.get(0);

        // 첫 번째 등록 이후의 누적 입출금 계산
        BigDecimal totalDepWd = depWdHistory.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add) // 전체 합계 계산
                .subtract(firstDepWd); // 첫 번째 금액 차감

        // 첫 번째 등록 이후라면 오늘의 금액 포함
        if (depWdHistory.size() > 1) {
            totalDepWd = totalDepWd.add(todayDepWd);
        }

        return totalDepWd;
    }

    /**
     * 주어진 일별 손익률 리스트에서 최대값을 계산하고 음수일 경우 0으로 처리합니다.
     *
     * @param dailyPlRate 일별 손익률(BigDecimal) 리스트
     * @return 최대 일 이익률 (음수일 경우 0 반환, 소수점 4자리까지 반올림)
     */
    public static BigDecimal calculateMaxDailyProfitRate(List<BigDecimal> dailyPlRate) {
        // 최대값 계산 (리스트가 비어있는 경우 대비 기본값 설정)
        BigDecimal maxDailyProfitRate = dailyPlRate.stream()
                .max(BigDecimal::compareTo) // 리스트에서 최대값 찾기
                .orElse(BigDecimal.ZERO);  // 리스트가 비어있으면 0 반환

        // 최대값이 음수일 경우 0으로 처리하고, 소수점 4자리까지 반올림
        return maxDailyProfitRate.max(BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 최대 일 손실률 계산.
     * @param dailyPlRates 오늘까지의 모든 일손익률 리스트 (등록된 순서대로 정렬)
     * @param currentDailyPlRate 현재 입력되는 일손익률
     * @return 최대 일 손실률 (최소값 포함)
     */
    public static BigDecimal calculateMaxDailyLossRate(List<BigDecimal> dailyPlRates, BigDecimal currentDailyPlRate) {
        if (dailyPlRates == null || dailyPlRates.isEmpty()) {
            return currentDailyPlRate != null
                    ? currentDailyPlRate
                    : BigDecimal.ZERO; // 현재 값만 있는 경우 그대로 반환
        }

        if (currentDailyPlRate == null) {
            return dailyPlRates.stream()
                    .min(BigDecimal::compareTo) // 기존 리스트의 최소값 찾기
                    .orElse(BigDecimal.ZERO); // 리스트에 값이 없으면 0 반환
        }

        // 현재 입력 값 포함하여 최소값 계산
        return dailyPlRates.stream()
                .min(BigDecimal::compareTo) // 리스트 최소값 찾기
                .map(minRate -> minRate.min(currentDailyPlRate)) // 최소값과 현재 값 비교
                .orElse(currentDailyPlRate); // 현재 값만 있는 경우 반환
    }

    /**
     * Peak (최대 누적손익) 계산.
     * @param cumulativeProfitLossHistory 과거의 누적손익 리스트
     * @param currentCumulativeProfitLoss 현재 누적손익
     * @return 최대 누적손익 (양수 값만 고려)
     */
    public static BigDecimal calculatePeak(List<BigDecimal> cumulativeProfitLossHistory, BigDecimal currentCumulativeProfitLoss) {
        if (cumulativeProfitLossHistory == null || cumulativeProfitLossHistory.isEmpty()) {
            // 과거 데이터가 없으면 현재 값이 최대값
            return currentCumulativeProfitLoss.max(BigDecimal.ZERO);
        }

        // 과거 최대값 계산
        BigDecimal pastMax = cumulativeProfitLossHistory.stream()
                .filter(value -> value.compareTo(BigDecimal.ZERO) > 0) // 양수만 고려
                .max(BigDecimal::compareTo) // 리스트 중 최대값 찾기
                .orElse(BigDecimal.ZERO);

        // 현재 값 포함하여 최대값 계산
        return pastMax.max(currentCumulativeProfitLoss).max(BigDecimal.ZERO);
    }

    /**
     * Peak Rate (최대 누적손익률) 계산.
     * @param cumulativeProfitLossRates 과거의 누적손익률 리스트
     * @param currentCumulativeProfitLossRate 현재 누적손익률
     * @return 최대 누적손익률 (양수 값만 고려)
     */
    public static BigDecimal calculatePeakRate(List<BigDecimal> cumulativeProfitLossRates, BigDecimal currentCumulativeProfitLossRate) {
        if (cumulativeProfitLossRates == null || cumulativeProfitLossRates.isEmpty()) {
            // 과거 데이터가 없으면 현재 값이 최대값
            return currentCumulativeProfitLossRate.max(BigDecimal.ZERO);
        }

        // 과거 최대값 계산
        BigDecimal pastMax = cumulativeProfitLossRates.stream()
                .filter(value -> value.compareTo(BigDecimal.ZERO) > 0) // 양수만 고려
                .max(BigDecimal::compareTo) // 리스트 중 최대값 찾기
                .orElse(BigDecimal.ZERO);

        // 현재 값 포함하여 최대값 계산
        return pastMax.max(currentCumulativeProfitLossRate).max(BigDecimal.ZERO);
    }

    /**
     * 현재 DD 기간 (현재 자본인하율 기준)을 계산합니다.
     *
     * @param currentDrawdownRate 현재 자본인하율
     * @param previousDdDay       이전 DD 기간
     * @return 계산된 현재 DD 기간 (일 단위)
     */
    public static int calculateDdDay(BigDecimal currentDrawdownRate, int previousDdDay) {
        return currentDrawdownRate.compareTo(BigDecimal.ZERO) >= 0 ? 0 : previousDdDay + 1;
    }

    /**
     * DD 기간 내 최대 자본인하율 (최저값) 계산.
     *
     * @param currentDrawdownRate 현재 자본인하율
     * @param previousMaxDdInRate 이전 최대 자본인하율
     * @param ddDay               현재 DD 기간 (1 이상이면 DD 기간 내 계산)
     * @return 계산된 DD 기간 내 최대 자본인하율 (소수점 4자리까지 반올림)
     */
    public static BigDecimal calculateMaxDdInRate(BigDecimal currentDrawdownRate, BigDecimal previousMaxDdInRate, int ddDay) {
        // DD 기간이 1 이상인 경우만 계산
        if (ddDay > 0) {
            return previousMaxDdInRate.min(
                            currentDrawdownRate.setScale(10, RoundingMode.HALF_UP) // 중간 계산 소수점 10자리까지 유지
                    )
                    .setScale(4, RoundingMode.HALF_UP); // 최종 결과는 소수점 4자리로 반올림
        }
        // DD 기간이 0이면 0 반환 (소수점 4자리까지 설정)
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    //월간 데이터 계산
    /**
     * 월평균 원금 계산.
     * @Param 해당 월의 원금 리스트
     * @return 월평균 원금
     */
    public static BigDecimal calculateMonthlyAveragePrincipal(List<BigDecimal> principals){
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal principal : principals) {
            sum = sum.add(principal);
        }
        return sum.divide(BigDecimal.valueOf(principals.size()), 4, RoundingMode.HALF_EVEN);
    }

    /**
     * 월 입출금 총액 계산
     * @Param 해당 월의 입출금 리스트
     * @return 월 입출금 총액
     */
    public static BigDecimal calculateMonthlyDepWdAmount(List<BigDecimal> dailyDepWdAmounts){
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal dailyDepWdAmount : dailyDepWdAmounts) {
            sum = sum.add(dailyDepWdAmount);
        }

        return sum.setScale(4, RoundingMode.HALF_EVEN);
    }

    /**
     * 월손익 계산
     * @Param 해당 월의 일손익 리스트
     * @return 월 일손익 총액
     */
    public static BigDecimal calculateMonthlyProfitLoss(List<BigDecimal> dailyProfitLosses){
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal dailyProfitLoss : dailyProfitLosses) {
            sum = sum.add(dailyProfitLoss);
        }

        return sum.setScale(4, RoundingMode.HALF_EVEN);
    }

    /**
     * 월 손익률 계산
     * @Param 해당월 첫번째 기준가, 해당월 마지막 기준가
     * @return 마지막 기준가 / 첫번째 기준가 - 1
     */
    public static BigDecimal calculateMonthlyReturn(BigDecimal firstReferencePrice, BigDecimal lastReferencePrice){
        BigDecimal result = BigDecimal.ZERO;
        result = firstReferencePrice.divide(lastReferencePrice, 4, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE);

        return result.setScale(4, RoundingMode.HALF_EVEN);
    }

    /**
     * 월 누적 손익 계산
     * @Param 해당 전략의 월손익 리스트
     * @return 해당 전략의 월 누적 손익
     */
    public static BigDecimal calculateMonthlyCumulativeProfitLoss(List<BigDecimal> monthlyProfitLosses){
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal monthlyProfitLoss : monthlyProfitLosses) {
            sum = sum.add(monthlyProfitLoss);
        }

        return sum.setScale(4, RoundingMode.HALF_EVEN);
    }

    /**
     * 월 누적 손익률 계산
     * @Param 해당 월 마지막 기준가
     * @return 해당월 마지막 기준가 / 1000 -1
     */
    public static BigDecimal calculateMonthlyCumulativeReturn(BigDecimal lastDailyReferencePrice){
        BigDecimal result = lastDailyReferencePrice;
        result = result.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE);

        return result.setScale(4, RoundingMode.HALF_EVEN);
    }

    /**
     * 월 평균 잔고 계산
     * @Param 해당 월 일간데이터 잔고 리스트
     * @return 월 평균 잔고
     */
    public static BigDecimal calculateMonthlyAverageBalance(List<BigDecimal> dailyBalances){
        BigDecimal sum = BigDecimal.ZERO;
        for(BigDecimal dailyBalance : dailyBalances){
            sum = sum.add(dailyBalance);
        }

        return sum.divide(BigDecimal.valueOf(dailyBalances.size()), 4, RoundingMode.HALF_EVEN);
    }

    // 총 전략 운용일수 계산
    public static Integer calculateStrategyOperationDays(LocalDate earliestDate, LocalDate latestDate) {
        if (earliestDate == null || latestDate == null) {
            return 1; // 기본값으로 운용일수 1 반환
        }
        // 두 날짜 간의 일 수 계산 (포함 관계로 인해 +1)
        return (int) java.time.temporal.ChronoUnit.DAYS.between(earliestDate, latestDate) + 1;
    }

    /**
     * 최근 1년 수익률을 계산합니다.
     * 수익률 계산 공식: ((오늘 기준가 / 1년 전 기준가) - 1) * 100
     *
     * - 1년 전 기준가가 없어도 상관없으며, 기준가는 리스트의 첫 번째 값과 마지막 값으로 설정됩니다.
     * - 리스트의 첫 번째 값: 가장 오래된 기준가
     * - 리스트의 마지막 값: 가장 최신 기준가
     * - 결과는 소수점 셋째 자리에서 반올림하여 둘째 자리까지 표현됩니다.
     *
     * @param referencePrices 기준가 리스트 (날짜 오름차순으로 정렬된 값)
     * @return 최근 1년 수익률 (백분율, 소수점 둘째 자리까지 표현)
     */
    public static BigDecimal calculateRecentOneYearReturn(List<BigDecimal> referencePrices) {
        if (referencePrices == null || referencePrices.size() < 2) {
            // 기준가가 2개 미만일 경우 계산 불가, 0 반환
            return BigDecimal.ZERO;
        }

        // 리스트의 첫 번째 값 (가장 오래된 기준가)
        BigDecimal oldestReferencePrice = referencePrices.get(0);
        // 리스트의 마지막 값 (가장 최신 기준가)
        BigDecimal latestReferencePrice = referencePrices.get(referencePrices.size() - 1);

        if (oldestReferencePrice.compareTo(BigDecimal.ZERO) <= 0) {
            // 1년 전 기준가가 0 이하일 경우 계산 불가, 0 반환
            return BigDecimal.ZERO;
        }

        // 수익률 계산: ((오늘 기준가 / 1년 전 기준가) - 1) * 100
        return latestReferencePrice.divide(oldestReferencePrice, 4, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP); // 소수점 셋째 자리에서 반올림하여 둘째 자리까지 표현
    }

    /**
     * KP Ratio를 계산합니다.
     *
     * KP Ratio 공식:
     * KP Ratio = cumulativeProfitLossRate / (maxDdInRateSum * sqrt(ddDaySum / tradingDays))
     *
     * - ddDay와 maxDdInRate는 0이 아닌 구간만 합산합니다.
     * - inputDdDay와 inputMaxDdInRate는 현재 입력값으로 추가 계산에 포함됩니다.
     * - currentDrawdownRate가 0인 부분에서 ∑(ddDay)와 ∑(maxDdInRate)를 갱신합니다.
     * - 계산 결과는 소수점 11자리에서 계산한 후, 소수점 10자리까지 반올림하여 반환합니다.
     * - maxDdInRateSum 또는 ddDaySum이 0일 경우 KP Ratio는 0으로 반환됩니다.
     * - 누적손익률(cumulativeProfitLossRate)이 0 이하일 경우 KP Ratio는 0으로 반환됩니다.
     *
     * @param ddDayAndMaxDdInRateList ddDay와 maxDDInRate 리스트 (날짜 오름차순)
     *                                - 각 배열: [0] = ddDay (int), [1] = maxDDInRate (BigDecimal)
     * @param cumulativeProfitLossRate 누적손익률 (cumulativeProfitLossRate)
     * @param tradingDays              총 거래일수 (tradingDays)
     * @return KP Ratio (소수점 10자리까지 반올림)
     */
    public static BigDecimal calculateKPRatio(List<DdDayAndMaxDdInRate> ddDayAndMaxDdInRateList,
                                              BigDecimal currentDrawdownRate,
                                              BigDecimal cumulativeProfitLossRate,
                                              int tradingDays) {

        if (cumulativeProfitLossRate.compareTo(BigDecimal.ZERO) <= 0) {
            // 누적손익률이 0 이하일 경우 KP Ratio는 0
            return BigDecimal.ZERO;
        }

        int ddDaySum = 0; // ddDay의 합
        BigDecimal maxDdInRateSum = BigDecimal.ZERO; // maxDDInRate의 합

        // 이전 값을 추적하기 위한 변수
        int prevDdDay = 0;
        BigDecimal prevMaxDdInRate = BigDecimal.ZERO;

        // ===== Step 1: ddDay와 maxDdInRate 합산 =====
        for (DdDayAndMaxDdInRate row : ddDayAndMaxDdInRateList) {
            int currentDdDay = row.getDdDay();
            BigDecimal currentMaxDdInRate = row.getMaxDdInRate();

            // 현재자본인하율(currentDrawdownRate)이 0인 경우
            if (currentDrawdownRate.compareTo(BigDecimal.ZERO) == 0) {
                if (prevDdDay != 0 && prevMaxDdInRate.compareTo(BigDecimal.ZERO) != 0) {
                    // 이전 값이 0이 아닌 경우만 누적
                    ddDaySum += prevDdDay;
                    maxDdInRateSum = maxDdInRateSum.add(prevMaxDdInRate);
                }
                // 이전 값 초기화
                prevDdDay = 0;
                prevMaxDdInRate = BigDecimal.ZERO;
            } else {
                // 자본인하율이 0 미만인 경우, 현재 값을 갱신
                prevDdDay = currentDdDay;
                prevMaxDdInRate = currentMaxDdInRate;
            }
        }

        ddDaySum += prevDdDay;
        maxDdInRateSum = maxDdInRateSum.add(prevMaxDdInRate);

        // ===== Step 2: 유효성 검사 =====
        if (maxDdInRateSum.compareTo(BigDecimal.ZERO) == 0 || ddDaySum == 0 || tradingDays <= 0) {
            // maxDdInRateSum, ddDaySum, 또는 tradingDays가 0 이하일 경우 KP Ratio는 0
            return BigDecimal.ZERO;
        }

        // ===== Step 3: KP Ratio 계산 =====
        // ddDay 평균 계산: ddDaySum / tradingDays
        BigDecimal ddDayAverage = BigDecimal.valueOf(ddDaySum)
                .divide(BigDecimal.valueOf(tradingDays), 10, RoundingMode.HALF_UP);

        // ddDay 평균의 제곱근 계산: sqrt(ddDayAverage)
        BigDecimal ddDaySqrt = BigDecimal.valueOf(Math.sqrt(ddDayAverage.doubleValue()));

        // 분모 계산: |maxDdInRateSum * sqrt(ddDaySum / tradingDays)|
        BigDecimal denominator = maxDdInRateSum
                .multiply(ddDaySqrt) // maxDdInRateSum * sqrt(ddDaySum / tradingDays)
                .abs(); // 절대값 적용

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            // 분모가 0이면 KP Ratio는 0
            return BigDecimal.ZERO;
        }

        // KP Ratio = 누적손익률 / 분모
        BigDecimal kpRatio = cumulativeProfitLossRate.divide(denominator, 11, RoundingMode.HALF_UP) // 소수점 11자리까지 계산
                .setScale(10, RoundingMode.HALF_UP); // 최종적으로 소수점 10자리로 반올림

        return kpRatio;
    }

    /**
     * KP-RATIO 평균, 분산, 표준편차 및 SM-SCORE를 계산하고 갱신하는 메서드.
     *
     * @param kpRatios 전략별 KP-RATIO 리스트
     * @return 전략별 SM-SCORE 리스트 (전략 ID와 SM-SCORE를 매핑)
     */
    public static Map<Long, BigDecimal> calculateAndUpdateSmScores(List<StrategyKpDto> kpRatios) {
        // 1. KP-RATIO 평균 계산
        BigDecimal kpMean = kpRatios.stream()
                .map(StrategyKpDto::getKpRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(kpRatios.size()), 10, RoundingMode.HALF_UP);

        // 2. 분산 및 표준편차 계산
        BigDecimal variance = kpRatios.stream()
                .map(dto -> dto.getKpRatio().subtract(kpMean).pow(2)) // (KP - 평균)^2
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(kpRatios.size()), 10, RoundingMode.HALF_UP);

        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(10, RoundingMode.HALF_UP); // 소수점 10자리까지 반올림

        // 3. 표준편차가 0인 경우 처리
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            // 모든 SM-SCORE를 0으로 설정
            return kpRatios.stream().collect(Collectors.toMap(
                    StrategyKpDto::getStrategyId,
                    dto -> BigDecimal.ZERO
            ));
        }

        // 3. SM-SCORE 계산 및 갱신
        Map<Long, BigDecimal> smScores = new HashMap<>();
        for (StrategyKpDto dto : kpRatios) {
            // Z-Score = (KP - 평균) / 표준편차
            BigDecimal zScore = dto.getKpRatio().subtract(kpMean).divide(stdDev, 10, RoundingMode.HALF_UP);

            // 표준정규누적분포값 계산
            double normDist = calculateNormDist(zScore.doubleValue()); // 표준정규누적분포

            // SM-SCORE = 표준정규누적분포값 * 100
            BigDecimal smScore = BigDecimal.valueOf(normDist * 100).setScale(2, RoundingMode.HALF_UP);

            // SM-SCORE 저장
            smScores.put(dto.getStrategyId(), smScore);
        }

        return smScores; // 전략 ID와 SM-SCORE를 매핑한 결과 반환
    }

    public static double calculateNormDist(double zScore) {
        NormalDistribution normalDist = new NormalDistribution(); // Apache Commons Math 라이브러리
        return normalDist.cumulativeProbability(zScore);
    }
}