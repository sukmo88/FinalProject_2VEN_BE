package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.cs.mapper.ConsultationMapper;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import com.sysmatic2.finalbe.exception.ConsultationAlreadyCompletedException;
import com.sysmatic2.finalbe.exception.ConsultationNotFoundException;
import com.sysmatic2.finalbe.exception.InvestorNotFoundException;
import com.sysmatic2.finalbe.exception.ReplyNotFoundException;
import com.sysmatic2.finalbe.exception.StrategyNotFoundException;
import com.sysmatic2.finalbe.exception.TraderNotFoundException;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConsultationService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

  @Mock
  private ConsultationRepository consultationRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private StrategyRepository strategyRepository;

  @Mock
  private ConsultationMapper consultationMapper;

  @InjectMocks
  private ConsultationService consultationService;

  private MemberEntity investor;
  private MemberEntity trader;
  private StrategyEntity strategy;
  private ConsultationEntity consultationEntity;
  private ConsultationDetailResponseDto consultationDetailDto;
  private ConsultationListResponseDto consultationListDto;

  @BeforeEach
  void setUp() {
    // 투자자 설정
    investor = new MemberEntity();
    investor.setMemberId("inv123");
    investor.setNickname("투자자닉네임");

    // 트레이더 설정
    trader = new MemberEntity();
    trader.setMemberId("trd456");
    trader.setNickname("트레이더닉네임");

    // 전략 설정
    strategy = new StrategyEntity();
    strategy.setStrategyId(1L);
    strategy.setStrategyTitle("성장 전략");

    // 상담 엔티티 설정
    consultationEntity = new ConsultationEntity();
    consultationEntity.setId(100L);
    consultationEntity.setInvestor(investor);
    consultationEntity.setTrader(trader);
    consultationEntity.setStrategy(strategy);
    consultationEntity.setInvestmentAmount(5000.0);
    consultationEntity.setInvestmentDate(LocalDateTime.now());
    consultationEntity.setTitle("투자 문의");
    consultationEntity.setContent("성장 전략에 대해 더 알고 싶습니다.");
    consultationEntity.setStatus(ConsultationStatus.PENDING);
    consultationEntity.setCreatedAt(LocalDateTime.now());
    consultationEntity.setUpdatedAt(LocalDateTime.now());

    // 상담 상세 응답 DTO 설정
    consultationDetailDto = new ConsultationDetailResponseDto();
    consultationDetailDto.setId(100L);
    consultationDetailDto.setInvestorId("inv123");
    consultationDetailDto.setInvestorName("투자자닉네임");
    consultationDetailDto.setTraderId("trd456");
    consultationDetailDto.setTraderName("트레이더닉네임");
    consultationDetailDto.setStrategyId(1L);
    consultationDetailDto.setStrategyName("성장 전략");
    consultationDetailDto.setInvestmentAmount(5000.0);
    consultationDetailDto.setInvestmentDate(consultationEntity.getInvestmentDate());
    consultationDetailDto.setTitle("투자 문의");
    consultationDetailDto.setContent("성장 전략에 대해 더 알고 싶습니다.");
    consultationDetailDto.setStatus(ConsultationStatus.PENDING);
    consultationDetailDto.setCreatedAt(consultationEntity.getCreatedAt());
    consultationDetailDto.setUpdatedAt(consultationEntity.getUpdatedAt());

    // 상담 리스트 응답 DTO 설정
    consultationListDto = new ConsultationListResponseDto();
    consultationListDto.setId(100L);
    consultationListDto.setInvestorName("투자자닉네임");
    consultationListDto.setTraderName("트레이더닉네임");
    consultationListDto.setStrategyName("성장 전략");
    consultationListDto.setInvestmentDate(consultationEntity.getInvestmentDate());
    consultationListDto.setTitle("투자 문의");
    consultationListDto.setStatus(ConsultationStatus.PENDING);
    consultationListDto.setCreatedAt(consultationEntity.getCreatedAt());
  }

  /**
   * 상담 생성 테스트 - 성공
   */
  @Test
  void 상담_생성_성공() {
    // Given
    ConsultationCreateDto createDto = new ConsultationCreateDto();
    createDto.setInvestorId("inv123");
    createDto.setTraderId("trd456");
    createDto.setStrategyId(1L);
    createDto.setStrategyName("성장 전략"); // 일치하도록 설정
    createDto.setInvestmentAmount(5000.0);
    createDto.setInvestmentDate(LocalDateTime.now());
    createDto.setTitle("투자 문의");
    createDto.setContent("성장 전략에 대해 더 알고 싶습니다.");
    createDto.setStatus(ConsultationStatus.PENDING);

    when(memberRepository.findById("inv123")).thenReturn(Optional.of(investor));
    when(memberRepository.findById("trd456")).thenReturn(Optional.of(trader));
    when(strategyRepository.findById(1L)).thenReturn(Optional.of(strategy));
    when(consultationMapper.toEntityFromCreateDto(any(ConsultationCreateDto.class), any(MemberEntity.class), any(MemberEntity.class), any(StrategyEntity.class)))
            .thenReturn(consultationEntity);
    when(consultationRepository.save(any(ConsultationEntity.class))).thenReturn(consultationEntity);
    when(consultationMapper.toDetailResponseDto(eq(consultationEntity))).thenReturn(consultationDetailDto);

    // When
    ConsultationDetailResponseDto result = consultationService.createConsultation(createDto);
    System.out.println("Result: " + result); // 디버깅용 출력

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getTitle()).isEqualTo("투자 문의");
    assertThat(result.getStrategyId()).isEqualTo(1L); // 추가 검증
    assertThat(result.getStrategyName()).isEqualTo("성장 전략"); // 일치 검증
    verify(memberRepository, times(1)).findById("inv123");
    verify(memberRepository, times(1)).findById("trd456");
    verify(strategyRepository, times(1)).findById(1L);
    verify(consultationMapper, times(1)).toEntityFromCreateDto(createDto, investor, trader, strategy);
    verify(consultationRepository, times(1)).save(consultationEntity);
    verify(consultationMapper, times(1)).toDetailResponseDto(consultationEntity);
  }

  /**
   * 상담 목록 조회 테스트 - 투자자 필터링
   */
  @Test
  void 상담_목록_조회_투자자_필터링() {
    // Given
    String investorId = "inv123";
    String traderId = null;
    int page = 0;
    ConsultationListResponseDto listDto = consultationListDto;

    ConsultationEntity consultationEntity2 = new ConsultationEntity();
    consultationEntity2.setId(101L);
    consultationEntity2.setInvestor(investor);
    consultationEntity2.setTrader(trader);
    consultationEntity2.setStrategy(strategy);
    consultationEntity2.setInvestmentAmount(7000.0);
    consultationEntity2.setInvestmentDate(LocalDateTime.now());
    consultationEntity2.setTitle("추가 상담");
    consultationEntity2.setContent("추가 상담 내용입니다.");
    consultationEntity2.setStatus(ConsultationStatus.COMPLETED);
    consultationEntity2.setCreatedAt(LocalDateTime.now());
    consultationEntity2.setUpdatedAt(LocalDateTime.now());

    ConsultationListResponseDto consultationListDto2 = new ConsultationListResponseDto();
    consultationListDto2.setId(101L);
    consultationListDto2.setInvestorName("투자자닉네임");
    consultationListDto2.setTraderName("트레이더닉네임");
    consultationListDto2.setStrategyName("성장 전략");
    consultationListDto2.setInvestmentDate(consultationEntity2.getInvestmentDate());
    consultationListDto2.setTitle("추가 상담");
    consultationListDto2.setStatus(ConsultationStatus.COMPLETED);
    consultationListDto2.setCreatedAt(consultationEntity2.getCreatedAt());

    List<ConsultationEntity> entityList = Arrays.asList(consultationEntity);
    List<ConsultationListResponseDto> dtoList = Arrays.asList(consultationListDto);

    when(consultationRepository.findAllByInvestor_MemberId(eq(investorId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(entityList, PageRequest.of(page, 10), 1));
    when(consultationMapper.toListResponseDtos(entityList)).thenReturn(dtoList);

    // When
    PaginatedResponseDto<ConsultationListResponseDto> result = consultationService.getConsultations(investorId, traderId, page);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("투자 문의");
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(10);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getTotalPages()).isEqualTo(1);
    verify(consultationRepository, times(1)).findAllByInvestor_MemberId(eq(investorId), any(Pageable.class));
    verify(consultationMapper, times(1)).toListResponseDtos(entityList);
  }

  /**
   * 상담 목록 조회 테스트 - 트레이더 필터링
   */
  @Test
  void 상담_목록_조회_트레이더_필터링() {
    // Given
    String investorId = null;
    String traderId = "trd456";
    int page = 0;
    ConsultationListResponseDto listDto = consultationListDto;

    List<ConsultationEntity> entityList = Arrays.asList(consultationEntity);
    List<ConsultationListResponseDto> dtoList = Arrays.asList(consultationListDto);

    when(consultationRepository.findAllByTrader_MemberId(eq(traderId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(entityList, PageRequest.of(page, 10), 1));
    when(consultationMapper.toListResponseDtos(entityList)).thenReturn(dtoList);

    // When
    PaginatedResponseDto<ConsultationListResponseDto> result = consultationService.getConsultations(investorId, traderId, page);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("투자 문의");
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(10);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getTotalPages()).isEqualTo(1);
    verify(consultationRepository, times(1)).findAllByTrader_MemberId(eq(traderId), any(Pageable.class));
    verify(consultationMapper, times(1)).toListResponseDtos(entityList);
  }

  /**
   * 상담 목록 조회 테스트 - 전체 조회
   */
  @Test
  void 상담_목록_조회_전체() {
    // Given
    String investorId = null;
    String traderId = null;
    int page = 0;
    ConsultationListResponseDto listDto1 = consultationListDto;

    ConsultationEntity consultationEntity2 = new ConsultationEntity();
    consultationEntity2.setId(101L);
    consultationEntity2.setInvestor(investor);
    consultationEntity2.setTrader(trader);
    consultationEntity2.setStrategy(strategy);
    consultationEntity2.setInvestmentAmount(7000.0);
    consultationEntity2.setInvestmentDate(LocalDateTime.now());
    consultationEntity2.setTitle("추가 상담");
    consultationEntity2.setContent("추가 상담 내용입니다.");
    consultationEntity2.setStatus(ConsultationStatus.COMPLETED);
    consultationEntity2.setCreatedAt(LocalDateTime.now());
    consultationEntity2.setUpdatedAt(LocalDateTime.now());

    ConsultationListResponseDto consultationListDto2 = new ConsultationListResponseDto();
    consultationListDto2.setId(101L);
    consultationListDto2.setInvestorName("투자자닉네임");
    consultationListDto2.setTraderName("트레이더닉네임");
    consultationListDto2.setStrategyName("성장 전략");
    consultationListDto2.setInvestmentDate(consultationEntity2.getInvestmentDate());
    consultationListDto2.setTitle("추가 상담");
    consultationListDto2.setStatus(ConsultationStatus.COMPLETED);
    consultationListDto2.setCreatedAt(consultationEntity2.getCreatedAt());

    List<ConsultationEntity> entityList = Arrays.asList(consultationEntity, consultationEntity2);
    List<ConsultationListResponseDto> dtoList = Arrays.asList(consultationListDto, consultationListDto2);

    when(consultationRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(entityList, PageRequest.of(page, 10), 2));
    when(consultationMapper.toListResponseDtos(entityList)).thenReturn(dtoList);

    // When
    PaginatedResponseDto<ConsultationListResponseDto> result = consultationService.getConsultations(investorId, traderId, page);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("투자 문의");
    assertThat(result.getContent().get(1).getTitle()).isEqualTo("추가 상담");
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(10);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getTotalPages()).isEqualTo(1);
    verify(consultationRepository, times(1)).findAll(any(Pageable.class));
    verify(consultationMapper, times(1)).toListResponseDtos(entityList);
  }

  /**
   * 상담 목록 조회 테스트 - 페이지가 비어 있음
   */
  @Test
  void 상담_목록_조회_비어있음() {
    // Given
    String investorId = "inv123";
    String traderId = null;
    int page = 0;

    // Mock 설정: 빈 리스트가 반환될 때, mapper도 빈 리스트를 반환하도록 설정
    when(consultationRepository.findAllByInvestor_MemberId(eq(investorId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(page, 10), 0));
    when(consultationMapper.toListResponseDtos(Collections.emptyList())).thenReturn(Collections.emptyList());

    // When
    PaginatedResponseDto<ConsultationListResponseDto> result = consultationService.getConsultations(investorId, traderId, page);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(10);
    assertThat(result.getTotalElements()).isEqualTo(0);
    assertThat(result.getTotalPages()).isEqualTo(0);
    verify(consultationRepository, times(1)).findAllByInvestor_MemberId(eq(investorId), any(Pageable.class));
    verify(consultationMapper, times(1)).toListResponseDtos(Collections.emptyList());
  }

  /**
   * 상담 업데이트 테스트 - 성공
   */
  @Test
  void 상담_업데이트_성공() {
    // Given
    Long consultationId = 100L;
    ConsultationUpdateDto updateDto = new ConsultationUpdateDto();
    updateDto.setTitle("업데이트된 제목");
    updateDto.setContent("업데이트된 내용");
    updateDto.setStrategyId(1L);
    updateDto.setStrategyName("성장 전략"); // 일치하도록 수정
    updateDto.setInvestmentAmount(6000.0);
    updateDto.setInvestmentDate(LocalDateTime.now());
    updateDto.setStatus(ConsultationStatus.COMPLETED);

    ConsultationEntity updatedEntity = new ConsultationEntity();
    updatedEntity.setId(100L);
    updatedEntity.setInvestor(investor);
    updatedEntity.setTrader(trader);
    updatedEntity.setStrategy(strategy);
    updatedEntity.setInvestmentAmount(6000.0);
    updatedEntity.setInvestmentDate(updateDto.getInvestmentDate());
    updatedEntity.setTitle("업데이트된 제목");
    updatedEntity.setContent("업데이트된 내용");
    updatedEntity.setStatus(ConsultationStatus.COMPLETED);
    updatedEntity.setCreatedAt(consultationEntity.getCreatedAt());
    updatedEntity.setUpdatedAt(LocalDateTime.now());

    ConsultationDetailResponseDto updatedDto = new ConsultationDetailResponseDto();
    updatedDto.setId(100L);
    updatedDto.setInvestorId("inv123");
    updatedDto.setInvestorName("투자자닉네임");
    updatedDto.setTraderId("trd456");
    updatedDto.setTraderName("트레이더닉네임");
    updatedDto.setStrategyId(1L); // 추가
    updatedDto.setStrategyName("성장 전략"); // 일치하도록 수정
    updatedDto.setInvestmentAmount(6000.0);
    updatedDto.setInvestmentDate(updateDto.getInvestmentDate());
    updatedDto.setTitle("업데이트된 제목");
    updatedDto.setContent("업데이트된 내용");
    updatedDto.setStatus(ConsultationStatus.COMPLETED);
    updatedDto.setCreatedAt(consultationEntity.getCreatedAt());
    updatedDto.setUpdatedAt(updatedEntity.getUpdatedAt());

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));
    when(strategyRepository.findById(1L)).thenReturn(Optional.of(strategy));
    when(consultationMapper.updateEntityFromDto(eq(consultationEntity), any(ConsultationUpdateDto.class), eq(strategy))).thenReturn(updatedEntity);
    when(consultationRepository.save(consultationEntity)).thenReturn(updatedEntity);
    when(consultationMapper.toDetailResponseDto(updatedEntity)).thenReturn(updatedDto);

    // When
    ConsultationDetailResponseDto result = consultationService.updateConsultation(consultationId, updateDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getTitle()).isEqualTo("업데이트된 제목");
    assertThat(result.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
    assertThat(result.getStrategyId()).isEqualTo(1L); // 추가 검증
    assertThat(result.getStrategyName()).isEqualTo("성장 전략"); // 일치 검증
    verify(consultationRepository, times(1)).findById(consultationId);
    verify(strategyRepository, times(1)).findById(1L);
    verify(consultationMapper, times(1)).updateEntityFromDto(eq(consultationEntity), any(ConsultationUpdateDto.class), eq(strategy));
    verify(consultationRepository, times(1)).save(consultationEntity);
    verify(consultationMapper, times(1)).toDetailResponseDto(updatedEntity);
  }

  /**
   * 상담 업데이트 테스트 - 존재하지 않음
   */
  @Test
  void 상담_업데이트_존재하지_않음() {
    // Given
    Long consultationId = 999L;
    ConsultationUpdateDto updateDto = new ConsultationUpdateDto();
    updateDto.setTitle("업데이트된 제목");
    updateDto.setContent("업데이트된 내용");
    updateDto.setStrategyId(1L);
    updateDto.setInvestmentAmount(1000.0);
    updateDto.setInvestmentDate(LocalDateTime.now());
    updateDto.setStatus(ConsultationStatus.COMPLETED);

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> consultationService.updateConsultation(consultationId, updateDto))
            .isInstanceOf(ConsultationNotFoundException.class)
            .hasMessageContaining("해당 ID의 상담을 찾을 수 없습니다: " + consultationId);

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationMapper, times(0)).updateEntityFromDto(any(), any(), any());
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담 업데이트 테스트 - 전략 존재하지 않음
   */
  @Test
  void 상담_업데이트_전략_존재하지_않음() {
    // Given
    Long consultationId = 100L;
    ConsultationUpdateDto updateDto = new ConsultationUpdateDto();
    updateDto.setTitle("업데이트된 제목");
    updateDto.setContent("업데이트된 내용");
    updateDto.setStrategyId(999L); // 존재하지 않는 전략 ID
    updateDto.setInvestmentAmount(1000.0);
    updateDto.setInvestmentDate(LocalDateTime.now());
    updateDto.setStatus(ConsultationStatus.COMPLETED);

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));
    when(strategyRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> consultationService.updateConsultation(consultationId, updateDto))
            .isInstanceOf(StrategyNotFoundException.class)
            .hasMessageContaining("전략을 찾을 수 없습니다: 999");

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(strategyRepository, times(1)).findById(999L);
    verify(consultationMapper, times(0)).updateEntityFromDto(any(), any(), any());
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담 삭제 테스트 - 성공
   */
  @Test
  void 상담_삭제_성공() {
    // Given
    Long consultationId = 100L;
    when(consultationRepository.existsById(consultationId)).thenReturn(true);
    doNothing().when(consultationRepository).deleteById(consultationId);

    // When
    consultationService.deleteConsultation(consultationId);

    // Then
    verify(consultationRepository, times(1)).existsById(consultationId);
    verify(consultationRepository, times(1)).deleteById(consultationId);
  }

  /**
   * 상담 삭제 테스트 - 존재하지 않음
   */
  @Test
  void 상담_삭제_존재하지_않음() {
    // Given
    Long consultationId = 999L;
    when(consultationRepository.existsById(consultationId)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> consultationService.deleteConsultation(consultationId))
            .isInstanceOf(ConsultationNotFoundException.class)
            .hasMessageContaining("해당 ID의 상담을 찾을 수 없습니다: " + consultationId);

    verify(consultationRepository, times(1)).existsById(consultationId);
    verify(consultationRepository, times(0)).deleteById(anyLong());
  }

  /**
   * 상담에 답변하기 테스트 - 성공
   */
  @Test
  void 상담_답변_성공() {
    // Given
    Long consultationId = 100L;
    String replyContent = "성장 전략에 대해 아래와 같이 답변드립니다.";

    // 기존 상담 엔티티 (status: PENDING, answerDate: null)
    ConsultationEntity updatedEntity = new ConsultationEntity();
    updatedEntity.setId(100L);
    updatedEntity.setInvestor(investor);
    updatedEntity.setTrader(trader);
    updatedEntity.setStrategy(strategy);
    updatedEntity.setInvestmentAmount(5000.0);
    updatedEntity.setInvestmentDate(consultationEntity.getInvestmentDate());
    updatedEntity.setTitle("투자 문의");
    updatedEntity.setContent("성장 전략에 대해 더 알고 싶습니다.");
    updatedEntity.setStatus(ConsultationStatus.COMPLETED);
    updatedEntity.setCreatedAt(consultationEntity.getCreatedAt());
    updatedEntity.setUpdatedAt(LocalDateTime.now());
    updatedEntity.setReplyContent(replyContent);
    updatedEntity.setAnswerDate(LocalDateTime.now());
    updatedEntity.setReplyCreatedAt(LocalDateTime.now());
    updatedEntity.setReplyUpdatedAt(null); // 처음 답변이므로

    // 상담 상세 응답 DTO 설정
    ConsultationDetailResponseDto updatedDto = new ConsultationDetailResponseDto();
    updatedDto.setId(100L);
    updatedDto.setInvestorId("inv123");
    updatedDto.setInvestorName("투자자닉네임");
    updatedDto.setTraderId("trd456");
    updatedDto.setTraderName("트레이더닉네임");
    updatedDto.setStrategyId(1L);
    updatedDto.setStrategyName("성장 전략");
    updatedDto.setInvestmentAmount(5000.0);
    updatedDto.setInvestmentDate(consultationEntity.getInvestmentDate());
    updatedDto.setTitle("투자 문의");
    updatedDto.setContent("성장 전략에 대해 더 알고 싶습니다.");
    updatedDto.setStatus(ConsultationStatus.COMPLETED);
    updatedDto.setCreatedAt(consultationEntity.getCreatedAt());
    updatedDto.setUpdatedAt(updatedEntity.getUpdatedAt());
    updatedDto.setReplyContent(replyContent);
    updatedDto.setAnswerDate(LocalDateTime.now());
    updatedDto.setReplyCreatedAt(LocalDateTime.now());
    updatedDto.setReplyUpdatedAt(null);

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));
    when(consultationRepository.save(any(ConsultationEntity.class))).thenReturn(updatedEntity);
    when(consultationMapper.toDetailResponseDto(updatedEntity)).thenReturn(updatedDto);

    // When
    ConsultationDetailResponseDto result = consultationService.replyToConsultation(consultationId, replyContent);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
    assertThat(result.getReplyContent()).isEqualTo(replyContent);
    assertThat(result.getAnswerDate()).isNotNull();
    assertThat(result.getReplyCreatedAt()).isNotNull();
    assertThat(result.getReplyUpdatedAt()).isNull();
    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(1)).save(consultationEntity);
    verify(consultationMapper, times(1)).toDetailResponseDto(updatedEntity);
  }

  /**
   * 상담에 답변하기 테스트 - 이미 답변 완료된 상담
   */
  @Test
  void 상담_답변_이미_완료된_상담() {
    // Given
    Long consultationId = 100L;
    String replyContent = "추가 답변 내용";

    // 기존 상담 엔티티 (status: COMPLETED, answerDate: not null)
    consultationEntity.setStatus(ConsultationStatus.COMPLETED);
    consultationEntity.setAnswerDate(LocalDateTime.now());
    consultationEntity.setReplyContent("기존 답변 내용");
    consultationEntity.setReplyCreatedAt(LocalDateTime.now());
    consultationEntity.setReplyUpdatedAt(null); // 기존에는 업데이트되지 않음

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));

    // When & Then
    assertThatThrownBy(() -> consultationService.replyToConsultation(consultationId, replyContent))
            .isInstanceOf(ConsultationAlreadyCompletedException.class)
            .hasMessageContaining("이미 답변이 완료된 상담입니다.");

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담에 답변하기 테스트 - 존재하지 않는 상담
   */
  @Test
  void 상담_답변_존재하지_않음() {
    // Given
    Long consultationId = 999L;
    String replyContent = "답변 내용";

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> consultationService.replyToConsultation(consultationId, replyContent))
            .isInstanceOf(ConsultationNotFoundException.class)
            .hasMessageContaining("해당 ID의 상담을 찾을 수 없습니다: " + consultationId);

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담에 답변 수정 테스트 - 성공
   */
  @Test
  void 상담_답변_수정_성공() {
    // Given
    Long consultationId = 100L;
    String newReplyContent = "수정된 답변 내용";

    // 기존 상담 엔티티 (status: COMPLETED, answerDate: not null)
    consultationEntity.setStatus(ConsultationStatus.COMPLETED);
    consultationEntity.setReplyContent("기존 답변 내용");
    consultationEntity.setAnswerDate(LocalDateTime.now());
    consultationEntity.setReplyCreatedAt(LocalDateTime.now());
    consultationEntity.setReplyUpdatedAt(null); // 기존에는 업데이트되지 않음

    ConsultationDetailResponseDto updatedDto = new ConsultationDetailResponseDto();
    updatedDto.setId(100L);
    updatedDto.setInvestorId("inv123");
    updatedDto.setInvestorName("투자자닉네임");
    updatedDto.setTraderId("trd456");
    updatedDto.setTraderName("트레이더닉네임");
    updatedDto.setStrategyId(1L);
    updatedDto.setStrategyName("성장 전략");
    updatedDto.setInvestmentAmount(5000.0);
    updatedDto.setInvestmentDate(consultationEntity.getInvestmentDate());
    updatedDto.setTitle("투자 문의");
    updatedDto.setContent("성장 전략에 대해 더 알고 싶습니다.");
    updatedDto.setStatus(ConsultationStatus.COMPLETED);
    updatedDto.setCreatedAt(consultationEntity.getCreatedAt());
    updatedDto.setUpdatedAt(LocalDateTime.now());
    updatedDto.setReplyContent(newReplyContent);
    updatedDto.setAnswerDate(consultationEntity.getAnswerDate());
    updatedDto.setReplyCreatedAt(consultationEntity.getReplyCreatedAt());
    updatedDto.setReplyUpdatedAt(LocalDateTime.now());

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));
    when(consultationRepository.save(any(ConsultationEntity.class))).thenReturn(consultationEntity);
    when(consultationMapper.toDetailResponseDto(consultationEntity)).thenReturn(updatedDto);

    // When
    ConsultationDetailResponseDto result = consultationService.updateReply(consultationId, newReplyContent);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getReplyContent()).isEqualTo(newReplyContent);
    assertThat(result.getReplyUpdatedAt()).isNotNull();
    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(1)).save(consultationEntity);
    verify(consultationMapper, times(1)).toDetailResponseDto(consultationEntity);
  }

  /**
   * 상담에 답변 수정 테스트 - 답변이 존재하지 않음
   */
  @Test
  void 상담_답변_수정_답변_존재하지_않음() {
    // Given
    Long consultationId = 100L;
    String newReplyContent = "수정된 답변 내용";

    // 기존 상담 엔티티 (status: PENDING, replyContent: null)
    consultationEntity.setStatus(ConsultationStatus.PENDING);
    consultationEntity.setReplyContent(null);
    consultationEntity.setAnswerDate(null);
    consultationEntity.setReplyCreatedAt(null);
    consultationEntity.setReplyUpdatedAt(null);

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));

    // When & Then
    assertThatThrownBy(() -> consultationService.updateReply(consultationId, newReplyContent))
            .isInstanceOf(ReplyNotFoundException.class)
            .hasMessageContaining("답변이 존재하지 않거나 이미 완료되지 않은 상담입니다.");

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담에 답변 수정 테스트 - 존재하지 않음
   */
  @Test
  void 상담_답변_수정_존재하지_않음() {
    // Given
    Long consultationId = 999L;
    String newReplyContent = "수정된 답변 내용";

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> consultationService.updateReply(consultationId, newReplyContent))
            .isInstanceOf(ConsultationNotFoundException.class)
            .hasMessageContaining("해당 ID의 상담을 찾을 수 없습니다: " + consultationId);

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담에 답변 삭제 테스트 - 성공
   */
  @Test
  void 상담_답변_삭제_성공() {
    // Given
    Long consultationId = 100L;

    // 기존 상담 엔티티 (status: COMPLETED, replyContent: not null)
    consultationEntity.setStatus(ConsultationStatus.COMPLETED);
    consultationEntity.setReplyContent("기존 답변 내용");
    consultationEntity.setAnswerDate(LocalDateTime.now());
    consultationEntity.setReplyCreatedAt(LocalDateTime.now());
    consultationEntity.setReplyUpdatedAt(null);

    ConsultationDetailResponseDto updatedDto = new ConsultationDetailResponseDto();
    updatedDto.setId(100L);
    updatedDto.setInvestorId("inv123");
    updatedDto.setInvestorName("투자자닉네임");
    updatedDto.setTraderId("trd456");
    updatedDto.setTraderName("트레이더닉네임");
    updatedDto.setStrategyId(1L);
    updatedDto.setStrategyName("성장 전략");
    updatedDto.setInvestmentAmount(5000.0);
    updatedDto.setInvestmentDate(consultationEntity.getInvestmentDate());
    updatedDto.setTitle("투자 문의");
    updatedDto.setContent("성장 전략에 대해 더 알고 싶습니다.");
    updatedDto.setStatus(ConsultationStatus.PENDING);
    updatedDto.setCreatedAt(consultationEntity.getCreatedAt());
    updatedDto.setUpdatedAt(LocalDateTime.now());
    updatedDto.setReplyContent(null);
    updatedDto.setAnswerDate(null);
    updatedDto.setReplyCreatedAt(null);
    updatedDto.setReplyUpdatedAt(null);

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));
    when(consultationRepository.save(any(ConsultationEntity.class))).thenReturn(consultationEntity);
    when(consultationMapper.toDetailResponseDto(consultationEntity)).thenReturn(updatedDto);

    // When
    ConsultationDetailResponseDto result = consultationService.deleteReply(consultationId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getReplyContent()).isNull();
    assertThat(result.getStatus()).isEqualTo(ConsultationStatus.PENDING);
    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(1)).save(consultationEntity);
    verify(consultationMapper, times(1)).toDetailResponseDto(consultationEntity);
  }

  /**
   * 상담에 답변 삭제 테스트 - 답변이 존재하지 않음
   */
  @Test
  void 상담_답변_삭제_답변_존재하지_않음() {
    // Given
    Long consultationId = 100L;

    // 기존 상담 엔티티 (status: PENDING, replyContent: null)
    consultationEntity.setStatus(ConsultationStatus.PENDING);
    consultationEntity.setReplyContent(null);
    consultationEntity.setAnswerDate(null);
    consultationEntity.setReplyCreatedAt(null);
    consultationEntity.setReplyUpdatedAt(null);

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultationEntity));

    // When & Then
    assertThatThrownBy(() -> consultationService.deleteReply(consultationId))
            .isInstanceOf(ReplyNotFoundException.class)
            .hasMessageContaining("답변이 존재하지 않거나 이미 완료되지 않은 상담입니다.");

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }

  /**
   * 상담에 답변 삭제 테스트 - 존재하지 않음
   */
  @Test
  void 상담_답변_삭제_존재하지_않음() {
    // Given
    Long consultationId = 999L;

    when(consultationRepository.findById(consultationId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> consultationService.deleteReply(consultationId))
            .isInstanceOf(ConsultationNotFoundException.class)
            .hasMessageContaining("해당 ID의 상담을 찾을 수 없습니다: " + consultationId);

    verify(consultationRepository, times(1)).findById(consultationId);
    verify(consultationRepository, times(0)).save(any());
    verify(consultationMapper, times(0)).toDetailResponseDto(any());
  }
}
