package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.common.MonthlyStatisticsCalculator;
import com.sysmatic2.finalbe.strategy.dto.MonthlyAnalysisResponseDto;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import com.sysmatic2.finalbe.util.CreatePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthlyStatisticsService {
    private final MonthlyStatisticsRepository monthlyStatisticsRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    /**
     * 월간 통계 데이터를 업데이트하는 메서드.
     *
     * @param strategyId      업데이트할 전략의 ID
     * @param dailyStatistics 해당 전략의 일간 통계 데이터
     */
    @Transactional
    public void updateMonthlyStatistics(Long strategyId, DailyStatisticsEntity dailyStatistics) {
        // 1. 현재 월과 이전 월의 YearMonth 객체 생성
        YearMonth currentMonth = YearMonth.from(dailyStatistics.getDate()); // 일간 통계 데이터의 날짜로 현재 월 추출
        YearMonth previousMonth = currentMonth.minusMonths(1); // 현재 월에서 1개월 차감해 이전 월 계산

        // 2. 월간 통계 데이터 가져오기 (없으면 새로 생성)
        MonthlyStatisticsEntity monthlyStatistics = MonthlyStatisticsCalculator.getOrCreateMonthlyStatistics(
                strategyId, dailyStatistics, currentMonth, monthlyStatisticsRepository);

        // 3. 월간 통계 계산
        // 3.1 월평균 원금 계산
        BigDecimal avgPrincipal = MonthlyStatisticsCalculator.calculateMonthlyAveragePrincipal(
                strategyId, dailyStatistics, currentMonth, dailyStatisticsRepository);
        monthlyStatistics.setMonthlyAvgPrincipal(avgPrincipal);

        // 3.2 월 입출금 총액 계산
        BigDecimal totalDepWd = MonthlyStatisticsCalculator.calculateTotalDepWdAmount(
                strategyId, dailyStatistics, currentMonth, dailyStatisticsRepository);
        monthlyStatistics.setMonthlyDepWdAmount(totalDepWd);

        // 3.3 월 손익 계산
        BigDecimal totalProfitLoss = MonthlyStatisticsCalculator.calculateTotalProfitLoss(
                strategyId, dailyStatistics, currentMonth, dailyStatisticsRepository);
        monthlyStatistics.setMonthlyProfitLoss(totalProfitLoss);

        // 3.4 월 손익률 계산
        BigDecimal monthlyReturn = MonthlyStatisticsCalculator.calculateMonthlyReturn(
                strategyId, currentMonth, dailyStatisticsRepository);
        monthlyStatistics.setMonthlyReturn(monthlyReturn);

        // 3.5 월 누적 손익 계산
        BigDecimal cumulativeProfitLoss = MonthlyStatisticsCalculator.calculateCumulativeProfitLoss(
                strategyId, dailyStatistics.getDailyProfitLoss(), monthlyStatisticsRepository);
        monthlyStatistics.setMonthlyCumulativeProfitLoss(cumulativeProfitLoss);

        // 3.6 월 누적 손익률 계산
        BigDecimal cumulativeReturn = MonthlyStatisticsCalculator.calculateCumulativeReturn(
                strategyId, currentMonth, dailyStatisticsRepository);
        monthlyStatistics.setMonthlyCumulativeReturn(cumulativeReturn);

        // 4. 업데이트된 월간 통계 데이터 저장
        monthlyStatisticsRepository.save(monthlyStatistics);
    }

    /**
     * 전략의 월간 분석 데이터를 페이징 처리하여 조회하는 메서드.
     *
     * @param strategyId 전략 ID
     * @param page       페이지 번호
     * @param pageSize   페이지 크기
     * @return 월간 분석 데이터가 담긴 페이징 응답 맵
     */
    public Map<String, Object> getMonthlyAnalysis(Long strategyId, int page, int pageSize) {
        // 1. 페이지 요청 생성
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        // 2. 레포지토리에서 월간 분석 데이터 조회
        Page<MonthlyStatisticsEntity> monthlyPage = monthlyStatisticsRepository.findByStrategyEntityStrategyIdOrderByAnalysisMonthDesc(strategyId, pageRequest);

        // 3. 데이터 변환
        Page<MonthlyAnalysisResponseDto> responsePage = monthlyPage.map(entity -> MonthlyAnalysisResponseDto.builder()
                .strategyMonthlyDataId(entity.getStrategyEntity().getStrategyId())
                .analysisMonth(entity.getAnalysisMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .monthlyAveragePrincipal(entity.getMonthlyAvgPrincipal())
                .monthlyDepWdAmount(entity.getMonthlyDepWdAmount())
                .monthlyPl(entity.getMonthlyProfitLoss())
                .monthlyReturn(entity.getMonthlyReturn())
                .monthlyCumulativePl(entity.getMonthlyCumulativeProfitLoss())
                .monthlyCumulativeReturn(entity.getMonthlyCumulativeReturn())
                .build());

        // 4. 페이지 응답 생성 유틸리티 활용
        return CreatePageResponse.createPageResponse(responsePage);
    }
}
