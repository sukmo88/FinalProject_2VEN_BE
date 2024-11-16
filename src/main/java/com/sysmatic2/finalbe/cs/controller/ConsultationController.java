package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.dto.ConsultationDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDto;
import com.sysmatic2.finalbe.cs.dto.SendMessageDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationSummaryDto;
import com.sysmatic2.finalbe.cs.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

  private final ConsultationService consultationService;

  @Autowired
  public ConsultationController(ConsultationService consultationService) {
    this.consultationService = consultationService;
  }

  /**
   * 상담 생성
   *
   * @param investorId        투자자 ID (쿼리 파라미터)
   * @param traderId          트레이더 ID (쿼리 파라미터)
   * @param strategyId        전략 ID (쿼리 파라미터)
   * @param consultationTitle 상담 제목 (쿼리 파라미터)
   * @param content           상담 내용 (쿼리 파라미터)
   * @return ConsultationDto
   */
  @PostMapping
  public ConsultationDto createConsultation(@RequestParam Long investorId,
                                            @RequestParam Long traderId,
                                            @RequestParam Long strategyId,
                                            @RequestParam String consultationTitle,
                                            @RequestParam String content) {
    return consultationService.createConsultation(investorId, traderId, strategyId, consultationTitle, content);
  }

  /**
   * 메시지 전송
   *
   * @param senderId 발신자 ID (쿼리 파라미터)
   * @param dto      SendMessageDto (바디)
   * @return ConsultationMessageDto
   */
  @PostMapping("/messages")
  public ConsultationMessageDto sendMessage(@RequestParam Long senderId,
                                            @RequestBody SendMessageDto dto) {
    return consultationService.sendMessage(senderId, dto);
  }

  /**
   * 사용자별 메시지 조회
   *
   * @param userId   사용자 ID (쿼리 파라미터)
   * @param sent     보낸 메시지 여부 (쿼리 파라미터, true: 보낸 메시지, false: 받은 메시지)
   * @param pageable 페이징 및 정렬 정보
   * @return Page<ConsultationMessageDto>
   */
  @GetMapping("/messages")
  public Page<ConsultationMessageDto> getUserMessages(@RequestParam Long userId,
                                                      @RequestParam boolean sent,
                                                      Pageable pageable) {
    return consultationService.getUserMessages(userId, sent, pageable);
  }

  /**
   * 메시지 키워드 검색
   *
   * @param keyword  검색 키워드 (쿼리 파라미터)
   * @param pageable 페이징 및 정렬 정보
   * @return Page<ConsultationMessageDto>
   */
  @GetMapping("/messages/search")
  public Page<ConsultationMessageDto> searchMessagesByKeyword(@RequestParam String keyword,
                                                              Pageable pageable) {
    return consultationService.searchMessagesByKeyword(keyword, pageable);
  }

  /**
   * 메시지 날짜 범위 검색
   *
   * @param startDate 시작 날짜 (쿼리 파라미터, ISO-8601 형식)
   * @param endDate   종료 날짜 (쿼리 파라미터, ISO-8601 형식)
   * @param pageable  페이징 및 정렬 정보
   * @return Page<ConsultationMessageDto>
   */
  @GetMapping("/messages/date-range")
  public Page<ConsultationMessageDto> searchMessagesByDateRange(@RequestParam String startDate,
                                                                @RequestParam String endDate,
                                                                Pageable pageable) {
    // ISO-8601 형식의 문자열을 LocalDateTime으로 변환
    LocalDateTime start = LocalDateTime.parse(startDate);
    LocalDateTime end = LocalDateTime.parse(endDate);
    return consultationService.searchMessagesByDateRange(start, end, pageable);
  }

  /**
   * 메시지 읽음 상태 업데이트
   *
   * @param messageId 메시지 ID (경로 변수)
   * @param userId    사용자 ID (쿼리 파라미터)
   */
  @PutMapping("/messages/{id}/read")
  public void markMessageAsRead(@PathVariable("id") Long messageId,
                                @RequestParam Long userId) {
    consultationService.markMessageAsRead(messageId, userId);
  }

  /**
   * 사용자별 상담 요약 목록 조회
   *
   * @param userId 사용자 ID (쿼리 파라미터)
   * @return List<ConsultationSummaryDto>
   */
  @GetMapping("/summary")
  public List<ConsultationSummaryDto> getUserConsultations(@RequestParam Long userId) {
    return consultationService.getUserConsultations(userId);
  }

  // TODO: 인증(Authentication) 및 권한(Authorization) 로직 추가
}
