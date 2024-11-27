// src/test/java/com/sysmatic2/finalbe/strategy/service/ExcelGeneratorServiceTest.java
package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelFileCreationException;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.util.ExcelGenerator;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  private StrategyEntity strategy;
  private List<DailyStatisticsEntity> dailyStatistics;
  private List<MonthlyStatisticsEntity> monthlyStatistics;

  @BeforeEach
  void setUp() {
    // StrategyEntity 설정
    strategy = new StrategyEntity();
    strategy.setStrategyId(1L);
    // 필요한 다른 필드도 설정

    // DailyStatisticsEntity 설정
    DailyStatisticsEntity daily1 = new DailyStatisticsEntity();
    daily1.setDailyStatisticsId(1L);
    daily1.setStrategyEntity(strategy);
    // 필요한 다른 필드도 설정

    dailyStatistics = List.of(daily1);

    // MonthlyStatisticsEntity 설정
    MonthlyStatisticsEntity monthly1 = new MonthlyStatisticsEntity();
    monthly1.setMonthlyStatisticsId(1L);
    monthly1.setStrategyEntity(strategy);
    // 필요한 다른 필드도 설정

    monthlyStatistics = List.of(monthly1);
  }

  /**
   * 1. exportDailyStatisticsToExcel - 성공 케이스
   */
  @Test
  void testExportDailyStatisticsToExcel_Success() throws Exception {
    Long strategyId = 1L;
    boolean includeAnalysis = true;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // ExcelGenerator의 정적 메서드 모킹
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      Workbook mockWorkbook = mock(Workbook.class);
      mockedExcelGenerator.when(() -> ExcelGenerator.generateDailyStatisticsExcel(dailyStatistics, includeAnalysis))
              .thenReturn(mockWorkbook);

      // Workbook의 write 메서드 모킹: 특정 바이트를 출력하도록 설정
      byte[] expectedBytes = "dummy bytes".getBytes();
      doAnswer(invocation -> {
        ByteArrayOutputStream out = invocation.getArgument(0);
        out.write(expectedBytes);
        return null;
      }).when(mockWorkbook).write(any(ByteArrayOutputStream.class));

      // 서비스 메서드 호출
      byte[] result = excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis);

      // 결과 검증
      assertNotNull(result);
      assertArrayEquals(expectedBytes, result);

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateDailyStatisticsExcel(dailyStatistics, includeAnalysis), times(1));
      // Workbook의 write 및 close 메서드 호출 검증
      verify(mockWorkbook, times(1)).write(any(ByteArrayOutputStream.class));
      verify(mockWorkbook, times(1)).close();
    }
  }

  /**
   * 2. exportDailyStatisticsToExcel - 데이터 없음
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
      excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis);
    });

    assertEquals("Strategy ID 1에 해당하는 일간 통계가 없습니다.", exception.getMessage());

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 3. exportDailyStatisticsToExcel - ExcelGenerator 예외 발생
   */
  @Test
  void testExportDailyStatisticsToExcel_ExcelGeneratorThrowsException() throws Exception {
    Long strategyId = 1L;
    boolean includeAnalysis = true;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // ExcelGenerator의 정적 메서드 모킹: 예외 발생
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      mockedExcelGenerator.when(() -> ExcelGenerator.generateDailyStatisticsExcel(dailyStatistics, includeAnalysis))
              .thenThrow(new RuntimeException("Generation error"));

      // 서비스 메서드 호출 시 예외 발생 확인
      ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
        excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis);
      });

      assertEquals("엑셀 파일 생성 중 오류가 발생했습니다.", exception.getMessage());
      assertTrue(exception.getCause() instanceof RuntimeException);
      assertEquals("Generation error", exception.getCause().getMessage());

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateDailyStatisticsExcel(dailyStatistics, includeAnalysis), times(1));
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 4. exportDailyStatisticsToExcel - Workbook.write IOException 발생
   */
  @Test
  void testExportDailyStatisticsToExcel_WorkbookWriteThrowsIOException() throws Exception {
    Long strategyId = 1L;
    boolean includeAnalysis = true;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // ExcelGenerator의 정적 메서드 모킹
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      Workbook mockWorkbook = mock(Workbook.class);
      mockedExcelGenerator.when(() -> ExcelGenerator.generateDailyStatisticsExcel(dailyStatistics, includeAnalysis))
              .thenReturn(mockWorkbook);

      // Workbook의 write 메서드 모킹: IOException 발생
      doThrow(new IOException("Write error")).when(mockWorkbook).write(any(ByteArrayOutputStream.class));

      // 서비스 메서드 호출 시 예외 발생 확인
      ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
        excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis);
      });

      assertEquals("엑셀 파일 생성 중 I/O 오류가 발생했습니다.", exception.getMessage());
      assertTrue(exception.getCause() instanceof IOException);
      assertEquals("Write error", exception.getCause().getMessage());

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateDailyStatisticsExcel(dailyStatistics, includeAnalysis), times(1));
      // Workbook의 write 및 close 메서드 호출 검증
      verify(mockWorkbook, times(1)).write(any(ByteArrayOutputStream.class));
      verify(mockWorkbook, times(1)).close();
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 5. exportMonthlyStatisticsToExcel - 성공 케이스
   */
  @Test
  void testExportMonthlyStatisticsToExcel_Success() throws Exception {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 월간 통계 반환
    when(monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(monthlyStatistics);

    // ExcelGenerator의 정적 메서드 모킹
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      Workbook mockWorkbook = mock(Workbook.class);
      mockedExcelGenerator.when(() -> ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics))
              .thenReturn(mockWorkbook);

      // Workbook의 write 메서드 모킹: 특정 바이트를 출력하도록 설정
      byte[] expectedBytes = "monthly dummy bytes".getBytes();
      doAnswer(invocation -> {
        ByteArrayOutputStream out = invocation.getArgument(0);
        out.write(expectedBytes);
        return null;
      }).when(mockWorkbook).write(any(ByteArrayOutputStream.class));

      // 서비스 메서드 호출
      byte[] result = excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId);

      // 결과 검증
      assertNotNull(result);
      assertArrayEquals(expectedBytes, result);

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics), times(1));
      // Workbook의 write 및 close 메서드 호출 검증
      verify(mockWorkbook, times(1)).write(any(ByteArrayOutputStream.class));
      verify(mockWorkbook, times(1)).close();
    }
  }

  /**
   * 6. exportMonthlyStatisticsToExcel - 데이터 없음
   */
  @Test
  void testExportMonthlyStatisticsToExcel_NoData() {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 월간 통계 없음
    when(monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(List.of());

    // 서비스 메서드 호출 시 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId);
    });

    assertEquals("Strategy ID 1에 해당하는 월간 통계가 없습니다.", exception.getMessage());

    // 리포지토리 호출 검증
    verify(monthlyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 7. exportMonthlyStatisticsToExcel - ExcelGenerator 예외 발생
   */
  @Test
  void testExportMonthlyStatisticsToExcel_ExcelGeneratorThrowsException() throws Exception {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 월간 통계 반환
    when(monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(monthlyStatistics);

    // ExcelGenerator의 정적 메서드 모킹: 예외 발생
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      mockedExcelGenerator.when(() -> ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics))
              .thenThrow(new RuntimeException("Generation error"));

      // 서비스 메서드 호출 시 예외 발생 확인
      ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
        excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId);
      });

      assertEquals("엑셀 파일 생성 중 오류가 발생했습니다.", exception.getMessage());
      assertTrue(exception.getCause() instanceof RuntimeException);
      assertEquals("Generation error", exception.getCause().getMessage());

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics), times(1));
    }

    // 리포지토리 호출 검증
    verify(monthlyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 8. exportMonthlyStatisticsToExcel - Workbook.write IOException 발생
   */
  @Test
  void testExportMonthlyStatisticsToExcel_WorkbookWriteThrowsIOException() throws Exception {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 월간 통계 반환
    when(monthlyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(monthlyStatistics);

    // ExcelGenerator의 정적 메서드 모킹
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      Workbook mockWorkbook = mock(Workbook.class);
      mockedExcelGenerator.when(() -> ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics))
              .thenReturn(mockWorkbook);

      // Workbook의 write 메서드 모킹: IOException 발생
      doThrow(new IOException("Write error")).when(mockWorkbook).write(any(ByteArrayOutputStream.class));

      // 서비스 메서드 호출 시 예외 발생 확인
      ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
        excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId);
      });

      assertEquals("엑셀 파일 생성 중 I/O 오류가 발생했습니다.", exception.getMessage());
      assertTrue(exception.getCause() instanceof IOException);
      assertEquals("Write error", exception.getCause().getMessage());

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateMonthlyStatisticsExcel(monthlyStatistics), times(1));
      // Workbook의 write 및 close 메서드 호출 검증
      verify(mockWorkbook, times(1)).write(any(ByteArrayOutputStream.class));
      verify(mockWorkbook, times(1)).close();
    }

    // 리포지토리 호출 검증
    verify(monthlyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 9. exportDailyAnalysisIndicatorsToExcel - 성공 케이스
   */
  @Test
  void testExportDailyAnalysisIndicatorsToExcel_Success() throws Exception {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 분석 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // ExcelGenerator의 정적 메서드 모킹
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      Workbook mockWorkbook = mock(Workbook.class);
      mockedExcelGenerator.when(() -> ExcelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics))
              .thenReturn(mockWorkbook);

      // Workbook의 write 메서드 모킹: 특정 바이트를 출력하도록 설정
      byte[] expectedBytes = "analysis dummy bytes".getBytes();
      doAnswer(invocation -> {
        ByteArrayOutputStream out = invocation.getArgument(0);
        out.write(expectedBytes);
        return null;
      }).when(mockWorkbook).write(any(ByteArrayOutputStream.class));

      // 서비스 메서드 호출
      byte[] result = excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId);

      // 결과 검증
      assertNotNull(result);
      assertArrayEquals(expectedBytes, result);

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics), times(1));
      // Workbook의 write 및 close 메서드 호출 검증
      verify(mockWorkbook, times(1)).write(any(ByteArrayOutputStream.class));
      verify(mockWorkbook, times(1)).close();
    }
  }

  /**
   * 10. exportDailyAnalysisIndicatorsToExcel - 데이터 없음
   */
  @Test
  void testExportDailyAnalysisIndicatorsToExcel_NoData() {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 분석 통계 없음
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(List.of());

    // 서비스 메서드 호출 시 예외 발생 확인
    ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
      excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId);
    });

    assertEquals("Strategy ID 1에 해당하는 일간 분석 통계가 없습니다.", exception.getMessage());

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 11. exportDailyAnalysisIndicatorsToExcel - ExcelGenerator 예외 발생
   */
  @Test
  void testExportDailyAnalysisIndicatorsToExcel_ExcelGeneratorThrowsException() throws Exception {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 분석 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // ExcelGenerator의 정적 메서드 모킹: 예외 발생
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      mockedExcelGenerator.when(() -> ExcelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics))
              .thenThrow(new RuntimeException("Generation error"));

      // 서비스 메서드 호출 시 예외 발생 확인
      ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
        excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId);
      });

      assertEquals("엑셀 파일 생성 중 오류가 발생했습니다.", exception.getMessage());
      assertTrue(exception.getCause() instanceof RuntimeException);
      assertEquals("Generation error", exception.getCause().getMessage());

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics), times(1));
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }

  /**
   * 12. exportDailyAnalysisIndicatorsToExcel - Workbook.write IOException 발생
   */
  @Test
  void testExportDailyAnalysisIndicatorsToExcel_WorkbookWriteThrowsIOException() throws Exception {
    Long strategyId = 1L;

    // 리포지토리 모킹: 전략 ID에 해당하는 일간 분석 통계 반환
    when(dailyStatisticsRepository.findByStrategyEntity_StrategyId(strategyId))
            .thenReturn(dailyStatistics);

    // ExcelGenerator의 정적 메서드 모킹
    try (MockedStatic<ExcelGenerator> mockedExcelGenerator = mockStatic(ExcelGenerator.class)) {
      Workbook mockWorkbook = mock(Workbook.class);
      mockedExcelGenerator.when(() -> ExcelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics))
              .thenReturn(mockWorkbook);

      // Workbook의 write 메서드 모킹: IOException 발생
      doThrow(new IOException("Write error")).when(mockWorkbook).write(any(ByteArrayOutputStream.class));

      // 서비스 메서드 호출 시 예외 발생 확인
      ExcelFileCreationException exception = assertThrows(ExcelFileCreationException.class, () -> {
        excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId);
      });

      assertEquals("엑셀 파일 생성 중 I/O 오류가 발생했습니다.", exception.getMessage());
      assertTrue(exception.getCause() instanceof IOException);
      assertEquals("Write error", exception.getCause().getMessage());

      // ExcelGenerator의 메서드 호출 검증
      mockedExcelGenerator.verify(() -> ExcelGenerator.generateDailyAnalysisIndicatorsExcel(dailyStatistics), times(1));
      // Workbook의 write 및 close 메서드 호출 검증
      verify(mockWorkbook, times(1)).write(any(ByteArrayOutputStream.class));
      verify(mockWorkbook, times(1)).close();
    }

    // 리포지토리 호출 검증
    verify(dailyStatisticsRepository, times(1)).findByStrategyEntity_StrategyId(strategyId);
  }
}
