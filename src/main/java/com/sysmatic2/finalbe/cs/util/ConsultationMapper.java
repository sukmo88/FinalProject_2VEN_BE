package com.sysmatic2.finalbe.cs.util;

import com.sysmatic2.finalbe.cs.dto.ConsultationDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationSummaryDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationThreadEntity;
import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;

public class ConsultationMapper {

  public static ConsultationDto toDto(ConsultationThreadEntity entity) {
    ConsultationDto dto = new ConsultationDto();
    dto.setId(entity.getId());
    dto.setInvestorId(entity.getInvestor().getMemberId());
    dto.setTraderId(entity.getTrader().getMemberId());
    dto.setStrategyId(entity.getStrategy().getStrategyId());
    dto.setStrategyName(entity.getStrategy().getStrategyTitle()); // 전략 이름
    dto.setConsultationTitle(entity.getConsultationTitle()); // 상담 제목
    dto.setInitialContent(entity.getMessages().isEmpty() ? "" : entity.getMessages().get(0).getContent()); // 초기 메시지 내용
    dto.setCreatedAt(entity.getCreatedAt());
    return dto;
  }

  // ConsultationThreadEntity → ConsultationSummaryDto 변환
  public static ConsultationSummaryDto toSummaryDto(ConsultationThreadEntity entity) {
    ConsultationSummaryDto dto = new ConsultationSummaryDto();
    dto.setId(entity.getId());
    dto.setConsultationTitle(entity.getConsultationTitle()); // 상담 제목
    dto.setInvestorNickname(entity.getInvestor().getNickname()); // 투자자 닉네임
    dto.setTraderNickname(entity.getTrader().getNickname()); // 트레이더 닉네임
    dto.setCreatedAt(entity.getCreatedAt());

    // 최신 메시지의 읽음 상태 기준
    if (!entity.getMessages().isEmpty()) {
      ConsultationMessageEntity latestMessage = entity.getMessages().get(entity.getMessages().size() - 1);
      dto.setIsRead(latestMessage.getIsRead());
    } else {
      dto.setIsRead(false);
    }

    return dto;
  }
}
