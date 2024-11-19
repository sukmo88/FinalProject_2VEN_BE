package com.sysmatic2.finalbe.cs.util;

import com.sysmatic2.finalbe.cs.dto.EmailNotificationDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;

public class EmailNotificationMapper {

  public static EmailNotificationDto toEmailDto(ConsultationMessageEntity entity, MemberEntity recipient) {
    EmailNotificationDto dto = new EmailNotificationDto();
    dto.setRecipientEmail(recipient.getEmail());
    dto.setSubject("새로운 쪽지가 도착했습니다!");
    StringBuilder body = new StringBuilder();
    body.append("안녕하세요, ").append(recipient.getNickname()).append("님!\n\n");
    body.append("새로운 메시지가 도착했습니다.\n\n");
    body.append("발신자: ").append(entity.getSender().getNickname()).append("\n");
    body.append("내용: ").append(entity.getContent()).append("\n\n");
    body.append("감사합니다.");
    dto.setBody(body.toString());
    return dto;
  }
}
