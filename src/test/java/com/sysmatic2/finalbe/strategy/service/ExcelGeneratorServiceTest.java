// ExcelGeneratorServiceTest.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelFileCreationException;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.util.ExcelGenerator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelGeneratorServiceTest {

  @Mock
  private DailyStatisticsRepository dailyStatisticsRepository;

  @Mock
  private MonthlyStatisticsRepository monthlyStatisticsRepository;

  @Mock
  private ExcelGenerator excelGenerator; // ExcelGenerator 모킹

  @InjectMocks
  private ExcelGeneratorService excelGeneratorService;

  private List<DailyStatisticsEntity> dailyStatistics;
  private List<MonthlyStatisticsEntity> monthlyStatistics;
  private StrategyEntity strategy;

  @BeforeEach
  void setUp() {
    // StrategyEntity 설정
    strategy = new StrategyEntity();
    strategy.setStrategyId(1L);
    strategy.setStrategyStatusCode("운용중");
    strategy.setIsPosted("Y");
    strategy.setIsApproved("P");
    strategy.setStrategyTitle("Test Strategy");
    strategy.setMinInvestmentAmount("1000000");
    strategy.setStrategyOverview("This is a test strategy.");
    strategy.setWriterId("admin");
    strategy.setWritedAt(LocalDate.now().atStartOfDay());

    // Sample DailyStatisticsEntity 데이터 설정
    DailyStatisticsEntity daily1 = new DailyStatisticsEntity();
    daily1.setDailyStatisticsId(1L);
    daily1.setStrategyEntity(strategy);
    daily1.setDate(LocalDate.of(2024, 4, 1));
    daily1.setDepWdPrice(new BigDecimal("1000.00"));
    daily1.setDailyProfitLoss(new BigDecimal("150.00"));
    daily1.setDailyPlRate(new BigDecimal("0.15"));
    daily1.setCumulativeProfitLoss(new BigDecimal("150.00"));
    daily1.setCumulativeProfitLossRate(new BigDecimal("0.15"));
    daily1.setKpRatio(new BigDecimal("1.2"));
    daily1.setSmScore(new BigDecimal("75.50"));
    daily1.setReferencePrice(new BigDecimal("10000.00"));
    daily1.setCumulativeDepWdPrice(new BigDecimal("5000.00"));
    daily1.setDepositAmount(new BigDecimal("1000.00"));
    daily1.setCumulativeDepositAmount(new BigDecimal("3000.00"));
    daily1.setWithdrawAmount(new BigDecimal("0.00"));
    daily1.setCumulativeWithdrawAmount(new BigDecimal("0.00"));
    daily1.setMaxDailyProfit(new BigDecimal("200.00"));
    daily1.setMaxDailyProfitRate(new BigDecimal("0.20"));
    daily1.setMaxDailyLoss(new BigDecimal("-50.00"));
    daily1.setMaxDailyLossRate(new BigDecimal("-0.05"));
    daily1.setTotalProfit(new BigDecimal("150.00"));
    daily1.setTotalProfitDays(1);
    daily1.setAverageProfit(new BigDecimal("150.00"));
    daily1.setTotalLoss(new BigDecimal("-50.00"));
    daily1.setTotalLossDays(1);
    daily1.setAverageLoss(new BigDecimal("-50.00"));
    daily1.setCumulativeProfitLossRate(new BigDecimal("0.15"));
    daily1.setMaxCumulativeProfitLoss(new BigDecimal("150.00"));
    daily1.setMaxCumulativeProfitLossRate(new BigDecimal("0.15"));
    daily1.setAverageProfitLoss(new BigDecimal("100.00"));
    daily1.setAverageProfitLossRate(new BigDecimal("0.10"));
    daily1.setPeak(new BigDecimal("150.00"));
    daily1.setPeakRate(new BigDecimal("0.15"));
    daily1.setDaysSincePeak(5);
    daily1.setCurrentDrawdownAmount(new BigDecimal("0.00"));
    daily1.setCurrentDrawdownRate(new BigDecimal("0.00"));
    daily1.setMaxDrawdownAmount(new BigDecimal("-50.00"));
    daily1.setMaxDrawdownRate(new BigDecimal("-0.05"));
    daily1.setWinRate(new BigDecimal("0.50"));
    daily1.setProfitFactor(new BigDecimal("3.00"));
    daily1.setRoa(new BigDecimal("0.02"));
    daily1.setAverageProfitLossRatio(new BigDecimal("3.00"));
    daily1.setCoefficientOfVariation(new BigDecimal("10.00"));
    daily1.setSharpRatio(new BigDecimal("1.50"));
    daily1.setCurrentConsecutivePlDays(1);
    daily1.setMaxConsecutiveProfitDays(3);
    daily1.setMaxConsecutiveLossDays(2);
    daily1.setRecentOneYearReturn(new BigDecimal("0.25"));
    daily1.setStrategyOperationDays(100);

    DailyStatisticsEntity daily2 = new DailyStatisticsEntity();
    daily2.setDailyStatisticsId(2L);
    daily2.setStrategyEntity(strategy);
    daily2.setDate(LocalDate.of(2024, 4, 2));
    daily2.setDepWdPrice(new BigDecimal("2000.00"));
    daily2.setDailyProfitLoss(new BigDecimal("250.00"));
    daily2.setDailyPlRate(new BigDecimal("0.25"));
    daily2.setCumulativeProfitLoss(new BigDecimal("400.00"));
    daily2.setCumulativeProfitLossRate(new BigDecimal("0.40"));
    daily2.setKpRatio(new BigDecimal("1.3"));
    daily2.setSmScore(new BigDecimal("80.00"));
    daily2.setReferencePrice(new BigDecimal("10500.00"));
    daily2.setCumulativeDepWdPrice(new BigDecimal("7000.00"));
    daily2.setDepositAmount(new BigDecimal("2000.00"));
    daily2.setCumulativeDepositAmount(new BigDecimal("5000.00"));
    daily2.setWithdrawAmount(new BigDecimal("0.00"));
    daily2.setCumulativeWithdrawAmount(new BigDecimal("0.00"));
    daily2.setMaxDailyProfit(new BigDecimal("250.00"));
    daily2.setMaxDailyProfitRate(new BigDecimal("0.25"));
    daily2.setMaxDailyLoss(new BigDecimal("-50.00"));
    daily2.setMaxDailyLossRate(new BigDecimal("-0.05"));
    daily2.setTotalProfit(new BigDecimal("400.00"));
    daily2.setTotalProfitDays(2);
    daily2.setAverageProfit(new BigDecimal("200.00"));
    daily2.setTotalLoss(new BigDecimal("-50.00"));
    daily2.setTotalLossDays(1);
    daily2.setAverageLoss(new BigDecimal("-50.00"));
    daily2.setCumulativeProfitLossRate(new BigDecimal("0.40"));
    daily2.setMaxCumulativeProfitLoss(new BigDecimal("400.00"));
    daily2.setMaxCumulativeProfitLossRate(new BigDecimal("0.40"));
    daily2.setAverageProfitLoss(new BigDecimal("175.00"));
    daily2.setAverageProfitLossRate(new BigDecimal("0.20"));
    daily2.setPeak(new BigDecimal("400.00"));
    daily2.setPeakRate(new BigDecimal("0.40"));
    daily2.setDaysSincePeak(3);
    daily2.setCurrentDrawdownAmount(new BigDecimal("0.00"));
    daily2.setCurrentDrawdownRate(new BigDecimal("0.00"));
    daily2.setMaxDrawdownAmount(new BigDecimal("-50.00"));
    daily2.setMaxDrawdownRate(new BigDecimal("-0.05"));
    daily2.setWinRate(new BigDecimal("0.60"));
    daily2.setProfitFactor(new BigDecimal("4.00"));
    daily2.setRoa(new BigDecimal("0.03"));
    daily2.setAverageProfitLossRatio(new BigDecimal("4.00"));
    daily2.setCoefficientOfVariation(new BigDecimal("12.00"));
    daily2.setSharpRatio(new BigDecimal("1.80"));
    daily2.setCurrentConsecutivePlDays(2);
    daily2.setMaxConsecutiveProfitDays(4);
    daily2.setMaxConsecutiveLossDays(1);
    daily2.setRecentOneYearReturn(new BigDecimal("0.30"));
    daily2.setStrategyOperationDays(100);

    dailyStatistics = Arrays.asList(daily1, daily2);

    // Sample MonthlyStatisticsEntity 데이터 설정
    MonthlyStatisticsEntity monthly1 = new MonthlyStatisticsEntity();
    monthly1.setMonthlyStatisticsId(1L);
    monthly1.setStrategyEntity(strategy);
    monthly1.setAnalysisMonth(YearMonth.of(2024, 4));
    monthly1.setMonthlyAvgPrincipal(new BigDecimal("15000.00"));
    monthly1.setMonthlyDepWdAmount(new BigDecimal("3000.00"));
    monthly1.setMonthlyProfitLoss(new BigDecimal("400.00"));
    monthly1.setMonthlyReturn(new BigDecimal("0.04"));
    monthly1.setMonthlyCumulativeProfitLoss(new BigDecimal("400.00"));
    monthly1.setMonthlyCumulativeReturn(new BigDecimal("0.04"));
    monthly1.setMonthlyAvgBalance(new BigDecimal("12000.00"));

    monthlyStatistics = Arrays.asList(monthly1);
  }

  @Test
  @DisplayName("일간 통계 엑셀 생성 성공 테스트")
  void testExportDailyStatisticsToExcel_Success() throws Exception {
    Long strategyId = 1L;
    int pageNumber = 0;
    int pageSize = 10;

    // 리포지토리 메서드 모킹
    Page<DailyStatisticsEntity> dailyStatsPage = new PageImpl<>(dailyStatistics);
    when(dailyStatisticsRepository.findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(dailyStatsPage);

    // ExcelGenerator Mock: 실제로 시트가 있는 Workbook 반환
    Workbook mockWorkbook = new XSSFWorkbook();
    mockWorkbook.createSheet("일간 통계"); // 시트 추가
    when(excelGenerator.generateDailyStatisticsExcel(dailyStatistics))
            .thenReturn(mockWorkbook);

    // 엑셀 생성 서비스 호출
    byte[] excelBytes = excelGeneratorService.exportDailyStatisticsToExcel(strategyId, false, pageNumber, pageSize);

    // 검증: 엑셀 바이트 배열이 null이 아니며 길이가 0보다 큼
    assertNotNull(excelBytes, "엑셀 바이트 배열이 null이어서는 안 됩니다.");
    assertTrue(excelBytes.length > 0, "엑셀 바이트 배열의 길이는 0보다 커야 합니다.");

    // 엑셀 파일의 내용 검증
    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
      org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("일간 통계");
      assertNotNull(sheet, "엑셀 시트가 존재해야 합니다.");
    } catch (IOException e) {
      fail("엑셀 파일 검증 중 예외 발생: " + e.getMessage());
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1))
            .findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize));

    // ExcelGenerator 호출 검증
    verify(excelGenerator, times(1)).generateDailyStatisticsExcel(dailyStatistics);
  }

  /**
   * 2. 월간 통계 엑셀 생성 테스트 - 성공
   */
  @Test
  @DisplayName("월간 통계 엑셀 생성 성공 테스트")
  void testExportMonthlyStatisticsToExcel_Success() throws Exception {
    Long strategyId = 1L;
    int pageNumber = 0;
    int pageSize = 10;

    // 리포지토리 메서드 모킹
    Page<MonthlyStatisticsEntity> monthlyStatsPage = new PageImpl<>(monthlyStatistics);
    when(monthlyStatisticsRepository.findByStrategyEntityStrategyIdOrderByAnalysisMonthAsc(strategyId, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(monthlyStatsPage);

    // ExcelGenerator Mock: 실제로 시트가 있는 Workbook 반환
    Workbook mockWorkbook = new XSSFWorkbook();
    mockWorkbook.createSheet("월간 통계"); // 시트 추가
    when(excelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics))
            .thenReturn(mockWorkbook);

    // 엑셀 생성 서비스 호출
    byte[] excelBytes = excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId, pageNumber, pageSize);

    // 검증: 엑셀 바이트 배열이 null이 아니며 길이가 0보다 큼
    assertNotNull(excelBytes, "엑셀 바이트 배열이 null이어서는 안 됩니다.");
    assertTrue(excelBytes.length > 0, "엑셀 바이트 배열의 길이는 0보다 커야 합니다.");

    // 엑셀 파일의 내용 검증
    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
      org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("월간 통계");
      assertNotNull(sheet, "엑셀 시트가 존재해야 합니다.");
    } catch (IOException e) {
      fail("엑셀 파일 검증 중 예외 발생: " + e.getMessage());
    }

    // 리포지토리 호출 검증
    verify(monthlyStatisticsRepository, times(1))
            .findByStrategyEntityStrategyIdOrderByAnalysisMonthAsc(strategyId, PageRequest.of(pageNumber, pageSize));

    // ExcelGenerator 호출 검증
    verify(excelGenerator, times(1)).generateMonthlyStatisticsExcel(monthlyStatistics);
  }

  /**
   * 3. 일간 분석 지표 엑셀 생성 테스트 - 성공
   */
  @Test
  @DisplayName("일간 분석 지표 엑셀 생성 성공 테스트")
  void testExportDailyAnalysisIndicatorsToExcel_Success() throws Exception {
    Long strategyId = 1L;
    int pageNumber = 0;
    int pageSize = 10;

    // 리포지토리 메서드 모킹: 전략 ID에 해당하는 일간 통계 페이징 반환
    Page<DailyStatisticsEntity> dailyStatsPage = new PageImpl<>(dailyStatistics);
    when(dailyStatisticsRepository.findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(dailyStatsPage);

    // ExcelGenerator 유틸리티 메서드 모킹
    Workbook realWorkbook = new XSSFWorkbook();
    realWorkbook.createSheet("일간 분석 지표"); // 시트를 추가하여 엑셀 데이터 준비
    when(excelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics)).thenReturn(realWorkbook);

    // 엑셀 생성 서비스 호출
    byte[] excelBytes = excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId, pageNumber, pageSize);

    // 검증: 엑셀 바이트 배열이 null이 아니며 길이가 0보다 큼
    assertNotNull(excelBytes, "엑셀 바이트 배열이 null이어서는 안 됩니다.");
    assertTrue(excelBytes.length > 0, "엑셀 바이트 배열의 길이는 0보다 커야 합니다.");

    // 엑셀 파일의 내용 검증
    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
      org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("일간 분석 지표");
      assertNotNull(sheet, "엑셀 시트가 존재해야 합니다.");
    } catch (IOException e) {
      fail("엑셀 파일 검증 중 예외 발생: " + e.getMessage());
    }

    // 리포지토리 메서드 호출 검증
    verify(dailyStatisticsRepository, times(1))
            .findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize));

    // ExcelGenerator 메서드 호출 검증
    verify(excelGenerator, times(1)).generateDailyAnalysisIndicatorsExcel(dailyStatistics);
  }


  /**
   * 4. 월간 통계 엑셀 생성 테스트 - 실패 (데이터 없음)
   */
  @Test
  @DisplayName("월간 통계 엑셀 생성 실패 테스트 - 데이터 없음")
  void testExportMonthlyStatisticsToExcel_NoData() {
    Long strategyId = 1L;
    int pageNumber = 0;
    int pageSize = 10;

    // 리포지토리 메서드 모킹: 빈 페이지 반환
    Page<MonthlyStatisticsEntity> emptyPage = new PageImpl<>(List.of());
    when(monthlyStatisticsRepository.findByStrategyEntityStrategyIdOrderByAnalysisMonthAsc(strategyId, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(emptyPage);

    // 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId, pageNumber, pageSize);
    });

    assertEquals("Strategy ID 1에 해당하는 월간 통계가 없습니다.", exception.getMessage(), "예외 메시지가 예상과 다릅니다.");

    // 리포지토리 메서드 호출 검증
    verify(monthlyStatisticsRepository, times(1))
            .findByStrategyEntityStrategyIdOrderByAnalysisMonthAsc(strategyId, PageRequest.of(pageNumber, pageSize));
  }

  /**
   * 5. 일간 통계 엑셀 생성 테스트 - 실패 (데이터 없음)
   */
  @Test
  @DisplayName("일간 통계 엑셀 생성 실패 테스트 - 데이터 없음")
  void testExportDailyStatisticsToExcel_NoData() {
    Long strategyId = 1L;
    boolean includeAnalysis = true;
    int pageNumber = 0;
    int pageSize = 10;

    // 리포지토리 메서드 모킹: 빈 페이지 반환
    Page<DailyStatisticsEntity> emptyPage = new PageImpl<>(List.of());
    when(dailyStatisticsRepository.findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(emptyPage);

    // 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis, pageNumber, pageSize);
    });

    assertEquals("Strategy ID 1에 해당하는 일간 통계가 없습니다.", exception.getMessage(), "예외 메시지가 예상과 다릅니다.");

    // 리포지토리 메서드 호출 검증
    verify(dailyStatisticsRepository, times(1))
            .findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize));
  }

  /**
   * 6. 일간 분석 지표 엑셀 생성 테스트 - 실패 (데이터 없음)
   */
  @Test
  @DisplayName("일간 분석 지표 엑셀 생성 실패 테스트 - 데이터 없음")
  void testExportDailyAnalysisIndicatorsToExcel_NoData() {
    Long strategyId = 1L;
    int pageNumber = 0;
    int pageSize = 10;

    // 리포지토리 메서드 모킹: 빈 페이지 반환
    Page<DailyStatisticsEntity> emptyPage = new PageImpl<>(List.of());
    when(dailyStatisticsRepository.findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(emptyPage);

    // 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId, pageNumber, pageSize);
    });

    assertEquals("Strategy ID 1에 해당하는 일간 분석 통계가 없습니다.", exception.getMessage(), "예외 메시지가 예상과 다릅니다.");

    // 리포지토리 메서드 호출 검증
    verify(dailyStatisticsRepository, times(1))
            .findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize));
  }
}
