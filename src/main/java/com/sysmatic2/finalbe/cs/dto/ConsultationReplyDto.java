package com.sysmatic2.finalbe.cs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 상담 답변 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class ConsultationReplyDto {
  @NotBlank(message = "답변 내용은 필수입니다.")
  private String replyContent;
}
