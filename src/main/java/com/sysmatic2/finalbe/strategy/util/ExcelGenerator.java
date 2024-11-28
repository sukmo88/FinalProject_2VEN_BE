package com.sysmatic2.finalbe.strategy.util;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class ExcelGenerator {

  // 데이터 타입별 셀 설정 핸들러 맵
  private static final Map<Class<?>, BiConsumer<Cell, Object>> CELL_SETTERS = new HashMap<>();

  static {
    CELL_SETTERS.put(String.class, (cell, value) -> cell.setCellValue((String) value));
    CELL_SETTERS.put(Long.class, (cell, value) -> cell.setCellValue((Long) value));
    CELL_SETTERS.put(Integer.class, (cell, value) -> cell.setCellValue((Integer) value));
    CELL_SETTERS.put(Double.class, (cell, value) -> cell.setCellValue((Double) value));
    CELL_SETTERS.put(BigDecimal.class, (cell, value) -> cell.setCellValue(((BigDecimal) value).doubleValue()));
    CELL_SETTERS.put(LocalDate.class, (cell, value) -> cell.setCellValue(((LocalDate) value).toString()));
    CELL_SETTERS.put(YearMonth.class, (cell, value) -> cell.setCellValue(((YearMonth) value).toString()));
    // 필요에 따라 추가적인 타입 핸들러를 정의할 수 있습니다.
  }

  /**
   * 일간 통계 엑셀 파일 생성 메서드 (필요한 필드만 포함)
   *
   * @param dailyStats 일간 통계 데이터 리스트
   * @return Workbook 객체
   */
  public Workbook generateDailyStatisticsExcel(List<DailyStatisticsEntity> dailyStats) {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("일간 통계");

    // 헤더 스타일 설정
    CellStyle headerStyle = getHeaderCellStyle(workbook);

    // 필요한 칼럼 헤더 설정 (한글)
    String[] headers = {
            "일자", "원금", "입출금", "일손익", "일손익률", "누적손익", "누적수익률"
    };

    // 헤더 행 생성
    createHeaderRow(workbook, sheet, sheet.createRow(0), headers);

    // 데이터 행 추가
    int rowIdx = 1;
    for (DailyStatisticsEntity stat : dailyStats) {
      Row row = sheet.createRow(rowIdx++);
      int colIdx = 0;

      // 일자
      Cell dateCell = row.createCell(colIdx++);
      dateCell.setCellValue(stat.getDate());
      dateCell.setCellStyle(createDateCellStyle(workbook));

      // 원금
      Cell principalCell = row.createCell(colIdx++);
      setBigDecimalCellValue(principalCell, stat.getPrincipal(), createNumberCellStyle(workbook));

      // 입출금
      Cell depWdCell = row.createCell(colIdx++);
      setBigDecimalCellValue(depWdCell, stat.getDepWdPrice(), createNumberCellStyle(workbook));

      // 일손익
      Cell dailyPlCell = row.createCell(colIdx++);
      setBigDecimalCellValue(dailyPlCell, stat.getDailyProfitLoss(), createNumberCellStyle(workbook));

      // 일손익률
      Cell dailyPlRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(dailyPlRateCell, stat.getDailyPlRate(), createPercentageCellStyle(workbook));

      // 누적손익
      Cell cumulativePlCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativePlCell, stat.getCumulativeProfitLoss(), createNumberCellStyle(workbook));

      // 누적수익률
      Cell cumulativePlRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativePlRateCell, stat.getCumulativeProfitLossRate(), createPercentageCellStyle(workbook));
    }

    // 셀 너비 자동 조정
    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }

    return workbook;
  }

  /**
   * 월간 통계 엑셀 파일 생성 메서드 (필요한 필드만 포함)
   *
   * @param monthlyStats 월간 통계 데이터 리스트
   * @return Workbook 객체
   */
  public Workbook generateMonthlyStatisticsExcel(List<MonthlyStatisticsEntity> monthlyStats) {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("월간 통계");

    // 헤더 스타일 설정
    CellStyle headerStyle = getHeaderCellStyle(workbook);

    // 월간 통계 칼럼 헤더 설정 (한글)
    String[] headers = {
            "월", "원금", "입출금", "월손익", "월손익률", "누적손익", "누적수익률"
    };

    // 헤더 행 생성
    createHeaderRow(workbook, sheet, sheet.createRow(0), headers);

    // 데이터 행 추가
    int rowIdx = 1;
    for (MonthlyStatisticsEntity stat : monthlyStats) {
      Row row = sheet.createRow(rowIdx++);
      int colIdx = 0;

      // 월
      Cell monthCell = row.createCell(colIdx++);
      monthCell.setCellValue(stat.getAnalysisMonth().toString());
      monthCell.setCellStyle(createDateCellStyle(workbook));

      // 원금
      Cell principalCell = row.createCell(colIdx++);
      setBigDecimalCellValue(principalCell, stat.getMonthlyAvgPrincipal(), createNumberCellStyle(workbook));

      // 입출금
      Cell depWdCell = row.createCell(colIdx++);
      setBigDecimalCellValue(depWdCell, stat.getMonthlyDepWdAmount(), createNumberCellStyle(workbook));

      // 월손익
      Cell monthlyPlCell = row.createCell(colIdx++);
      setBigDecimalCellValue(monthlyPlCell, stat.getMonthlyProfitLoss(), createNumberCellStyle(workbook));

      // 월손익률
      Cell monthlyPlRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(monthlyPlRateCell, stat.getMonthlyReturn(), createPercentageCellStyle(workbook));

      // 누적손익
      Cell cumulativePlCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativePlCell, stat.getMonthlyCumulativeProfitLoss(), createNumberCellStyle(workbook));

      // 누적수익률
      Cell cumulativePlRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativePlRateCell, stat.getMonthlyCumulativeReturn(), createPercentageCellStyle(workbook));
    }

    // 셀 너비 자동 조정
    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }

    return workbook;
  }

  /**
   * 일간 분석 지표 엑셀 파일 생성 메서드 (필요한 필드만 포함, 팔로워수 제외)
   *
   * @param statistics 일간 통계 데이터 리스트
   * @return Workbook 객체
   */
  public Workbook generateDailyAnalysisIndicatorsExcel(List<DailyStatisticsEntity> statistics) {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("일간 분석 지표");

    // 헤더 스타일 설정
    CellStyle headerStyle = getHeaderCellStyle(workbook);

    // 필요한 칼럼 헤더 설정 (한글)
    String[] headers = {
            "일자", "원금", "입출금", "일손익", "일손익률", "누적손익", "누적수익률",
            "최대일이익", "최대일이익률", "최대일손실", "최대일손실률", "총이익",
            "총이익일수", "평균이익", "총손실", "총손실일수", "평균손실",
            "누적손익", "누적손익률", "최대누적손익", "최대누적손익률",
            "평균손익", "평균손익률", "Peak", "Peak(%)", "고점후경과일",
            "현재자본인하금액", "현재자본인하율", "최대자본인하금액",
            "최대자본인하율", "승률", "Profit Factor", "ROA",
            "평균손익비", "변동계수", "Sharp Ratio",
            "현재 연속 손익일수", "최대 연속 수익일수",
            "최대 연속 손실일수", "최근 1년 수익률", "총전략운용일수",
            "DD 기간", "DD기간 내 최대 자본인하율"
    };

    // 헤더 행 생성
    createHeaderRow(workbook, sheet, sheet.createRow(0), headers);

    // 데이터 행 추가
    int rowIdx = 1;
    for (DailyStatisticsEntity stat : statistics) {
      Row row = sheet.createRow(rowIdx++);
      int colIdx = 0;

      // 일자
      Cell dateCell = row.createCell(colIdx++);
      dateCell.setCellValue(stat.getDate());
      dateCell.setCellStyle(createDateCellStyle(workbook));

      // 원금
      Cell principalCell = row.createCell(colIdx++);
      setBigDecimalCellValue(principalCell, stat.getPrincipal(), createNumberCellStyle(workbook));

      // 입출금
      Cell depWdCell = row.createCell(colIdx++);
      setBigDecimalCellValue(depWdCell, stat.getDepWdPrice(), createNumberCellStyle(workbook));

      // 일손익
      Cell dailyPlCell = row.createCell(colIdx++);
      setBigDecimalCellValue(dailyPlCell, stat.getDailyProfitLoss(), createNumberCellStyle(workbook));

      // 일손익률
      Cell dailyPlRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(dailyPlRateCell, stat.getDailyPlRate(), createPercentageCellStyle(workbook));

      // 누적손익
      Cell cumulativePlCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativePlCell, stat.getCumulativeProfitLoss(), createNumberCellStyle(workbook));

      // 누적수익률
      Cell cumulativePlRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativePlRateCell, stat.getCumulativeProfitLossRate(), createPercentageCellStyle(workbook));

      // 최대일이익
      Cell maxDailyProfitCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDailyProfitCell, stat.getMaxDailyProfit(), createNumberCellStyle(workbook));

      // 최대일이익률
      Cell maxDailyProfitRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDailyProfitRateCell, stat.getMaxDailyProfitRate(), createPercentageCellStyle(workbook));

      // 최대일손실
      Cell maxDailyLossCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDailyLossCell, stat.getMaxDailyLoss(), createNumberCellStyle(workbook));

      // 최대일손실률
      Cell maxDailyLossRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDailyLossRateCell, stat.getMaxDailyLossRate(), createPercentageCellStyle(workbook));

      // 총이익
      Cell totalProfitCell = row.createCell(colIdx++);
      setBigDecimalCellValue(totalProfitCell, stat.getTotalProfit(), createNumberCellStyle(workbook));

      // 총이익일수
      Cell totalProfitDaysCell = row.createCell(colIdx++);
      totalProfitDaysCell.setCellValue(stat.getTotalProfitDays());

      // 평균이익
      Cell averageProfitCell = row.createCell(colIdx++);
      setBigDecimalCellValue(averageProfitCell, stat.getAverageProfit(), createNumberCellStyle(workbook));

      // 총손실
      Cell totalLossCell = row.createCell(colIdx++);
      setBigDecimalCellValue(totalLossCell, stat.getTotalLoss(), createNumberCellStyle(workbook));

      // 총손실일수
      Cell totalLossDaysCell = row.createCell(colIdx++);
      totalLossDaysCell.setCellValue(stat.getTotalLossDays());

      // 평균손실
      Cell averageLossCell = row.createCell(colIdx++);
      setBigDecimalCellValue(averageLossCell, stat.getAverageLoss(), createNumberCellStyle(workbook));

      // 누적손익
      Cell cumulativeProfitLossCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativeProfitLossCell, stat.getCumulativeProfitLoss(), createNumberCellStyle(workbook));

      // 누적수익률
      Cell cumulativeProfitLossRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(cumulativeProfitLossRateCell, stat.getCumulativeProfitLossRate(), createPercentageCellStyle(workbook));

      // 최대누적손익
      Cell maxCumulativeProfitLossCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxCumulativeProfitLossCell, stat.getMaxCumulativeProfitLoss(), createNumberCellStyle(workbook));

      // 최대누적손익률
      Cell maxCumulativeProfitLossRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxCumulativeProfitLossRateCell, stat.getMaxCumulativeProfitLossRate(), createPercentageCellStyle(workbook));

      // 평균손익
      Cell averageProfitLossCell = row.createCell(colIdx++);
      setBigDecimalCellValue(averageProfitLossCell, stat.getAverageProfitLoss(), createNumberCellStyle(workbook));

      // 평균손익률
      Cell averageProfitLossRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(averageProfitLossRateCell, stat.getAverageProfitLossRate(), createPercentageCellStyle(workbook));

      // Peak
      Cell peakCell = row.createCell(colIdx++);
      setBigDecimalCellValue(peakCell, stat.getPeak(), createNumberCellStyle(workbook));

      // Peak(%)
      Cell peakRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(peakRateCell, stat.getPeakRate(), createPercentageCellStyle(workbook));

      // 고점후경과일
      Cell daysSincePeakCell = row.createCell(colIdx++);
      daysSincePeakCell.setCellValue(stat.getDaysSincePeak());

      // 현재자본인하금액
      Cell currentDrawdownAmountCell = row.createCell(colIdx++);
      setBigDecimalCellValue(currentDrawdownAmountCell, stat.getCurrentDrawdownAmount(), createNumberCellStyle(workbook));

      // 현재자본인하율
      Cell currentDrawdownRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(currentDrawdownRateCell, stat.getCurrentDrawdownRate(), createPercentageCellStyle(workbook));

      // 최대자본인하금액
      Cell maxDrawdownAmountCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDrawdownAmountCell, stat.getMaxDrawdownAmount(), createNumberCellStyle(workbook));

      // 최대자본인하율
      Cell maxDrawdownRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDrawdownRateCell, stat.getMaxDrawdownRate(), createPercentageCellStyle(workbook));

      // 승률
      Cell winRateCell = row.createCell(colIdx++);
      setBigDecimalCellValue(winRateCell, stat.getWinRate(), createPercentageCellStyle(workbook));

      // Profit Factor
      Cell profitFactorCell = row.createCell(colIdx++);
      setBigDecimalCellValue(profitFactorCell, stat.getProfitFactor(), createNumberCellStyle(workbook));

      // ROA
      Cell roaCell = row.createCell(colIdx++);
      setBigDecimalCellValue(roaCell, stat.getRoa(), createNumberCellStyle(workbook));

      // 평균손익비
      Cell averageProfitLossRatioCell = row.createCell(colIdx++);
      setBigDecimalCellValue(averageProfitLossRatioCell, stat.getAverageProfitLossRatio(), createNumberCellStyle(workbook));

      // 변동계수
      Cell coefficientOfVariationCell = row.createCell(colIdx++);
      setBigDecimalCellValue(coefficientOfVariationCell, stat.getCoefficientOfVariation(), createNumberCellStyle(workbook));

      // Sharp Ratio
      Cell sharpRatioCell = row.createCell(colIdx++);
      setBigDecimalCellValue(sharpRatioCell, stat.getSharpRatio(), createNumberCellStyle(workbook));

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
      setBigDecimalCellValue(recentOneYearReturnCell, stat.getRecentOneYearReturn(), createPercentageCellStyle(workbook));

      // 총전략운용일수
      Cell strategyOperationDaysCell = row.createCell(colIdx++);
      strategyOperationDaysCell.setCellValue(stat.getStrategyOperationDays());

      // DD 기간
      Cell ddPeriodCell = row.createCell(colIdx++);
      ddPeriodCell.setCellValue(stat.getDdDay());

      // DD기간 내 최대 자본인하율
      Cell maxDrawdownRateWithinDdPeriodCell = row.createCell(colIdx++);
      setBigDecimalCellValue(maxDrawdownRateWithinDdPeriodCell, stat.getMaxDDInRate(), createPercentageCellStyle(workbook));
    }

    // 셀 너비 자동 조정
    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }

    return workbook;
  }

  /**
   * 헤더 행 생성 메서드 (기본 스타일 적용)
   *
   * @param workbook   워크북 객체
   * @param sheet      시트 객체
   * @param headerRow  헤더 행 객체
   * @param headers    헤더 이름 배열
   */
  public void createHeaderRow(Workbook workbook, Sheet sheet, Row headerRow, String[] headers) {
    CellStyle style = getHeaderCellStyle(workbook);
    for (int col = 0; col < headers.length; col++) {
      Cell cell = headerRow.createCell(col);
      cell.setCellValue(headers[col]);
      cell.setCellStyle(style);
    }
  }

  /**
   * 헤더 셀 스타일 설정 메서드
   *
   * @param workbook Workbook 객체
   * @return CellStyle 객체
   */
  public CellStyle getHeaderCellStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    // 추가적인 스타일 설정 가능 (예: 배경색, 테두리 등)
    return style;
  }

  /**
   * 날짜 셀 스타일 생성 메서드
   *
   * @param workbook Workbook 객체
   * @return 날짜 셀 스타일
   */
  private CellStyle createDateCellStyle(Workbook workbook) {
    CellStyle dateStyle = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
    return dateStyle;
  }

  /**
   * 숫자 셀 스타일 생성 메서드
   *
   * @param workbook Workbook 객체
   * @return 숫자 셀 스타일
   */
  private CellStyle createNumberCellStyle(Workbook workbook) {
    CellStyle numberStyle = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
    return numberStyle;
  }

  /**
   * 퍼센트 셀 스타일 생성 메서드
   *
   * @param workbook Workbook 객체
   * @return 퍼센트 셀 스타일
   */
  private CellStyle createPercentageCellStyle(Workbook workbook) {
    CellStyle percentageStyle = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    percentageStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));
    return percentageStyle;
  }

  /**
   * BigDecimal 값을 셀에 설정하는 헬퍼 메소드
   *
   * @param cell      셀 객체
   * @param value     BigDecimal 값
   * @param cellStyle 셀 스타일
   */
  public void setBigDecimalCellValue(Cell cell, BigDecimal value, CellStyle cellStyle) {
    if (value != null) {
      cell.setCellValue(value.doubleValue());
      cell.setCellStyle(cellStyle);
    } else {
      cell.setCellValue(0);
      cell.setCellStyle(cellStyle);
    }
  }

  /**
   * 셀에 값을 설정하는 유틸리티 메서드
   *
   * @param cell  설정할 셀
   * @param value 설정할 값
   */
  public void setCellValue(Cell cell, Object value) {
    if (value == null) {
      cell.setCellValue("");
      return;
    }

    BiConsumer<Cell, Object> setter = CELL_SETTERS.get(value.getClass());
    if (setter != null) {
      setter.accept(cell, value);
    } else {
      // 기본 처리: toString()
      cell.setCellValue(value.toString());
    }
  }
}
