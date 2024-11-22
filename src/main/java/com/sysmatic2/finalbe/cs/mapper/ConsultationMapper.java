package com.sysmatic2.finalbe.cs.mapper;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 상담 매퍼
 */
@Component
public class ConsultationMapper {

  /**
   * ConsultationEntity를 ConsultationDetailResponseDto로 변환
   */
  public ConsultationDetailResponseDto toDetailResponseDto(ConsultationEntity entity) {
    return ConsultationDetailResponseDto.builder()
            .id(entity.getId())
            .investorId(entity.getInvestor().getMemberId())
            .investorName(entity.getInvestor().getNickname())
            .traderId(entity.getTrader().getMemberId())
            .traderName(entity.getTrader().getNickname())
            .strategyId(entity.getStrategy().getStrategyId())
            .strategyName(entity.getStrategy().getStrategyTitle())
            .investmentAmount(entity.getInvestmentAmount())
            .investmentDate(entity.getInvestmentDate())
            .title(entity.getTitle())
            .content(entity.getContent())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
  }

  /**
   * ConsultationEntity를 ConsultationListResponseDto로 변환
   */
  public ConsultationListResponseDto toListResponseDto(ConsultationEntity entity) {
    return ConsultationListResponseDto.builder()
            .id(entity.getId())
            .investorName(entity.getInvestor().getNickname())
//            .investorProfileUrl(entity.getInvestor().getProfilePath())
            .traderName(entity.getTrader().getNickname())
//            .traderProfileUrl(entity.getTrader().getProfilePath())
            .strategyId(entity.getStrategy().getStrategyId())
            .strategyName(entity.getStrategy().getStrategyTitle())
            .investmentDate(entity.getInvestmentDate())
            .title(entity.getTitle())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .build();
  }

  /**
   * ConsultationCreateDto를 ConsultationEntity로 변환
   */
  public ConsultationEntity toEntityFromCreateDto(ConsultationCreateDto dto,
                                                  MemberEntity investor,
                                                  MemberEntity trader,
                                                  StrategyEntity strategy) {
    return ConsultationEntity.builder()
            .investor(investor)
            .trader(trader)
            .strategy(strategy)
            .investmentAmount(dto.getInvestmentAmount())
            .investmentDate(dto.getInvestmentDate())
            .title(dto.getTitle())
            .content(dto.getContent())
            .status(dto.getStatus())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  /**
   * ConsultationUpdateDto를 기존 ConsultationEntity에 반영
   */
  public ConsultationEntity updateEntityFromDto(ConsultationEntity entity,
                                                ConsultationUpdateDto dto,
                                                StrategyEntity strategy) {
    if (dto.getTitle() != null) {
      entity.setTitle(dto.getTitle());
    }
    if (dto.getContent() != null) {
      entity.setContent(dto.getContent());
    }
    if (strategy != null) {
      entity.setStrategy(strategy);
    }
    if (dto.getInvestmentAmount() != null && dto.getInvestmentAmount() > 0) {
      entity.setInvestmentAmount(dto.getInvestmentAmount());
    }
    if (dto.getInvestmentDate() != null) {
      entity.setInvestmentDate(dto.getInvestmentDate());
    }
    if (dto.getStatus() != null) {
      entity.setStatus(dto.getStatus());
    }
    return entity;
  }
}
