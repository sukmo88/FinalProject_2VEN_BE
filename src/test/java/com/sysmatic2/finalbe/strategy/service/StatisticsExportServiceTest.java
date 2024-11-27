// StatisticsExportServiceTest.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelFileCreationException;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsExportServiceTest {

  @Mock
  private DailyStatisticsRepository dailyStatisticsRepository;

  @Mock
  private MonthlyStatisticsRepository monthlyStatisticsRepository;

  @InjectMocks
  private StatisticsExportService statisticsExportService;

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
    // 필요한 경우 다른 필드도 설정

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
    // 필요한 경우 다른 필드도 설정

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
    // 필요한 경우 다른 필드도 설정

    dailyStatistics = Arrays.asList(daily1, daily2);

    // Sample MonthlyStatisticsEntity 데이터 설정 (필요시 추가)
    // 예시로 빈 리스트를 사용합니다.
    monthlyStatistics = Arrays.asList();
  }

  /**
   * 1. 일간 통계 엑셀 생성 테스트 - 성공
   */
  @Test
  void testExportDailyStatisticsToExcel_Success() {
    Long strategyId = 1L;
    boolean includeAnalysis = true;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // 서비스 메서드 호출
    byte[] excelBytes = statisticsExportService.exportDailyStatisticsToExcel(strategyId, includeAnalysis);
    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0);

    // Apache POI를 사용하여 엑셀 내용을 검증
    try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excelBytes))) {
      org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("일간 통계");
      assertNotNull(sheet);

      // 헤더 검증
      org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
      assertNotNull(headerRow);
      String[] expectedHeaders = {
              "전략 일간 통계 ID", "전략 ID", "일자", "일손익", "입출금", "거래일수", "원금",
              "잔고", "평가손익", "KP-Ratio", "SM-Score", "기준가", "누적입출금액",
              "입금", "누적입금액", "출금", "누적출금액", "일손익률",
              "최대일이익", "최대일이익률", "최대일손실", "최대일손실률", "총이익",
              "총이익일수", "평균이익", "총손실", "총손실일수", "평균손실",
              "누적손익", "누적손익률", "최대누적손익", "최대누적손익률",
              "평균손익", "평균손익률", "Peak", "Peak(%)", "고점후경과일",
              "현재자본인하금액", "현재자본인하율", "최대자본인하금액",
              "최대자본인하율", "승률", "Profit Factor", "ROA",
              "평균손익비", "변동계수", "Sharp Ratio",
              "현재 연속 손익일수", "최대 연속 수익일수",
              "최대 연속 손실일수", "최근 1년 수익률", "총전략운용일수",
              "DD 기간", "DD기간 내 최대 자본인하율", "팔로워수"
      };
      for (int i = 0; i < expectedHeaders.length; i++) {
        org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
        assertNotNull(cell);
        assertEquals(expectedHeaders[i], cell.getStringCellValue());
      }

      // 데이터 검증
      for (int i = 0; i < dailyStatistics.size(); i++) {
        DailyStatisticsEntity stat = dailyStatistics.get(i);
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(i + 1);
        assertNotNull(row);

        int colIdx = 0;

        // 전략 일간 통계 ID
        org.apache.poi.ss.usermodel.Cell idCell = row.getCell(colIdx++);
        assertNotNull(idCell);
        assertEquals(stat.getDailyStatisticsId().doubleValue(), idCell.getNumericCellValue());

        // 전략 ID
        org.apache.poi.ss.usermodel.Cell strategyIdCell = row.getCell(colIdx++);
        assertNotNull(strategyIdCell);
        assertEquals(stat.getStrategyEntity().getStrategyId().doubleValue(), strategyIdCell.getNumericCellValue());

        // 일자
        org.apache.poi.ss.usermodel.Cell dateCell = row.getCell(colIdx++);
        assertNotNull(dateCell);
        assertEquals(stat.getDate().toString(), dateCell.getStringCellValue());

        // 일손익
        org.apache.poi.ss.usermodel.Cell plCell = row.getCell(colIdx++);
        assertNotNull(plCell);
        assertEquals(stat.getDailyProfitLoss().doubleValue(), plCell.getNumericCellValue());

        // 입출금
        org.apache.poi.ss.usermodel.Cell depWdCell = row.getCell(colIdx++);
        assertNotNull(depWdCell);
        assertEquals(stat.getDepWdPrice().doubleValue(), depWdCell.getNumericCellValue());

        // 거래일수
        org.apache.poi.ss.usermodel.Cell tradeDaysCell = row.getCell(colIdx++);
        assertNotNull(tradeDaysCell);
        assertEquals(stat.getTradingDays(), (int) tradeDaysCell.getNumericCellValue());

        // 원금
        org.apache.poi.ss.usermodel.Cell principalCell = row.getCell(colIdx++);
        assertNotNull(principalCell);
        assertEquals(stat.getPrincipal().doubleValue(), principalCell.getNumericCellValue());

        // 잔고
        org.apache.poi.ss.usermodel.Cell balanceCell = row.getCell(colIdx++);
        assertNotNull(balanceCell);
        assertEquals(stat.getBalance().doubleValue(), balanceCell.getNumericCellValue());

        // 평가손익
        org.apache.poi.ss.usermodel.Cell evalPlCell = row.getCell(colIdx++);
        assertNotNull(evalPlCell);
        assertEquals(stat. getUnrealizedProfitLoss().doubleValue(), evalPlCell.getNumericCellValue());

        // KP-Ratio
        org.apache.poi.ss.usermodel.Cell kpRatioCell = row.getCell(colIdx++);
        assertNotNull(kpRatioCell);
        assertEquals(stat.getKpRatio().doubleValue(), kpRatioCell.getNumericCellValue());

        // SM-Score
        org.apache.poi.ss.usermodel.Cell smScoreCell = row.getCell(colIdx++);
        assertNotNull(smScoreCell);
        assertEquals(stat.getSmScore().doubleValue(), smScoreCell.getNumericCellValue());

        // 기준가
        org.apache.poi.ss.usermodel.Cell referencePriceCell = row.getCell(colIdx++);
        assertNotNull(referencePriceCell);
        assertEquals(stat.getReferencePrice().doubleValue(), referencePriceCell.getNumericCellValue());

        // 누적입출금액
        org.apache.poi.ss.usermodel.Cell cumulativeDepWdPriceCell = row.getCell(colIdx++);
        assertNotNull(cumulativeDepWdPriceCell);
        assertEquals(stat.getCumulativeDepWdPrice().doubleValue(), cumulativeDepWdPriceCell.getNumericCellValue());

        // 입금
        org.apache.poi.ss.usermodel.Cell depositAmountCell = row.getCell(colIdx++);
        assertNotNull(depositAmountCell);
        assertEquals(stat.getDepositAmount().doubleValue(), depositAmountCell.getNumericCellValue());

        // 누적입금액
        org.apache.poi.ss.usermodel.Cell cumulativeDepositAmountCell = row.getCell(colIdx++);
        assertNotNull(cumulativeDepositAmountCell);
        assertEquals(stat.getCumulativeDepositAmount().doubleValue(), cumulativeDepositAmountCell.getNumericCellValue());

        // 출금
        org.apache.poi.ss.usermodel.Cell withdrawAmountCell = row.getCell(colIdx++);
        assertNotNull(withdrawAmountCell);
        assertEquals(stat.getWithdrawAmount().doubleValue(), withdrawAmountCell.getNumericCellValue());

        // 누적출금액
        org.apache.poi.ss.usermodel.Cell cumulativeWithdrawAmountCell = row.getCell(colIdx++);
        assertNotNull(cumulativeWithdrawAmountCell);
        assertEquals(stat.getCumulativeWithdrawAmount().doubleValue(), cumulativeWithdrawAmountCell.getNumericCellValue());

        // 일손익률
        org.apache.poi.ss.usermodel.Cell dailyPlRateCell = row.getCell(colIdx++);
        assertNotNull(dailyPlRateCell);
        assertEquals(stat.getDailyPlRate().doubleValue(), dailyPlRateCell.getNumericCellValue());

        // 추가 분석 지표 검증 (includeAnalysis = true)
        // 최대일이익
        org.apache.poi.ss.usermodel.Cell maxDailyProfitCell = row.getCell(colIdx++);
        assertNotNull(maxDailyProfitCell);
        assertEquals(stat.getMaxDailyProfit().doubleValue(), maxDailyProfitCell.getNumericCellValue());

        // 최대일이익률
        org.apache.poi.ss.usermodel.Cell maxDailyProfitRateCell = row.getCell(colIdx++);
        assertNotNull(maxDailyProfitRateCell);
        assertEquals(stat.getMaxDailyProfitRate().doubleValue(), maxDailyProfitRateCell.getNumericCellValue());

        // 최대일손실
        org.apache.poi.ss.usermodel.Cell maxDailyLossCell = row.getCell(colIdx++);
        assertNotNull(maxDailyLossCell);
        assertEquals(stat.getMaxDailyLoss().doubleValue(), maxDailyLossCell.getNumericCellValue());

        // 최대일손실률
        org.apache.poi.ss.usermodel.Cell maxDailyLossRateCell = row.getCell(colIdx++);
        assertNotNull(maxDailyLossRateCell);
        assertEquals(stat.getMaxDailyLossRate().doubleValue(), maxDailyLossRateCell.getNumericCellValue());

        // 총이익
        org.apache.poi.ss.usermodel.Cell totalProfitCell = row.getCell(colIdx++);
        assertNotNull(totalProfitCell);
        assertEquals(stat.getTotalProfit().doubleValue(), totalProfitCell.getNumericCellValue());

        // 총이익일수
        org.apache.poi.ss.usermodel.Cell totalProfitDaysCell = row.getCell(colIdx++);
        assertNotNull(totalProfitDaysCell);
        assertEquals(stat.getTotalProfitDays(), (int) totalProfitDaysCell.getNumericCellValue());

        // 평균이익
        org.apache.poi.ss.usermodel.Cell averageProfitCell = row.getCell(colIdx++);
        assertNotNull(averageProfitCell);
        assertEquals(stat.getAverageProfit().doubleValue(), averageProfitCell.getNumericCellValue());

        // 총손실
        org.apache.poi.ss.usermodel.Cell totalLossCell = row.getCell(colIdx++);
        assertNotNull(totalLossCell);
        assertEquals(stat.getTotalLoss().doubleValue(), totalLossCell.getNumericCellValue());

        // 총손실일수
        org.apache.poi.ss.usermodel.Cell totalLossDaysCell = row.getCell(colIdx++);
        assertNotNull(totalLossDaysCell);
        assertEquals(stat.getTotalLossDays(), (int) totalLossDaysCell.getNumericCellValue());

        // 평균손실
        org.apache.poi.ss.usermodel.Cell averageLossCell = row.getCell(colIdx++);
        assertNotNull(averageLossCell);
        assertEquals(stat.getAverageLoss().doubleValue(), averageLossCell.getNumericCellValue());

        // 누적손익
        org.apache.poi.ss.usermodel.Cell cumulativeProfitLossCell = row.getCell(colIdx++);
        assertNotNull(cumulativeProfitLossCell);
        assertEquals(stat.getCumulativeProfitLoss().doubleValue(), cumulativeProfitLossCell.getNumericCellValue());

        // 누적손익률
        org.apache.poi.ss.usermodel.Cell cumulativeProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(cumulativeProfitLossRateCell);
        assertEquals(stat.getCumulativeProfitLossRate().doubleValue(), cumulativeProfitLossRateCell.getNumericCellValue());

        // 최대누적손익
        org.apache.poi.ss.usermodel.Cell maxCumulativeProfitLossCell = row.getCell(colIdx++);
        assertNotNull(maxCumulativeProfitLossCell);
        assertEquals(stat.getMaxCumulativeProfitLoss().doubleValue(), maxCumulativeProfitLossCell.getNumericCellValue());

        // 최대누적손익률
        org.apache.poi.ss.usermodel.Cell maxCumulativeProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(maxCumulativeProfitLossRateCell);
        assertEquals(stat.getMaxCumulativeProfitLossRate().doubleValue(), maxCumulativeProfitLossRateCell.getNumericCellValue());

        // 평균손익
        org.apache.poi.ss.usermodel.Cell averageProfitLossCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossCell);
        assertEquals(stat.getAverageProfitLoss().doubleValue(), averageProfitLossCell.getNumericCellValue());

        // 평균손익률
        org.apache.poi.ss.usermodel.Cell averageProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossRateCell);
        assertEquals(stat.getAverageProfitLossRate().doubleValue(), averageProfitLossRateCell.getNumericCellValue());

        // Peak
        org.apache.poi.ss.usermodel.Cell peakCell = row.getCell(colIdx++);
        assertNotNull(peakCell);
        assertEquals(stat.getPeak().doubleValue(), peakCell.getNumericCellValue());

        // Peak(%)
        org.apache.poi.ss.usermodel.Cell peakRateCell = row.getCell(colIdx++);
        assertNotNull(peakRateCell);
        assertEquals(stat.getPeakRate().doubleValue(), peakRateCell.getNumericCellValue());

        // 고점후경과일
        org.apache.poi.ss.usermodel.Cell daysSincePeakCell = row.getCell(colIdx++);
        assertNotNull(daysSincePeakCell);
        assertEquals(stat.getDaysSincePeak(), (int) daysSincePeakCell.getNumericCellValue());

        // 현재자본인하금액
        org.apache.poi.ss.usermodel.Cell currentDrawdownAmountCell = row.getCell(colIdx++);
        assertNotNull(currentDrawdownAmountCell);
        assertEquals(stat.getCurrentDrawdownAmount().doubleValue(), currentDrawdownAmountCell.getNumericCellValue());

        // 현재자본인하율
        org.apache.poi.ss.usermodel.Cell currentDrawdownRateCell = row.getCell(colIdx++);
        assertNotNull(currentDrawdownRateCell);
        assertEquals(stat.getCurrentDrawdownRate().doubleValue(), currentDrawdownRateCell.getNumericCellValue());

        // 최대자본인하금액
        org.apache.poi.ss.usermodel.Cell maxDrawdownAmountCell = row.getCell(colIdx++);
        assertNotNull(maxDrawdownAmountCell);
        assertEquals(stat.getMaxDrawdownAmount().doubleValue(), maxDrawdownAmountCell.getNumericCellValue());

        // 최대자본인하율
        org.apache.poi.ss.usermodel.Cell maxDrawdownRateCell = row.getCell(colIdx++);
        assertNotNull(maxDrawdownRateCell);
        assertEquals(stat.getMaxDrawdownRate().doubleValue(), maxDrawdownRateCell.getNumericCellValue());

        // 승률
        org.apache.poi.ss.usermodel.Cell winRateCell = row.getCell(colIdx++);
        assertNotNull(winRateCell);
        assertEquals(stat.getWinRate().doubleValue(), winRateCell.getNumericCellValue());

        // Profit Factor
        org.apache.poi.ss.usermodel.Cell profitFactorCell = row.getCell(colIdx++);
        assertNotNull(profitFactorCell);
        assertEquals(stat.getProfitFactor().doubleValue(), profitFactorCell.getNumericCellValue());

        // ROA
        org.apache.poi.ss.usermodel.Cell roaCell = row.getCell(colIdx++);
        assertNotNull(roaCell);
        assertEquals(stat.getRoa().doubleValue(), roaCell.getNumericCellValue());

        // 평균손익비
        org.apache.poi.ss.usermodel.Cell averageProfitLossRatioCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossRatioCell);
        assertEquals(stat.getAverageProfitLossRatio().doubleValue(), averageProfitLossRatioCell.getNumericCellValue());

        // 변동계수
        org.apache.poi.ss.usermodel.Cell coefficientOfVariationCell = row.getCell(colIdx++);
        assertNotNull(coefficientOfVariationCell);
        assertEquals(stat.getCoefficientOfVariation().doubleValue(), coefficientOfVariationCell.getNumericCellValue());

        // Sharp Ratio
        org.apache.poi.ss.usermodel.Cell sharpRatioCell = row.getCell(colIdx++);
        assertNotNull(sharpRatioCell);
        assertEquals(stat.getSharpRatio().doubleValue(), sharpRatioCell.getNumericCellValue());

        // 현재 연속 손익일수
        org.apache.poi.ss.usermodel.Cell currentConsecutivePlDaysCell = row.getCell(colIdx++);
        assertNotNull(currentConsecutivePlDaysCell);
        assertEquals(stat.getCurrentConsecutivePlDays(), (int) currentConsecutivePlDaysCell.getNumericCellValue());

        // 최대 연속 수익일수
        org.apache.poi.ss.usermodel.Cell maxConsecutiveProfitDaysCell = row.getCell(colIdx++);
        assertNotNull(maxConsecutiveProfitDaysCell);
        assertEquals(stat.getMaxConsecutiveProfitDays(), (int) maxConsecutiveProfitDaysCell.getNumericCellValue());

        // 최대 연속 손실일수
        org.apache.poi.ss.usermodel.Cell maxConsecutiveLossDaysCell = row.getCell(colIdx++);
        assertNotNull(maxConsecutiveLossDaysCell);
        assertEquals(stat.getMaxConsecutiveLossDays(), (int) maxConsecutiveLossDaysCell.getNumericCellValue());

        // 최근 1년 수익률
        org.apache.poi.ss.usermodel.Cell recentOneYearReturnCell = row.getCell(colIdx++);
        assertNotNull(recentOneYearReturnCell);
        assertEquals(stat.getRecentOneYearReturn().doubleValue(), recentOneYearReturnCell.getNumericCellValue());

        // 총전략운용일수
        org.apache.poi.ss.usermodel.Cell strategyOperationDaysCell = row.getCell(colIdx++);
        assertNotNull(strategyOperationDaysCell);
        assertEquals(stat.getStrategyOperationDays(), (int) strategyOperationDaysCell.getNumericCellValue());
      }

    } catch (Exception e) {
      fail("엑셀 파일 검증 중 예외 발생: " + e.getMessage());
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 2. 월간 통계 엑셀 생성 테스트 - 성공
   */
  @Test
  void testExportMonthlyStatisticsToExcel_Success() {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 월간 통계 반환
    when(monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(monthlyStatistics);

    // 서비스 메서드 호출
    byte[] excelBytes = statisticsExportService.exportMonthlyStatisticsToExcel(strategyId);
    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0);

    // Apache POI를 사용하여 엑셀 내용을 검증
    try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excelBytes))) {
      org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("월간 통계");
      assertNotNull(sheet);

      // 헤더 검증
      org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
      assertNotNull(headerRow);
      String[] expectedHeaders = {
              "전략 월간 통계 ID", "전략 ID", "분석 월", "월평균 원금", "월 입출금 총액",
              "월손익", "월 손익률", "월누적손익", "월누적손익률", "월평균 잔고"
      };
      for (int i = 0; i < expectedHeaders.length; i++) {
        org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
        assertNotNull(cell);
        assertEquals(expectedHeaders[i], cell.getStringCellValue());
      }

      // 데이터 검증 (현재는 빈 리스트이므로 데이터 없음)
      assertEquals(0, sheet.getLastRowNum());
    } catch (Exception e) {
      fail("엑셀 파일 검증 중 예외 발생: " + e.getMessage());
    }

    // 리포지토리 호출 검증
    verify(monthlyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 3. 일간 분석 지표 엑셀 생성 테스트 - 성공
   */
  @Test
  void testExportDailyAnalysisIndicatorsToExcel_Success() {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // 서비스 메서드 호출
    byte[] excelBytes = statisticsExportService.exportDailyAnalysisIndicatorsToExcel(strategyId);
    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0);

    // Apache POI를 사용하여 엑셀 내용을 검증
    try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excelBytes))) {
      org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("일간 분석 지표");
      assertNotNull(sheet);

      // 헤더 검증
      org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
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
        org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
        assertNotNull(cell);
        assertEquals(expectedHeaders[i], cell.getStringCellValue());
      }

      // 데이터 검증
      for (int i = 0; i < dailyStatistics.size(); i++) {
        DailyStatisticsEntity stat = dailyStatistics.get(i);
        org.apache.poi.ss.usermodel.Row row = sheet.getRow(i + 1);
        assertNotNull(row);

        int colIdx = 0;

        // 일자
        org.apache.poi.ss.usermodel.Cell dateCell = row.getCell(colIdx++);
        assertNotNull(dateCell);
        assertEquals(stat.getDate().toString(), dateCell.getStringCellValue());

        // KP-Ratio
        org.apache.poi.ss.usermodel.Cell kpRatioCell = row.getCell(colIdx++);
        assertNotNull(kpRatioCell);
        assertEquals(stat.getKpRatio().doubleValue(), kpRatioCell.getNumericCellValue());

        // SM-Score
        org.apache.poi.ss.usermodel.Cell smScoreCell = row.getCell(colIdx++);
        assertNotNull(smScoreCell);
        assertEquals(stat.getSmScore().doubleValue(), smScoreCell.getNumericCellValue());

        // 기준가
        org.apache.poi.ss.usermodel.Cell referencePriceCell = row.getCell(colIdx++);
        assertNotNull(referencePriceCell);
        assertEquals(stat.getReferencePrice().doubleValue(), referencePriceCell.getNumericCellValue());

        // 누적입출금액
        org.apache.poi.ss.usermodel.Cell cumulativeDepWdPriceCell = row.getCell(colIdx++);
        assertNotNull(cumulativeDepWdPriceCell);
        assertEquals(stat.getCumulativeDepWdPrice().doubleValue(), cumulativeDepWdPriceCell.getNumericCellValue());

        // 입금
        org.apache.poi.ss.usermodel.Cell depositAmountCell = row.getCell(colIdx++);
        assertNotNull(depositAmountCell);
        assertEquals(stat.getDepositAmount().doubleValue(), depositAmountCell.getNumericCellValue());

        // 누적입금액
        org.apache.poi.ss.usermodel.Cell cumulativeDepositAmountCell = row.getCell(colIdx++);
        assertNotNull(cumulativeDepositAmountCell);
        assertEquals(stat.getCumulativeDepositAmount().doubleValue(), cumulativeDepositAmountCell.getNumericCellValue());

        // 출금
        org.apache.poi.ss.usermodel.Cell withdrawAmountCell = row.getCell(colIdx++);
        assertNotNull(withdrawAmountCell);
        assertEquals(stat.getWithdrawAmount().doubleValue(), withdrawAmountCell.getNumericCellValue());

        // 누적출금액
        org.apache.poi.ss.usermodel.Cell cumulativeWithdrawAmountCell = row.getCell(colIdx++);
        assertNotNull(cumulativeWithdrawAmountCell);
        assertEquals(stat.getCumulativeWithdrawAmount().doubleValue(), cumulativeWithdrawAmountCell.getNumericCellValue());

        // 일손익률
        org.apache.poi.ss.usermodel.Cell dailyPlRateCell = row.getCell(colIdx++);
        assertNotNull(dailyPlRateCell);
        assertEquals(stat.getDailyPlRate().doubleValue(), dailyPlRateCell.getNumericCellValue());

        // 최대일이익
        org.apache.poi.ss.usermodel.Cell maxDailyProfitCell = row.getCell(colIdx++);
        assertNotNull(maxDailyProfitCell);
        assertEquals(stat.getMaxDailyProfit().doubleValue(), maxDailyProfitCell.getNumericCellValue());

        // 최대일이익률
        org.apache.poi.ss.usermodel.Cell maxDailyProfitRateCell = row.getCell(colIdx++);
        assertNotNull(maxDailyProfitRateCell);
        assertEquals(stat.getMaxDailyProfitRate().doubleValue(), maxDailyProfitRateCell.getNumericCellValue());

        // 최대일손실
        org.apache.poi.ss.usermodel.Cell maxDailyLossCell = row.getCell(colIdx++);
        assertNotNull(maxDailyLossCell);
        assertEquals(stat.getMaxDailyLoss().doubleValue(), maxDailyLossCell.getNumericCellValue());

        // 최대일손실률
        org.apache.poi.ss.usermodel.Cell maxDailyLossRateCell = row.getCell(colIdx++);
        assertNotNull(maxDailyLossRateCell);
        assertEquals(stat.getMaxDailyLossRate().doubleValue(), maxDailyLossRateCell.getNumericCellValue());

        // 총이익
        org.apache.poi.ss.usermodel.Cell totalProfitCell = row.getCell(colIdx++);
        assertNotNull(totalProfitCell);
        assertEquals(stat.getTotalProfit().doubleValue(), totalProfitCell.getNumericCellValue());

        // 총이익일수
        org.apache.poi.ss.usermodel.Cell totalProfitDaysCell = row.getCell(colIdx++);
        assertNotNull(totalProfitDaysCell);
        assertEquals(stat.getTotalProfitDays(), (int) totalProfitDaysCell.getNumericCellValue());

        // 평균이익
        org.apache.poi.ss.usermodel.Cell averageProfitCell = row.getCell(colIdx++);
        assertNotNull(averageProfitCell);
        assertEquals(stat.getAverageProfit().doubleValue(), averageProfitCell.getNumericCellValue());

        // 총손실
        org.apache.poi.ss.usermodel.Cell totalLossCell = row.getCell(colIdx++);
        assertNotNull(totalLossCell);
        assertEquals(stat.getTotalLoss().doubleValue(), totalLossCell.getNumericCellValue());

        // 총손실일수
        org.apache.poi.ss.usermodel.Cell totalLossDaysCell = row.getCell(colIdx++);
        assertNotNull(totalLossDaysCell);
        assertEquals(stat.getTotalLossDays(), (int) totalLossDaysCell.getNumericCellValue());

        // 평균손실
        org.apache.poi.ss.usermodel.Cell averageLossCell = row.getCell(colIdx++);
        assertNotNull(averageLossCell);
        assertEquals(stat.getAverageLoss().doubleValue(), averageLossCell.getNumericCellValue());

        // 누적손익
        org.apache.poi.ss.usermodel.Cell cumulativeProfitLossCell = row.getCell(colIdx++);
        assertNotNull(cumulativeProfitLossCell);
        assertEquals(stat.getCumulativeProfitLoss().doubleValue(), cumulativeProfitLossCell.getNumericCellValue());

        // 누적손익률
        org.apache.poi.ss.usermodel.Cell cumulativeProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(cumulativeProfitLossRateCell);
        assertEquals(stat.getCumulativeProfitLossRate().doubleValue(), cumulativeProfitLossRateCell.getNumericCellValue());

        // 최대누적손익
        org.apache.poi.ss.usermodel.Cell maxCumulativeProfitLossCell = row.getCell(colIdx++);
        assertNotNull(maxCumulativeProfitLossCell);
        assertEquals(stat.getMaxCumulativeProfitLoss().doubleValue(), maxCumulativeProfitLossCell.getNumericCellValue());

        // 최대누적손익률
        org.apache.poi.ss.usermodel.Cell maxCumulativeProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(maxCumulativeProfitLossRateCell);
        assertEquals(stat.getMaxCumulativeProfitLossRate().doubleValue(), maxCumulativeProfitLossRateCell.getNumericCellValue());

        // 평균손익
        org.apache.poi.ss.usermodel.Cell averageProfitLossCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossCell);
        assertEquals(stat.getAverageProfitLoss().doubleValue(), averageProfitLossCell.getNumericCellValue());

        // 평균손익률
        org.apache.poi.ss.usermodel.Cell averageProfitLossRateCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossRateCell);
        assertEquals(stat.getAverageProfitLossRate().doubleValue(), averageProfitLossRateCell.getNumericCellValue());

        // Peak
        org.apache.poi.ss.usermodel.Cell peakCell = row.getCell(colIdx++);
        assertNotNull(peakCell);
        assertEquals(stat.getPeak().doubleValue(), peakCell.getNumericCellValue());

        // Peak(%)
        org.apache.poi.ss.usermodel.Cell peakRateCell = row.getCell(colIdx++);
        assertNotNull(peakRateCell);
        assertEquals(stat.getPeakRate().doubleValue(), peakRateCell.getNumericCellValue());

        // 고점후경과일
        org.apache.poi.ss.usermodel.Cell daysSincePeakCell = row.getCell(colIdx++);
        assertNotNull(daysSincePeakCell);
        assertEquals(stat.getDaysSincePeak(), (int) daysSincePeakCell.getNumericCellValue());

        // 현재자본인하금액
        org.apache.poi.ss.usermodel.Cell currentDrawdownAmountCell = row.getCell(colIdx++);
        assertNotNull(currentDrawdownAmountCell);
        assertEquals(stat.getCurrentDrawdownAmount().doubleValue(), currentDrawdownAmountCell.getNumericCellValue());

        // 현재자본인하율
        org.apache.poi.ss.usermodel.Cell currentDrawdownRateCell = row.getCell(colIdx++);
        assertNotNull(currentDrawdownRateCell);
        assertEquals(stat.getCurrentDrawdownRate().doubleValue(), currentDrawdownRateCell.getNumericCellValue());

        // 최대자본인하금액
        org.apache.poi.ss.usermodel.Cell maxDrawdownAmountCell = row.getCell(colIdx++);
        assertNotNull(maxDrawdownAmountCell);
        assertEquals(stat.getMaxDrawdownAmount().doubleValue(), maxDrawdownAmountCell.getNumericCellValue());

        // 최대자본인하율
        org.apache.poi.ss.usermodel.Cell maxDrawdownRateCell = row.getCell(colIdx++);
        assertNotNull(maxDrawdownRateCell);
        assertEquals(stat.getMaxDrawdownRate().doubleValue(), maxDrawdownRateCell.getNumericCellValue());

        // 승률
        org.apache.poi.ss.usermodel.Cell winRateCell = row.getCell(colIdx++);
        assertNotNull(winRateCell);
        assertEquals(stat.getWinRate().doubleValue(), winRateCell.getNumericCellValue());

        // Profit Factor
        org.apache.poi.ss.usermodel.Cell profitFactorCell = row.getCell(colIdx++);
        assertNotNull(profitFactorCell);
        assertEquals(stat.getProfitFactor().doubleValue(), profitFactorCell.getNumericCellValue());

        // ROA
        org.apache.poi.ss.usermodel.Cell roaCell = row.getCell(colIdx++);
        assertNotNull(roaCell);
        assertEquals(stat.getRoa().doubleValue(), roaCell.getNumericCellValue());

        // 평균손익비
        org.apache.poi.ss.usermodel.Cell averageProfitLossRatioCell = row.getCell(colIdx++);
        assertNotNull(averageProfitLossRatioCell);
        assertEquals(stat.getAverageProfitLossRatio().doubleValue(), averageProfitLossRatioCell.getNumericCellValue());

        // 변동계수
        org.apache.poi.ss.usermodel.Cell coefficientOfVariationCell = row.getCell(colIdx++);
        assertNotNull(coefficientOfVariationCell);
        assertEquals(stat.getCoefficientOfVariation().doubleValue(), coefficientOfVariationCell.getNumericCellValue());

        // Sharp Ratio
        org.apache.poi.ss.usermodel.Cell sharpRatioCell = row.getCell(colIdx++);
        assertNotNull(sharpRatioCell);
        assertEquals(stat.getSharpRatio().doubleValue(), sharpRatioCell.getNumericCellValue());

        // 현재 연속 손익일수
        org.apache.poi.ss.usermodel.Cell currentConsecutivePlDaysCell = row.getCell(colIdx++);
        assertNotNull(currentConsecutivePlDaysCell);
        assertEquals(stat.getCurrentConsecutivePlDays(), (int) currentConsecutivePlDaysCell.getNumericCellValue());

        // 최대 연속 수익일수
        org.apache.poi.ss.usermodel.Cell maxConsecutiveProfitDaysCell = row.getCell(colIdx++);
        assertNotNull(maxConsecutiveProfitDaysCell);
        assertEquals(stat.getMaxConsecutiveProfitDays(), (int) maxConsecutiveProfitDaysCell.getNumericCellValue());

        // 최대 연속 손실일수
        org.apache.poi.ss.usermodel.Cell maxConsecutiveLossDaysCell = row.getCell(colIdx++);
        assertNotNull(maxConsecutiveLossDaysCell);
        assertEquals(stat.getMaxConsecutiveLossDays(), (int) maxConsecutiveLossDaysCell.getNumericCellValue());

        // 최근 1년 수익률
        org.apache.poi.ss.usermodel.Cell recentOneYearReturnCell = row.getCell(colIdx++);
        assertNotNull(recentOneYearReturnCell);
        assertEquals(stat.getRecentOneYearReturn().doubleValue(), recentOneYearReturnCell.getNumericCellValue());

        // 총전략운용일수
        org.apache.poi.ss.usermodel.Cell strategyOperationDaysCell = row.getCell(colIdx++);
        assertNotNull(strategyOperationDaysCell);
        assertEquals(stat.getStrategyOperationDays(), (int) strategyOperationDaysCell.getNumericCellValue());
      }

    } catch (Exception e) {
      fail("엑셀 파일 검증 중 예외 발생: " + e.getMessage());
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 4. 월간 통계 엑셀 생성 테스트 - 실패 (데이터 없음)
   */
  @Test
  void testExportMonthlyStatisticsToExcel_NoData() {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 월간 통계 없음
    when(monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(List.of());

    // 서비스 메서드 호출 시 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      statisticsExportService.exportMonthlyStatisticsToExcel(strategyId);
    });

    assertEquals("Strategy ID 1에 해당하는 월간 통계가 없습니다.", exception.getMessage());

    // 리포지토리 호출 검증
    verify(monthlyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 5. 일간 통계 엑셀 생성 테스트 - 실패 (데이터 없음)
   */
  @Test
  void testExportDailyStatisticsToExcel_NoData() {
    Long strategyId = 1L;
    boolean includeAnalysis = true;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 없음
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(List.of());

    // 서비스 메서드 호출 시 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      statisticsExportService.exportDailyStatisticsToExcel(strategyId, includeAnalysis);
    });

    assertEquals("Strategy ID 1에 해당하는 일간 통계가 없습니다.", exception.getMessage());

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 6. 일간 분석 지표 엑셀 생성 테스트 - 실패 (데이터 없음)
   */
  @Test
  void testExportDailyAnalysisIndicatorsToExcel_NoData() {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 없음
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(List.of());

    // 서비스 메서드 호출 시 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      statisticsExportService.exportDailyAnalysisIndicatorsToExcel(strategyId);
    });

    assertEquals("Strategy ID 1에 해당하는 일간 분석 통계가 없습니다.", exception.getMessage());

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }
}
