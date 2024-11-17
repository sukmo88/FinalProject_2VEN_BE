package com.sysmatic2.finalbe.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.admin.controller.TradingCycleController;
import com.sysmatic2.finalbe.config.SecurityConfig;
import com.sysmatic2.finalbe.exception.TradingCycleNotFoundException;
import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminRequestDto;
import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminResponseDto;
import com.sysmatic2.finalbe.admin.service.TradingCycleService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradingCycleController.class)
@Import(SecurityConfig.class)
class TradingCycleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradingCycleService tradingCycleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("투자주기 목록 조회 - 정상 호출")
    @WithMockUser
    void getAllTradingCycles_shouldReturnOkStatus() throws Exception {
        Map<String, Object> response = Map.of("data", "Sample Data");
        when(tradingCycleService.findAllTradingCycles(0, 2, null)).thenReturn(response);

        mockMvc.perform(get("/api/admin/trading-cycles")
                        .param("page", "0")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Sample Data"));
    }

    @Test
    @DisplayName("투자주기 목록 조회 - 잘못된 파라미터로 호출 시 400 반환")
    @WithMockUser
    void getAllTradingCycles_shouldReturnBadRequestWhenParamsInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/trading-cycles")
                        .param("page", "-1")
                        .param("pageSize", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("투자주기 ID로 상세 조회 - 정상 호출")
    @WithMockUser
    void getTradingCycleById_shouldReturnTradingCycle() throws Exception {
        TradingCycleAdminResponseDto dto = new TradingCycleAdminResponseDto();
        dto.setTradingCycleId(1);
        dto.setTradingCycleName("Sample Cycle");
        when(tradingCycleService.findTradingCycleById(1)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/trading-cycles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tradingCycleId").value(1))
                .andExpect(jsonPath("$.data.tradingCycleName").value("Sample Cycle"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("투자주기 ID로 조회 - 존재하지 않는 ID로 호출 시 404 반환")
    @WithMockUser
    void getTradingCycleById_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        int invalidId = 999;
        when(tradingCycleService.findTradingCycleById(invalidId))
                .thenThrow(new TradingCycleNotFoundException(invalidId));

        mockMvc.perform(get("/api/admin/trading-cycles/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("투자주기 등록 - 정상 호출")
    @WithMockUser
    void createTradingCycle_shouldReturnCreatedStatus() throws Exception {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleName("New Cycle");
        requestDto.setTradingCycleIcon("New Icon");

        mockMvc.perform(post("/api/admin/trading-cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("CREATE_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("투자주기 등록 - 유효하지 않은 데이터로 호출 시 400 반환")
    @WithMockUser
    void createTradingCycle_shouldReturnBadRequestWhenRequestInvalid() throws Exception {
        TradingCycleAdminRequestDto invalidDto = new TradingCycleAdminRequestDto();
        invalidDto.setTradingCycleIcon("Icon Only");

        mockMvc.perform(post("/api/admin/trading-cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("투자주기 삭제 - 정상 호출")
    @WithMockUser
    void deleteTradingCycle_shouldReturnOkStatus() throws Exception {
        mockMvc.perform(delete("/api/admin/trading-cycles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("DELETE_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("투자주기 삭제 - 존재하지 않는 ID로 호출 시 404 반환")
    @WithMockUser
    void deleteTradingCycle_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        int invalidId = 999;
        Mockito.doThrow(new TradingCycleNotFoundException(invalidId))
                .when(tradingCycleService).deleteTradingCycle(invalidId);

        mockMvc.perform(delete("/api/admin/trading-cycles/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("투자주기 수정 - 정상 호출")
    @WithMockUser
    void updateTradingCycle_shouldReturnOkStatus() throws Exception {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleName("Updated Cycle");
        requestDto.setTradingCycleIcon("Updated Icon");

        mockMvc.perform(put("/api/admin/trading-cycles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("UPDATE_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("투자주기 수정 - 존재하지 않는 ID로 호출 시 404 반환")
    @WithMockUser
    void updateTradingCycle_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleName("Updated Cycle");
        requestDto.setTradingCycleIcon("Updated Icon");

        int invalidId = 999;
        Mockito.doThrow(new TradingCycleNotFoundException(invalidId))
                .when(tradingCycleService).updateTradingCycle(eq(invalidId), any(TradingCycleAdminRequestDto.class));

        mockMvc.perform(put("/api/admin/trading-cycles/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("투자주기 수정 - 유효하지 않은 데이터로 호출 시 400 반환")
    @WithMockUser
    void updateTradingCycle_shouldReturnBadRequestWhenRequestInvalid() throws Exception {
        TradingCycleAdminRequestDto invalidDto = new TradingCycleAdminRequestDto();
        invalidDto.setTradingCycleIcon("Icon Only");

        mockMvc.perform(put("/api/admin/trading-cycles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}