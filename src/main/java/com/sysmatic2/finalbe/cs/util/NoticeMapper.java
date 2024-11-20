package com.sysmatic2.finalbe.cs.util;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;

public class NoticeMapper {

  // NoticeEntity → NoticeDto 변환
  public static NoticeDto toDto(NoticeEntity entity) {
    NoticeDto dto = new NoticeDto();
    dto.setId(entity.getId());
    dto.setNoticeStatus(entity.getNoticeStatus());
    dto.setTitle(entity.getTitle());
    dto.setContent(entity.getContent());
    dto.setPostedAt(entity.getPostedAt());
    dto.setUpdatedAt(entity.getUpdatedAt());
    dto.setScheduledAt(entity.getScheduledAt());
    dto.setViewCount(entity.getViewCount());
    dto.setWriterId(entity.getWriter() != null ? entity.getWriter().getMemberId() : null); // String 타입
    return dto;
  }

  // NoticeEntity → NoticeSummaryDto 변환
  public static NoticeSummaryDto toSummaryDto(NoticeEntity entity) {
    NoticeSummaryDto dto = new NoticeSummaryDto();
    dto.setId(entity.getId());
    dto.setTitle(entity.getTitle());
    dto.setPostedAt(entity.getPostedAt());
    dto.setViewCount(entity.getViewCount());
    return dto;
  }

  // CreateNoticeDto → NoticeEntity 변환
  public static NoticeEntity toEntity(CreateNoticeDto dto, MemberEntity writer) {
    NoticeEntity entity = new NoticeEntity();
    entity.setNoticeStatus(dto.getNoticeStatus());
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setScheduledAt(dto.getScheduledAt());
    entity.setWriter(writer);
    return entity;
  }

  // UpdateNoticeDto → NoticeEntity 변환
  public static NoticeEntity toEntity(UpdateNoticeDto dto, MemberEntity writer) {
    NoticeEntity entity = new NoticeEntity();
    entity.setId(dto.getId());
    entity.setNoticeStatus(dto.getNoticeStatus());
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setScheduledAt(dto.getScheduledAt());
    entity.setWriter(writer);
    return entity;
  }
}
