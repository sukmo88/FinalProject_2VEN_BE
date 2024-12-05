//package com.sysmatic2.finalbe.strategy.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
//import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
//import com.sysmatic2.finalbe.strategy.service.ExcelUploadService;
//import com.sysmatic2.finalbe.exception.ExcelValidationException;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.io.ByteArrayOutputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ExcelUploadController.class)
//public class ExcelUploadControllerTest {
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @MockBean // ExcelUploadService를 Mock으로 주입
//  private ExcelUploadService excelUploadService;
//
//  @Autowired
//  private ObjectMapper objectMapper;
//
//  private MockMultipartFile validFile;
//  private MockMultipartFile invalidFile;
//  private MockMultipartFile serverErrorFile;
//  private MockMultipartFile specificErrorFile;
//
//  private Long validStrategyId = 1L;
//  private Long invalidStrategyId = 999L;
//
//  @BeforeEach
//  public void setup() throws Exception {
//    // 성공적인 업로드를 위한 유효한 엑셀 파일 준비
//    XSSFWorkbook workbook = new XSSFWorkbook();
//    var sheet = workbook.createSheet("Sheet1");
//    var header = sheet.createRow(0);
//    header.createCell(0).setCellValue("Date");
//    header.createCell(1).setCellValue("DepWdPrice");
//    header.createCell(2).setCellValue("DailyProfitLoss");
//
//    var row1 = sheet.createRow(1);
//    row1.createCell(0).setCellValue(LocalDate.of(2024, 1, 1).toString());
//    row1.createCell(1).setCellValue(1000.00); // 입금
//    row1.createCell(2).setCellValue(200.00); // 이익
//
//    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//    workbook.write(bos);
//    workbook.close();
//    validFile = new MockMultipartFile(
//            "file",
//            "valid.xlsx",
//            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//            bos.toByteArray()
//    );
//
//    // 유효성 검사 실패를 위한 잘못된 엑셀 파일 준비 (여러 시트 포함)
//    XSSFWorkbook invalidWorkbook = new XSSFWorkbook();
//    invalidWorkbook.createSheet("Sheet1");
//    invalidWorkbook.createSheet("Sheet2"); // 두 번째 시트 추가 (유효성 검사 실패)
//    ByteArrayOutputStream invalidBos = new ByteArrayOutputStream();
//    invalidWorkbook.write(invalidBos);
//    invalidWorkbook.close();
//    invalidFile = new MockMultipartFile(
//            "file",
//            "invalid_sheets.xlsx",
//            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//            invalidBos.toByteArray()
//    );
//
//    // 서버 오류를 시뮬레이션하기 위한 파일 준비 (내용은 동일하지만 서비스에서 예외 발생)
//    XSSFWorkbook serverErrorWorkbook = new XSSFWorkbook();
//    var serverSheet = serverErrorWorkbook.createSheet("Sheet1");
//    var serverHeader = serverSheet.createRow(0);
//    serverHeader.createCell(0).setCellValue("Date");
//    serverHeader.createCell(1).setCellValue("DepWdPrice");
//    serverHeader.createCell(2).setCellValue("DailyProfitLoss");
//
//    var serverRow1 = serverSheet.createRow(1);
//    serverRow1.createCell(0).setCellValue("2024-01-11");
//    serverRow1.createCell(1).setCellValue(300000000);
//    serverRow1.createCell(2).setCellValue(-4018350);
//
//    ByteArrayOutputStream serverBos = new ByteArrayOutputStream();
//    serverErrorWorkbook.write(serverBos);
//    serverErrorWorkbook.close();
//    serverErrorFile = new MockMultipartFile(
//            "file",
//            "server_error.xlsx",
//            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//            serverBos.toByteArray()
//    );
//
//    // 특정 예외 시나리오를 위한 파일 준비 (예: average_loss 컬럼 누락)
//    XSSFWorkbook specificErrorWorkbook = new XSSFWorkbook();
//    var specificSheet = specificErrorWorkbook.createSheet("Sheet1");
//    var specificHeader = specificSheet.createRow(0);
//    specificHeader.createCell(0).setCellValue("Date");
//    specificHeader.createCell(1).setCellValue("DepWdPrice");
//    // "DailyProfitLoss" 컬럼 누락하여 오류 유발
//
//    ByteArrayOutputStream specificBos = new ByteArrayOutputStream();
//    specificErrorWorkbook.write(specificBos);
//    specificErrorWorkbook.close();
//    specificErrorFile = new MockMultipartFile(
//            "file",
//            "specific_error.xlsx",
//            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//            specificBos.toByteArray()
//    );
//  }
//
//  // 엑셀 파일 업로드가 성공적으로 처리되는지 확인하는 테스트
//  @Test
//  @WithMockUser // 사용자 인증 정보 제공
//  public void 엑셀파일업로드_성공적일경우_생성상태가반환되어야한다() throws Exception {
//    // 서비스의 동작을 미리 정의 (예시)
//    StrategyEntity strategyEntity = new StrategyEntity();
//    strategyEntity.setStrategyId(validStrategyId);
//
//    DailyStatisticsEntity mockSavedEntity = DailyStatisticsEntity.builder()
//            .dailyStatisticsId(1L)
//            .strategyEntity(strategyEntity)
//            .date(LocalDate.of(2024, 1, 1))
//            .depWdPrice(new BigDecimal("1000.00"))
//            .dailyProfitLoss(new BigDecimal("200.00"))
//            // 필요한 다른 필드도 설정
//            .build();
//
//    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), eq(validStrategyId)))
//            .thenReturn(List.of(mockSavedEntity));
//
//    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
//                    .file(validFile) // 유효한 엑셀 파일 업로드
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .with(csrf())) // CSRF 토큰 포함
//            .andExpect(status().isCreated()) // 상태 201 Created 예상
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // Content-Type 호환성 검사
//            .andExpect(jsonPath("$.msg").value("CREATE_SUCCESS")) // 응답 메시지 확인
//            .andExpect(jsonPath("$.data").isArray()) // data가 배열인지 확인
//            .andExpect(jsonPath("$.data[0].date").value("2024-01-01"))
//            .andExpect(jsonPath("$.data[0].depWdPrice").value(1000.00))
//            .andExpect(jsonPath("$.data[0].dailyProfitLoss").value(200.00));
//
//    // Service 메서드 호출 검증
//    verify(excelUploadService, times(1)).extractAndSaveData(any(MockMultipartFile.class), eq(validStrategyId));
//  }
//
//  // 잘못된 형식의 엑셀 파일이 업로드될 경우 400 Bad Request를 반환하는지 확인하는 테스트
//  @Test
//  @WithMockUser // 사용자 인증 정보 제공
//  public void 엑셀파일업로드_유효성검사실패_잘못된형식의엑셀파일일경우_오류메시지가반환되어야한다() throws Exception {
//    // 서비스의 동작을 미리 정의 (유효성 검사 실패 시 ExcelValidationException 던지기)
//    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), eq(validStrategyId)))
//            .thenThrow(new ExcelValidationException("엑셀 파일에 여러 시트가 포함되어 있습니다. 첫 번째 시트만 허용됩니다."));
//
//    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
//                    .file(invalidFile) // 잘못된 형식의 엑셀 파일 업로드
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .with(csrf())) // CSRF 토큰 포함
//            .andExpect(status().isBadRequest()) // 상태 400 Bad Request 예상
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.msg").value("EXCEL_VALIDATION_FAILED")) // 응답 메시지 확인
//            .andExpect(jsonPath("$.error").value("엑셀 파일에 여러 시트가 포함되어 있습니다. 첫 번째 시트만 허용됩니다.")); // 오류 메시지 확인
//
//    // Service 메서드 호출 검증
//    verify(excelUploadService, times(1)).extractAndSaveData(any(MockMultipartFile.class), eq(validStrategyId));
//  }
//
//  // 유효하지 않은 전략 ID로 엑셀 파일 업로드 시 400 Bad Request를 반환하는지 확인하는 테스트
//  @Test
//  @WithMockUser // 사용자 인증 정보 제공
//  public void 엑셀파일업로드_유효하지않은전략ID일경우_오류메시지가반환되어야한다() throws Exception {
//    // 서비스의 동작을 미리 정의 (유효하지 않은 전략 ID 시 IllegalArgumentException 던지기)
//    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), eq(invalidStrategyId)))
//            .thenThrow(new IllegalArgumentException("유효하지 않은 전략 ID입니다: " + invalidStrategyId));
//
//    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", invalidStrategyId)
//                    .file(validFile) // 유효한 엑셀 파일 업로드
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .with(csrf())) // CSRF 토큰 포함
//            .andExpect(status().isBadRequest()) // 상태 400 Bad Request 예상
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.msg").value("EXCEL_UPLOAD_FAILED")) // 응답 메시지 확인
//            .andExpect(jsonPath("$.error").value("유효하지 않은 전략 ID입니다: " + invalidStrategyId)); // 오류 메시지 확인
//
//    // Service 메서드 호출 검증
//    verify(excelUploadService, times(1)).extractAndSaveData(any(MockMultipartFile.class), eq(invalidStrategyId));
//  }
//
//  // 서버 내부 오류가 발생할 경우 500 Internal Server Error를 반환하는지 확인하는 테스트
//  @Test
//  @WithMockUser // 사용자 인증 정보 제공
//  public void 엑셀파일업로드_서버오류시_500반환() throws Exception {
//    // 서비스의 동작을 미리 정의 (서버 오류 시 RuntimeException 던지기)
//    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), eq(validStrategyId)))
//            .thenThrow(new RuntimeException("Unexpected error"));
//
//    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
//                    .file(serverErrorFile) // 서버 오류를 시뮬레이션하는 파일 업로드
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .with(csrf())) // CSRF 토큰 포함
//            .andExpect(status().isInternalServerError()) // 상태 500 Internal Server Error 예상
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.msg").value("EXCEL_UPLOAD_FAILED")) // 응답 메시지 확인
//            .andExpect(jsonPath("$.error").value("Unexpected error")); // 오류 메시지 확인
//
//    // Service 메서드 호출 검증
//    verify(excelUploadService, times(1)).extractAndSaveData(any(MockMultipartFile.class), eq(validStrategyId));
//  }
//
//  // 특정 예외 시나리오 - 컬럼 누락으로 인한 오류 발생 시 400 Bad Request를 반환하는 테스트
//  @Test
//  @WithMockUser // 사용자 인증 정보 제공
//  public void 엑셀파일업로드_특정예외시_400반환() throws Exception {
//    // 서비스의 동작을 미리 정의 (특정 조건 시 ExcelValidationException 던지기)
//    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), eq(1L)))
//            .thenThrow(new ExcelValidationException("Column 'average_loss' cannot be null"));
//
//    // 특정 예외 시나리오를 위한 파일 준비
//    ByteArrayOutputStream out = new ByteArrayOutputStream();
//    var workbook = new XSSFWorkbook();
//    var sheet = workbook.createSheet("Sheet1");
//
//    var header = sheet.createRow(0);
//    header.createCell(0).setCellValue("Date");
//    header.createCell(1).setCellValue("DepWdPrice");
//    // "DailyProfitLoss" 컬럼 누락하여 특정 예외 유발
//
//    workbook.write(out);
//    workbook.close();
//
//    MockMultipartFile specificErrorFile = new MockMultipartFile(
//            "file",
//            "specific_error.xlsx",
//            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
//            out.toByteArray()
//    );
//
//    mockMvc.perform(multipart("/api/strategies/1/upload")
//                    .file(specificErrorFile)
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .with(csrf()))
//            .andExpect(status().isBadRequest()) // HTTP 400 예상
//            .andExpect(jsonPath("$.msg").value("EXCEL_UPLOAD_FAILED"))
//            .andExpect(jsonPath("$.error").value("Column 'average_loss' cannot be null"));
//
//    // Service 메서드 호출 검증
//    verify(excelUploadService, times(1)).extractAndSaveData(any(MockMultipartFile.class), eq(1L));
//  }
//
//  // CSRF 토큰이 누락된 경우 403 Forbidden을 반환하는지 확인하는 테스트
//  @Test
//  public void 엑셀파일업로드_CSRF토큰누락시_403반환() throws Exception {
//    // CSRF 토큰이 누락된 경우 403을 반환하는지 확인
//    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
//                    .file(validFile) // 엑셀 파일 업로드
//                    .contentType(MediaType.MULTIPART_FORM_DATA))
//            .andExpect(status().isForbidden()); // 상태 403 Forbidden 예상
//  }
//}
