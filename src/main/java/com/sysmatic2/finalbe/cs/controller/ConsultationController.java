package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.service.ConsultationService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultations")
@Validated
public class ConsultationController {

  private final ConsultationService consultationService;

  public ConsultationController(ConsultationService consultationService) {
    this.consultationService = consultationService;
  }

  /**
   * 상담 생성
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConsultationDetailResponseDto createConsultation(@Valid @RequestBody ConsultationCreateDto dto) {
    return consultationService.createConsultation(dto);
  }

  /**
   * 단일 상담 조회
   */
  @GetMapping("/{id}")
  public ConsultationDetailResponseDto getConsultationById(@PathVariable Long id) {
    return consultationService.getConsultationById(id);
  }

  /**
   * 상담 목록 조회
   */
  @GetMapping
  public PaginatedResponseDto<ConsultationListResponseDto> getConsultations(
          @RequestParam(required = false) String investorId,
          @RequestParam(required = false) String traderId,
          @RequestParam(defaultValue = "0") int page) {
    return consultationService.getConsultations(investorId, traderId, page);
  }

  /**
   * 상담 업데이트
   */
  @PutMapping("/{id}")
  public ConsultationDetailResponseDto updateConsultation(@PathVariable Long id, @Valid @RequestBody ConsultationUpdateDto dto) {
    return consultationService.updateConsultation(id, dto);
  }

  /**
   * 상담에 답변하기
   */
  @PostMapping("/{id}/reply")
  @ResponseStatus(HttpStatus.OK)
  public ConsultationDetailResponseDto replyToConsultation(@PathVariable Long id, @Valid @RequestBody ConsultationReplyDto replyDto) {
    return consultationService.replyToConsultation(id, replyDto.getReplyContent());
  }

  /**
   * 상담에 대한 답변 수정
   */
  @PutMapping("/{id}/reply")
  public ConsultationDetailResponseDto updateReply(@PathVariable Long id, @Valid @RequestBody ConsultationReplyDto replyDto) {
    return consultationService.updateReply(id, replyDto.getReplyContent());
  }

  /**
   * 상담에 대한 답변 삭제
   */
  @DeleteMapping("/{id}/reply")
  public ConsultationDetailResponseDto deleteReply(@PathVariable Long id) {
    return consultationService.deleteReply(id);
  }

  /**
   * 상담 삭제
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteConsultation(@PathVariable Long id) {
    consultationService.deleteConsultation(id);
  }
}
