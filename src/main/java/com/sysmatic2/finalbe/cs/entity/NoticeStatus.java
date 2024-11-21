package com.sysmatic2.finalbe.cs.entity;

import lombok.Getter;

/**
 * 공지사항 상태 열거형
 */
@Getter
public enum NoticeStatus {
  DRAFT,      // 초안 상태
  SCHEDULED,  // 예정 상태
  PUBLISHED,  // 공개 상태
  ARCHIVED    // 보관 상태
}
