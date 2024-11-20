package com.sysmatic2.finalbe.cs.mapper;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ConsultationMapper {

  /**
   * Converts ConsultationEntity to ConsultationDetailResponseDto.
   * This includes full details for a single consultation.
   */
  public ConsultationDetailResponseDto toDetailResponseDto(ConsultationEntity entity) {
    return ConsultationDetailResponseDto.builder()
            .id(entity.getId())
            .investorId(entity.getInvestor().getMemberId())
            .investorName(entity.getInvestor().getNickname())
            .traderId(entity.getTrader().getMemberId())
            .traderName(entity.getTrader().getNickname())
            .strategyName(entity.getStrategyName() != null ? entity.getStrategyName().getStrategyTitle() : null) // Strategy name
            .investmentAmount(entity.getInvestmentAmount())
            .investmentDate(entity.getInvestmentDate()) // Keep LocalDateTime as is
            .title(entity.getTitle())
            .content(entity.getContent())
            .status(entity.getStatus()) // Keep ConsultationStatus enum as is
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
  }

  /**
   * Converts ConsultationEntity to ConsultationListResponseDto.
   * This is optimized for paginated or summarized results.
   */
  public ConsultationListResponseDto toListResponseDto(ConsultationEntity entity) {
    return ConsultationListResponseDto.builder()
            .id(entity.getId())
            .investorName(entity.getInvestor().getNickname())
            .traderName(entity.getTrader().getNickname())
            .strategyName(entity.getStrategyName() != null ? entity.getStrategyName().getStrategyTitle() : null) // Strategy name
            .investmentDate(entity.getInvestmentDate()) // Keep LocalDateTime as is
            .title(entity.getTitle())
            .status(entity.getStatus()) // Keep ConsultationStatus enum as is
            .createdAt(entity.getCreatedAt())
            .build();
  }

  /**
   * Converts ConsultationCreateDto to ConsultationEntity.
   * Assumes investor, trader, and strategy are fetched from the database.
   */
  public ConsultationEntity toEntityFromCreateDto(ConsultationCreateDto dto,
                                                  MemberEntity investor,
                                                  MemberEntity trader,
                                                  StrategyEntity strategy) {
    return ConsultationEntity.builder()
            .investor(investor)
            .trader(trader)
            .strategyName(strategy) // Use the fetched StrategyEntity
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
   * Updates an existing ConsultationEntity using ConsultationUpdateDto.
   * Assumes strategy is fetched from the database if provided.
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
      entity.setStrategyName(strategy); // Use the fetched StrategyEntity
    }
    if (dto.getInvestmentAmount() > 0) {
      entity.setInvestmentAmount(dto.getInvestmentAmount());
    }
    if (dto.getInvestmentDate() != null) {
      entity.setInvestmentDate(dto.getInvestmentDate());
    }
    if (dto.getStatus() != null) {
      entity.setStatus(dto.getStatus());
    }
    entity.setUpdatedAt(LocalDateTime.now());
    return entity;
  }
}
