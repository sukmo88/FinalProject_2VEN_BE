package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.DuplicateDateException;
import com.sysmatic2.finalbe.strategy.dto.*;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.strategy.common.StatisticsCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class DailyStatisticsService {
    private static final Pageable SINGLE_RESULT_PAGE = PageRequest.of(0, 1); // 기존 `PageRequest.of(0, 1)`를 대체
    private final DailyStatisticsRepository dsp;
    // TODO 이력테이블
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
        List<DailyStatisticsEntity> latestStatisticsList = dsp.findLatestStatisticsByStrategyId(strategyId, SINGLE_RESULT_PAGE);

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
     * 일일 통계 데이터를 수기 등록하는 메서드
     *
     * @param strategyId 전략 ID
     * @param reqDto     요청 데이터
     */
    @Transactional
    public void registerDailyStatistics(Long strategyId, DailyStatisticsReqDto reqDto) {

        // 전략 ID 유효성 검사
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID는 null일 수 없습니다.");
        }

        // 전략 존재 여부 확인
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy with ID " + strategyId + " does not exist."));

        // 1. 요청 날짜가 이미 존재하는지 확인
        if (dsp.existsByStrategyIdAndDate(strategyId, reqDto.getDate())) {
            throw new DuplicateDateException("이미 등록된 날짜입니다: " + reqDto.getDate());
        }

        // 2. 이전 데이터 및 이후 데이터 조회
        List<DailyStatisticsEntity> previousStates = dsp.findLatestBeforeDate(strategyId, reqDto.getDate(), SINGLE_RESULT_PAGE);
        List<DailyStatisticsEntity> afterStates = dsp.findOldestAfterDateList(strategyId, reqDto.getDate(), SINGLE_RESULT_PAGE);

        // 이전 데이터 가져오기 (존재하지 않으면 null)
        DailyStatisticsEntity previousState = previousStates.isEmpty() ? null : previousStates.get(0);

        // 이후 데이터 가져오기 (존재하지 않으면 null)
        DailyStatisticsEntity afterState = afterStates.isEmpty() ? null : afterStates.get(0);

        // 3. 등록하려는 데이터 저장 및 재계산
        boolean firstEntry = previousState == null; // 첫 번째 데이터 여부
        DailyStatisticsEntity newEntry = calculateDailyStatistics(
                strategyId,
                reqDto,
                firstEntry,
                Optional.ofNullable(previousState),
                strategyEntity
        );
        dsp.save(newEntry);

        // 4. 이후 데이터에 대한 영향 처리 (재계산)
        if (afterState != null) {
            // 등록한 날짜 이후의 데이터 가져오기
            List<DailyStatisticsEntity> affectedRows = dsp.findAllAfterDate(strategyId, afterState.getDate());

            // 등록한 데이터부터 이후 데이터를 삭제
            dsp.deleteFromDate(strategyId, afterState.getDate());

            // 이후 데이터를 재계산 및 저장
            recalculateAndSave(affectedRows, newEntry, strategyId);
        }
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

        // 3. 날짜 비교: 수정 전 날짜 vs 수정 후 날짜
        boolean isDateEarlier = targetData.getDate().isBefore(reqDto.getDate());

        // 직전 최신 데이터 조회
        List<DailyStatisticsEntity> previousDataList = dsp.findLatestBeforeDate(
                strategyId,
                isDateEarlier ? targetData.getDate() : reqDto.getDate(),
                SINGLE_RESULT_PAGE
        );
        // 직후 오래된 데이터 조회
        List<DailyStatisticsEntity> afterDataList = dsp.findOldestAfterDateList(
                strategyId,
                targetData.getDate(),
                SINGLE_RESULT_PAGE
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

        // 가장 뒤 데이터를 더 늦은 일자로 수정한 경우 해당 날짜 데이터만 수정하고 리턴
        if(afterData == null) return;

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
        affectedRows = dsp.findAllAfterDate(strategyId, fromDate);
        dsp.deleteFromDate(strategyId, fromDate);
        // 리스트 데이터 재등록 및 재계산
        recalculateAndSave(affectedRows, previousData, strategyId);
    }

    /**
     * 일간 분석 데이터를 삭제하고 이후 데이터를 재계산합니다.
     *
     * @param strategyId        삭제 대상이 포함된 전략의 ID
     * @param dailyStatisticsIds 삭제할 데이터 ID 리스트
     * @return 재계산된 데이터 개수
     */
    @Transactional
    public void deleteAndRecalculate(Long strategyId, List<Long> dailyStatisticsIds) {
        // 1. 삭제할 ID 리스트가 비어있는지 확인
        if (dailyStatisticsIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 ID 리스트가 비어 있습니다.");
        }

        // 2. 삭제 대상 ID 리스트의 모든 엔티티가 존재하는지 검증
        List<DailyStatisticsEntity> entitiesToDelete = dsp.findAllById(dailyStatisticsIds);

        if (entitiesToDelete.size() != dailyStatisticsIds.size()) {
            throw new IllegalArgumentException("삭제 대상 중 일부 데이터가 존재하지 않습니다.");
        }

        // 3. 삭제 대상(dailyStatisticsIds) 중 가장 오래된 날짜 찾기
        LocalDate oldestDateInIds = entitiesToDelete.stream()
                .map(DailyStatisticsEntity::getDate) // 날짜만 추출
                .min(LocalDate::compareTo) // 가장 오래된 날짜 찾기
                .orElseThrow(() -> new IllegalArgumentException("삭제 대상 데이터가 존재하지 않습니다."));

        // 4. 삭제 대상 이전 데이터 계산
        Optional<DailyStatisticsEntity> previousState = dsp.findPreviousStates(strategyId, oldestDateInIds, SINGLE_RESULT_PAGE)
                .getContent().stream().findFirst();

        // 5. 삭제 대상 데이터를 삭제
        dsp.deleteAllById(dailyStatisticsIds);

        // 6. 삭제 이후 재계산을 위한 다음 날짜 조회
        LocalDate nextDate;
        if (previousState.isPresent()) {
            nextDate = dsp.findNextDatesAfter(strategyId, previousState.get().getDate(), SINGLE_RESULT_PAGE)
                    .getContent().stream().findFirst()
                    .orElse(null);
        } else {
            // 이전 데이터가 없는 경우 삭제된 데이터의 가장 오래된 날짜 이후를 기준으로 설정
            nextDate = dsp.findNextDatesAfter(strategyId, oldestDateInIds, SINGLE_RESULT_PAGE)
                    .getContent().stream().findFirst()
                    .orElse(null);
        }

        // 7. nextDate가 없는 경우 바로 리턴
        if (nextDate == null) {
            return;
        }

        // 7. 기준일(포함) 이후 데이터를 조회
        List<DailyStatisticsEntity> entitiesAfterDeletion = dsp.findAllAfterDate(strategyId, nextDate);

        // 8. 기준일(포함) 이후 데이터 삭제
        dsp.deleteFromDate(strategyId, nextDate);

        // 9. 기준일(포함) 이후 데이터를 순회하며 재계산
        if (!entitiesAfterDeletion.isEmpty()) {
            recalculateAndSave(entitiesAfterDeletion, previousState.orElse(null), strategyId);
        }
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
        Integer previousCurrentConsecutivePlDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getCurrentConsecutivePlDays).orElse(0); // 이전 연속 손익일수
        Integer previousMaxConsecutiveProfitDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getMaxConsecutiveProfitDays).orElse(0); // 이전 최대 연속 수익일수
        Integer previousMaxConsecutiveLossDays = firstEntry ? 0 : previousState.map(DailyStatisticsEntity::getMaxConsecutiveLossDays).orElse(0); // 이전 최대 연속 손실일수
        BigDecimal previousMaxDdInRate = previousState
                .map(DailyStatisticsEntity::getMaxDdInRate)
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

        // 거래일수 = 일손익이 0이 아닌 경우 1 증가 -> 이전 거래일수 + 1
        Integer tradingDays = previousTradingDays + 1;
//        Integer tradingDays = StatisticsCalculator.calculateTradingDays(previousTradingDays, dailyProfitLoss);

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
        BigDecimal currentDrawdownAmount = cumulativeProfitLoss.compareTo(BigDecimal.ZERO) > 0
                ? cumulativeProfitLoss.subtract(maxCumulativeProfitLoss) // 누적손익 - 최대 누적손익
                : BigDecimal.ZERO; // 누적손익이 0보다 작거나 같으면 0

        // 최대 자본인하 금액 계산
        List<BigDecimal> drawdownAmounts = dsp.findAllDrawdownAmountsByStrategyId(strategyId);

        // 현재 자본인하 금액을 리스트에 추가
        drawdownAmounts.add(currentDrawdownAmount);

        // 전체 자본인하 금액 중 최소값 계산 후, 0과 비교해 최소값 반환
        BigDecimal maxDrawdownAmount = drawdownAmounts.stream()
                .min(BigDecimal::compareTo) // 전체 최소값
                .orElse(BigDecimal.ZERO)    // 리스트가 비어있으면 0 반환
                .min(BigDecimal.ZERO);      // 결과가 0보다 크면 0 반환

        // 현재 자본인하율 계산을 위한 데이터 조회
        List<BigDecimal> allReferencePrices = dsp.findAllReferencePricesByStrategyId(strategyId);
        // 현재 자본인하율 = (기준가 - max(이전 기준가, 현재 기준가)) / 기준가
        // - 기준가가 1000 초과인 경우 계산
        // - 기준가가 1000 이하이거나 데이터가 없는 경우 0 반환
        BigDecimal currentDrawdownRate = StatisticsCalculator.calculateCurrentDrawdownRate(referencePrice, allReferencePrices);

        // 최대 자본인하율 계산: 현재 자본인하율 포함 모든 자본인하율의 최소값
        List<BigDecimal> drawdownRates = dsp.findAllDrawdownRatesByStrategyId(strategyId);
        drawdownRates.add(currentDrawdownRate);

        BigDecimal maxDrawdownRate = drawdownRates.stream()
                .min(BigDecimal::compareTo) // 전체 최소값
                .orElse(BigDecimal.ZERO)    // 리스트가 비어있으면 0 반환
                .min(BigDecimal.ZERO)       // 결과가 0보다 크면 0 반환
                .setScale(4, RoundingMode.HALF_UP); // 소수점 4자리까지 반올림

        // 승률 = 이익일수 / 거래일수
        BigDecimal winRate = StatisticsCalculator.calculateWinRate(totalProfitDays, tradingDays);

        // Profit Factor = 총 이익 / |총 손실|
        BigDecimal profitFactor = StatisticsCalculator.calculateProfitFactor(totalProfit, totalLoss);

        // ROA = 누적손익 / 최대 자본인하 금액 * -1
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

        // 총 전략 운용일수 = 일간분석 첫 등록 일자와 마지막 등록 일자 범위
        // 가장 오래된 날짜와 가장 최신 날짜를 조회하여 계산
        Integer strategyOperationDays = dsp.findEarliestAndLatestDatesByStrategyId(strategyId)
                .map(dateRange -> {
                    LocalDate latestDate = dateRange.getLatestDate(); // DB에서 조회한 가장 최신 날짜 (null일 가능성 있음)
                    LocalDate currentDate = reqDto.getDate(); // 현재 입력 중인 데이터의 날짜

                    // 최신 날짜가 null인 경우 현재 입력 날짜를 사용
                    LocalDate adjustedLatestDate = (latestDate != null && latestDate.isAfter(currentDate))
                            ? latestDate
                            : currentDate; // 더 최신 날짜를 선택

                    // 가장 오래된 날짜와 최종 조정된 최신 날짜로 운용일수를 계산
                    return StatisticsCalculator.calculateStrategyOperationDays(
                            dateRange.getEarliestDate(), // 가장 오래된 날짜
                            adjustedLatestDate           // 조정된 최신 날짜
                    );
                })
                .orElse(1); // 데이터가 없으면 운용일수는 최소 1로 설정


        // 최근 1년 수익률 = ((오늘 기준가 / 1년 전 기준가) - 1) * 100
        List<BigDecimal> referencePrices = dsp.findReferencePricesOneYearAgo(strategyId, reqDto.getDate().minusYears(1));
        referencePrices.add(referencePrice);
        BigDecimal recentOneYearReturn = StatisticsCalculator.calculateRecentOneYearReturn(referencePrices);

        // 고점 이후 최대 하락 기간(dd_day) 계산
        Integer ddDay = StatisticsCalculator.calculateDdDay(
                currentDrawdownRate, // 현재 자본인하율
                previousState.map(DailyStatisticsEntity::getDdDay).orElse(0) // 이전 DD 기간
        );

        // maxDdInRate 계산
        BigDecimal maxDdInRate = StatisticsCalculator.calculateMaxDdInRate(
                currentDrawdownRate,    // 현재 자본인하율
                previousMaxDdInRate,    // 이전 maxDdInRate
                ddDay                  // 현재 DD 기간
        );

        // ddDay와 maxDdInRate 데이터를 날짜 오름차순으로 조회
        List<DdDayAndMaxDdInRate> ddDayAndMaxDdInRateList = dsp.findDdDayAndMaxDdInRateByStrategyIdOrderByDate(strategyId);
        // 현재 입력 데이터도 포함
        ddDayAndMaxDdInRateList.add(new DdDayAndMaxDdInRate(ddDay, maxDdInRate));

        // KP-RATIO 계산
        BigDecimal kpRatio = StatisticsCalculator.calculateKPRatio(
                ddDayAndMaxDdInRateList,   // ddDay 및 maxDdInRate 리스트
                currentDrawdownRate, // 현재자본인하율
                cumulativeProfitLossRate, // 누적손익률
                tradingDays               // 거래일수
        );
        // KP-RATIO 값에 따라 전략 테이블의 KP-RATIO와 SM-SCORE를 업데이트합니다.
        if (kpRatio.compareTo(BigDecimal.ZERO) == 0) {
            // KP-RATIO가 0인 경우, SM-SCORE도 0으로 업데이트
            strategyRepository.updateKpRatioAndSmScoreByStrategyId(strategyId, kpRatio, BigDecimal.ZERO);
        } else {
            // KP-RATIO만 업데이트
            strategyRepository.updateKpRatioByStrategyId(strategyId, kpRatio);
        }

        // SM-SCORE 배치 처리
        batchUpdateSmScores();

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
                .kpRatio(kpRatio)
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
                .maxDdInRate(maxDdInRate) // DD 기간 내 최대 자본인하율
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
                .strategyEntity(strategyEntity)
                .build();
    }

    /**
     * 배치로 SM-SCORE를 업데이트하는 메서드.
     *
     * - KP-RATIO가 0보다 큰 전략 데이터를 페이징 처리로 조회하여 SM-SCORE를 계산하고 갱신합니다.
     * - 트랜잭션 격리 수준은 Repeatable Read를 사용하여 데이터의 일관성을 보장합니다.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void batchUpdateSmScores() {
        // 페이징 처리 작업을 공통 로직으로 수행
        executePagedOperation(
                // 1. KP-RATIO가 0보다 큰 전략 데이터를 페이징 처리하여 조회
                page -> strategyRepository.findByNonZeroKpRatio(PageRequest.of(page, 1000)),
                // 2. 조회된 데이터를 기반으로 SM-SCORE 계산 및 업데이트
                kpRatiosPage -> {
                    if (kpRatiosPage.isEmpty()) {
                        return; // KP-RATIO가 없는 경우 기본값 유지
                    }

                    // 2.1. 조회된 전략 데이터(KP-RATIO 리스트)로 SM-SCORE 계산
                    List<StrategyKpDto> kpRatios = kpRatiosPage.getContent();

                    // 2.2. KP-RATIO의 데이터가 1개인 경우 모든 SM-SCORE를 0으로 처리
                    if (kpRatios.size() == 1) {
                        Long strategyId = kpRatios.get(0).getStrategyId();
                        strategyRepository.updateSmScoreByStrategyId(strategyId, BigDecimal.ZERO);
                        return;
                    }

                    // 2.3. 데이터가 2개 이상인 경우 SM-SCORE 계산
                    Map<Long, BigDecimal> smScores = StatisticsCalculator.calculateAndUpdateSmScores(kpRatios);

                    // 2.4. 계산된 SM-SCORE를 전략 테이블에 업데이트
                    smScores.forEach(strategyRepository::updateSmScoreByStrategyId);
                }
        );
    }

    /**
     * 일간 통계 데이터와 SM-SCORE를 갱신하는 메서드.
     *
     * - 전날 데이터가 없는 전략에 대해 일간 데이터를 생성하고 등록합니다.
     * - 모든 전략의 SM-SCORE를 갱신하고 일간 통계에 반영합니다.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateSmScoreInDailyStatistics() {
        // 1. 전날 날짜를 계산
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 2. 데이터가 없는 전략에 대해 일간 데이터를 생성 및 등록
        executePagedOperation(
                // 2.1. 전날 데이터가 없는 전략 ID를 페이징 처리하여 조회
                page -> dsp.findStrategyIdsWithoutDailyStatistics(yesterday, PageRequest.of(page, 1000)),
                // 2.2. 조회된 전략 ID에 대해 기본 일간 데이터 생성 및 등록
                strategyIdsPage -> strategyIdsPage.forEach(strategyId -> {
                    DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                            .date(yesterday)
                            .depWdPrice(BigDecimal.ZERO) // 입출금 금액 기본값
                            .dailyProfitLoss(BigDecimal.ZERO) // 일 손익 기본값
                            .build();
                    // 일간 데이터 등록
                    registerDailyStatistics(strategyId, reqDto);
                })
        );

        // 3. 모든 전략의 SM-SCORE를 갱신하고 일간 통계에 반영
        executePagedOperation(
                // 3.1. 모든 전략의 SM-SCORE 데이터를 페이징 처리하여 조회
                page -> strategyRepository.findAllStrategySmScores(PageRequest.of(page, 1000)),
                // 3.2. 조회된 SM-SCORE를 일간 통계에 업데이트
                smScorePage -> smScorePage.forEach(strategy -> {
                    Optional<DailyStatisticsEntity> existingRecord = dsp.findByStrategyIdAndDate(strategy.getStrategyId(), yesterday);

                    // 전날 데이터가 존재하면 SM-SCORE를 갱신
                    existingRecord.ifPresentOrElse(
                            record -> {
                                record.setSmScore(strategy.getSmScore());
                                dsp.save(record);
                            },
                            // 전날 데이터가 존재하지 않으면 예외를 발생시킴
                            () -> {
                                throw new IllegalStateException("전날 데이터 자동 등록에 실패했습니다. " +
                                        "Strategy ID: " + strategy.getStrategyId() + ", Date: " + yesterday);
                            }
                    );
                })
        );
    }

    /**
     * 페이징 처리를 포함한 반복 작업을 수행하는 공통 메서드.
     *
     * - 페이징 데이터를 조회(fetchPage)하고, 조회된 데이터를 처리(processPage)합니다.
     *
     * @param fetchPage  현재 페이지 번호를 입력받아 해당 페이지 데이터를 반환하는 함수
     * @param processPage 조회된 페이지 데이터를 처리하는 함수
     * @param <T>        페이징 데이터의 타입
     */
    private <T> void executePagedOperation(Function<Integer, Page<T>> fetchPage, Consumer<Page<T>> processPage) {
        int page = 0; // 초기 페이지 번호
        Page<T> pageData; // 현재 페이지 데이터

        do {
            // 1. 현재 페이지 데이터를 조회
            pageData = fetchPage.apply(page);

            // 2. 조회된 데이터를 처리
            processPage.accept(pageData);

            // 3. 다음 페이지로 이동
            page++;
        } while (!pageData.isLast()); // 마지막 페이지가 아니면 반복
    }
}