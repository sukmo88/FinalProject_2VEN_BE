package com.sysmatic2.finalbe.cs.entity;

import lombok.Getter;

/**
 * 상담 상태 열거형
 */
@Getter
public enum ConsultationStatus {

  PENDING("대기"),
  COMPLETED("완료");

  private final String description;

  ConsultationStatus(String description) {
    this.description = description;
  }

}
