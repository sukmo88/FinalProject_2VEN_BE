package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.exception.ExcelFileCreationException;
import com.sysmatic2.finalbe.strategy.service.ExcelGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StrategyExportController.class)
class StrategyExportControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ExcelGeneratorService excelGeneratorService;

  @Test
  @DisplayName("월간 통계 엑셀 다운로드 성공 테스트")
  @WithMockUser(username = "testUser", roles = {"USER"})
  void downloadMonthlyStatistics_Success() throws Exception {
    Long strategyId = 2L;
    int pageNumber = 1;
    int pageSize = 10;
    byte[] excelBytes = "dummy monthly excel content".getBytes(StandardCharsets.UTF_8);

    when(excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId, pageNumber, pageSize))
            .thenReturn(excelBytes);

    mockMvc.perform(get("/api/strategies/export/monthly")
                    .param("strategyId", strategyId.toString())
                    .param("pageNumber", String.valueOf(pageNumber))
                    .param("pageSize", String.valueOf(pageSize)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=monthly_statistics_page_2.xlsx"))
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"))
            .andExpect(content().bytes(excelBytes));

    verify(excelGeneratorService, times(1))
            .exportMonthlyStatisticsToExcel(strategyId, pageNumber, pageSize);
  }

  @Test
  @DisplayName("일간 통계 엑셀 다운로드 성공 테스트")
  @WithMockUser(username = "testUser", roles = {"USER"})
  void downloadDailyStatistics_Success() throws Exception {
    Long strategyId = 1L;
    boolean includeAnalysis = true;
    int pageNumber = 0;
    int pageSize = 10;
    byte[] excelBytes = "dummy excel content".getBytes(StandardCharsets.UTF_8);

    when(excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis, pageNumber, pageSize))
            .thenReturn(excelBytes);

    mockMvc.perform(get("/api/strategies/export/daily")
                    .param("strategyId", strategyId.toString())
                    .param("includeAnalysis", String.valueOf(includeAnalysis))
                    .param("pageNumber", String.valueOf(pageNumber))
                    .param("pageSize", String.valueOf(pageSize)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=daily_statistics_page_1.xlsx"))
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"))
            .andExpect(content().bytes(excelBytes));

    verify(excelGeneratorService, times(1))
            .exportDailyStatisticsToExcel(strategyId, includeAnalysis, pageNumber, pageSize);
  }

  @Test
  @DisplayName("일간 분석 지표 엑셀 다운로드 성공 테스트")
  @WithMockUser(username = "testUser", roles = {"USER"})
  void downloadDailyAnalysisIndicators_Success() throws Exception {
    Long strategyId = 3L;
    int pageNumber = 2;
    int pageSize = 10;
    byte[] excelBytes = "dummy daily analysis excel content".getBytes(StandardCharsets.UTF_8);

    when(excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId, pageNumber, pageSize))
            .thenReturn(excelBytes);

    mockMvc.perform(get("/api/strategies/export/daily-analysis")
                    .param("strategyId", strategyId.toString())
                    .param("pageNumber", String.valueOf(pageNumber))
                    .param("pageSize", String.valueOf(pageSize)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=daily_analysis_page_3.xlsx"))
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"))
            .andExpect(content().bytes(excelBytes));

    verify(excelGeneratorService, times(1))
            .exportDailyAnalysisIndicatorsToExcel(strategyId, pageNumber, pageSize);
  }

  @Test
  @DisplayName("페이지 사이즈 초과 테스트")
  @WithMockUser(username = "testUser", roles = {"USER"})
  void downloadDailyStatistics_InvalidPageSize() throws Exception {
    Long strategyId = 1L;
    int pageSize = 200; // 유효성 검사 실패 예상

    mockMvc.perform(get("/api/strategies/export/daily")
                    .param("strategyId", strategyId.toString())
                    .param("includeAnalysis", "false")
                    .param("pageNumber", "0")
                    .param("pageSize", String.valueOf(pageSize)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("유효성 검사에 실패했습니다."))
            .andExpect(jsonPath("$.errors['downloadDailyStatistics.pageSize']").value("pageSize는 최대 100까지 허용됩니다."));

    verify(excelGeneratorService, times(0))
            .exportDailyStatisticsToExcel(anyLong(), anyBoolean(), anyInt(), anyInt());
  }

  @Test
  @DisplayName("인증되지 않은 사용자 접근 테스트")
  void unauthorizedAccessTest() throws Exception {
    mockMvc.perform(get("/api/strategies/export/daily")
                    .param("strategyId", "1")
                    .param("includeAnalysis", "true")
                    .param("pageNumber", "0")
                    .param("pageSize", "10"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("전략 ID가 존재하지 않는 경우 테스트")
  @WithMockUser(username = "testUser", roles = {"USER"})
  void strategyNotFoundTest() throws Exception {
    // Given: 존재하지 않는 전략 ID
    Long strategyId = 999L;
    String expectedErrorMessage = "Strategy ID " + strategyId + "에 해당하는 일간 통계가 없습니다.";

    // When: 서비스에서 RuntimeException 발생 시
    when(excelGeneratorService.exportDailyStatisticsToExcel(eq(strategyId), anyBoolean(), anyInt(), anyInt()))
            .thenThrow(new ExcelFileCreationException(expectedErrorMessage));

    // Then: MockMvc 요청 및 예외 응답 검증
    mockMvc.perform(get("/api/strategies/export/daily")
                    .param("strategyId", strategyId.toString())
                    .param("includeAnalysis", "true")
                    .param("pageNumber", "0")
                    .param("pageSize", "10"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("EXCEL_CREATION_ERROR"))
            .andExpect(jsonPath("$.errorType").value("ExcelFileCreationException"))
            .andExpect(jsonPath("$.message").value(expectedErrorMessage))
            .andExpect(jsonPath("$.timestamp").exists());

    // Verify: 서비스 호출이 정확히 한 번 수행되었는지 확인
    verify(excelGeneratorService, times(1))
            .exportDailyStatisticsToExcel(eq(strategyId), anyBoolean(), anyInt(), anyInt());
  }

}
