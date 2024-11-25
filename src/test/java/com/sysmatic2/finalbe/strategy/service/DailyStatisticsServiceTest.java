package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyStatisticsServiceTest {

    @InjectMocks
    private DailyStatisticsService dailyStatisticsService;

    @Mock
    private DailyStatisticsRepository dssp;

    @Mock
    private DailyStatisticsHistoryRepository dsshp;

    @Mock
    private StrategyRepository strategyRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("이전 상태가 있는 경우 일일 통계 계산 테스트")
    void testCalculateDailyStatistics_withPreviousState() {
        // Given: 이전 상태와 사용자 요청 데이터
        DailyStatisticsEntity previousState = DailyStatisticsEntity.builder()
                .balance(BigDecimal.valueOf(1200))
                .principal(BigDecimal.valueOf(1000))
                .cumulativeProfitLoss(BigDecimal.valueOf(200))
                .referencePrice(BigDecimal.valueOf(1100))
                .tradingDays(10)
                .totalProfitDays(6)
                .totalLossDays(4)
                .totalProfit(BigDecimal.valueOf(600))
                .totalLoss(BigDecimal.valueOf(-400))
                .maxCumulativeProfitLoss(BigDecimal.valueOf(250))
                .maxCumulativeProfitLossRate(BigDecimal.valueOf(0.25))
                .build();

        DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                .date(LocalDate.now())
                .dailyProfitLoss(BigDecimal.valueOf(150))
                .depWdPrice(BigDecimal.valueOf(50))
                .build();

        Long strategyId = 1L;

        // Mock: 데이터베이스에서 이전 상태 반환
        when(dssp.findLatestByStrategyId(eq(strategyId), any())).thenReturn(List.of(previousState));

        // Mock: StrategyEntity 조회
        StrategyEntity strategyEntity = new StrategyEntity();  // StrategyEntity 생성
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));

        // When: 서비스 메서드 호출
        DailyStatisticsEntity result = dailyStatisticsService.calculateDailyStatistics(
                strategyId,
                reqDto,
                false,
                Optional.of(previousState),
                strategyEntity
        );

        // Then: 계산 결과 검증
        assertNotNull(result, "결과 엔티티는 null이 아니어야 합니다.");
        assertEquals(BigDecimal.valueOf(1400), result.getBalance(), "잔고 계산 결과가 예상과 일치하지 않습니다.");
        assertEquals(BigDecimal.valueOf(1041.6667), result.getPrincipal(), "원금 계산 결과가 예상과 일치하지 않습니다.");
        assertEquals(BigDecimal.valueOf(350), result.getCumulativeProfitLoss(), "누적손익 계산 결과가 예상과 일치하지 않습니다.");
        assertEquals(BigDecimal.valueOf(1343.9999).setScale(4), result.getReferencePrice(), "기준가 계산 결과가 예상과 일치하지 않습니다.");

        // Mock 검증
        verify(dssp, never()).save(any()); // 이 테스트에서는 save가 호출되지 않아야 합니다.
    }

    @Test
    @DisplayName("이전 상태가 없는 경우 첫 번째 데이터 처리 테스트")
    void testCalculateDailyStatistics_withNoPreviousState() {
        // Given: 이전 상태가 없는 경우와 사용자 요청 데이터
        Long strategyId = 1L;
        DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                .date(LocalDate.now())
                .dailyProfitLoss(BigDecimal.valueOf(150))
                .depWdPrice(BigDecimal.valueOf(50)) // 입출금 금액
                .build();

        // Mock: 이전 상태 없음
        when(dssp.findLatestByStrategyId(eq(strategyId), any())).thenReturn(List.of());

        // Mock: StrategyEntity 조회
        StrategyEntity strategyEntity = new StrategyEntity();
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));

        // When: 서비스 메서드 호출
        DailyStatisticsEntity result = dailyStatisticsService.calculateDailyStatistics(
                strategyId,
                reqDto,
                true,
                Optional.empty(),
                strategyEntity
        );

        // Then: 초기 상태를 기준으로 계산 결과 검증
        assertNotNull(result, "결과 엔티티는 null이 아니어야 합니다.");
        assertEquals(BigDecimal.valueOf(50), result.getPrincipal(), "원금 계산 결과가 예상과 일치하지 않습니다."); // 원금 = 입출금 금액
    }

    @Test
    @DisplayName("일일 통계 처리 메서드 테스트")
    void testProcessDailyStatistics() {
        // Given: 사용자 요청 데이터
        Long strategyId = 1L;
        DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                .date(LocalDate.now())
                .dailyProfitLoss(BigDecimal.valueOf(150))
                .depWdPrice(BigDecimal.valueOf(50))
                .build();

        DailyStatisticsEntity previousState = DailyStatisticsEntity.builder()
                .balance(BigDecimal.valueOf(1200))
                .principal(BigDecimal.valueOf(1000))
                .build();

        // Mock: 이전 상태와 저장 동작 설정
        when(dssp.findLatestByStrategyId(eq(strategyId), any())).thenReturn(List.of(previousState));
        when(dssp.save(any(DailyStatisticsEntity.class))).thenReturn(previousState);

        // Mock: StrategyEntity 조회
        StrategyEntity strategyEntity = new StrategyEntity();  // StrategyEntity 생성
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));

        // When: 서비스 메서드 호출
        dailyStatisticsService.processDailyStatistics(strategyId, reqDto);

        // Then: 저장 동작 검증
        verify(dssp, times(1)).save(any(DailyStatisticsEntity.class));
    }

    @Test
    @DisplayName("모든 필드 계산 검증 테스트")
    void testCalculateDailyStatistics_allFieldsCalculation_withPreviousState() {
        // Given: 이전 상태와 사용자 요청 데이터
        DailyStatisticsEntity previousState = DailyStatisticsEntity.builder()
                .balance(BigDecimal.valueOf(1200))
                .principal(BigDecimal.valueOf(1000))
                .cumulativeProfitLoss(BigDecimal.valueOf(200))
                .referencePrice(BigDecimal.valueOf(1100))
                .totalProfitDays(6)
                .totalLossDays(4)
                .maxCumulativeProfitLoss(BigDecimal.valueOf(250))
                .build();

        DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                .date(LocalDate.now())
                .dailyProfitLoss(BigDecimal.valueOf(150))
                .depWdPrice(BigDecimal.valueOf(50))
                .build();

        Long strategyId = 1L;

        // Mock: StrategyEntity
        StrategyEntity strategyEntity = new StrategyEntity();
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));

        // When: 서비스 메서드 호출
        DailyStatisticsEntity result = dailyStatisticsService.calculateDailyStatistics(
                strategyId,
                reqDto,
                false,
                Optional.of(previousState),
                strategyEntity
        );

        // Then: 계산된 모든 필드 검증
        assertEquals(BigDecimal.valueOf(1400), result.getBalance(), "잔고 계산 오류");
        assertEquals(BigDecimal.valueOf(1041.6667), result.getPrincipal(), "원금 계산 오류");
        assertEquals(BigDecimal.valueOf(350), result.getCumulativeProfitLoss(), "누적손익 계산 오류");
        assertEquals(BigDecimal.valueOf(1343.9999).setScale(4, RoundingMode.HALF_UP), result.getReferencePrice(), "기준가 계산 오류");
        assertEquals(BigDecimal.valueOf(0.2218).setScale(4, RoundingMode.HALF_UP), result.getDailyPlRate(), "일손익률 계산 오류");
        assertEquals(BigDecimal.valueOf(34.4000).setScale(4, RoundingMode.HALF_UP), result.getCumulativeProfitLossRate(), "누적손익률 계산 오류");
    }

    @Test
    @DisplayName("전략 ID가 유효하지 않을 때 예외 처리 테스트")
    void testProcessDailyStatistics_strategyNotFound() {
        // Given: 잘못된 전략 ID
        Long strategyId = 999L;
        DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                .date(LocalDate.now())
                .dailyProfitLoss(BigDecimal.valueOf(150))
                .depWdPrice(BigDecimal.valueOf(50))
                .build();

        // Mock: 잘못된 StrategyEntity
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.empty());

        // When/Then: 예외 발생 검증
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            dailyStatisticsService.processDailyStatistics(strategyId, reqDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "적절한 상태 코드가 반환되지 않았습니다.");
    }

    @Test
    @DisplayName("첫 번째 데이터 입력 처리 테스트")
    void testProcessDailyStatistics_firstEntry() {
        // Given: 첫 번째 데이터
        Long strategyId = 1L;
        DailyStatisticsReqDto reqDto = DailyStatisticsReqDto.builder()
                .date(LocalDate.now())
                .dailyProfitLoss(BigDecimal.valueOf(150))
                .depWdPrice(BigDecimal.valueOf(50))
                .build();

        // Mock: 이전 상태 없음
        when(dssp.findLatestByStrategyId(eq(strategyId), any())).thenReturn(List.of());

        // Mock: StrategyEntity 조회
        StrategyEntity strategyEntity = new StrategyEntity();
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));

        // When: 서비스 호출
        dailyStatisticsService.processDailyStatistics(strategyId, reqDto);

        // Then: 저장 동작 검증
        verify(dssp, times(1)).save(argThat(savedEntity -> {
            assertEquals(BigDecimal.valueOf(50), savedEntity.getPrincipal(), "첫 번째 데이터 원금 오류");
            assertEquals(BigDecimal.valueOf(200), savedEntity.getBalance(), "첫 번째 데이터 잔고 오류");
            assertEquals(BigDecimal.valueOf(150), savedEntity.getCumulativeProfitLoss(), "첫 번째 데이터 누적손익 오류");
            return true;
        }));
    }
}