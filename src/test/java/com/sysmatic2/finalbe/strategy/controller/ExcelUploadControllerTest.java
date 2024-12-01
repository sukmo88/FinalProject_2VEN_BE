package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.service.ExcelUploadService;
import com.sysmatic2.finalbe.exception.ExcelValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExcelUploadController.class)
public class ExcelUploadControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ExcelUploadService excelUploadService;

  private MockMultipartFile validFile;
  private MockMultipartFile invalidFile;
  private MockMultipartFile serverErrorFile;

  private Long validStrategyId = 1L;
  private Long invalidStrategyId = 999L;

  @BeforeEach
  public void setup() {
    // 성공적인 업로드를 위한 유효한 엑셀 파일 준비
    validFile = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "dummy content".getBytes()
    );

    // 유효성 검사 실패를 위한 잘못된 엑셀 파일 준비
    invalidFile = new MockMultipartFile(
            "file",
            "invalid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "invalid content".getBytes()
    );

    // 서버 오류를 시뮬레이션하기 위한 파일 준비 (내용은 동일하지만 서비스에서 예외 발생)
    serverErrorFile = new MockMultipartFile(
            "file",
            "server_error.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "server error content".getBytes()
    );
  }

  // 엑셀 파일 업로드가 성공적으로 처리되는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_성공적일경우_생성상태가반환되어야한다() throws Exception {
    // 서비스의 동작을 미리 정의 (예시)
    List<DailyStatisticsReqDto> mockResult = List.of(
            DailyStatisticsReqDto.builder()
                    .date(LocalDate.of(2024, 1, 1))
                    .depWdPrice(new BigDecimal("1000.00"))
                    .dailyProfitLoss(new BigDecimal("200.00"))
                    .build()
    );

    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), anyLong()))
            .thenReturn(mockResult);

    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
                    .file(validFile) // 유효한 엑셀 파일 업로드
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isCreated()) // 상태 201 Created 예상
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // Content-Type 호환성 검사
            .andExpect(jsonPath("$.msg").value("CREATE_SUCCESS")) // 응답 메시지 확인
            .andExpect(jsonPath("$.data").isArray()) // data가 배열인지 확인
            .andExpect(jsonPath("$.data[0].date").value("2024-01-01"))
            .andExpect(jsonPath("$.data[0].depWdPrice").value(1000.00))
            .andExpect(jsonPath("$.data[0].dailyProfitLoss").value(200.00));
  }

  // 잘못된 형식의 엑셀 파일이 업로드될 경우 400 Bad Request를 반환하는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_유효성검사실패_잘못된형식의엑셀파일일경우_오류메시지가반환되어야한다() throws Exception {
    // 서비스의 동작을 미리 정의 (유효성 검사 실패 시 예외 던지기)
    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), anyLong()))
            .thenThrow(new ExcelValidationException("Invalid Excel file format"));

    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
                    .file(invalidFile) // 잘못된 형식의 엑셀 파일 업로드
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isBadRequest()) // 상태 400 Bad Request 예상
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.msg").value("EXCEL_VALIDATION_FAILED")) // 응답 메시지 확인
            .andExpect(jsonPath("$.error").value("Invalid Excel file format")); // 오류 메시지 확인
  }

  // 전략 ID가 유효하지 않은 경우 400 Bad Request를 반환하는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_유효하지않은전략ID일경우_오류메시지가반환되어야한다() throws Exception {
    // 서비스의 동작을 미리 정의 (유효하지 않은 전략 ID 시 예외 던지기)
    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), anyLong()))
            .thenThrow(new IllegalArgumentException("Invalid strategyId: " + invalidStrategyId));

    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", invalidStrategyId)
                    .file(validFile) // 유효한 엑셀 파일 업로드
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isBadRequest()) // 상태 400 Bad Request 예상
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.msg").value("EXCEL_UPLOAD_FAILED")) // 응답 메시지 확인
            .andExpect(jsonPath("$.error").value("Invalid strategyId: " + invalidStrategyId)); // 오류 메시지 확인
  }

  // 서버 내부 오류가 발생할 경우 500 Internal Server Error를 반환하는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_서버오류시_500반환() throws Exception {
    // 서비스의 동작을 미리 정의 (서버 오류 시 예외 던지기)
    when(excelUploadService.extractAndSaveData(any(MockMultipartFile.class), anyLong()))
            .thenThrow(new RuntimeException("Unexpected error"));

    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
                    .file(serverErrorFile) // 서버 오류를 시뮬레이션하는 파일 업로드
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isInternalServerError()) // 상태 500 Internal Server Error 예상
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.msg").value("EXCEL_UPLOAD_FAILED")) // 응답 메시지 확인
            .andExpect(jsonPath("$.error").value("Unexpected error")); // 오류 메시지 확인
  }

  // CSRF 토큰이 누락된 경우 403 Forbidden을 반환하는지 확인하는 테스트
  @Test
  public void 엑셀파일업로드_CSRF토큰누락시_403반환() throws Exception {
    // CSRF 토큰이 누락된 경우 403을 반환하는지 확인
    mockMvc.perform(multipart("/api/strategies/{strategyId}/upload", validStrategyId)
                    .file(validFile) // 엑셀 파일 업로드
                    .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isForbidden()); // 상태 403 Forbidden 예상
  }
}
