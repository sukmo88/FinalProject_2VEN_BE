package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.DuplicateDateException;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsResponseDto;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.util.StatisticsCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DailyStatisticsService {

    private final DailyStatisticsRepository dsp;
    private final DailyStatisticsHistoryRepository dshp;
    private final StrategyRepository strategyRepository;

    /**
     * 특정 전략의 통계 데이터를 조회합니다.
     *
     * @param strategyId 조회할 전략 ID
     * @return 전략 통계 데이터를 Map 형식으로 반환
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDailyStatistics(Long strategyId) {
        // 전략 존재 여부 확인
        Optional<StrategyEntity> strategyOpt = strategyRepository.findById(strategyId);
        if (strategyOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy with ID " + strategyId + " does not exist.");
        }

        // 최신 일간 통계 데이터 조회
        // 최신 일간 통계 데이터 조회
        Pageable pageable = PageRequest.of(0, 1); // 첫 번째 데이터만 요청
        List<DailyStatisticsEntity> latestStatisticsList = dsp.findLatestStatisticsByStrategyId(strategyId, pageable);

        if (latestStatisticsList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No daily statistics found for strategy with ID " + strategyId);
        }
        // 첫 번째 데이터 가져오기
        DailyStatisticsEntity latestStatistics = latestStatisticsList.get(0);


        // 최초 입력 일자 조회
        Optional<LocalDate> earliestDateOpt = dsp.findEarliestDateByStrategyId(strategyId);
        if (earliestDateOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data found for strategy with ID " + strategyId);
        }
        LocalDate startDate = earliestDateOpt.get();
        LocalDate endDate = latestStatistics.getDate();

        // 운용기간 계산
        long operationPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

        // Map 생성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("balance", latestStatistics.getBalance()); // 잔고
        response.put("cumulative_dep_wd_price", latestStatistics.getCumulativeDepWdPrice()); // 누적 입출금액
        response.put("principal", latestStatistics.getPrincipal()); // 원금
        response.put("operationPeriod", operationPeriod); // 운용 기간
        response.put("startDate", startDate); // 전략 시작일
        response.put("endDate", endDate); // 종료일
        response.put("cumulativeProfitLoss", latestStatistics.getCumulativeProfitLoss()); // 누적 손익 금액
        response.put("cumulativeProfitLossRate", latestStatistics.getCumulativeProfitLossRate()); // 누적 손익률
        response.put("maxCumulativeProfitLoss", latestStatistics.getMaxCumulativeProfitLoss()); // 최대 누적 손익 금액
        response.put("maxCumulativeProfitLossRatio", latestStatistics.getMaxCumulativeProfitLossRate()); // 최대 누적 손익률
        response.put("currentDrawdownAmount", latestStatistics.getCurrentDrawdownAmount()); // 현재 자본 인하 금액
        response.put("currentDrawdownRate", latestStatistics.getCurrentDrawdownRate()); // 현재 자본 인하율
        response.put("maxDrawdownAmount", latestStatistics.getMaxDrawdownAmount()); // 최대 자본 인하 금액
        response.put("maxDrawdownRate", latestStatistics.getMaxDrawdownRate()); // 최대 자본 인하율
        response.put("unrealizedProfitLoss", latestStatistics.getUnrealizedProfitLoss()); // 평가 손익
        response.put("averageProfitLossRate", latestStatistics.getAverageProfitLossRate()); // 평균 손익률
        response.put("maxDailyProfit", latestStatistics.getMaxDailyProfit()); // 최대 일 수익 금액
        response.put("maxDailyProfitRate", latestStatistics.getMaxDailyProfitRate()); // 최대 일 수익률
        response.put("maxDailyLoss", latestStatistics.getMaxDailyLoss()); // 최대 일 손실 금액
        response.put("maxDailyLossRate", latestStatistics.getMaxDailyLossRate()); // 최대 일 손실률
        response.put("tradingDays", latestStatistics.getTradingDays()); // 총 매매 일수
        response.put("totalProfitDays", latestStatistics.getTotalProfitDays()); // 총 이익 일수
        response.put("totalLossDays", latestStatistics.getTotalLossDays()); // 총 손실 일수
        response.put("currentConsecutivePlDays", latestStatistics.getCurrentConsecutivePlDays()); // 현재 연속 손익 일수
        response.put("maxConsecutiveProfitDays", latestStatistics.getMaxConsecutiveProfitDays()); // 최대 연속 이익 일수
        response.put("maxConsecutiveLossDays", latestStatistics.getMaxConsecutiveLossDays()); // 최대 연속 손실 일수
        response.put("winRate", latestStatistics.getWinRate()); // 승률
        response.put("daysSincePeak", latestStatistics.getDaysSincePeak()); // 고점 갱신 후 경과일
        response.put("profitFactor", latestStatistics.getProfitFactor()); // Profit Factor
        response.put("roa", latestStatistics.getRoa()); // ROA

        return response;
    }

    /**
     * 특정 전략의 일간 통계 데이터를 최신일자순으로 페이징하여 조회합니다.
     *
     * @param strategyId 전략 ID.
     * @param page       페이지 번호 (기본값: 0).
     * @param pageSize   페이지 크기 (기본값: 5).
     * @return 일간 통계 목록의 페이징된 결과.
     */
    public Page<DailyStatisticsResponseDto> getDailyStatisticsByStrategy(Long strategyId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<DailyStatisticsEntity> entities = dsp.findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, pageRequest);
        return entities.map(DailyStatisticsResponseDto::fromEntity); // 엔티티를 DTO로 변환
    }

    /**
     * 일일 통계 데이터를 처리하는 메서드
     *
     * @param strategyId 전략 ID
     * @param reqDto     요청 데이터
     */
    @Transactional
    public void processDailyStatistics(Long strategyId, DailyStatisticsReqDto reqDto) {
        if (dsp.existsByStrategyIdAndDate(strategyId, reqDto.getDate())) {
            throw new DuplicateDateException("이미 등록된 날짜입니다: " + reqDto.getDate());
        }
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID는 null일 수 없습니다.");
        }

        // 전략 존재 여부 확인
        Optional<StrategyEntity> strategy = strategyRepository.findById(strategyId);
        if (strategy.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy with ID " + strategyId + " does not exist.");
        }

        // 1. 이전 데이터 가져오기 (최신 데이터 1개)
        List<DailyStatisticsEntity> previousStates = dsp.findLatestByStrategyId(strategyId, PageRequest.of(0, 1));

        // 2. 첫 번째 데이터 여부 판단 후 처리
        DailyStatisticsEntity dailyStatistics;
        boolean firstEntry = previousStates.isEmpty(); // 첫 번째 데이터 여부 판단
        dailyStatistics = calculateDailyStatistics(
                strategyId,
                reqDto,
                firstEntry,
                firstEntry ? Optional.empty() : Optional.of(previousStates.get(0)),
                strategy.get()
        );

        // 3. 저장 처리
        dsp.save(dailyStatistics);
    }

    /**
     * 일간 통계 데이터를 수정하고 지표를 재계산합니다.
     *
     * @param strategyId   전략 ID
     * @param dailyDataId  수정할 데이터의 ID
     * @param reqDto       수정 요청 데이터
     */
    @Transactional
    public void updateDailyData(Long strategyId, Long dailyDataId, DailyStatisticsReqDto reqDto) {
        // 1. 수정 대상 데이터 조회
        DailyStatisticsEntity targetData = dsp.findById(dailyDataId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Daily data not found"));

        // 2. 수정 날짜 중복 확인
        if (dsp.existsByStrategyIdAndDate(strategyId, reqDto.getDate()) && !targetData.getDate().equals(reqDto.getDate())) {
            throw new DuplicateDateException("Date already exists: " + reqDto.getDate());
        }

//        System.out.println("Target Date: " + targetData.getDate());
//        System.out.println("Request Date: " + reqDto.getDate());
//        System.out.println("isDateEarlier: " + targetData.getDate().isBefore(reqDto.getDate()));
        // 3. 날짜 비교: 수정 전 날짜 vs 수정 후 날짜
        boolean isDateEarlier = targetData.getDate().isBefore(reqDto.getDate());

        // 직전 최신 데이터 조회
        Pageable pageable = PageRequest.of(0, 1); // 1개 데이터만 반환
        List<DailyStatisticsEntity> previousDataList = dsp.findLatestBeforeDate(
                strategyId,
                isDateEarlier ? targetData.getDate() : reqDto.getDate(),
                pageable
        );
        // 직후 오래된 데이터 조회
        List<DailyStatisticsEntity> afterDataList = dsp.findOldestAfterDate(
                strategyId,
                targetData.getDate(),
                pageable
        );

        // 가장 처음 데이터일 경우 null
        DailyStatisticsEntity previousData = previousDataList.isEmpty() ? null : previousDataList.get(0);
        // 가장 처음 데이터일 경우 null
        DailyStatisticsEntity afterData = afterDataList.isEmpty() ? null : afterDataList.get(0);

        // 수정된 데이터 업데이트
        targetData.setDate(reqDto.getDate());
        targetData.setDailyProfitLoss(reqDto.getDailyProfitLoss());
        targetData.setDepWdPrice(reqDto.getDepWdPrice());
        dsp.save(targetData);

        List<DailyStatisticsEntity> affectedRows = null;
        LocalDate fromDate = null; // 기준이 되는 날짜
        if (isDateEarlier) {
            // (1) 수정 전 날짜가 빠른 경우

            // 직전 최신 데이터부터 리스트 조회
            // 가장 처음 데이터면 현재 데이터부터 리스트 조회
            // 직전 최신 데이터부터 삭제
            // 가장 처음 데이터면 현재 데이터부터 삭제
            fromDate = afterData.getDate();
        } else {
            // (2) 수정 전 날짜가 느린 경우

            // 수정된 데이터부터 리스트 조회
            // 수정된 데이터부터 삭제 및 재등록
            fromDate = reqDto.getDate();
        }
        affectedRows = dsp.findFromDate(strategyId, fromDate);
        dsp.deleteFromDate(strategyId, fromDate);
        // 리스트 데이터 재등록 및 재계산
        recalculateAndSave(affectedRows, previousData, strategyId);
    }

    /**
     * 주어진 데이터 리스트를 재계산하여 저장합니다.
     *
     * @param affectedRows 수정 후 영향을 받는 데이터 리스트
     * @param previousData 직전 최신 데이터 (수정된 데이터 기준)
     * @param strategyId   전략 ID
     */
    private void recalculateAndSave(List<DailyStatisticsEntity> affectedRows, DailyStatisticsEntity previousData, Long strategyId) {
        Optional<DailyStatisticsEntity> previousState = Optional.ofNullable(previousData);

        // 전략 존재 여부 확인
        Optional<StrategyEntity> strategy = strategyRepository.findById(strategyId);
        if (strategy.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy with ID " + strategyId + " does not exist.");
        }

        for (DailyStatisticsEntity row : affectedRows) {
            // 기존 메서드에 맞춰 매개변수 가공
            DailyStatisticsReqDto reqDto = new DailyStatisticsReqDto(
                    row.getDate(),
                    row.getDepWdPrice(),
                    row.getDailyProfitLoss()
            );

            boolean firstEntry = previousData == null;

            DailyStatisticsEntity recalculatedData = calculateDailyStatistics(
                    strategyId,
                    reqDto,
                    firstEntry,
                    previousState,
                    strategy.get()
            );

            dsp.save(recalculatedData);
            previousState = Optional.of(recalculatedData); // 업데이트된 데이터를 다음 계산의 기준으로 사용
        }
    }

    /**
     * 일일 통계를 계산하는 메서드
     *
     * @param strategyId   전략 ID
     * @param reqDto       요청 데이터
     * @param firstEntry   첫 번째 데이터 여부
     * @param previousState 이전 상태 데이터
     * @param strategyEntity 전략 엔티티
     * @return 계산된 일일 통계 데이터 엔티티
     */
    @Transactional
    public DailyStatisticsEntity calculateDailyStatistics(
        Long strategyId,
        DailyStatisticsReqDto reqDto,
        boolean firstEntry,
        Optional<DailyStatisticsEntity> previousState,
        StrategyEntity strategyEntity) {

        // 이전 상태 가져오기
        // ===== 첫 번째 데이터 초기화 처리 =====
        BigDecimal previousBalance = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getBalance).orElse(BigDecimal.ZERO); // 이전 잔고
        BigDecimal previousCumulativeProfitLoss = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getCumulativeProfitLoss).orElse(BigDecimal.ZERO); // 이전 누적손익
        BigDecimal previousPrincipal = firstEntry ? reqDto.getDepWdPrice() : previousState.map(DailyStatisticsEntity::getPrincipal).orElse(BigDecimal.ZERO); // 이전 원금
        BigDecimal previousReferencePrice = firstEntry ? BigDecimal.valueOf(1000) : previousState.map(DailyStatisticsEntity::getReferencePrice).orElse(BigDecimal.ZERO); // 이전 기준가
        BigDecimal previousMaxCumulativeProfitLoss = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getMaxCumulativeProfitLoss).orElse(BigDecimal.ZERO); // 이전 최대 누적손익
        BigDecimal previousMaxCumulativeProfitLossRate = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getMaxCumulativeProfitLossRate).orElse(BigDecimal.ZERO); // 이전 최대 누적손익률
        BigDecimal previousMaxDrawdownAmount = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getMaxDrawdownAmount).orElse(BigDecimal.ZERO); // 이전 최대 자본인하 금액
        BigDecimal previousMaxDrawdownRate = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getMaxDrawdownRate).orElse(BigDecimal.ZERO); // 이전 최대 자본인하율
        Integer previousTradingDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getTradingDays).orElse(0); // 이전 거래일수
        Integer previousProfitDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getTotalProfitDays).orElse(0); // 이전 총 이익일수
        Integer previousLossDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getTotalLossDays).orElse(0); // 이전 총 손실일수
        BigDecimal previousTotalProfit = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getTotalProfit).orElse(BigDecimal.ZERO); // 이전 총 이익
        BigDecimal previousTotalLoss = firstEntry ? BigDecimal.ZERO : previousState.map(DailyStatisticsEntity::getTotalLoss).orElse(BigDecimal.ZERO); // 이전 총 손실
        Integer previousStrategyOperationDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getStrategyOperationDays).orElse(0); // 이전 전략 운용일수
        Integer previousCurrentConsecutivePlDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getCurrentConsecutivePlDays).orElse(0); // 이전 연속 손익일수
        Integer previousMaxConsecutiveProfitDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getMaxConsecutiveProfitDays).orElse(0); // 이전 최대 연속 수익일수
        Integer previousMaxConsecutiveLossDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getMaxConsecutiveLossDays).orElse(0); // 이전 최대 연속 손실일수
        BigDecimal previousMaxDDInRate = previousState
                .map(DailyStatisticsEntity::getMaxDDInRate)
                .orElse(BigDecimal.ZERO); // 이전 maxDDInRate 값 가져오기

        // 사용자 입력값
        BigDecimal dailyProfitLoss = reqDto.getDailyProfitLoss(); // 오늘의 일손익
        BigDecimal depWdPrice = reqDto.getDepWdPrice(); // 오늘의 입출금 금액

        /**
         * 계산된 지표들
         */

        // 잔고 = 이전 잔고 + 일손익 + 입출금
        BigDecimal balance = StatisticsCalculator.calculateBalance(previousBalance, dailyProfitLoss, depWdPrice);

        // 원금 = 이전 원금 + 입출금
        BigDecimal principal;
        if (firstEntry) {
            // 첫 데이터일 경우 입출금 금액만 반영
            principal = depWdPrice;
        } else {
            // 이전 원금 + 입출금 금액
            principal = StatisticsCalculator.calculatePrincipal(previousPrincipal, depWdPrice, previousBalance);
        }

        // 누적손익 = 이전 누적손익 + 일손익
        BigDecimal cumulativeProfitLoss = StatisticsCalculator.calculateCumulativeProfitLoss(previousCumulativeProfitLoss, dailyProfitLoss);

        // 거래일수 = 일손익이 0이 아닌 경우 1 증가
        Integer tradingDays = StatisticsCalculator.calculateTradingDays(previousTradingDays, dailyProfitLoss);

        // 손실일수 = 이전 손실일수 + (일손익 < 0 인 경우 1 증가)
        Integer totalLossDays = previousLossDays + (dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? 1 : 0);

        // 이익일수 = 이전 이익일수 + (일손익 > 0 인 경우 1 증가)
        Integer totalProfitDays = previousProfitDays + (dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);

        // 총 손실 = 이전 총 손실 + (일손익 < 0 인 경우 해당 일손익 추가)
        BigDecimal totalLoss = previousTotalLoss.add(dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? dailyProfitLoss : BigDecimal.ZERO);

        // 총 이익 = 이전 총 이익 + (일손익 > 0 인 경우 해당 일손익 추가)
        BigDecimal totalProfit = previousTotalProfit.add(dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0 ? dailyProfitLoss : BigDecimal.ZERO);

        // 평균 손실 = 총 손실 / 손실일수
        BigDecimal averageLoss = StatisticsCalculator.calculateAverageLoss(totalLoss, totalLossDays);

        // 평균 이익 = 총 이익 / 이익일수
        BigDecimal averageProfit = StatisticsCalculator.calculateAverageProfit(totalProfit, totalProfitDays);

        // 기준가 = (잔고 / 원금) * 1000
        BigDecimal referencePrice = StatisticsCalculator.calculateReferencePrice(balance, principal);

        // 이전 기준가가 null이거나 0 이하인지 확인하여 첫 번째 등록 여부 판단
        boolean isFirstEntry = previousReferencePrice == null || previousReferencePrice.compareTo(BigDecimal.ZERO) <= 0;

        // 일손익률 = (오늘 기준가 - 이전 기준가) / 이전 기준가 (첫 번째 등록 시 (기준가 - 1000) / 1000)
        BigDecimal dailyPlRate
                = StatisticsCalculator.calculateDailyPlRate(referencePrice,
                previousReferencePrice == null ? BigDecimal.ZERO : previousReferencePrice,
                isFirstEntry);

        // 누적손익률 = (기준가 / 1000) - 1
        BigDecimal cumulativeProfitLossRate = StatisticsCalculator.calculateCumulativeProfitLossRate(referencePrice);

        // 최대 누적 손익 = max(현재 누적손익, 이전 최대 누적손익)
        BigDecimal maxCumulativeProfitLoss = StatisticsCalculator.calculateMaxCumulativeProfitLoss(cumulativeProfitLoss, previousMaxCumulativeProfitLoss);

        // 최대 누적 손익률 = max(현재 누적 손익률, 이전 최대 누적 손익률)
        BigDecimal maxCumulativeProfitLossRate = cumulativeProfitLossRate.max(previousMaxCumulativeProfitLossRate);

        // 현재 자본인하 금액 = max(누적손익 - 최대 누적손익, 0)
        BigDecimal currentDrawdownAmount = cumulativeProfitLoss
                .subtract(maxCumulativeProfitLoss) // 누적손익 - 최대 누적손익
                .max(BigDecimal.ZERO);            // 결과가 0보다 작으면 0 반환

        // 최대 자본인하 금액 = min(현재 자본인하 금액, 이전 최대 자본인하 금액)
        BigDecimal maxDrawdownAmount = currentDrawdownAmount.min(previousMaxDrawdownAmount);

        // 현재 자본인하율 계산을 위한 데이터 조회
        List<BigDecimal> allReferencePrices = dsp.findAllReferencePricesByStrategyId(strategyId);
        // 현재 자본인하율 = (기준가 - max(이전 기준가, 현재 기준가)) / 기준가
        // - 기준가가 1000 초과인 경우 계산
        // - 기준가가 1000 이하이거나 데이터가 없는 경우 0 반환
        BigDecimal currentDrawdownRate = StatisticsCalculator.calculateCurrentDrawdownRate(referencePrice, allReferencePrices);

        // 최대 자본인하율 = min(현재 자본인하율, 이전 최대 자본인하율)
        BigDecimal maxDrawdownRate = currentDrawdownRate.min(previousMaxDrawdownRate);

        // 승률 = 이익일수 / 거래일수
        BigDecimal winRate = StatisticsCalculator.calculateWinRate(totalProfitDays, tradingDays);

        // Profit Factor = 총 이익 / |총 손실|
        BigDecimal profitFactor = StatisticsCalculator.calculateProfitFactor(totalProfit, totalLoss);

        // ROA = -누적손익 / |최대 자본인하 금액|
        BigDecimal roa = StatisticsCalculator.calculateROA(cumulativeProfitLoss, maxDrawdownAmount);

        // 평균 손익비 = 평균 이익 / |평균 손실|
        BigDecimal averageProfitLossRatio = averageLoss.compareTo(BigDecimal.ZERO) != 0
                ? averageProfit.divide(averageLoss.abs(), 11, RoundingMode.HALF_UP) // 11번째 자리까지 계산
                .setScale(10, RoundingMode.HALF_UP) // 10번째 자리로 반올림
                : BigDecimal.ZERO;

        // `dailyProfitLosses` 리스트 가져오기
        List<BigDecimal> dailyProfitLosses = dsp.findDailyProfitLossesByStrategyId(strategyId);
        // 현재 입력하려는 일손익도 추가
        dailyProfitLosses.add(reqDto.getDailyProfitLoss());

        // 평가손익 = 원금 - 잔고
        BigDecimal unrealizedProfitLoss = StatisticsCalculator.calculateUnrealizedProfitLoss(principal, balance);

        // 고점 후 경과일 = 현재 고점과 이전 고점 비교
        Integer daysSincePeak = StatisticsCalculator.calculateDaysSincePeak(maxCumulativeProfitLoss, previousMaxCumulativeProfitLoss, previousState.map(DailyStatisticsEntity::getDaysSincePeak).orElse(0));

        // 누적 입출금, 입금, 출금 계산
        BigDecimal cumulativeDepWdPrice = StatisticsCalculator.calculateCumulativeDepWd(
                dsp.findDepWdHistoryByStrategyId(strategyId), // 전략 ID를 기준으로 모든 입출금 내역 조회
                reqDto.getDepWdPrice()
        );

        BigDecimal depositAmount = StatisticsCalculator.calculateDepositAmount(depWdPrice, firstEntry); // 입금 = 오늘 입출금 금액이 양수인 경우
        BigDecimal cumulativeDepositAmount = StatisticsCalculator.calculateCumulativeDeposit(
                previousState.map(DailyStatisticsEntity::getCumulativeDepositAmount).orElse(BigDecimal.ZERO),
                depositAmount); // 누적 입금 = 이전 누적 입금 + 오늘 입금 금액

        BigDecimal withdrawAmount = StatisticsCalculator.calculateWithdrawAmount(depWdPrice, firstEntry); // 출금 = 오늘 입출금 금액이 음수인 경우
        BigDecimal cumulativeWithdrawAmount = StatisticsCalculator.calculateCumulativeWithdraw(
                previousState.map(DailyStatisticsEntity::getCumulativeWithdrawAmount).orElse(BigDecimal.ZERO),
                withdrawAmount); // 누적 출금 = 이전 누적 출금 + 오늘 출금 금액

        // 최대 일 이익 = max(이전 최대 일 이익, 오늘 일손익)
        BigDecimal maxDailyProfit = previousState.map(DailyStatisticsEntity::getMaxDailyProfit).orElse(BigDecimal.ZERO).max(dailyProfitLoss);


        // 1. 일 손익률 데이터 조회
        List<BigDecimal> dailyPlRates = dsp.findDailyPlRatesByStrategyId(strategyId);
        dailyPlRates.add(dailyPlRate);
        // 최대 일 이익률 = 일손익률 중 최대값. 음수면 0
        BigDecimal maxDailyProfitRate = StatisticsCalculator.calculateMaxDailyProfitRate(dailyPlRates);

        // 최대 일 손실 = min(이전 최대 일 손실, 오늘 일손익)
        BigDecimal maxDailyLoss = previousState.map(DailyStatisticsEntity::getMaxDailyLoss).orElse(BigDecimal.ZERO).min(dailyProfitLoss);

        // 최대 일 손실률 = MIN(일 손익률 리스트 중 최소 값, 0)
        // 2. 현재 일 손익률 포함하여 최대 일 손실률 계산
        // - 기존의 모든 일 손익률 데이터와 현재 입력된 일 손익률을 고려하여 최소값(최대 음수)을 반환합니다.
        // - 최대 일 손실률은 항상 음수 또는 0이어야 합니다.
        BigDecimal maxDailyLossRate = StatisticsCalculator.calculateMaxDailyLossRate(dailyPlRates, dailyPlRate);

        // 평균 손익 = (총 이익 + 총 손실) / 거래일수
        BigDecimal averageProfitLoss = tradingDays > 0
                ? totalProfit.add(totalLoss)
                .divide(BigDecimal.valueOf(tradingDays), 0, RoundingMode.HALF_UP) // 소수점 첫째 자리에서 반올림 후 정수 반환
                : BigDecimal.ZERO;

        // 평균 손익률 = (누적손익률 / 거래일수) * 100
        BigDecimal averageProfitLossRate = tradingDays > 0
                ? cumulativeProfitLossRate.divide(BigDecimal.valueOf(tradingDays), 10, RoundingMode.DOWN) // 중간 계산에서 높은 정밀도로 계산
                .setScale(4, RoundingMode.DOWN) // 최종적으로 4자리까지 표현 (반올림 없이)
                : BigDecimal.ZERO;

        // 변동계수(Coefficient of Variation) 계산
        // (일손익 합산의 표준편차) / 평균손익 * 100
        BigDecimal coefficientOfVariation = dailyProfitLosses.isEmpty()
                ? BigDecimal.ZERO
                : StatisticsCalculator.calculateCoefficientOfVariation(dailyProfitLosses, averageProfitLoss);

        // Sharp Ratio = 평균손익 / (일손익 합산의 표준편차)
        BigDecimal sharpRatio = StatisticsCalculator.calculateSharpRatio(dailyProfitLosses, averageProfitLoss);

        // 현재 연속 손익일수 계산
        Integer currentConsecutivePlDays;

        if (dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0) {
            // 이익인 경우: 손실에서 이익으로 전환 시 1로 초기화, 이익이 지속되면 +1
            currentConsecutivePlDays = previousCurrentConsecutivePlDays < 0 ? 1 : previousCurrentConsecutivePlDays + 1;
        } else if (dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0) {
            // 손실인 경우: 이익에서 손실로 전환 시 -1로 초기화, 손실이 지속되면 -1씩 감소
            currentConsecutivePlDays = previousCurrentConsecutivePlDays > 0 ? -1 : previousCurrentConsecutivePlDays - 1;
        } else {
            // 손익이 0일 경우 연속 손익일수 초기화
            currentConsecutivePlDays = 0;
        }

        // 최대 연속 수익일수 계산
        Integer maxConsecutiveProfitDays = dailyProfitLoss.compareTo(BigDecimal.ZERO) > 0
                ? Math.max(previousMaxConsecutiveProfitDays, currentConsecutivePlDays)
                : previousMaxConsecutiveProfitDays;

        // 최대 연속 손실일수 계산 (음수로 누적된 값의 절대값이 가장 큰 음수 선택)
        Integer maxConsecutiveLossDays = dailyProfitLoss.compareTo(BigDecimal.ZERO) < 0
                ? Math.min(previousMaxConsecutiveLossDays, currentConsecutivePlDays) // 음수에서 최솟값(더 작은 음수) 선택
                : previousMaxConsecutiveLossDays;

        // 총 전략 운용일수 = 이전 전략 운용일수 + 1
        Integer strategyOperationDays = previousStrategyOperationDays + 1;

        // 최근 1년 수익률 = ((오늘 기준가 / 1년 전 기준가) - 1) * 100
        List<BigDecimal> referencePrices = dsp.findReferencePricesOneYearAgo(strategyId, reqDto.getDate().minusYears(1));
        referencePrices.add(referencePrice);
        BigDecimal recentOneYearReturn = StatisticsCalculator.calculateRecentOneYearReturn(referencePrices);

        // 고점 이후 최대 하락 기간(dd_day) 계산
        Integer ddDay = StatisticsCalculator.calculateDdDay(
                currentDrawdownRate, // 현재 자본인하율
                previousState.map(DailyStatisticsEntity::getDdDay).orElse(0) // 이전 DD 기간
        );

        // maxDDInRate 계산
        BigDecimal maxDDInRate = StatisticsCalculator.calculateMaxDDInRate(
                currentDrawdownRate,    // 현재 자본인하율
                previousMaxDDInRate,    // 이전 maxDDInRate
                ddDay                  // 현재 DD 기간
        );

        // 누적손익 리스트 가져오기
        List<BigDecimal> cumulativeProfitLossHistory = dsp.findCumulativeProfitLossByStrategyId(strategyId);

        // 누적손익률 리스트 가져오기
        List<BigDecimal> cumulativeProfitLossRateHistory = dsp.findCumulativeProfitLossRateByStrategyId(strategyId);

        // 누적손익의 최대값 (Peak) 계산
        BigDecimal peak = StatisticsCalculator.calculatePeak(cumulativeProfitLossHistory, cumulativeProfitLoss);

        // 누적손익률의 최대값 (Peak Rate) 계산
        BigDecimal peakRate = StatisticsCalculator.calculatePeakRate(cumulativeProfitLossRateHistory, cumulativeProfitLossRate);


        // 빌더 패턴으로 결과 엔티티 생성
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
                .peak(peak)
                .peakRate(peakRate)
                .daysSincePeak(daysSincePeak)
                .ddDay(ddDay)
                .maxDDInRate(maxDDInRate) // DD 기간 내 최대 자본인하율
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
                .strategyEntity(strategyEntity)  // StrategyEntity 설정
                .build();
    }

    /**
     * 특정 전략의 가장 최근 팔로워 수를 조회합니다.
     *
     * @param strategyId 조회할 전략 ID
     * @return 최신 팔로워 수
     */
    @Transactional(readOnly = true)
    public Long getLatestFollowersCount(Long strategyId) {
        Pageable pageable = PageRequest.of(0, 1); // 최신 1개 데이터만 요청
        List<Long> latestFollowersCount = dsp.findLatestFollowersCountByStrategyId(strategyId, pageable);

        if (latestFollowersCount.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No followers count found for strategy with ID " + strategyId);
        }

        return latestFollowersCount.get(0); // 최신 팔로워 수 반환
    }
}