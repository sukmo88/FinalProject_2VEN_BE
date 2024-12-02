package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelValidationException;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ExcelUploadServiceTest {

  @Mock
  private StrategyRepository strategyRepository;

  @Mock
  private DailyStatisticsService dailyStatisticsService;

  @Mock
  private DailyStatisticsRepository dailyStatisticsRepository;

  @InjectMocks
  private ExcelUploadService excelUploadService;

  private Validator validator;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    excelUploadService = new ExcelUploadService(validator, strategyRepository, dailyStatisticsService, dailyStatisticsRepository);
  }

  /**
   * extractAndSaveData 메서드가 정상적으로 데이터를 추출하고 저장하는지 테스트
   */
  @Test
  public void extractAndSaveData_유효한파일과전략ID일경우_데이터저장및반환() throws Exception {
    // 유효한 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.of(2024, 1, 1).toString());
    row1.createCell(1).setCellValue(1000.00); // 입금
    row1.createCell(2).setCellValue(200.00); // 이익

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile validFile = new MockMultipartFile(
            "file",
            "valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 전략 ID 설정 및 StrategyEntity 생성
    Long strategyId = 1L;
    StrategyEntity strategyEntity = new StrategyEntity();
    strategyEntity.setStrategyId(strategyId);

    when(strategyRepository.existsById(eq(strategyId))).thenReturn(true);

    // Mock the registerDailyStatistics method
    doNothing().when(dailyStatisticsService).registerDailyStatistics(eq(strategyId), any(DailyStatisticsReqDto.class));

    // Mock the repository to return the saved entity
    DailyStatisticsEntity savedEntity = DailyStatisticsEntity.builder()
            .dailyStatisticsId(1L)
            .strategyEntity(strategyEntity)
            .date(LocalDate.of(2024, 1, 1))
            .depWdPrice(new BigDecimal("1000.00"))
            .dailyProfitLoss(new BigDecimal("200.00"))
            .build();

    when(dailyStatisticsRepository.findByStrategyIdAndDate(eq(strategyId), eq(LocalDate.of(2024, 1, 1))))
            .thenReturn(Optional.of(savedEntity));

    // 메서드 실행
    List<DailyStatisticsEntity> result = excelUploadService.extractAndSaveData(validFile, strategyId);

    // 검증
    assertNotNull(result);
    assertEquals(1, result.size());

    DailyStatisticsEntity entity = result.get(0);
    assertEquals(LocalDate.of(2024, 1, 1), entity.getDate());
    assertTrue(entity.getDepWdPrice().compareTo(new BigDecimal("1000.00")) == 0, "depWdPrice should be 1000.00");
    assertTrue(entity.getDailyProfitLoss().compareTo(new BigDecimal("200.00")) == 0, "dailyProfitLoss should be 200.00");

    // Repository 메서드 호출 검증
    verify(strategyRepository, times(1)).existsById(eq(strategyId));
    verify(dailyStatisticsService, times(1)).registerDailyStatistics(eq(strategyId), any(DailyStatisticsReqDto.class));
    verify(dailyStatisticsRepository, times(1)).findByStrategyIdAndDate(eq(strategyId), eq(LocalDate.of(2024, 1, 1)));
  }


  /**
   * extractAndSaveData 메서드가 유효하지 않은 전략 ID일 경우 예외를 던지는지 테스트
   */
  @Test
  public void extractAndSaveData_유효하지않은전략ID일경우_예외발생() throws Exception {
    // 유효한 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.of(2024, 1, 1).toString());
    row1.createCell(1).setCellValue(1000.00); // 입금
    row1.createCell(2).setCellValue(200.00); // 이익

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile validFile = new MockMultipartFile(
            "file",
            "valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 유효하지 않은 전략 ID 설정
    Long invalidStrategyId = 999L;

    when(strategyRepository.existsById(invalidStrategyId)).thenReturn(false);

    // 메서드 실행 및 예외 검증
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      excelUploadService.extractAndSaveData(validFile, invalidStrategyId);
    });

    assertEquals("유효하지 않은 전략 ID입니다: " + invalidStrategyId, exception.getMessage());

    // Repository 메서드 호출 검증: existsById만 호출되었는지 확인
    verify(strategyRepository, times(1)).existsById(invalidStrategyId);
    verify(dailyStatisticsService, times(0)).registerDailyStatistics(anyLong(), any(DailyStatisticsReqDto.class));
    verify(dailyStatisticsRepository, times(0)).findByStrategyIdAndDate(anyLong(), any(LocalDate.class));
  }

  /**
   * extractAndSaveData 메서드가 저장 시 서버 오류가 발생할 경우 예외를 던지는지 테스트
   */
  @Test
  public void extractAndSaveData_저장중서버오류발생시_예외던지기() throws Exception {
    // 유효한 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.of(2024, 1, 1).toString());
    row1.createCell(1).setCellValue(1000.00); // 입금
    row1.createCell(2).setCellValue(200.00); // 이익

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile validFile = new MockMultipartFile(
            "file",
            "valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 전략 ID 설정 및 StrategyEntity 생성
    Long strategyId = 1L;

    // Mocking repository and service behavior
    when(strategyRepository.existsById(eq(strategyId))).thenReturn(true);

    // Mocking the service method to throw an exception
    doThrow(new RuntimeException("Database error"))
            .when(dailyStatisticsService).registerDailyStatistics(eq(strategyId), any(DailyStatisticsReqDto.class));

    // 메서드 실행 및 예외 검증
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      excelUploadService.extractAndSaveData(validFile, strategyId);
    });

    assertEquals("Database error", exception.getMessage());

    // Repository 및 서비스 호출 검증
    verify(strategyRepository, times(1)).existsById(eq(strategyId));
    verify(dailyStatisticsService, times(1)).registerDailyStatistics(eq(strategyId), any(DailyStatisticsReqDto.class));
    verify(dailyStatisticsRepository, times(0)).findByStrategyIdAndDate(anyLong(), any(LocalDate.class));
  }

  /**
   * extractAndSaveData 메서드가 유효하지 않은 엑셀 파일일 경우 예외를 던지는지 테스트
   */
  @Test
  public void extractAndSaveData_유효하지않은엑셀파일일경우_예외발생() throws Exception {
    // 잘못된 형식의 엑셀 파일 생성 (여러 시트 포함)
    XSSFWorkbook workbook = new XSSFWorkbook();
    workbook.createSheet("Sheet1");
    workbook.createSheet("Sheet2"); // 두 번째 시트 추가
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "invalid_sheets.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 전략 ID 설정 및 StrategyEntity 생성 (빌더 패턴 사용하지 않음)
    Long strategyId = 1L;
    StrategyEntity strategyEntity = new StrategyEntity();
    strategyEntity.setStrategyId(strategyId);
    // 필요에 따라 다른 필드도 설정
    // strategyEntity.setOtherField(...);

    when(strategyRepository.existsById(strategyId)).thenReturn(true);

    // Mocking the repository to throw exception when multiple sheets are present
    // Since the service itself checks for multiple sheets and throws exception,
    // we don't need to mock repository behavior here.

    // 메서드 실행 및 예외 검증
    ExcelValidationException exception = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndSaveData(invalidFile, strategyId);
    });

    assertEquals("엑셀 파일에 여러 시트가 포함되어 있습니다. 첫 번째 시트만 허용됩니다.", exception.getMessage());

    // Repository 메서드 호출 검증
    verify(strategyRepository, times(1)).existsById(strategyId);
    verify(dailyStatisticsService, times(0)).registerDailyStatistics(anyLong(), any(DailyStatisticsReqDto.class));
    verify(dailyStatisticsRepository, times(0)).findByStrategyIdAndDate(anyLong(), any(LocalDate.class));
  }
}
