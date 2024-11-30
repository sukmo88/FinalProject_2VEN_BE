// ExcelGeneratorService.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelFileCreationException;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.common.ExcelGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelGeneratorService {

  private final DailyStatisticsRepository dailyStatisticsRepository;
  private final MonthlyStatisticsRepository monthlyStatisticsRepository;
  private final ExcelGenerator excelGenerator; // ExcelGenerator 주입

  /**
   * 일간 통계 엑셀 파일 생성 (페이징 지원)
   *
   * @param strategyId      전략 ID
   * @param includeAnalysis 분석 지표 포함 여부
   * @param pageNumber      조회할 페이지 번호 (0부터 시작)
   * @param pageSize        페이지 크기
   * @return 엑셀 파일 바이트 배열
   * @throws ExcelFileCreationException 엑셀 생성 중 발생하는 예외
   */
  public byte[] exportDailyStatisticsToExcel(Long strategyId, boolean includeAnalysis, int pageNumber, int pageSize) {
    Page<DailyStatisticsEntity> dailyStatsPage = dailyStatisticsRepository
            .findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize));
    List<DailyStatisticsEntity> statistics = dailyStatsPage.getContent();

    if (statistics.isEmpty()) {
      throw new ExcelFileCreationException("Strategy ID " + strategyId + "에 해당하는 일간 통계가 없습니다.");
    }

    Workbook workbook;
    try {
      if (includeAnalysis) {
        workbook = excelGenerator.generateDailyAnalysisIndicatorsExcel(statistics);
      } else {
        workbook = excelGenerator.generateDailyStatisticsExcel(statistics);
      }
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
   * 월간 통계 엑셀 파일 생성 (페이징 지원)
   *
   * @param strategyId 전략 ID
   * @param pageNumber 조회할 페이지 번호 (0부터 시작)
   * @param pageSize   페이지 크기
   * @return 엑셀 파일 바이트 배열
   * @throws ExcelFileCreationException 엑셀 생성 중 발생하는 예외
   */
  public byte[] exportMonthlyStatisticsToExcel(Long strategyId, int pageNumber, int pageSize) {
    Page<MonthlyStatisticsEntity> monthlyStatsPage = monthlyStatisticsRepository
            .findByStrategyEntityStrategyIdOrderByAnalysisMonthAsc(strategyId, PageRequest.of(pageNumber, pageSize));
    List<MonthlyStatisticsEntity> statistics = monthlyStatsPage.getContent();

    if (statistics.isEmpty()) {
      throw new ExcelFileCreationException("Strategy ID " + strategyId + "에 해당하는 월간 통계가 없습니다.");
    }

    Workbook workbook;
    try {
      workbook = excelGenerator.generateMonthlyStatisticsExcel(statistics);
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
   * 일간 분석 지표 엑셀 파일 생성 (페이징 지원)
   *
   * @param strategyId 전략 ID
   * @param pageNumber 조회할 페이지 번호 (0부터 시작)
   * @param pageSize   페이지 크기
   * @return 엑셀 파일 바이트 배열
   * @throws ExcelFileCreationException 엑셀 생성 중 발생하는 예외
   */
  public byte[] exportDailyAnalysisIndicatorsToExcel(Long strategyId, int pageNumber, int pageSize) {
    Page<DailyStatisticsEntity> dailyStatsPage = dailyStatisticsRepository
            .findByStrategyEntityStrategyIdOrderByDateDesc(strategyId, PageRequest.of(pageNumber, pageSize));
    List<DailyStatisticsEntity> statistics = dailyStatsPage.getContent();

    if (statistics.isEmpty()) {
      throw new ExcelFileCreationException("Strategy ID " + strategyId + "에 해당하는 일간 분석 통계가 없습니다.");
    }

    Workbook workbook;
    try {
      workbook = excelGenerator.generateDailyAnalysisIndicatorsExcel(statistics);
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
