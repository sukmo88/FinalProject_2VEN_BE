// ExcelGeneratorService.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelGeneratorService {

  private final DailyStatisticsRepository dailyStatisticsRepository;
  private final MonthlyStatisticsRepository monthlyStatisticsRepository;

  public ExcelGeneratorService(DailyStatisticsRepository dailyStatisticsRepository,
                               MonthlyStatisticsRepository monthlyStatisticsRepository) {
    this.dailyStatisticsRepository = dailyStatisticsRepository;
    this.monthlyStatisticsRepository = monthlyStatisticsRepository;
  }

  /**
   * 일간 통계 엑셀 파일 생성
   *
   * @return ByteArrayInputStream 엑셀 파일 스트림
   * @throws IOException I/O 에러 발생 시
   */
  public ByteArrayInputStream generateDailyStatisticsExcel() throws IOException {
    List<DailyStatisticsEntity> statistics = dailyStatisticsRepository.findAll();

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("일간 통계");

      // 헤더 행 생성
      Row headerRow = sheet.createRow(0);
      String[] headers = {"날짜", "입출금", "손익", "손익률", "누적손익", "누적손익률"};
      createHeaderRow(workbook, sheet, headerRow, headers);

      // 데이터 행 채우기
      int rowIdx = 1;
      CreationHelper createHelper = workbook.getCreationHelper();
      CellStyle dateStyle = workbook.createCellStyle();
      dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

      CellStyle numberStyle = workbook.createCellStyle();
      numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

      for (DailyStatisticsEntity stat : statistics) {
        Row row = sheet.createRow(rowIdx++);

        // 날짜
        Cell dateCell = row.createCell(0);
        dateCell.setCellValue(stat.getDate());
        dateCell.setCellStyle(dateStyle);

        // 입출금
        Cell depWdCell = row.createCell(1);
        depWdCell.setCellValue(stat.getDepWdPrice().doubleValue());
        depWdCell.setCellStyle(numberStyle);

        // 손익
        Cell plCell = row.createCell(2);
        plCell.setCellValue(stat.getDailyProfitLoss().doubleValue());
        plCell.setCellStyle(numberStyle);

        // 손익률
        Cell plRateCell = row.createCell(3);
        plRateCell.setCellValue(stat.getDailyPlRate().doubleValue());
        plRateCell.setCellStyle(numberStyle);

        // 누적손익
        Cell cumPlCell = row.createCell(4);
        cumPlCell.setCellValue(stat.getCumulativeProfitLoss().doubleValue());
        cumPlCell.setCellStyle(numberStyle);

        // 누적손익률
        Cell cumPlRateCell = row.createCell(5);
        cumPlRateCell.setCellValue(stat.getCumulativeProfitLossRate().doubleValue());
        cumPlRateCell.setCellStyle(numberStyle);
      }

      // 열 너비 자동 조정
      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    }
  }

  /**
   * 월간 통계 엑셀 파일 생성
   *
   * @return ByteArrayInputStream 엑셀 파일 스트림
   * @throws IOException I/O 에러 발생 시
   */
  public ByteArrayInputStream generateMonthlyStatisticsExcel() throws IOException {
    List<MonthlyStatisticsEntity> statistics = monthlyStatisticsRepository.findAll();

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("월간 통계");

      // 헤더 행 생성
      Row headerRow = sheet.createRow(0);
      String[] headers = {"날짜", "입출금", "손익", "손익률", "누적손익", "누적손익률"};
      createHeaderRow(workbook, sheet, headerRow, headers);

      // 데이터 행 채우기
      int rowIdx = 1;
      CreationHelper createHelper = workbook.getCreationHelper();
      CellStyle dateStyle = workbook.createCellStyle();
      dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm"));

      CellStyle numberStyle = workbook.createCellStyle();
      numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

      for (MonthlyStatisticsEntity stat : statistics) {
        Row row = sheet.createRow(rowIdx++);

        // 날짜 (년월)
        Cell dateCell = row.createCell(0);
        dateCell.setCellValue(stat.getAnalysisMonth().toString());
        dateCell.setCellStyle(dateStyle);

        // 입출금
        Cell depWdCell = row.createCell(1);
        depWdCell.setCellValue(stat.getMonthlyDepWdAmount().doubleValue());
        depWdCell.setCellStyle(numberStyle);

        // 손익
        Cell plCell = row.createCell(2);
        plCell.setCellValue(stat.getMonthlyProfitLoss().doubleValue());
        plCell.setCellStyle(numberStyle);

        // 손익률
        Cell plRateCell = row.createCell(3);
        plRateCell.setCellValue(stat.getMonthlyReturn().doubleValue());
        plRateCell.setCellStyle(numberStyle);

        // 누적손익
        Cell cumPlCell = row.createCell(4);
        cumPlCell.setCellValue(stat.getMonthlyCumulativeProfitLoss().doubleValue());
        cumPlCell.setCellStyle(numberStyle);

        // 누적손익률
        Cell cumPlRateCell = row.createCell(5);
        cumPlRateCell.setCellValue(stat.getMonthlyCumulativeReturn().doubleValue());
        cumPlRateCell.setCellStyle(numberStyle);
      }

      // 열 너비 자동 조정
      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    }
  }

  /**
   * 일간 분석 지표 엑셀 파일 생성
   *
   * @return ByteArrayInputStream 엑셀 파일 스트림
   * @throws IOException I/O 에러 발생 시
   */
  public ByteArrayInputStream generateDailyAnalysisIndicatorsExcel() throws IOException {
    List<DailyStatisticsEntity> statistics = dailyStatisticsRepository.findAll();

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("일간 분석 지표");

      // 헤더 행 생성
      Row headerRow = sheet.createRow(0);
      String[] headers = {
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
      createHeaderRow(workbook, sheet, headerRow, headers);

      // 데이터 행 채우기
      int rowIdx = 1;
      CreationHelper createHelper = workbook.getCreationHelper();

      // 스타일 설정
      CellStyle dateStyle = workbook.createCellStyle();
      dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

      CellStyle numberStyle = workbook.createCellStyle();
      numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

      CellStyle percentageStyle = workbook.createCellStyle();
      percentageStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));

      for (DailyStatisticsEntity stat : statistics) {
        Row row = sheet.createRow(rowIdx++);

        int colIdx = 0;

        // 일자
        Cell dateCell = row.createCell(colIdx++);
        dateCell.setCellValue(stat.getDate());
        dateCell.setCellStyle(dateStyle);

        // KP-Ratio
        Cell kpRatioCell = row.createCell(colIdx++);
        setBigDecimalCellValue(kpRatioCell, stat.getKpRatio(), numberStyle);

        // SM-Score
        Cell smScoreCell = row.createCell(colIdx++);
        setBigDecimalCellValue(smScoreCell, stat.getSmScore(), numberStyle);

        // 기준가
        Cell referencePriceCell = row.createCell(colIdx++);
        setBigDecimalCellValue(referencePriceCell, stat.getReferencePrice(), numberStyle);

        // 누적입출금액
        Cell cumulativeDepWdPriceCell = row.createCell(colIdx++);
        setBigDecimalCellValue(cumulativeDepWdPriceCell, stat.getCumulativeDepWdPrice(), numberStyle);

        // 입금
        Cell depositAmountCell = row.createCell(colIdx++);
        setBigDecimalCellValue(depositAmountCell, stat.getDepositAmount(), numberStyle);

        // 누적입금액
        Cell cumulativeDepositAmountCell = row.createCell(colIdx++);
        setBigDecimalCellValue(cumulativeDepositAmountCell, stat.getCumulativeDepositAmount(), numberStyle);

        // 출금
        Cell withdrawAmountCell = row.createCell(colIdx++);
        setBigDecimalCellValue(withdrawAmountCell, stat.getWithdrawAmount(), numberStyle);

        // 누적출금액
        Cell cumulativeWithdrawAmountCell = row.createCell(colIdx++);
        setBigDecimalCellValue(cumulativeWithdrawAmountCell, stat.getCumulativeWithdrawAmount(), numberStyle);

        // 일손익률
        Cell dailyPlRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(dailyPlRateCell, stat.getDailyPlRate(), percentageStyle);

        // 최대일이익
        Cell maxDailyProfitCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxDailyProfitCell, stat.getMaxDailyProfit(), numberStyle);

        // 최대일이익률
        Cell maxDailyProfitRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxDailyProfitRateCell, stat.getMaxDailyProfitRate(), percentageStyle);

        // 최대일손실
        Cell maxDailyLossCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxDailyLossCell, stat.getMaxDailyLoss(), numberStyle);

        // 최대일손실률
        Cell maxDailyLossRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxDailyLossRateCell, stat.getMaxDailyLossRate(), percentageStyle);

        // 총이익
        Cell totalProfitCell = row.createCell(colIdx++);
        setBigDecimalCellValue(totalProfitCell, stat.getTotalProfit(), numberStyle);

        // 총이익일수
        Cell totalProfitDaysCell = row.createCell(colIdx++);
        totalProfitDaysCell.setCellValue(stat.getTotalProfitDays());

        // 평균이익
        Cell averageProfitCell = row.createCell(colIdx++);
        setBigDecimalCellValue(averageProfitCell, stat.getAverageProfit(), numberStyle);

        // 총손실
        Cell totalLossCell = row.createCell(colIdx++);
        setBigDecimalCellValue(totalLossCell, stat.getTotalLoss(), numberStyle);

        // 총손실일수
        Cell totalLossDaysCell = row.createCell(colIdx++);
        totalLossDaysCell.setCellValue(stat.getTotalLossDays());

        // 평균손실
        Cell averageLossCell = row.createCell(colIdx++);
        setBigDecimalCellValue(averageLossCell, stat.getAverageLoss(), numberStyle);

        // 누적손익
        Cell cumulativeProfitLossCell = row.createCell(colIdx++);
        setBigDecimalCellValue(cumulativeProfitLossCell, stat.getCumulativeProfitLoss(), numberStyle);

        // 누적손익률
        Cell cumulativeProfitLossRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(cumulativeProfitLossRateCell, stat.getCumulativeProfitLossRate(), percentageStyle);

        // 최대누적손익
        Cell maxCumulativeProfitLossCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxCumulativeProfitLossCell, stat.getMaxCumulativeProfitLoss(), numberStyle);

        // 최대누적손익률
        Cell maxCumulativeProfitLossRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxCumulativeProfitLossRateCell, stat.getMaxCumulativeProfitLossRate(), percentageStyle);

        // 평균손익
        Cell averageProfitLossCell = row.createCell(colIdx++);
        setBigDecimalCellValue(averageProfitLossCell, stat.getAverageProfitLoss(), numberStyle);

        // 평균손익률
        Cell averageProfitLossRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(averageProfitLossRateCell, stat.getAverageProfitLossRate(), percentageStyle);

        // Peak
        Cell peakCell = row.createCell(colIdx++);
        setBigDecimalCellValue(peakCell, stat.getPeak(), numberStyle);

        // Peak(%)
        Cell peakRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(peakRateCell, stat.getPeakRate(), percentageStyle);

        // 고점후경과일
        Cell daysSincePeakCell = row.createCell(colIdx++);
        daysSincePeakCell.setCellValue(stat.getDaysSincePeak());

        // 현재자본인하금액
        Cell currentDrawdownAmountCell = row.createCell(colIdx++);
        setBigDecimalCellValue(currentDrawdownAmountCell, stat.getCurrentDrawdownAmount(), numberStyle);

        // 현재자본인하율
        Cell currentDrawdownRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(currentDrawdownRateCell, stat.getCurrentDrawdownRate(), percentageStyle);

        // 최대자본인하금액
        Cell maxDrawdownAmountCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxDrawdownAmountCell, stat.getMaxDrawdownAmount(), numberStyle);

        // 최대자본인하율
        Cell maxDrawdownRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(maxDrawdownRateCell, stat.getMaxDrawdownRate(), percentageStyle);

        // 승률
        Cell winRateCell = row.createCell(colIdx++);
        setBigDecimalCellValue(winRateCell, stat.getWinRate(), percentageStyle);

        // Profit Factor
        Cell profitFactorCell = row.createCell(colIdx++);
        setBigDecimalCellValue(profitFactorCell, stat.getProfitFactor(), numberStyle);

        // ROA
        Cell roaCell = row.createCell(colIdx++);
        setBigDecimalCellValue(roaCell, stat.getRoa(), numberStyle);

        // 평균손익비
        Cell averageProfitLossRatioCell = row.createCell(colIdx++);
        setBigDecimalCellValue(averageProfitLossRatioCell, stat.getAverageProfitLossRatio(), numberStyle);

        // 변동계수
        Cell coefficientOfVariationCell = row.createCell(colIdx++);
        setBigDecimalCellValue(coefficientOfVariationCell, stat.getCoefficientOfVariation(), numberStyle);

        // Sharp Ratio
        Cell sharpRatioCell = row.createCell(colIdx++);
        setBigDecimalCellValue(sharpRatioCell, stat.getSharpRatio(), numberStyle);

        // 현재 연속 손익일수
        Cell currentConsecutivePlDaysCell = row.createCell(colIdx++);
        currentConsecutivePlDaysCell.setCellValue(stat.getCurrentConsecutivePlDays());

        // 최대 연속 수익일수
        Cell maxConsecutiveProfitDaysCell = row.createCell(colIdx++);
        maxConsecutiveProfitDaysCell.setCellValue(stat.getMaxConsecutiveProfitDays());

        // 최대 연속 손실일수
        Cell maxConsecutiveLossDaysCell = row.createCell(colIdx++);
        maxConsecutiveLossDaysCell.setCellValue(stat.getMaxConsecutiveLossDays());

        // 최근 1년 수익률
        Cell recentOneYearReturnCell = row.createCell(colIdx++);
        setBigDecimalCellValue(recentOneYearReturnCell, stat.getRecentOneYearReturn(), percentageStyle);

        // 총전략운용일수
        Cell strategyOperationDaysCell = row.createCell(colIdx++);
        strategyOperationDaysCell.setCellValue(stat.getStrategyOperationDays());
      }

      // 열 너비 자동 조정
      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    }
  }

  /**
   * 헤더 행 생성 메소드
   *
   * @param workbook   워크북 객체
   * @param sheet      시트 객체
   * @param headerRow  헤더 행 객체
   * @param headers    헤더 이름 배열
   */
  private void createHeaderRow(Workbook workbook, Sheet sheet, Row headerRow, String[] headers) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);

    for (int col = 0; col < headers.length; col++) {
      Cell cell = headerRow.createCell(col);
      cell.setCellValue(headers[col]);
      cell.setCellStyle(style);
    }
  }

  /**
   * BigDecimal 값을 셀에 설정하는 헬퍼 메소드
   *
   * @param cell      셀 객체
   * @param value     BigDecimal 값
   * @param cellStyle 셀 스타일
   */
  private void setBigDecimalCellValue(Cell cell, java.math.BigDecimal value, CellStyle cellStyle) {
    if (value != null) {
      cell.setCellValue(value.doubleValue());
      cell.setCellStyle(cellStyle);
    } else {
      cell.setCellValue(0);
      cell.setCellStyle(cellStyle);
    }
  }
}
