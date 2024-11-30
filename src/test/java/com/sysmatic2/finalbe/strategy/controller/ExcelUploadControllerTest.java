package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.service.ExcelUploadService;
import com.sysmatic2.finalbe.exception.ExcelValidationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

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

  @InjectMocks
  private ExcelUploadController excelUploadController;

  private MockMultipartFile file;

  @BeforeEach
  public void setup() {
    // 테스트용 엑셀 파일(mock file) 준비
    file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "dummy content".getBytes());
  }

  // 엑셀 파일 업로드가 성공적으로 처리되는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_성공적일경우_생성상태가반환되어야한다() throws Exception {
    // 서비스의 동작을 미리 정의 (예시)
    when(excelUploadService.extractAndValidateData(file)).thenReturn(List.of(new DailyStatisticsReqDto()));

    mockMvc.perform(multipart("/api/strategies/upload")
                    .file(file) // 엑셀 파일 업로드
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isCreated()) // 상태 201 Created 예상
            .andExpect(jsonPath("$.msg").value("CREATE_SUCCESS")) // 응답 메시지 확인
            .andExpect(jsonPath("$.data").isArray()); // data가 배열인지 확인
  }

  // 잘못된 형식의 엑셀 파일이 업로드될 경우 400 Bad Request를 반환하는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_유효성검사실패_잘못된형식의엑셀파일일경우_오류메시지가반환되어야한다() throws Exception {
    // 잘못된 형식의 엑셀 파일을 업로드 (dummy content로 처리)
    when(excelUploadService.extractAndValidateData(file)).thenThrow(new ExcelValidationException("Invalid Excel file format"));

    mockMvc.perform(multipart("/api/strategies/upload")
                    .file(file) // 잘못된 형식의 엑셀 파일
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isBadRequest()) // 상태 400 Bad Request 예상
            .andExpect(jsonPath("$.msg").value("EXCEL_VALIDATION_FAILED")) // 응답 메시지 확인
            .andExpect(jsonPath("$.error").value("Invalid Excel file format")); // 오류 메시지 확인
  }

  // 서버 내부 오류가 발생할 경우 500을 반환하는지 확인하는 테스트
  @Test
  @WithMockUser // 사용자 인증 정보 제공
  public void 엑셀파일업로드_서버오류시_500반환() throws Exception {
    // 서버 오류를 시뮬레이션
    when(excelUploadService.extractAndValidateData(file)).thenThrow(new RuntimeException("Unexpected error"));

    mockMvc.perform(multipart("/api/strategies/upload")
                    .file(file) // 엑셀 파일 업로드
                    .with(csrf())) // CSRF 토큰 포함
            .andExpect(status().isInternalServerError()) // 상태 500 Internal Server Error 예상
            .andExpect(jsonPath("$.msg").value("EXCEL_UPLOAD_FAILED")) // 응답 메시지 확인
            .andExpect(jsonPath("$.error").value("Unexpected error")); // 오류 메시지 확인
  }

  // CSRF 토큰이 누락된 경우 403 Forbidden을 반환하는지 확인하는 테스트
  @Test
  public void 엑셀파일업로드_CSRF토큰누락시_403반환() throws Exception {
    // CSRF 토큰이 누락된 경우 403을 반환하는지 확인
    mockMvc.perform(multipart("/api/strategies/upload")
                    .file(file)) // CSRF 토큰 누락
            .andExpect(status().isForbidden()); // 상태 403 Forbidden 예상
  }
}
