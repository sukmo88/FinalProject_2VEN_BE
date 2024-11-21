package com.sysmatic2.finalbe.cs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.cs.dto.ConsultationCreateDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationDetailResponseDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationListResponseDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationUpdateDto;
import com.sysmatic2.finalbe.cs.dto.PaginatedResponseDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.exception.ConsultationNotFoundException;
import com.sysmatic2.finalbe.cs.service.ConsultationService;
import com.sysmatic2.finalbe.cs.mapper.ConsultationMapper;
import com.sysmatic2.finalbe.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 상담 컨트롤러 테스트
 */
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(ConsultationController.class)
@Import({ConsultationMapper.class, GlobalExceptionHandler.class}) // 글로벌 예외 핸들러 및 매퍼 포함
class ConsultationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ConsultationService consultationService;

  @Autowired
  private ObjectMapper objectMapper;

  private ConsultationDetailResponseDto detailDto;

  @BeforeEach
  void setUp() {
    // 상담 상세 응답 DTO 설정
    detailDto = ConsultationDetailResponseDto.builder()
            .id(100L)
            .investorId("inv123")
            .investorName("투자자닉네임")
            .traderId("trd456")
            .traderName("트레이더닉네임")
            .strategyId(1L)
            .strategyName("성장 전략")
            .investmentAmount(5000.0)
            .investmentDate(LocalDateTime.now())
            .title("투자 문의")
            .content("성장 전략에 대해 더 알고 싶습니다.")
            .status(ConsultationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  @Test
  void createConsultation_생성_성공() throws Exception {
    ConsultationCreateDto createDto = ConsultationCreateDto.builder()
            .investorId("inv123")
            .traderId("trd456")
            .strategyId(1L) // 전략 ID 사용
            .strategyName("성장 전략") // 전략 이름 사용
            .investmentAmount(5000.0)
            .investmentDate(LocalDateTime.now())
            .title("투자 문의")
            .content("성장 전략에 대해 더 알고 싶습니다.")
            .status(ConsultationStatus.PENDING)
            .build();

    when(consultationService.createConsultation(any(ConsultationCreateDto.class))).thenReturn(detailDto);

    mockMvc.perform(post("/api/consultations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto)))
            .andDo(print()) // 실제 응답 확인
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(100L))
            .andExpect(jsonPath("$.strategyId").value(1L))
            .andExpect(jsonPath("$.strategyName").value("성장 전략"))
            .andExpect(jsonPath("$.title").value("투자 문의"))
            .andExpect(jsonPath("$.investmentAmount").value(5000.0))
            .andExpect(jsonPath("$.investorName").value("투자자닉네임"))
            .andExpect(jsonPath("$.traderName").value("트레이더닉네임"));
    // 필요한 추가 검증을 여기에 작성할 수 있습니다.
  }

  @Test
  void createConsultation_잘못된_요청() throws Exception {
    ConsultationCreateDto createDto = new ConsultationCreateDto();
    // 필수 필드 누락 및 잘못된 값 설정
    createDto.setInvestorId(null);
    createDto.setTraderId(null);
    createDto.setStrategyId(null); // 전략 ID도 필수이므로 설정하지 않음
    createDto.setStrategyName(null); // 전략 이름도 필수이므로 설정하지 않음
    createDto.setInvestmentAmount(-100.0); // 잘못된 값
    createDto.setInvestmentDate(null);
    createDto.setTitle(null);
    createDto.setContent(null);
    createDto.setStatus(null);

    mockMvc.perform(post("/api/consultations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto)))
            .andDo(print()) // 실제 응답 확인을 위해 추가
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("유효성 검사에 실패했습니다."))
            .andExpect(jsonPath("$.errors.investorId").value("투자자 ID는 필수입니다."))
            .andExpect(jsonPath("$.errors.traderId").value("트레이더 ID는 필수입니다."))
            .andExpect(jsonPath("$.errors.strategyId").value("전략 ID는 필수입니다."))
            .andExpect(jsonPath("$.errors.strategyName").value("전략 이름은 필수입니다."))
            .andExpect(jsonPath("$.errors.investmentAmount").value("투자 금액은 양수여야 합니다."))
            .andExpect(jsonPath("$.errors.investmentDate").value("투자 시점은 필수입니다."))
            .andExpect(jsonPath("$.errors.title").value("상담 제목은 필수입니다."))
            .andExpect(jsonPath("$.errors.content").value("상담 내용은 필수입니다."))
            .andExpect(jsonPath("$.errors.status").value("상담 상태는 필수입니다."));
  }

  @Test
  void getConsultationById_존재함() throws Exception {
    when(consultationService.getConsultationById(100L)).thenReturn(detailDto);

    mockMvc.perform(get("/api/consultations/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(100L))
            .andExpect(jsonPath("$.strategyId").value(1L))
            .andExpect(jsonPath("$.strategyName").value("성장 전략"))
            .andExpect(jsonPath("$.investorName").value("투자자닉네임"))
            .andExpect(jsonPath("$.traderName").value("트레이더닉네임"))
            .andExpect(jsonPath("$.title").value("투자 문의"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    // 필요한 추가 검증을 여기에 작성할 수 있습니다.
  }

  @Test
  void getConsultationById_존재하지_않음() throws Exception {
    when(consultationService.getConsultationById(999L))
            .thenThrow(new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: 999"));

    mockMvc.perform(get("/api/consultations/999"))
            .andDo(print()) // 실제 응답 확인을 위해 추가
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("해당되는 데이터를 찾을 수 없습니다."))
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.errorType").value("ConsultationNotFoundException"));
  }

  @Test
  void getConsultations_리스트_조회() throws Exception {
    ConsultationListResponseDto listDto = ConsultationListResponseDto.builder()
            .id(100L)
            .investorName("투자자닉네임")
            .investorProfileUrl("http://example.com/inv123.png")
            .traderName("트레이더닉네임")
            .traderProfileUrl("http://example.com/trd456.png")
            .strategyId(1L)
            .strategyName("성장 전략")
            .investmentDate(LocalDateTime.now())
            .title("투자 문의")
            .status(ConsultationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    List<ConsultationListResponseDto> list = Collections.singletonList(listDto);
    PaginatedResponseDto<ConsultationListResponseDto> paginatedResponse = PaginatedResponseDto.<ConsultationListResponseDto>builder()
            .content(list)
            .page(0)
            .size(10)
            .totalElements(1)
            .totalPages(1)
            .build();

    when(consultationService.getConsultations(anyString(), any(), anyInt())).thenReturn(paginatedResponse);

    mockMvc.perform(get("/api/consultations")
                    .param("investorId", "inv123")
                    .param("page", "0"))
            .andDo(print()) // 실제 응답 확인을 위해 추가
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(100L))
            .andExpect(jsonPath("$.content[0].strategyId").value(1L))
            .andExpect(jsonPath("$.content[0].strategyName").value("성장 전략"))
            .andExpect(jsonPath("$.content[0].title").value("투자 문의"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  void updateConsultation_업데이트_성공() throws Exception {
    ConsultationUpdateDto updateDto = ConsultationUpdateDto.builder()
            .title("업데이트된 제목")
            .content("업데이트된 내용")
            .strategyId(2L) // 전략 ID 업데이트
            .strategyName("성장 전략 업데이트") // 전략 이름 업데이트
            .investmentAmount(6000.0)
            .investmentDate(LocalDateTime.now())
            .status(ConsultationStatus.COMPLETED)
            .build();

    ConsultationDetailResponseDto updatedDto = ConsultationDetailResponseDto.builder()
            .id(100L)
            .investorId("inv123")
            .investorName("투자자닉네임")
            .traderId("trd456")
            .traderName("트레이더닉네임")
            .strategyId(2L)
            .strategyName("성장 전략 업데이트")
            .investmentAmount(6000.0)
            .investmentDate(LocalDateTime.now())
            .title("업데이트된 제목")
            .content("업데이트된 내용")
            .status(ConsultationStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    when(consultationService.updateConsultation(eq(100L), any(ConsultationUpdateDto.class))).thenReturn(updatedDto);

    mockMvc.perform(put("/api/consultations/100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(100L))
            .andExpect(jsonPath("$.strategyId").value(2L))
            .andExpect(jsonPath("$.strategyName").value("성장 전략 업데이트"))
            .andExpect(jsonPath("$.title").value("업데이트된 제목"))
            .andExpect(jsonPath("$.content").value("업데이트된 내용"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.investorName").value("투자자닉네임"))
            .andExpect(jsonPath("$.traderName").value("트레이더닉네임"));
    // 필요한 추가 검증을 여기에 작성할 수 있습니다.
  }

  @Test
  void updateConsultation_존재하지_않음() throws Exception {
    // Given
    Long nonExistentId = 999L;
    ConsultationUpdateDto updateDto = ConsultationUpdateDto.builder()
            .title("업데이트된 제목")
            .content("업데이트된 내용")
            .strategyId(3L) // 전략 ID 업데이트
            .strategyName("존재하지 않는 전략") // 전략 이름 업데이트
            .investmentAmount(1000.0) // 유효한 양수 값 설정
            .investmentDate(LocalDateTime.now())
            .status(ConsultationStatus.COMPLETED)
            .build();

    // When
    when(consultationService.updateConsultation(eq(nonExistentId), any(ConsultationUpdateDto.class)))
            .thenThrow(new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + nonExistentId));

    // Then
    mockMvc.perform(put("/api/consultations/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print()) // 실제 응답 확인을 위해 추가
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("해당되는 데이터를 찾을 수 없습니다."))
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.errorType").value("ConsultationNotFoundException"));
  }

  @Test
  void deleteConsultation_삭제_성공() throws Exception {
    doNothing().when(consultationService).deleteConsultation(100L);

    mockMvc.perform(delete("/api/consultations/100"))
            .andExpect(status().isNoContent());
  }

  @Test
  void deleteConsultation_존재하지_않음() throws Exception {
    Long nonExistentId = 999L;
    doThrow(new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + nonExistentId))
            .when(consultationService).deleteConsultation(nonExistentId);

    mockMvc.perform(delete("/api/consultations/{id}", nonExistentId))
            .andDo(print()) // 실제 응답 확인을 위해 추가
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("해당되는 데이터를 찾을 수 없습니다."))
            .andExpect(jsonPath("$.error").value("NOT_FOUND"))
            .andExpect(jsonPath("$.errorType").value("ConsultationNotFoundException"));
  }
}
