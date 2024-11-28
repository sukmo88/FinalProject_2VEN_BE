package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.ExcelValidationException;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelUploadServiceTest {

  private ExcelUploadService excelUploadService;

  @BeforeEach
  public void 설정() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    excelUploadService = new ExcelUploadService(validator);
  }

  @Test
  public void 엑셀데이터추출_유효한파일_데이터반환() throws Exception {
    // 유효한 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.now().toString());
    row1.createCell(1).setCellValue(1000.00); // 입금
    row1.createCell(2).setCellValue(-500.00); // 손실

    Row row2 = sheet.createRow(2);
    row2.createCell(0).setCellValue(LocalDate.now().plusDays(1).toString());
    row2.createCell(1).setCellValue(-2000.00); // 출금
    row2.createCell(2).setCellValue(1000.00); // 이익

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 유효한파일 = new MockMultipartFile(
            "file",
            "valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행
    List<DailyStatisticsReqDto> 결과 = excelUploadService.extractAndValidateData(유효한파일);

    // 검증
    assertEquals(2, 결과.size());

    DailyStatisticsReqDto dto1 = 결과.get(0);
    assertEquals(LocalDate.now(), dto1.getDate());
    assertEquals(BigDecimal.valueOf(1000.00), dto1.getDepWdPrice());
    assertEquals(BigDecimal.valueOf(-500.00), dto1.getDailyProfitLoss());

    DailyStatisticsReqDto dto2 = 결과.get(1);
    assertEquals(LocalDate.now().plusDays(1), dto2.getDate());
    assertEquals(BigDecimal.valueOf(-2000.00), dto2.getDepWdPrice());
    assertEquals(BigDecimal.valueOf(1000.00), dto2.getDailyProfitLoss());
  }

  @Test
  public void 엑셀데이터추출_여러시트파일_예외발생() throws Exception {
    // 여러 시트가 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    workbook.createSheet("Sheet1");
    workbook.createSheet("Sheet2");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 여러시트파일 = new MockMultipartFile(
            "file",
            "invalid_sheets.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행 및 검증
    ExcelValidationException 예외 = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndValidateData(여러시트파일);
    });

    assertEquals("엑셀 파일에 여러 시트가 포함되어 있습니다. 첫 번째 시트만 허용됩니다.", 예외.getMessage());
  }

  @Test
  public void 엑셀데이터추출_잘못된컬럼수_예외발생() throws Exception {
    // 잘못된 컬럼 수가 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");
    header.createCell(3).setCellValue("ExtraColumn"); // 4번째 컬럼

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.now().toString());
    row1.createCell(1).setCellValue(1000.00);
    row1.createCell(2).setCellValue(-500.00);
    row1.createCell(3).setCellValue("ExtraData");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 잘못된컬럼수파일 = new MockMultipartFile(
            "file",
            "wrong_columns.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행 및 검증
    ExcelValidationException 예외 = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndValidateData(잘못된컬럼수파일);
    });

    assertEquals("행 2의 칼럼 수가 정확히 3개가 아닙니다.", 예외.getMessage());
  }

  @Test
  public void 엑셀데이터추출_너무많은행_예외발생() throws Exception {
    // 2001개 이상의 행이 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    for (int i = 1; i <= 2001; i++) { // 2001개 행
      Row row = sheet.createRow(i);
      row.createCell(0).setCellValue(LocalDate.now().plusDays(i).toString());
      row.createCell(1).setCellValue(1000.00 + i);
      row.createCell(2).setCellValue(-500.00 + i);
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 너무많은행파일 = new MockMultipartFile(
            "file",
            "too_many_rows.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행 및 검증
    ExcelValidationException 예외 = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndValidateData(너무많은행파일);
    });

    assertEquals("엑셀 파일의 행 수가 2000개를 초과했습니다.", 예외.getMessage());
  }

  @Test
  public void 엑셀데이터추출_중복된날짜_예외발생() throws Exception {
    // 중복된 날짜가 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.of(2024, 11, 26).toString());
    row1.createCell(1).setCellValue(1000.00);
    row1.createCell(2).setCellValue(-500.00);

    Row row2Duplicate = sheet.createRow(2);
    row2Duplicate.createCell(0).setCellValue(LocalDate.of(2024, 11, 26).toString()); // 중복된 날짜
    row2Duplicate.createCell(1).setCellValue(2000.00);
    row2Duplicate.createCell(2).setCellValue(1000.00);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 중복된날짜파일 = new MockMultipartFile(
            "file",
            "duplicate_date.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행 및 검증
    ExcelValidationException 예외 = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndValidateData(중복된날짜파일);
    });

    assertEquals("중복된 날짜가 발견되었습니다: 2024-11-26 (행 2, 3)", 예외.getMessage());
  }

  @Test
  public void 엑셀데이터추출_잘못된날짜형식_예외발생() throws Exception {
    // 잘못된 날짜 형식이 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue("InvalidDate"); // 잘못된 날짜 형식
    row1.createCell(1).setCellValue(1000.00);
    row1.createCell(2).setCellValue(500.00);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 잘못된날짜형식파일 = new MockMultipartFile(
            "file",
            "invalid_date.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행 및 검증
    ExcelValidationException 예외 = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndValidateData(잘못된날짜형식파일);
    });

    assertEquals("행 2의 날짜 형식이 유효하지 않습니다.", 예외.getMessage());
  }

  @Test
  public void 엑셀데이터추출_잘못된손익형식_예외발생() throws Exception {
    // 일손익이 숫자가 아닌 값이 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.now().toString());
    row1.createCell(1).setCellValue(1000.00);
    row1.createCell(2).setCellValue("InvalidNumber"); // 숫자가 아닌 일손익

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 잘못된손익파일 = new MockMultipartFile(
            "file",
            "invalid_dailyProfitLoss.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행 및 검증
    ExcelValidationException 예외 = assertThrows(ExcelValidationException.class, () -> {
      excelUploadService.extractAndValidateData(잘못된손익파일);
    });

    assertEquals("행 2의 일손익 금액이 유효한 숫자가 아닙니다.", 예외.getMessage());
  }

  @Test
  public void 엑셀데이터추출_출금depWdPrice_데이터반환() throws Exception {
    // 출금(음수 depWdPrice)이 있는 엑셀 파일 생성
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Sheet1");
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("DepWdPrice");
    header.createCell(2).setCellValue("DailyProfitLoss");

    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue(LocalDate.now().toString());
    row1.createCell(1).setCellValue(-1500.00); // 출금
    row1.createCell(2).setCellValue(300.00); // 이익

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();
    MockMultipartFile 출금파일 = new MockMultipartFile(
            "file",
            "withdrawal.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
    );

    // 실행
    List<DailyStatisticsReqDto> 결과 = excelUploadService.extractAndValidateData(출금파일);

    // 검증
    assertEquals(1, 결과.size());

    DailyStatisticsReqDto dto = 결과.get(0);
    assertEquals(LocalDate.now(), dto.getDate());
    assertEquals(BigDecimal.valueOf(-1500.00), dto.getDepWdPrice());
    assertEquals(BigDecimal.valueOf(300.00), dto.getDailyProfitLoss());
  }
}
