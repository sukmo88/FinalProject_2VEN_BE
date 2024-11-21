package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.ConsultationCreateDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationDetailResponseDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationListResponseDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationUpdateDto;
import com.sysmatic2.finalbe.cs.dto.PaginatedResponseDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
import com.sysmatic2.finalbe.exception.ConsultationNotFoundException;
import com.sysmatic2.finalbe.exception.TraderNotFoundException;
import com.sysmatic2.finalbe.exception.InvestorNotFoundException;
import com.sysmatic2.finalbe.exception.StrategyNotFoundException;
import com.sysmatic2.finalbe.cs.mapper.ConsultationMapper;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConsultationService {

  private static final int PAGE_SIZE = 10; // 기본 페이징 크기 설정
  private final ConsultationRepository consultationRepository;
  private final MemberRepository memberRepository;
  private final StrategyRepository strategyRepository;
  private final ConsultationMapper consultationMapper;

  public ConsultationService(ConsultationRepository consultationRepository,
                             MemberRepository memberRepository,
                             StrategyRepository strategyRepository,
                             ConsultationMapper consultationMapper) {
    this.consultationRepository = consultationRepository;
    this.memberRepository = memberRepository;
    this.strategyRepository = strategyRepository;
    this.consultationMapper = consultationMapper;
  }

  /**
   * 상담 생성
   */
  @Transactional
  public ConsultationDetailResponseDto createConsultation(ConsultationCreateDto dto) {
    // 투자자 조회
    MemberEntity investor = memberRepository.findById(dto.getInvestorId())
            .orElseThrow(() -> new InvestorNotFoundException("투자자를 찾을 수 없습니다: " + dto.getInvestorId()));

    // 트레이더 조회
    MemberEntity trader = memberRepository.findById(dto.getTraderId())
            .orElseThrow(() -> new TraderNotFoundException("트레이더를 찾을 수 없습니다: " + dto.getTraderId()));

    // 전략 조회 (필수)
    StrategyEntity strategy = strategyRepository.findById(dto.getStrategyId())
            .orElseThrow(() -> new StrategyNotFoundException("전략을 찾을 수 없습니다: " + dto.getStrategyId()));

    // DTO의 strategyName과 실제 전략 이름 일치 여부 검증
    if (!strategy.getStrategyTitle().equals(dto.getStrategyName())) {
      throw new IllegalArgumentException("전략 ID와 전략 이름이 일치하지 않습니다.");
    }

    // ConsultationEntity 생성
    ConsultationEntity entity = consultationMapper.toEntityFromCreateDto(dto, investor, trader, strategy);
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());

    // 저장 및 DTO 변환
    ConsultationEntity savedEntity = consultationRepository.save(entity);
    return consultationMapper.toDetailResponseDto(savedEntity);
  }

  /**
   * 단일 상담 조회
   */
  @Transactional(readOnly = true)
  public ConsultationDetailResponseDto getConsultationById(Long id) {
    ConsultationEntity entity = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));
    return consultationMapper.toDetailResponseDto(entity);
  }

  /**
   * 상담 목록 조회 (투자자, 트레이더 필터링)
   */
  @Transactional(readOnly = true)
  public PaginatedResponseDto<ConsultationListResponseDto> getConsultations(String investorId, String traderId, int page) {
    PageRequest pageable = PageRequest.of(page, PAGE_SIZE);
    Page<ConsultationEntity> consultations;

    if (investorId != null && !investorId.isEmpty()) {
      consultations = consultationRepository.findAllByInvestor_MemberId(investorId, pageable);
    } else if (traderId != null && !traderId.isEmpty()) {
      consultations = consultationRepository.findAllByTrader_MemberId(traderId, pageable);
    } else {
      consultations = consultationRepository.findAll(pageable);
    }

    Page<ConsultationListResponseDto> consultationPage = consultations.map(consultationMapper::toListResponseDto);

    PaginatedResponseDto<ConsultationListResponseDto> response = PaginatedResponseDto.<ConsultationListResponseDto>builder()
            .content(consultationPage.getContent())
            .page(consultationPage.getNumber())
            .size(consultationPage.getSize())
            .totalElements(consultationPage.getTotalElements())
            .totalPages(consultationPage.getTotalPages())
            .build();

    return response;
  }

  /**
   * 상담 업데이트
   */
  @Transactional
  public ConsultationDetailResponseDto updateConsultation(Long id, ConsultationUpdateDto dto) {
    ConsultationEntity existingEntity = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    // 전략 업데이트 (선택적)
    StrategyEntity strategy = null;
    if (dto.getStrategyId() != null) {
      strategy = strategyRepository.findById(dto.getStrategyId())
              .orElseThrow(() -> new StrategyNotFoundException("전략을 찾을 수 없습니다: " + dto.getStrategyId()));

      // DTO의 strategyName과 실제 전략 이름 일치 여부 검증
      if (!strategy.getStrategyTitle().equals(dto.getStrategyName())) {
        throw new IllegalArgumentException("전략 ID와 전략 이름이 일치하지 않습니다.");
      }
    }

    // ConsultationEntity 업데이트
    consultationMapper.updateEntityFromDto(existingEntity, dto, strategy);
    existingEntity.setUpdatedAt(LocalDateTime.now());

    // 저장 및 DTO 변환
    ConsultationEntity savedEntity = consultationRepository.save(existingEntity);
    return consultationMapper.toDetailResponseDto(savedEntity);
  }

  /**
   * 상담 삭제
   */
  @Transactional
  public void deleteConsultation(Long id) {
    if (!consultationRepository.existsById(id)) {
      throw new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id);
    }
    consultationRepository.deleteById(id);
  }
}
