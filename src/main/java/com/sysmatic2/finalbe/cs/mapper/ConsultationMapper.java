package com.sysmatic2.finalbe.cs.mapper;

import com.sysmatic2.finalbe.cs.dto.ConsultationCreateDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationDetailResponseDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationListResponseDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationUpdateDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상담 매퍼 클래스
 */
@Component
public class ConsultationMapper {

  /**
   * Create DTO를 기반으로 엔티티 생성
   */
  public ConsultationEntity toEntityFromCreateDto(ConsultationCreateDto dto,
                                                  com.sysmatic2.finalbe.member.entity.MemberEntity investor,
                                                  com.sysmatic2.finalbe.member.entity.MemberEntity trader,
                                                  com.sysmatic2.finalbe.strategy.entity.StrategyEntity strategy) {
    ConsultationEntity entity = new ConsultationEntity();
    entity.setInvestor(investor);
    entity.setTrader(trader);
    entity.setStrategy(strategy);
    entity.setInvestmentAmount(dto.getInvestmentAmount());
    entity.setInvestmentDate(dto.getInvestmentDate());
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setStatus(dto.getStatus());
    entity.setCreatedAt(java.time.LocalDateTime.now());
    entity.setUpdatedAt(java.time.LocalDateTime.now());
    return entity;
  }

  /**
   * 엔티티를 상세 응답 DTO로 변환
   */
  public ConsultationDetailResponseDto toDetailResponseDto(ConsultationEntity entity) {
    return ConsultationDetailResponseDto.builder()
            .id(entity.getId())
            .investorId(entity.getInvestor() == null ? null : entity.getInvestor().getMemberId())
            .investorName(entity.getInvestor() == null ? "탈퇴한 사용자" : entity.getInvestor().getNickname())
            .traderId(entity.getTrader() == null ? null : entity.getTrader().getMemberId())
            .traderName(entity.getTrader() == null ? "탈퇴한 사용자" : entity.getTrader().getNickname())
            .strategyId(entity.getStrategy() == null ? null : entity.getStrategy().getStrategyId())
            .strategyName(entity.getStrategy() == null ? "전략 정보 없음" : entity.getStrategy().getStrategyTitle())
            .investmentAmount(entity.getInvestmentAmount())
            .investmentDate(entity.getInvestmentDate())
            .title(entity.getTitle())
            .content(entity.getContent())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .replyContent(entity.getReplyContent())
            .answerDate(entity.getAnswerDate())
            .replyCreatedAt(entity.getReplyCreatedAt())
            .replyUpdatedAt(entity.getReplyUpdatedAt())
            .investorProfileUrl(entity.getInvestor() == null ? null : entity.getInvestor().getProfilePath())
            .traderProfileUrl(entity.getTrader() == null ? null : entity.getTrader().getProfilePath())
            .build();
  }

  /**
   * 엔티티를 리스트 응답 DTO로 변환
   */
  public ConsultationListResponseDto toListResponseDto(ConsultationEntity entity) {
    return ConsultationListResponseDto.builder()
            .id(entity.getId())
            .investorName(entity.getInvestor() == null ? "탈퇴한 사용자" : entity.getInvestor().getNickname())
            .traderName(entity.getTrader() == null ? "탈퇴한 사용자" : entity.getTrader().getNickname())
            .strategyName(entity.getStrategy() == null ? "전략 정보 없음" : entity.getStrategy().getStrategyTitle())
            .investmentDate(entity.getInvestmentDate())
            .title(entity.getTitle())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .investorProfileUrl(entity.getInvestor() == null ? null : entity.getInvestor().getProfilePath())
            .traderProfileUrl(entity.getTrader() == null ? null : entity.getTrader().getProfilePath())
            .build();
  }

  /**
   * 리스트 엔티티를 리스트 DTO로 변환
   */
  public List<ConsultationListResponseDto> toListResponseDtos(List<ConsultationEntity> entities) {
    return entities.stream()
            .map(this::toListResponseDto)
            .collect(Collectors.toList());
  }

  /**
   * 업데이트 DTO를 기반으로 엔티티 업데이트
   */
  public ConsultationEntity updateEntityFromDto(ConsultationEntity entity, ConsultationUpdateDto dto, StrategyEntity strategy) {
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setStrategy(strategy);
    entity.setInvestmentAmount(dto.getInvestmentAmount());
    entity.setInvestmentDate(dto.getInvestmentDate());
    entity.setStatus(dto.getStatus());
    entity.setUpdatedAt(java.time.LocalDateTime.now());
    return entity;
  }
}
