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
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultationService {

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
  public ConsultationDetailResponseDto createConsultation(ConsultationCreateDto createDto) {

    // 투자 금액 검증
    if (createDto.getInvestmentAmount().compareTo(new BigDecimal("10000000000.00")) > 0) {
      throw new IllegalArgumentException("투자 금액은 최대 100억을 초과할 수 없습니다.");
    }
    // 투자자 조회
    MemberEntity investor = memberRepository.findById(createDto.getInvestorId())
            .orElseThrow(() -> new InvestorNotFoundException("투자자를 찾을 수 없습니다: " + createDto.getInvestorId()));

    // 트레이더 조회
    MemberEntity trader = memberRepository.findById(createDto.getTraderId())
            .orElseThrow(() -> new TraderNotFoundException("트레이더를 찾을 수 없습니다: " + createDto.getTraderId()));

    // 전략 조회
    StrategyEntity strategy = strategyRepository.findById(createDto.getStrategyId())
            .orElseThrow(() -> new StrategyNotFoundException("전략을 찾을 수 없습니다: " + createDto.getStrategyId()));

    // DTO를 엔티티로 변환
    ConsultationEntity consultationEntity = consultationMapper.toEntityFromCreateDto(createDto, investor, trader, strategy);

    // 상담 저장
    ConsultationEntity savedEntity = consultationRepository.save(consultationEntity);

    // 엔티티를 상세 DTO로 변환하여 반환
    return consultationMapper.toDetailResponseDto(savedEntity);
  }

  /**
   * 단일 상담 조회
   */
  @Transactional(readOnly = true)
  public ConsultationDetailResponseDto getConsultationById(Long id) {
    ConsultationEntity consultation = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    return consultationMapper.toDetailResponseDto(consultation);
  }

  /**
   * 상담 목록 조회
   */
  @Transactional(readOnly = true)
  public PaginatedResponseDto<ConsultationListResponseDto> getConsultations(String investorId, String traderId, int page) {
    Page<ConsultationEntity> consultationsPage;

    // PageRequest에 정렬 기준 추가 (createdAt 필드를 기준으로 내림차순 정렬)
    PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

    if (investorId != null && !investorId.isEmpty()) {
      consultationsPage = consultationRepository.findAllByInvestor_MemberId(investorId, pageRequest);
    } else if (traderId != null && !traderId.isEmpty()) {
      consultationsPage = consultationRepository.findAllByTrader_MemberId(traderId, pageRequest);
    } else {
      consultationsPage = consultationRepository.findAll(pageRequest);
    }

    return new PaginatedResponseDto<>(
            consultationMapper.toListResponseDtos(consultationsPage.getContent()),
            consultationsPage.getNumber(),
            consultationsPage.getSize(),
            consultationsPage.getTotalElements(),
            consultationsPage.getTotalPages()
    );
  }

  /**
   * 상담 업데이트
   */
  @Transactional
  public ConsultationDetailResponseDto updateConsultation(Long id, ConsultationUpdateDto updateDto) {
    ConsultationEntity existingEntity = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    // 투자 금액 검증
    if (updateDto.getInvestmentAmount() != null &&
            updateDto.getInvestmentAmount().compareTo(new BigDecimal("10000000000.00")) > 0) {
      throw new IllegalArgumentException("투자 금액은 최대 100억을 초과할 수 없습니다.");
    }

    // 전략 조회
    StrategyEntity strategy = strategyRepository.findById(updateDto.getStrategyId())
            .orElseThrow(() -> new StrategyNotFoundException("전략을 찾을 수 없습니다: " + updateDto.getStrategyId()));

    // DTO를 기반으로 엔티티 업데이트
    ConsultationEntity updatedEntity = consultationMapper.updateEntityFromDto(existingEntity, updateDto, strategy);

    // 상담 저장
    ConsultationEntity savedEntity = consultationRepository.save(existingEntity);

    // 엔티티를 상세 DTO로 변환하여 반환
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

  /**
   * 전략에 해당하는 모든 상담 삭제
   */
  @Transactional
  public void deleteConsultationsByStrategy(StrategyEntity strategy) {
    consultationRepository.deleteAllByStrategy(strategy);
  }

  /**
   * 일반투자자 회원 탈퇴 시에 investor -> null로 바꾸기
   * investor로 상담 찾아서 investor -> null로 바꾸는 메소드
   */
  @Transactional
  public void setInvestorToNull(MemberEntity member) {
    List<ConsultationEntity> consultationsByInvestor = consultationRepository.findAllByInvestor(member);
    for (ConsultationEntity consultation : consultationsByInvestor) {
      consultation.setInvestor(null);
      consultationRepository.save(consultation);
    }
  }

  /**
   * 상담에 답변하기
   */
  @Transactional
  public ConsultationDetailResponseDto replyToConsultation(Long id, String replyContent) {
    ConsultationEntity existingEntity = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    // 이미 답변이 완료된 상담인지 확인
    if (existingEntity.getStatus() == ConsultationStatus.COMPLETED) {
      throw new ConsultationAlreadyCompletedException("이미 답변이 완료된 상담입니다.");
    }

    LocalDateTime now = LocalDateTime.now();

    // 답변 내용 설정 및 상태 변경
    existingEntity.setReplyContent(replyContent);
    existingEntity.setStatus(ConsultationStatus.COMPLETED);
    existingEntity.setAnswerDate(now);

    if (existingEntity.getReplyCreatedAt() == null) {
      existingEntity.setReplyCreatedAt(now);
    } else {
      existingEntity.setReplyUpdatedAt(now);
    }

    existingEntity.setUpdatedAt(now);

    // 상담 저장
    ConsultationEntity savedEntity = consultationRepository.save(existingEntity);

    // 엔티티를 상세 DTO로 변환하여 반환
    return consultationMapper.toDetailResponseDto(savedEntity);
  }

  /**
   * 상담에 답변 수정
   */
  @Transactional
  public ConsultationDetailResponseDto updateReply(Long id, String replyContent) {
    ConsultationEntity existingEntity = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    // 답변이 존재하는지 확인
    if (existingEntity.getStatus() != ConsultationStatus.COMPLETED || existingEntity.getReplyContent() == null) {
      throw new ReplyNotFoundException("답변이 존재하지 않거나 이미 완료되지 않은 상담입니다.");
    }

    LocalDateTime now = LocalDateTime.now();

    // 답변 내용 수정 및 수정일 업데이트
    existingEntity.setReplyContent(replyContent);
    existingEntity.setReplyUpdatedAt(now);
    existingEntity.setUpdatedAt(now);

    ConsultationEntity savedEntity = consultationRepository.save(existingEntity);
    return consultationMapper.toDetailResponseDto(savedEntity);
  }

  /**
   * 상담에 답변 삭제
   */
  @Transactional
  public ConsultationDetailResponseDto deleteReply(Long id) {
    ConsultationEntity existingEntity = consultationRepository.findById(id)
            .orElseThrow(() -> new ConsultationNotFoundException("해당 ID의 상담을 찾을 수 없습니다: " + id));

    // 답변이 존재하는지 확인
    if (existingEntity.getStatus() != ConsultationStatus.COMPLETED || existingEntity.getReplyContent() == null) {
      throw new ReplyNotFoundException("답변이 존재하지 않거나 이미 완료되지 않은 상담입니다.");
    }

    // 답변 삭제 및 상태 변경
    existingEntity.setReplyContent(null);
    existingEntity.setAnswerDate(null);
    existingEntity.setReplyUpdatedAt(null);
    existingEntity.setReplyCreatedAt(null);
    existingEntity.setStatus(ConsultationStatus.PENDING);
    existingEntity.setUpdatedAt(LocalDateTime.now());

    ConsultationEntity savedEntity = consultationRepository.save(existingEntity);
    return consultationMapper.toDetailResponseDto(savedEntity);
  }
}
