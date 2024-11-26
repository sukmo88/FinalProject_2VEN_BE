package com.sysmatic2.finalbe.strategy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.admin.repository.StrategyApprovalRequestsRepository;
import com.sysmatic2.finalbe.strategy.dto.StrategyPayloadDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyRegistrationDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyResponseDto;
import com.sysmatic2.finalbe.strategy.service.DailyStatisticsService;
import com.sysmatic2.finalbe.strategy.service.ExcelGeneratorService;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.startsWith;

@WebMvcTest(StrategyController.class)
class StrategyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StrategyService strategyService;

    @MockBean
    private ExcelGeneratorService excelGeneratorService; // 추가

    @MockBean
    private StrategyApprovalRequestsRepository strategyApprovalRequestsRepository; // 추가

    @MockBean
    private DailyStatisticsService dailyStatisticsService; // 추가

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Strategy Registration Form 조회 - 데이터 포함")
    @WithMockUser
    void testGetStrategyRegistrationForm_WithData() throws Exception {
        // Given
        StrategyRegistrationDto mockRegistrationDto = new StrategyRegistrationDto();
        mockRegistrationDto.setTradingTypeRegistrationDtoList(List.of());
        mockRegistrationDto.setInvestmentAssetClassesRegistrationDtoList(List.of());
        mockRegistrationDto.setTradingCycleRegistrationDtoList(List.of());
        when(strategyService.getStrategyRegistrationForm()).thenReturn(mockRegistrationDto);

        // When & Then
        mockMvc.perform(get("/api/strategies/registration-form"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(strategyService, times(1)).getStrategyRegistrationForm();
    }

    @Test
    @DisplayName("전략 목록 조회 - 성공")
    @WithMockUser
    void testGetStrategies_Success() throws Exception {
        // Given
        Map<String, Object> mockResponse = Map.of(
                "data", List.of("Strategy 1", "Strategy 2"),
                "pageSize", 10,
                "totalPages", 1,
                "totalElements", 2L
        );
        when(strategyService.getStrategies(eq(1), eq(2), eq(0), eq(10))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/strategies")
                        .param("tradingCycleId", "1")
                        .param("investmentAssetClassesId", "2")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2L));

        verify(strategyService, times(1)).getStrategies(1, 2, 0, 10);
    }

    @Test
    @DisplayName("전략 목록 조회 - 필터 조건 없음")
    @WithMockUser
    void testGetStrategies_NoFilters() throws Exception {
        // Given
        Map<String, Object> mockResponse = Map.of(
                "data", List.of("Strategy A", "Strategy B"),
                "pageSize", 10,
                "totalPages", 1,
                "totalElements", 2L
        );
        when(strategyService.getStrategies(isNull(), isNull(), eq(0), eq(10))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/strategies")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalElements").value(2L));

        verify(strategyService, times(1)).getStrategies(null, null, 0, 10);
    }

    @Test
    @DisplayName("전략 목록 조회 - 잘못된 요청")
    @WithMockUser
    void testGetStrategies_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/strategies")
                        .param("page", "-1") // 잘못된 페이지 번호
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    // 11. 일간 지표 다운로드 테스트
    @Test
    @DisplayName("일간 지표 다운로드 - 성공")
    @WithMockUser
    void testDownloadDailyStatisticsExcel_Success() throws Exception {
        // Given
        byte[] mockExcelData = "Excel Data".getBytes();
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(mockExcelData);
        when(excelGeneratorService.generateDailyStatisticsExcel()).thenReturn(mockInputStream);

        // When & Then
        mockMvc.perform(get("/api/strategies/download/daily-indicators"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=daily_statistics.xlsx"))
                .andExpect(header().string("Content-Type", startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(content().bytes(mockExcelData));

        verify(excelGeneratorService, times(1)).generateDailyStatisticsExcel();
    }

    // 12. 일간 분석 지표 다운로드 테스트
    @Test
    @DisplayName("일간 분석 지표 다운로드 - 성공")
    @WithMockUser
    void testDownloadDailyAnalysisIndicatorsExcel_Success() throws Exception {
        // Given
        byte[] mockExcelData = "Daily Analysis Excel Data".getBytes();
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(mockExcelData);
        when(excelGeneratorService.generateDailyAnalysisIndicatorsExcel()).thenReturn(mockInputStream);

        // When & Then
        mockMvc.perform(get("/api/strategies/download/daily-analysis-indicators"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=daily_analysis_indicators.xlsx"))
                .andExpect(header().string("Content-Type", startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(content().bytes(mockExcelData));

        verify(excelGeneratorService, times(1)).generateDailyAnalysisIndicatorsExcel();
    }

    // 13. 월간 지표 다운로드 테스트
    @Test
    @DisplayName("월간 지표 다운로드 - 성공")
    @WithMockUser
    void testDownloadMonthlyStatisticsExcel_Success() throws Exception {
        // Given
        byte[] mockExcelData = "Monthly Statistics Excel Data".getBytes();
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(mockExcelData);
        when(excelGeneratorService.generateMonthlyStatisticsExcel()).thenReturn(mockInputStream);

        // When & Then
        mockMvc.perform(get("/api/strategies/download/monthly-indicators"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=monthly_statistics.xlsx"))
                .andExpect(header().string("Content-Type", startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(content().bytes(mockExcelData));

        verify(excelGeneratorService, times(1)).generateMonthlyStatisticsExcel();
    }

    // 예외 케이스 테스트 (수정)
    @Test
    @DisplayName("일간 지표 다운로드 - 서버 오류")
    @WithMockUser
    void testDownloadDailyStatisticsExcel_ServerError() throws Exception {
        // Given
        when(excelGeneratorService.generateDailyStatisticsExcel()).thenThrow(new IOException("파일 생성 오류"));

        // When & Then
        mockMvc.perform(get("/api/strategies/download/daily-indicators"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("알 수 없는 오류가 발생했습니다."));

        verify(excelGeneratorService, times(1)).generateDailyStatisticsExcel();
    }
}