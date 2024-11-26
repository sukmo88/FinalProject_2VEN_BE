// ExcelGeneratorServiceTest.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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

  @InjectMocks
  private ExcelGeneratorService excelGeneratorService;

  private List<DailyStatisticsEntity> dailyStatistics;
  private List<MonthlyStatisticsEntity> monthlyStatistics;

  @BeforeEach
  void setUp() {
    // Sample DailyStatisticsEntity 데이터 설정
    DailyStatisticsEntity daily1 = DailyStatisticsEntity.builder()
            .dailyStatisticsId(1L)
            .date(LocalDate.of(2024, 4, 1))
            .depWdPrice(new BigDecimal("1000.00"))
            .dailyProfitLoss(new BigDecimal("150.00"))
            .dailyPlRate(new BigDecimal("0.15"))
            .cumulativeProfitLoss(new BigDecimal("150.00"))
            .cumulativeProfitLossRate(new BigDecimal("0.15"))
            // ... 다른 필드도 설정 가능
            .kpRatio(new BigDecimal("1.2"))
            .smScore(new BigDecimal("75.50"))
            .referencePrice(new BigDecimal("10000.00"))
            .cumulativeDepWdPrice(new BigDecimal("5000.00"))
            .depositAmount(new BigDecimal("1000.00"))
            .cumulativeDepositAmount(new BigDecimal("3000.00"))
            .withdrawAmount(new BigDecimal("0.00"))
            .cumulativeWithdrawAmount(new BigDecimal("0.00"))
            .maxDailyProfit(new BigDecimal("200.00"))
            .maxDailyProfitRate(new BigDecimal("0.20"))
            .maxDailyLoss(new BigDecimal("-50.00"))
            .maxDailyLossRate(new BigDecimal("-0.05"))
            .totalProfit(new BigDecimal("150.00"))
            .totalProfitDays(1)
            .averageProfit(new BigDecimal("150.00"))
            .totalLoss(new BigDecimal("-50.00"))
            .totalLossDays(1)
            .averageLoss(new BigDecimal("-50.00"))
            .cumulativeProfitLossRate(new BigDecimal("0.15"))
            .maxCumulativeProfitLoss(new BigDecimal("150.00"))
            .maxCumulativeProfitLossRate(new BigDecimal("0.15"))
            .averageProfitLoss(new BigDecimal("100.00"))
            .averageProfitLossRate(new BigDecimal("0.10"))
            .peak(new BigDecimal("150.00"))
            .peakRate(new BigDecimal("0.15"))
            .daysSincePeak(5)
            .currentDrawdownAmount(new BigDecimal("0.00"))
            .currentDrawdownRate(new BigDecimal("0.00"))
            .maxDrawdownAmount(new BigDecimal("-50.00"))
            .maxDrawdownRate(new BigDecimal("-0.05"))
            .winRate(new BigDecimal("0.50"))
            .profitFactor(new BigDecimal("3.00"))
            .roa(new BigDecimal("0.02"))
            .averageProfitLossRatio(new BigDecimal("3.00"))
            .coefficientOfVariation(new BigDecimal("10.00"))
            .sharpRatio(new BigDecimal("1.50"))
            .currentConsecutivePlDays(1)
            .maxConsecutiveProfitDays(3)
            .maxConsecutiveLossDays(2)
            .recentOneYearReturn(new BigDecimal("0.25"))
            .strategyOperationDays(100)
            .build();

    DailyStatisticsEntity daily2 = DailyStatisticsEntity.builder()
            .dailyStatisticsId(2L)
            .date(LocalDate.of(2024, 4, 2))
            .depWdPrice(new BigDecimal("2000.00"))
            .dailyProfitLoss(new BigDecimal("250.00"))
            .dailyPlRate(new BigDecimal("0.25"))
            .cumulativeProfitLoss(new BigDecimal("400.00"))
            .cumulativeProfitLossRate(new BigDecimal("0.40"))
            // ... 다른 필드도 설정 가능
            .kpRatio(new BigDecimal("1.3"))
            .smScore(new BigDecimal("80.00"))
            .referencePrice(new BigDecimal("10500.00"))
            .cumulativeDepWdPrice(new BigDecimal("7000.00"))
            .depositAmount(new BigDecimal("2000.00"))
            .cumulativeDepositAmount(new BigDecimal("5000.00"))
            .withdrawAmount(new BigDecimal("0.00"))
            .cumulativeWithdrawAmount(new BigDecimal("0.00"))
            .maxDailyProfit(new BigDecimal("250.00"))
            .maxDailyProfitRate(new BigDecimal("0.25"))
            .maxDailyLoss(new BigDecimal("-50.00"))
            .maxDailyLossRate(new BigDecimal("-0.05"))
            .totalProfit(new BigDecimal("400.00"))
            .totalProfitDays(2)
            .averageProfit(new BigDecimal("200.00"))
            .totalLoss(new BigDecimal("-50.00"))
            .totalLossDays(1)
            .averageLoss(new BigDecimal("-50.00"))
            .cumulativeProfitLossRate(new BigDecimal("0.40"))
            .maxCumulativeProfitLoss(new BigDecimal("400.00"))
            .maxCumulativeProfitLossRate(new BigDecimal("0.40"))
            .averageProfitLoss(new BigDecimal("175.00"))
            .averageProfitLossRate(new BigDecimal("0.20"))
            .peak(new BigDecimal("400.00"))
            .peakRate(new BigDecimal("0.40"))
            .daysSincePeak(3)
            .currentDrawdownAmount(new BigDecimal("0.00"))
            .currentDrawdownRate(new BigDecimal("0.00"))
            .maxDrawdownAmount(new BigDecimal("-50.00"))
            .maxDrawdownRate(new BigDecimal("-0.05"))
            .winRate(new BigDecimal("0.60"))
            .profitFactor(new BigDecimal("4.00"))
            .roa(new BigDecimal("0.03"))
            .averageProfitLossRatio(new BigDecimal("4.00"))
            .coefficientOfVariation(new BigDecimal("12.00"))
            .sharpRatio(new BigDecimal("1.80"))
            .currentConsecutivePlDays(2)
            .maxConsecutiveProfitDays(4)
            .maxConsecutiveLossDays(1)
            .recentOneYearReturn(new BigDecimal("0.30"))
            .strategyOperationDays(100)
            .build();

    dailyStatistics = Arrays.asList(daily1, daily2);

    // Sample MonthlyStatisticsEntity 데이터 설정 (필요시 추가)
    // 예시로 빈 리스트를 사용합니다.
    monthlyStatistics = Arrays.asList();
  }

  @Test
  void testGenerateDailyStatisticsExcel() throws IOException {
    when(dailyStatisticsRepository.findAll()).thenReturn(dailyStatistics);

    ByteArrayInputStream in = excelGeneratorService.generateDailyStatisticsExcel();
    assertNotNull(in);

    // Apache POI를 사용하여 엑셀 내용을 검증
    try (Workbook workbook = new XSSFWorkbook(in)) {
      Sheet sheet = workbook.getSheet("일간 통계");
      assertNotNull(sheet);

      // 헤더 검증
      Row headerRow = sheet.getRow(0);
      assertNotNull(headerRow);
      String[] expectedHeaders = {"날짜", "입출금", "손익", "손익률", "누적손익", "누적손익률"};
      for (int i = 0; i < expectedHeaders.length; i++) {
        Cell cell = headerRow.getCell(i);
        assertNotNull(cell);
        assertEquals(expectedHeaders[i], cell.getStringCellValue());
      }

      // 데이터 검증
      for (int i = 0; i < dailyStatistics.size(); i++) {
        DailyStatisticsEntity stat = dailyStatistics.get(i);
        Row row = sheet.getRow(i + 1);
        assertNotNull(row);

        // 날짜
        Cell dateCell = row.getCell(0);
        assertNotNull(dateCell);
        assertEquals(stat.getDate().toString(), dateCell.getLocalDateTimeCellValue().toLocalDate().toString());

        // 입출금
        Cell depWdCell = row.getCell(1);
        assertNotNull(depWdCell);
        assertEquals(stat.getDepWdPrice().doubleValue(), depWdCell.getNumericCellValue());

        // 손익
        Cell plCell = row.getCell(2);
        assertNotNull(plCell);
        assertEquals(stat.getDailyProfitLoss().doubleValue(), plCell.getNumericCellValue());

        // 손익률
        Cell plRateCell = row.getCell(3);
        assertNotNull(plRateCell);
        assertEquals(stat.getDailyPlRate().doubleValue(), plRateCell.getNumericCellValue());

        // 누적손익
        Cell cumPlCell = row.getCell(4);
        assertNotNull(cumPlCell);
        assertEquals(stat.getCumulativeProfitLoss().doubleValue(), cumPlCell.getNumericCellValue());

        // 누적손익률
        Cell cumPlRateCell = row.getCell(5);
        assertNotNull(cumPlRateCell);
        assertEquals(stat.getCumulativeProfitLossRate().doubleValue(), cumPlRateCell.getNumericCellValue());
      }
    }

    verify(dailyStatisticsRepository, times(1)).findAll();
  }

  @Test
  void testGenerateMonthlyStatisticsExcel() throws IOException {
    when(monthlyStatisticsRepository.findAll()).thenReturn(monthlyStatistics);

    ByteArrayInputStream in = excelGeneratorService.generateMonthlyStatisticsExcel();
    assertNotNull(in);

    // Apache POI를 사용하여 엑셀 내용을 검증
    try (Workbook workbook = new XSSFWorkbook(in)) {
      Sheet sheet = workbook.getSheet("월간 통계");
      assertNotNull(sheet);

      // 헤더 검증
      Row headerRow = sheet.getRow(0);
      assertNotNull(headerRow);
      String[] expectedHeaders = {"날짜", "입출금", "손익", "손익률", "누적손익", "누적손익률"};
      for (int i = 0; i < expectedHeaders.length; i++) {
        Cell cell = headerRow.getCell(i);
        assertNotNull(cell);
        assertEquals(expectedHeaders[i], cell.getStringCellValue());
      }

      // 데이터 검증 (현재는 빈 리스트이므로 데이터 없음)
      assertEquals(0, sheet.getLastRowNum());
    }

    verify(monthlyStatisticsRepository, times(1)).findAll();
  }

  @Test
  void testGenerateDailyAnalysisIndicatorsExcel() throws IOException {
    when(dailyStatisticsRepository.findAll()).thenReturn(dailyStatistics);

    ByteArrayInputStream in = excelGeneratorService.generateDailyAnalysisIndicatorsExcel();
    assertNotNull(in);

    // Apache POI를 사용하여 엑셀 내용을 검증
    try (Workbook workbook = new XSSFWorkbook(in)) {
      Sheet sheet = workbook.getSheet("일간 분석 지표");
      assertNotNull(sheet);

      // 헤더 검증
      Row headerRow = sheet.getRow(0);
      assertNotNull(headerRow);
      String[] expectedHeaders = {
              "일자", "KP-Ratio", "SM-Score", "기준가", "누적입출금액", "입금", "누적입금액",
              "출금", "누적출금액", "일손익률", "최대일이익", "최대일이익률", "최대일손실",
              "최대일손실률", "총이익", "총이익일수", "평균이익", "총손실",
              "총손실일수", "평균손실", "누적손익", "누적손익률", "최대누적손익",
              "최대누적손익률", "평균손익", "평균손익률", "Peak", "Peak(%)",
              "고점후경과일", "현재자본인하금액", "현재자본인하율", "최대자본인하금액",
              "최대자본인하율", "승률", "Profit Factor", "ROA", "평균손익비",
              "변동계수", "Sharp Ratio", "현재 연속 손익일수",
              "최대 연속 수익일수", "최대 연속 손실일수", "최근 1년 수익률", "총전략운용일수"
      };
      for (int i = 0; i < expectedHeaders.length; i++) {
        Cell cell = headerRow.getCell(i);
        assertNotNull(cell);
        assertEquals(expectedHeaders[i], cell.getStringCellValue());
      }

      // 데이터 검증
      for (int i = 0; i < dailyStatistics.size(); i++) {
        DailyStatisticsEntity stat = dailyStatistics.get(i);
        Row row = sheet.getRow(i + 1);
        assertNotNull(row);

        int colIdx = 0;

        // 일자
        Cell dateCell = row.getCell(colIdx++);
        assertNotNull(dateCell);
        assertEquals(stat.getDate().toString(), dateCell.getLocalDateTimeCellValue().toLocalDate().toString());

        // KP-Ratio
        Cell kpRatioCell = row.getCell(colIdx++);
        assertNotNull(kpRatioCell);
        assertEquals(stat.getKpRatio().doubleValue(), kpRatioCell.getNumericCellValue());

        // SM-Score
        Cell smScoreCell = row.getCell(colIdx++);
        assertNotNull(smScoreCell);
        assertEquals(stat.getSmScore().doubleValue(), smScoreCell.getNumericCellValue());

        // 기준가
        Cell referencePriceCell = row.getCell(colIdx++);
        assertNotNull(referencePriceCell);
        assertEquals(stat.getReferencePrice().doubleValue(), referencePriceCell.getNumericCellValue());

        // 누적입출금액
        Cell cumulativeDepWdPriceCell = row.getCell(colIdx++);
        assertNotNull(cumulativeDepWdPriceCell);
        assertEquals(stat.getCumulativeDepWdPrice().doubleValue(), cumulativeDepWdPriceCell.getNumericCellValue());

        // 입금
        Cell depositAmountCell = row.getCell(colIdx++);
        assertNotNull(depositAmountCell);
        assertEquals(stat.getDepositAmount().doubleValue(), depositAmountCell.getNumericCellValue());

        // 누적입금액
        Cell cumulativeDepositAmountCell = row.getCell(colIdx++);
        assertNotNull(cumulativeDepositAmountCell);
        assertEquals(stat.getCumulativeDepositAmount().doubleValue(), cumulativeDepositAmountCell.getNumericCellValue());

        // 출금
        Cell withdrawAmountCell = row.getCell(colIdx++);
        assertNotNull(withdrawAmountCell);
        assertEquals(stat.getWithdrawAmount().doubleValue(), withdrawAmountCell.getNumericCellValue());

        // 누적출금액
        Cell cumulativeWithdrawAmountCell = row.getCell(colIdx++);
        assertNotNull(cumulativeWithdrawAmountCell);
        assertEquals(stat.getCumulativeWithdrawAmount().doubleValue(), cumulativeWithdrawAmountCell.getNumericCellValue());

        // 일손익률
        Cell dailyPlRateCell = row.getCell(colIdx++);
        assertNotNull(dailyPlRateCell);
        assertEquals(stat.getDailyPlRate().doubleValue(), dailyPlRateCell.getNumericCellValue());

        // 최대일이익
        Cell maxDailyProfitCell = row.getCell(colIdx++);
        assertNotNull(maxDailyProfitCell);
        assertEquals(stat.getMaxDailyProfit().doubleValue(), maxDailyProfitCell.getNumericCellValue());

        // 최대일이익률
        Cell maxDailyProfitRateCell = row.getCell(colIdx++);
        assertNotNull(maxDailyProfitRateCell);
        assertEquals(stat.getMaxDailyProfitRate().doubleValue(), maxDailyProfitRateCell.getNumericCellValue());

        // 최대일손실
        Cell maxDailyLossCell = row.getCell(colIdx++);
        assertNotNull(maxDailyLossCell);
        assertEquals(stat.getMaxDailyLoss().doubleValue(), maxDailyLossCell.getNumericCellValue());

        // 최대일손실률
        Cell maxDailyLossRateCell = row.getCell(colIdx++);
        assertNotNull(maxDailyLossRateCell);
        assertEquals(stat.getMaxDailyLossRate().doubleValue(), maxDailyLossRateCell.getNumericCellValue());

        // 총이익
        Cell totalProfitCell = row.getCell(colIdx++);
        assertNotNull(totalProfitCell);
        assertEquals(stat.getTotalProfit().doubleValue(), totalProfitCell.getNumericCellValue());

        // 총이익일수
        Cell totalProfitDaysCell = row.getCell(colIdx++);
        assertNotNull(totalProfitDaysCell);
        assertEquals(stat.getTotalProfitDays(), (int) totalProfitDaysCell.getNumericCellValue());

        // 평균이익
        Cell averageProfitCell = row.getCell(colIdx++);
        assertNotNull(averageProfitCell);
        assertEquals(stat.getAverageProfit().doubleValue(), averageProfitCell.getNumericCellValue());

        // 총손실
        Cell totalLossCell = row.getCell(colIdx++);
        assertNotNull(totalLossCell);
        assertEquals(stat.getTotalLoss().doubleValue(), totalLossCell.getNumericCellValue());

        // 총손실일수
        Cell totalLossDaysCell = row.getCell(colIdx++);
        assertNotNull(totalLossDaysCell);
        assertEquals(stat.getTotalLossDays(), (int) totalLossDaysCell.getNumericCellValue());

        // 평균손실
        Cell averageLossCell = row.getCell(colIdx++);
        assertNotNull(averageLossCell);
        assertEquals(stat.getAverageLoss().doubleValue(), averageLossCell.getNumericCellValue());

        // 누적손익
        Cell cumulativeProfitLossCell = row.getCell(colIdx++);
        assertNotNull(cumulativeProfitLossCell);
        assertEquals(stat.getCumulativeProfitLoss().doubleValue(), cumulativeProfitLossCell.getNumericCellValue());

        // 누적손익률
        Cell cumulativeProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(cumulativeProfitLossRateCell);
        assertEquals(stat.getCumulativeProfitLossRate().doubleValue(), cumulativeProfitLossRateCell.getNumericCellValue());

        // 최대누적손익
        Cell maxCumulativeProfitLossCell = row.getCell(colIdx++);
        assertNotNull(maxCumulativeProfitLossCell);
        assertEquals(stat.getMaxCumulativeProfitLoss().doubleValue(), maxCumulativeProfitLossCell.getNumericCellValue());

        // 최대누적손익률
        Cell maxCumulativeProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(maxCumulativeProfitLossRateCell);
        assertEquals(stat.getMaxCumulativeProfitLossRate().doubleValue(), maxCumulativeProfitLossRateCell.getNumericCellValue());

        // 평균손익
        Cell averageProfitLossCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossCell);
        assertEquals(stat.getAverageProfitLoss().doubleValue(), averageProfitLossCell.getNumericCellValue());

        // 평균손익률
        Cell averageProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossRateCell);
        assertEquals(stat.getAverageProfitLossRate().doubleValue(), averageProfitLossRateCell.getNumericCellValue());

        // Peak
        Cell peakCell = row.getCell(colIdx++);
        assertNotNull(peakCell);
        assertEquals(stat.getPeak().doubleValue(), peakCell.getNumericCellValue());

        // Peak(%)
        Cell peakRateCell = row.getCell(colIdx++);
        assertNotNull(peakRateCell);
        assertEquals(stat.getPeakRate().doubleValue(), peakRateCell.getNumericCellValue());

        // 고점후경과일
        Cell daysSincePeakCell = row.getCell(colIdx++);
        assertNotNull(daysSincePeakCell);
        assertEquals(stat.getDaysSincePeak(), (int) daysSincePeakCell.getNumericCellValue());

        // 현재자본인하금액
        Cell currentDrawdownAmountCell = row.getCell(colIdx++);
        assertNotNull(currentDrawdownAmountCell);
        assertEquals(stat.getCurrentDrawdownAmount().doubleValue(), currentDrawdownAmountCell.getNumericCellValue());

        // 현재자본인하율
        Cell currentDrawdownRateCell = row.getCell(colIdx++);
        assertNotNull(currentDrawdownRateCell);
        assertEquals(stat.getCurrentDrawdownRate().doubleValue(), currentDrawdownRateCell.getNumericCellValue());

        // 최대자본인하금액
        Cell maxDrawdownAmountCell = row.getCell(colIdx++);
        assertNotNull(maxDrawdownAmountCell);
        assertEquals(stat.getMaxDrawdownAmount().doubleValue(), maxDrawdownAmountCell.getNumericCellValue());

        // 최대자본인하율
        Cell maxDrawdownRateCell = row.getCell(colIdx++);
        assertNotNull(maxDrawdownRateCell);
        assertEquals(stat.getMaxDrawdownRate().doubleValue(), maxDrawdownRateCell.getNumericCellValue());

        // 승률
        Cell winRateCell = row.getCell(colIdx++);
        assertNotNull(winRateCell);
        assertEquals(stat.getWinRate().doubleValue(), winRateCell.getNumericCellValue());

        // Profit Factor
        Cell profitFactorCell = row.getCell(colIdx++);
        assertNotNull(profitFactorCell);
        assertEquals(stat.getProfitFactor().doubleValue(), profitFactorCell.getNumericCellValue());

        // ROA
        Cell roaCell = row.getCell(colIdx++);
        assertNotNull(roaCell);
        assertEquals(stat.getRoa().doubleValue(), roaCell.getNumericCellValue());

        // 평균손익비
        Cell averageProfitLossRatioCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossRatioCell);
        assertEquals(stat.getAverageProfitLossRatio().doubleValue(), averageProfitLossRatioCell.getNumericCellValue());

        // 변동계수
        Cell coefficientOfVariationCell = row.getCell(colIdx++);
        assertNotNull(coefficientOfVariationCell);
        assertEquals(stat.getCoefficientOfVariation().doubleValue(), coefficientOfVariationCell.getNumericCellValue());

        // Sharp Ratio
        Cell sharpRatioCell = row.getCell(colIdx++);
        assertNotNull(sharpRatioCell);
        assertEquals(stat.getSharpRatio().doubleValue(), sharpRatioCell.getNumericCellValue());

        // 현재 연속 손익일수
        Cell currentConsecutivePlDaysCell = row.getCell(colIdx++);
        assertNotNull(currentConsecutivePlDaysCell);
        assertEquals(stat.getCurrentConsecutivePlDays(), (int) currentConsecutivePlDaysCell.getNumericCellValue());

        // 최대 연속 수익일수
        Cell maxConsecutiveProfitDaysCell = row.getCell(colIdx++);
        assertNotNull(maxConsecutiveProfitDaysCell);
        assertEquals(stat.getMaxConsecutiveProfitDays(), (int) maxConsecutiveProfitDaysCell.getNumericCellValue());

        // 최대 연속 손실일수
        Cell maxConsecutiveLossDaysCell = row.getCell(colIdx++);
        assertNotNull(maxConsecutiveLossDaysCell);
        assertEquals(stat.getMaxConsecutiveLossDays(), (int) maxConsecutiveLossDaysCell.getNumericCellValue());

        // 최근 1년 수익률
        Cell recentOneYearReturnCell = row.getCell(colIdx++);
        assertNotNull(recentOneYearReturnCell);
        assertEquals(stat.getRecentOneYearReturn().doubleValue(), recentOneYearReturnCell.getNumericCellValue());

        // 총전략운용일수
        Cell strategyOperationDaysCell = row.getCell(colIdx++);
        assertNotNull(strategyOperationDaysCell);
        assertEquals(stat.getStrategyOperationDays(), (int) strategyOperationDaysCell.getNumericCellValue());
      }
    }

    verify(dailyStatisticsRepository, times(1)).findAll();
  }
}
