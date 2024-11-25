package com.sysmatic2.finalbe.util;

import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class StatisticsCalculator {
    private StatisticsCalculator() {
        // 유틸리티 클래스는 인스턴스화 금지
    }

    /**
     * 변동계수(Coefficient of Variation, CV)를 계산하는 메서드.
     * @param dailyProfitLosses 오늘까지의 모든 일손익 데이터 리스트
     * @param averageProfit     기준일까지의 평균손익
     * @return 변동계수 (단위: %, 소수점 8자리까지 표시)
     */
    public static BigDecimal calculateCoefficientOfVariation(List<BigDecimal> dailyProfitLosses, BigDecimal averageProfit) {
        if (dailyProfitLosses == null || dailyProfitLosses.isEmpty()) {
            throw new IllegalArgumentException("일손익 데이터 리스트는 비어 있을 수 없습니다.");
        }
        if (averageProfit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 표준편차 계산
        BigDecimal mean = dailyProfitLosses.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyProfitLosses.size()), 9, BigDecimal.ROUND_HALF_UP); // 중간 계산은 9자리

        BigDecimal variance = dailyProfitLosses.stream()
                .map(profitLoss -> profitLoss.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyProfitLosses.size()), 9, BigDecimal.ROUND_HALF_UP); // 중간 계산은 9자리

        BigDecimal stdDevProfitLoss = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(9, RoundingMode.HALF_UP); // 표준편차 계산 후 소수점 9자리 반올림

        // 변동계수 계산
        return stdDevProfitLoss.divide(averageProfit, 9, BigDecimal.ROUND_HALF_UP) // 비율 계산 중간 단계는 9자리
                .multiply(BigDecimal.valueOf(100))
                .setScale(8, RoundingMode.HALF_UP); // 최종 결과는 8자리
    }

    /**
     * 승률 계산.
     * @param totalProfitDays 총 이익일수
     * @param tradingDays     총 거래일수
     * @return 승률 (단위: %)
     */
    public static BigDecimal calculateWinRate(int totalProfitDays, int tradingDays) {
        return tradingDays > 0
                ? BigDecimal.valueOf(totalProfitDays).divide(BigDecimal.valueOf(tradingDays), 4, BigDecimal.ROUND_HALF_UP)
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
        if (maxDrawdownAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 최대 자본인하 금액이 0이면 0 반환
        }

        // ROA 계산: (누적손익 / |최대 자본인하 금액|) * -1
        return cumulativeProfitLoss.divide(maxDrawdownAmount.abs(), 11, RoundingMode.HALF_UP) // 11번째 자리까지 계산
                .negate() // 음수 변환
                .setScale(10, RoundingMode.HALF_UP); // 10번째 자리까지 반올림
    }

    /**
     * Sharp Ratio 계산.
     * @param averageProfit   평균손익
     * @param stdDevProfitLoss 일손익의 표준편차
     * @return Sharp Ratio (소수점 10자리까지 표시)
     */
    public static BigDecimal calculateSharpRatio(BigDecimal averageProfit, BigDecimal stdDevProfitLoss) {
        return stdDevProfitLoss.compareTo(BigDecimal.ZERO) > 0
                ? averageProfit.divide(stdDevProfitLoss, 11, RoundingMode.HALF_UP) // 소수점 11자리로 계산 및 반올림
                .setScale(10, RoundingMode.HALF_UP) // 최종적으로 소수점 10자리 제한
                : BigDecimal.ZERO; // 표준편차가 0이면 0 반환
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
     * @return 입금액
     */
    public static BigDecimal calculateDepositAmount(BigDecimal depWdPrice) {
        return depWdPrice.compareTo(BigDecimal.ZERO) > 0 ? depWdPrice : BigDecimal.ZERO;
    }

    /**
     * 출금 계산.
     * @param depWdPrice 입출금
     * @return 출금액
     */
    public static BigDecimal calculateWithdrawAmount(BigDecimal depWdPrice) {
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
     * @return 계산된 기준가 (반올림 없이 소수점 네 번째 자리까지 표현)
     */
    public static BigDecimal calculateReferencePrice(BigDecimal balance, BigDecimal principal) {
        return principal.compareTo(BigDecimal.ZERO) > 0
                ? balance.divide(principal, 10, RoundingMode.DOWN) // 10자리까지 계산 후
                .multiply(BigDecimal.valueOf(1000))
                .setScale(4, RoundingMode.DOWN) // 최종적으로 4자리까지 설정
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
     * 표준편차(Standard Deviation)를 계산하는 메서드.
     * @param values 값들의 리스트 (예: 일손익 리스트)
     * @return 계산된 표준편차
     */
    public static BigDecimal calculateStdDev(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO; // 빈 리스트 기본값 처리
        }

        // 평균 계산
        BigDecimal mean = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 4, BigDecimal.ROUND_HALF_UP);

        // 분산 계산
        BigDecimal variance = values.stream()
                .map(value -> value.subtract(mean).pow(2)) // (X - 평균)^2
                .reduce(BigDecimal.ZERO, BigDecimal::add) // 분산 합산
                .divide(BigDecimal.valueOf(values.size()), 4, BigDecimal.ROUND_HALF_UP); // 분산 평균

        // 표준편차는 분산의 제곱근
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
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
     * 현재 자본인하율 계산.
     *
     * @param referencePrice       현재 기준가
     * @param allReferencePrices   지금까지의 모든 기준가 리스트 (등록된 순서대로 정렬)
     * @return 현재 자본인하율
     */
    public static BigDecimal calculateCurrentDrawdownRate(BigDecimal referencePrice, List<BigDecimal> allReferencePrices) {
        if (referencePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // 기준가가 0 이하일 경우 계산하지 않음
        }

        if (allReferencePrices == null || allReferencePrices.isEmpty()) {
            return BigDecimal.ZERO; // 기준가 데이터가 없을 경우
        }

        // 지금까지의 최대 기준가 계산
        BigDecimal maxReferencePriceSoFar = allReferencePrices.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO); // 최대값 없으면 0 반환

        if (referencePrice.compareTo(BigDecimal.valueOf(1000)) > 0) {
            // 현재 자본인하율 = (기준가 - max(이전 기준가, 현재 기준가)) / 기준가
            return referencePrice.subtract(maxReferencePriceSoFar)
                    .divide(referencePrice, 4, RoundingMode.HALF_UP); // 소수점 4자리까지 계산
        } else {
            return BigDecimal.ZERO; // 기준가가 1000 이하인 경우 0 반환
        }
    }

    /**
     * 일손익률 계산.
     * @param referencePrice 오늘의 기준가
     * @param previousReferencePrice 이전 기준가
     * @param isFirstEntry 첫 번째 등록 여부
     * @return 계산된 일손익률
     */
    public static BigDecimal calculateDailyPlRate(BigDecimal referencePrice, BigDecimal previousReferencePrice, boolean isFirstEntry) {
        if (isFirstEntry) {
            // 첫 번째 등록일 계산: (J3 - 1000) / 1000
            return referencePrice.subtract(BigDecimal.valueOf(1000))
                    .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP);
        } else if (previousReferencePrice.compareTo(BigDecimal.ZERO) > 0) {
            // 두 번째 이후 등록일 계산: (J4 - J3) / J3
            return referencePrice.subtract(previousReferencePrice)
                    .divide(previousReferencePrice, 4, BigDecimal.ROUND_HALF_UP);
        } else {
            // 이전 기준가가 0 이하인 경우, 0 반환
            return BigDecimal.ZERO;
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
     * 최대 일 손실률 계산 (백분율로 반환).
     * @param dailyPlRates 오늘까지의 모든 일손익률 리스트 (등록된 순서대로 정렬, 단위: %)
     * @param currentDailyPlRate 현재 입력되는 일손익률 (단위: %)
     * @return 최대 일 손실률 (백분율, 최소값 포함)
     */
    public static BigDecimal calculateMaxDailyLossRate(List<BigDecimal> dailyPlRates, BigDecimal currentDailyPlRate) {
        if (dailyPlRates == null || dailyPlRates.isEmpty()) {
            return currentDailyPlRate != null
                    ? currentDailyPlRate.multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO; // 현재 값만 있는 경우 백분율 변환하여 반환
        }

        if (currentDailyPlRate == null) {
            return dailyPlRates.stream()
                    .min(BigDecimal::compareTo) // 기존 리스트의 최소값 찾기
                    .map(minRate -> minRate.multiply(BigDecimal.valueOf(100))) // 백분율 변환
                    .orElse(BigDecimal.ZERO);
        }

        // 현재 입력 값 포함하여 최소값 계산 후 백분율 변환
        return dailyPlRates.stream()
                .min(BigDecimal::compareTo) // 리스트 최소값 찾기
                .map(minRate -> minRate.min(currentDailyPlRate).multiply(BigDecimal.valueOf(100))) // 최소값과 현재 값 비교 후 백분율 변환
                .orElse(currentDailyPlRate.multiply(BigDecimal.valueOf(100))); // 현재 값만 있는 경우 반환
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
     * @param previousMaxDDInRate 이전 최대 자본인하율
     * @param ddDay               현재 DD 기간 (1 이상이면 DD 기간 내 계산)
     * @return 계산된 DD 기간 내 최대 자본인하율
     */
    public static BigDecimal calculateMaxDDInRate(BigDecimal currentDrawdownRate, BigDecimal previousMaxDDInRate, int ddDay) {
        // DD 기간이 1 이상인 경우만 계산
        if (ddDay > 0) {
            return previousMaxDDInRate.min(currentDrawdownRate); // 이전 최대값과 현재 값 중 최저값 선택
        }
        // DD 기간이 0이면 0 반환
        return BigDecimal.ZERO;
    }
}
