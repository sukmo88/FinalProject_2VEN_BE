package com.sysmatic2.finalbe.strategy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.config.SecurityConfig;
import com.sysmatic2.finalbe.exception.TradingTypeNotFoundException;
import com.sysmatic2.finalbe.strategy.dto.TradingTypeRequestDto;
import com.sysmatic2.finalbe.strategy.dto.TradingTypeResponseDto;
import com.sysmatic2.finalbe.strategy.service.TradingTypeService;
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

@WebMvcTest(TradingTypeController.class)
@Import(SecurityConfig.class)
class TradingTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradingTypeService tradingTypeService;

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper를 사용하여 객체를 JSON 형식으로 직렬화

    @Test
    @DisplayName("매매유형 목록 조회 - 정상 호출")
    @WithMockUser
    void getAllTradingTypes_shouldReturnOkStatus() throws Exception {
        // Given: 서비스의 findAllTradingTypes 메서드가 호출되었을 때 결과를 반환하도록 설정
        Map<String, Object> response = Map.of("data", "Sample Data");
        when(tradingTypeService.findAllTradingTypes(0, 2, null)).thenReturn(response);

        // When & Then: API 호출 후 상태코드와 응답 데이터 검증
        mockMvc.perform(get("/api/admin/trading-types")
                        .param("page", "0")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Sample Data")); // JSON 응답의 "data" 필드 확인
    }

    @Test
    @DisplayName("매매유형 목록 조회 - 잘못된 파라미터로 호출 시 400 반환")
    @WithMockUser
    void getAllTradingTypes_shouldReturnBadRequestWhenParamsInvalid() throws Exception {
        // When & Then: 페이지 번호가 잘못되었을 때
        mockMvc.perform(get("/api/admin/trading-types")
                        .param("page", "-1")  // 잘못된 페이지 번호
                        .param("pageSize", "2"))
                .andExpect(status().isBadRequest());  // HTTP 상태 코드 400 Bad Request 검증
    }

    @Test
    @DisplayName("매매유형 ID로 상세 조회 - 정상 호출")
    @WithMockUser
    void getTradingTypeById_shouldReturnTradingType() throws Exception {
        // Given: 서비스의 findTradingTypeById 메서드가 호출되었을 때 결과를 반환하도록 설정
        TradingTypeResponseDto dto = new TradingTypeResponseDto();
        dto.setTradingTypeId(1);
        dto.setTradingTypeName("Sample Type");
        when(tradingTypeService.findTradingTypeById(1)).thenReturn(dto);

        // When & Then: API 호출 후 상태코드와 응답 데이터 검증
        mockMvc.perform(get("/api/admin/trading_types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tradingTypeId").value(1))
                .andExpect(jsonPath("$.data.tradingTypeName").value("Sample Type"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("매매유형 ID로 조회 - 존재하지 않는 ID로 호출 시 404 반환")
    @WithMockUser
    void getTradingTypeById_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Given: 서비스가 존재하지 않는 ID에 대해 예외를 던지도록 설정
        int invalidId = 999;
        when(tradingTypeService.findTradingTypeById(invalidId))
                .thenThrow(new TradingTypeNotFoundException(invalidId));

        // When & Then: 존재하지 않는 ID로 조회 시 404 상태 검증
        mockMvc.perform(get("/api/admin/trading_types/" + invalidId))
                .andExpect(status().isNotFound());  // HTTP 상태 코드 404 Not Found 검증
    }

    @Test
    @DisplayName("매매유형 등록 - 정상 호출")
    @WithMockUser
    void createTradingType_shouldReturnCreatedStatus() throws Exception {
        // Given: 요청 DTO 생성
        TradingTypeRequestDto requestDto = new TradingTypeRequestDto();
        requestDto.setTradingTypeName("New Type");
        requestDto.setTradingTypeIcon("New Icon");

        // When & Then: API 호출 후 상태코드와 응답 데이터 검증
        mockMvc.perform(post("/api/admin/trading_types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("CREATE_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("매매유형 등록 - 유효하지 않은 데이터로 호출 시 400 반환")
    @WithMockUser
    void createTradingType_shouldReturnBadRequestWhenRequestInvalid() throws Exception {
        // Given: 잘못된 요청 DTO 생성 (필수 필드가 누락된 경우)
        TradingTypeRequestDto invalidDto = new TradingTypeRequestDto();
        invalidDto.setTradingTypeIcon("Icon Only");  // 이름 필드가 비어 있음

        // When & Then: POST 요청을 통해 매매유형 등록, 응답 코드 검증
        mockMvc.perform(post("/api/admin/trading_types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());  // HTTP 상태 코드 400 Bad Request 검증
    }

    @Test
    @DisplayName("매매유형 삭제 - 정상 호출")
    @WithMockUser
    void deleteTradingType_shouldReturnNoContentStatus() throws Exception {
        // When & Then: API 호출 후 상태코드 검증
        mockMvc.perform(delete("/api/admin/trading_types/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.msg").value("DELETE_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("매매유형 삭제 - 존재하지 않는 ID로 호출 시 404 반환")
    @WithMockUser
    void deleteTradingType_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Given: 서비스가 존재하지 않는 ID에 대해 예외를 던지도록 설정
        int invalidId = 999;
        Mockito.doThrow(new TradingTypeNotFoundException(invalidId))
                .when(tradingTypeService).deleteTradingType(invalidId);

        // When & Then: DELETE 요청을 통해 존재하지 않는 ID로 삭제 시 404 상태 검증
        mockMvc.perform(delete("/api/admin/trading_types/" + invalidId))
                .andExpect(status().isNotFound());  // HTTP 상태 코드 404 Not Found 검증
    }

    @Test
    @DisplayName("매매유형 수정 - 정상 호출")
    @WithMockUser
    void updateTradingType_shouldReturnNoContentStatus() throws Exception {
        // Given: 요청 DTO 생성
        TradingTypeRequestDto requestDto = new TradingTypeRequestDto();
        requestDto.setTradingTypeName("Updated Type");
        requestDto.setTradingTypeIcon("Updated Icon");

        // When & Then: API 호출 후 상태코드와 응답 데이터 검증
        mockMvc.perform(put("/api/admin/trading_types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.msg").value("UPDATE_SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("매매유형 수정 - 존재하지 않는 ID로 호출 시 404 반환")
    @WithMockUser
    void updateTradingType_shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Given: 요청 DTO 생성
        TradingTypeRequestDto requestDto = new TradingTypeRequestDto();
        requestDto.setTradingTypeName("Updated Type");
        requestDto.setTradingTypeIcon("Updated Icon");

        // 존재하지 않는 ID로 호출 시 예외 발생 설정
        int invalidId = 999;
        Mockito.doThrow(new TradingTypeNotFoundException(invalidId))
                .when(tradingTypeService).updateTradingType(eq(invalidId), any(TradingTypeRequestDto.class));
        // When & Then: PUT 요청을 통해 존재하지 않는 ID로 수정 시 404 상태 검증
        mockMvc.perform(put("/api/admin/trading_types/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());  // HTTP 상태 코드 404 Not Found 검증
    }

    @Test
    @DisplayName("매매유형 수정 - 유효하지 않은 데이터로 호출 시 400 반환")
    @WithMockUser
    void updateTradingType_shouldReturnBadRequestWhenRequestInvalid() throws Exception {
        // Given: 잘못된 요청 DTO 생성 (필수 필드가 누락된 경우)
        TradingTypeRequestDto invalidDto = new TradingTypeRequestDto();
        invalidDto.setTradingTypeIcon("Icon Only");  // 이름 필드가 비어 있음

        // When & Then: PUT 요청을 통해 매매유형 수정, 응답 코드 검증
        mockMvc.perform(put("/api/admin/trading_types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());  // HTTP 상태 코드 400 Bad Request 검증
    }
}