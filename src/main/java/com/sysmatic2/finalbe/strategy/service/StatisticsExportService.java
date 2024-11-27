// StatisticsExportService.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelFileCreationException;
import com.sysmatic2.finalbe.exception.StrategyNotFoundException;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.util.ExcelGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsExportService {

  private final DailyStatisticsRepository dailyStatisticsRepository;
  private final MonthlyStatisticsRepository monthlyStatisticsRepository;

  /**
   * 일간 통계를 엑셀로 변환하는 메서드
   *
   * @param strategyId      전략 ID
   * @param includeAnalysis 분석 지표 포함 여부
   * @return 엑셀 파일 바이트 배열
   * @throws ExcelFileCreationException 엑셀 생성 중 발생하는 예외
   */
  public byte[] exportDailyStatisticsToExcel(Long strategyId, boolean includeAnalysis) {
    List<DailyStatisticsEntity> dailyStats = dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId);
    if (dailyStats.isEmpty()) {
      throw new ExcelFileCreationException("Strategy ID " + strategyId + "에 해당하는 일간 통계가 없습니다.");
    }

    Workbook workbook;
    try {
      workbook = ExcelGenerator.generateDailyStatisticsExcel(dailyStats, includeAnalysis);
    } catch (Exception e) {
      throw new ExcelFileCreationException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
    }

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      workbook.write(out);
      workbook.close();
      return out.toByteArray();
    } catch (IOException e) {
      throw new ExcelFileCreationException("엑셀 파일 생성 중 I/O 오류가 발생했습니다.", e);
    }
  }

  /**
   * 월간 통계를 엑셀로 변환하는 메서드
   *
   * @param strategyId 전략 ID
   * @return 엑셀 파일 바이트 배열
   * @throws ExcelFileCreationException 엑셀 생성 중 발생하는 예외
   */
  public byte[] exportMonthlyStatisticsToExcel(Long strategyId) {
    List<MonthlyStatisticsEntity> monthlyStats = monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId);
    if (monthlyStats.isEmpty()) {
      throw new ExcelFileCreationException("Strategy ID " + strategyId + "에 해당하는 월간 통계가 없습니다.");
    }

    Workbook workbook;
    try {
      workbook = ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStats);
    } catch (Exception e) {
      throw new ExcelFileCreationException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
    }

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      workbook.write(out);
      workbook.close();
      return out.toByteArray();
    } catch (IOException e) {
      throw new ExcelFileCreationException("엑셀 파일 생성 중 I/O 오류가 발생했습니다.", e);
    }
  }

  /**
   * 일간 분석 지표 엑셀 파일 생성
   *
   * @param strategyId 전략 ID
   * @return 엑셀 파일 바이트 배열
   * @throws ExcelFileCreationException 엑셀 생성 중 발생하는 예외
   */
  public byte[] exportDailyAnalysisIndicatorsToExcel(Long strategyId) {
    List<DailyStatisticsEntity> statistics = dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId);
    if (statistics.isEmpty()) {
      throw new ExcelFileCreationException("Strategy ID " + strategyId + "에 해당하는 일간 분석 통계가 없습니다.");
    }

    Workbook workbook;
    try {
      workbook = ExcelGenerator.generateDailyAnalysisIndicatorsExcel(statistics);
    } catch (Exception e) {
      throw new ExcelFileCreationException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
    }

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      workbook.write(out);
      workbook.close();
      return out.toByteArray();
    } catch (IOException e) {
      throw new ExcelFileCreationException("엑셀 파일 생성 중 I/O 오류가 발생했습니다.", e);
    }
  }
}
