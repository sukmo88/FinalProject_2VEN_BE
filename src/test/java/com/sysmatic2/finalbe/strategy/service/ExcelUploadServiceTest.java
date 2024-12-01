package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelValidationException;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
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
  private DailyStatisticsRepository dailyStatisticsRepository;

  @InjectMocks
  private ExcelUploadService excelUploadService;

  private Validator validator;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    excelUploadService = new ExcelUploadService(validator, strategyRepository, dailyStatisticsRepository);
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

    // 전략 ID 설정 및 StrategyEntity 생성 (빌더 패턴 사용하지 않음)
    Long strategyId = 1L;
    StrategyEntity strategyEntity = new StrategyEntity();
    strategyEntity.setStrategyId(strategyId);
    // 필요에 따라 다른 필드도 설정
    // strategyEntity.setOtherField(...);

    when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));
    when(dailyStatisticsRepository.saveAll(anyList())).thenReturn(List.of());

    // 메서드 실행
    List<DailyStatisticsReqDto> result = excelUploadService.extractAndSaveData(validFile, strategyId);

    // 검증
    assertNotNull(result);
    assertEquals(1, result.size());

    DailyStatisticsReqDto dto = result.get(0);
    assertEquals(LocalDate.of(2024, 1, 1), dto.getDate());

    // BigDecimal 비교 수정
    assertTrue(dto.getDepWdPrice().compareTo(new BigDecimal("1000.00")) == 0, "depWdPrice should be 1000.00");
    assertTrue(dto.getDailyProfitLoss().compareTo(new BigDecimal("200.00")) == 0, "dailyProfitLoss should be 200.00");

    // Repository 메서드 호출 검증
    verify(strategyRepository, times(1)).findById(strategyId);
    verify(dailyStatisticsRepository, times(1)).saveAll(anyList());
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

    when(strategyRepository.findById(invalidStrategyId)).thenReturn(Optional.empty());

    // 메서드 실행 및 예외 검증
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      excelUploadService.extractAndSaveData(validFile, invalidStrategyId);
    });

    assertEquals("Invalid strategyId: " + invalidStrategyId, exception.getMessage());

    // Repository 메서드 호출 검증: findById가 호출되었는지 확인
    verify(strategyRepository, times(1)).findById(invalidStrategyId);
    verify(dailyStatisticsRepository, times(0)).saveAll(anyList());
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

    // 전략 ID 설정 및 StrategyEntity 생성 (빌더 패턴 사용하지 않음)
    Long strategyId = 1L;
    StrategyEntity strategyEntity = new StrategyEntity();
    strategyEntity.setStrategyId(strategyId);
    // 필요에 따라 다른 필드도 설정
    // strategyEntity.setOtherField(...);

    when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));
    when(dailyStatisticsRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

    // 메서드 실행 및 예외 검증
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      excelUploadService.extractAndSaveData(validFile, strategyId);
    });

    assertEquals("Database error", exception.getMessage());

    // Repository 메서드 호출 검증
    verify(strategyRepository, times(1)).findById(strategyId);
    verify(dailyStatisticsRepository, times(1)).saveAll(anyList());
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

    when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategyEntity));

    // 메서드 실행 및 예외 검증
    ExcelValidationException exception = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndSaveData(invalidFile, strategyId);
    });

    assertEquals("엑셀 파일에 여러 시트가 포함되어 있습니다. 첫 번째 시트만 허용됩니다.", exception.getMessage());

    // Repository 메서드 호출 검증: 예외 발생 시 findById는 호출되지 않았어야 함
    verify(strategyRepository, never()).findById(anyLong());
    verify(dailyStatisticsRepository, times(0)).saveAll(anyList());
  }
}
