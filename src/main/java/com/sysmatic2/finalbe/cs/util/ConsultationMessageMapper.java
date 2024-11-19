package com.sysmatic2.finalbe.cs.util;

import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;

public class ConsultationMessageMapper {

  public static ConsultationMessageDto toDto(ConsultationMessageEntity entity) {
    ConsultationMessageDto dto = new ConsultationMessageDto();
    dto.setId(entity.getId());
    dto.setContent(entity.getContent());
    dto.setSenderNickname(entity.getSender().getNickname()); // 발신자 닉네임
    dto.setSentAt(entity.getSentAt());
    dto.setIsRead(entity.getIsRead());
    return dto;
  }
}
