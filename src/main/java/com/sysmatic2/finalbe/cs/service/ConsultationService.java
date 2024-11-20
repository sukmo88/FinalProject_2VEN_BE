package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.cs.mapper.ConsultationMapper;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsultationService {

  private static final int PAGE_SIZE = 10; // 기본 페이징 크기 설정
  private final ConsultationRepository consultationRepository;
  private final MemberRepository memberRepository;
  private final ConsultationMapper consultationMapper;

  public ConsultationService(ConsultationRepository consultationRepository,
                             MemberRepository memberRepository,
                             ConsultationMapper consultationMapper) {
    this.consultationRepository = consultationRepository;
    this.memberRepository = memberRepository;
    this.consultationMapper = consultationMapper;
  }

  // **1. 상담 생성 (Create)**
  public Optional<ConsultationDetailResponseDto> createConsultation(ConsultationCreateDto dto) {
    MemberEntity investor = memberRepository.findById(dto.getInvestorId())
            .orElseThrow(() -> new RuntimeException("투자자를 찾을 수 없습니다: " + dto.getInvestorId()));
    MemberEntity trader = memberRepository.findById(dto.getTraderId())
            .orElseThrow(() -> new RuntimeException("트레이더를 찾을 수 없습니다: " + dto.getTraderId()));

    ConsultationEntity entity = consultationMapper.toEntityFromCreateDto(dto, investor, trader, null);
    ConsultationEntity savedEntity = consultationRepository.save(entity);
    return Optional.of(consultationMapper.toDetailResponseDto(savedEntity));
  }

  // **2. 단일 상담 조회 (Read by ID)**
  public Optional<ConsultationDetailResponseDto> getConsultationById(Long id) {
    Optional<ConsultationEntity> entity = consultationRepository.findById(id);
    return entity.map(consultationMapper::toDetailResponseDto);
  }

  // **3. 상담 목록 조회 (투자자, 트레이더 필터링)**
  public Page<ConsultationListResponseDto> getConsultations(String investorId, String traderId, int page) {
    PageRequest pageable = PageRequest.of(page, PAGE_SIZE);
    Page<ConsultationEntity> consultations;

    if (investorId != null && !investorId.isEmpty()) {
      consultations = consultationRepository.findAllByInvestorMemberId(investorId, pageable);
    } else if (traderId != null && !traderId.isEmpty()) {
      consultations = consultationRepository.findAllByTraderMemberId(traderId, pageable);
    } else {
      consultations = consultationRepository.findAll(pageable);
    }

    return consultations.map(consultationMapper::toListResponseDto);
  }

  // **4. 상담 업데이트 (Update)**
  public Optional<ConsultationDetailResponseDto> updateConsultation(Long id, ConsultationUpdateDto dto) {
    ConsultationEntity existingEntity = consultationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    ConsultationEntity updatedEntity = consultationMapper.updateEntityFromDto(existingEntity, dto, null);
    ConsultationEntity savedEntity = consultationRepository.save(updatedEntity);

    return Optional.of(consultationMapper.toDetailResponseDto(savedEntity));
  }

  // **5. 상담 삭제 (Delete)**
  public boolean deleteConsultation(Long id) {
    if (!consultationRepository.existsById(id)) {
      return false;
    }
    consultationRepository.deleteById(id);
    return true;
  }
}
