package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.dto.CreateNoticeDto;
import com.sysmatic2.finalbe.cs.dto.NoticeDto;
import com.sysmatic2.finalbe.cs.dto.NoticeSummaryDto;
import com.sysmatic2.finalbe.cs.dto.UpdateNoticeDto;
import com.sysmatic2.finalbe.cs.service.NoticeService;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

  @Autowired
  private NoticeService noticeService;

  // 공지사항 생성
  @PostMapping
  public NoticeDto createNotice(@RequestBody CreateNoticeDto createDto) {
    return noticeService.createNotice(createDto);
  }

  // 공지사항 수정
  @PutMapping("/{id}")
  public NoticeDto updateNotice(@PathVariable Long id, @RequestBody UpdateNoticeDto updateDto) {
    updateDto.setId(id); // ID 설정
    return noticeService.updateNotice(updateDto);
  }

  // 공지사항 삭제
  @DeleteMapping("/{id}")
  public void deleteNotice(@PathVariable Long id) {
    noticeService.deleteNotice(id);
  }

  // 공지사항 상세 조회
  @GetMapping("/{id}")
  public NoticeDto getNoticeById(@PathVariable Long id) {
    return noticeService.getNoticeById(id);
  }

  // 공지사항 제목으로 검색
  @GetMapping("/search/title")
  public Page<NoticeSummaryDto> searchByTitle(@RequestParam String keyword, Pageable pageable) {
    return noticeService.searchByTitle(keyword, pageable);
  }

  // 공지사항 내용으로 검색
  @GetMapping("/search/content")
  public Page<NoticeSummaryDto> searchByContent(@RequestParam String keyword, Pageable pageable) {
    return noticeService.searchByContent(keyword, pageable);
  }

  // 공지사항 제목 또는 내용으로 검색
  @GetMapping("/search/title-or-content")
  public Page<NoticeSummaryDto> searchByTitleOrContent(@RequestParam String keyword, Pageable pageable) {
    return noticeService.searchByTitleOrContent(keyword, pageable);
  }

  // 상태별 공지사항 검색
  @GetMapping("/search/status")
  public Page<NoticeSummaryDto> searchByStatus(@RequestParam String status, Pageable pageable) {
    return noticeService.searchByStatus(status, pageable);
  }

  // 작성자별 공지사항 검색
  @GetMapping("/search/writer")
  public Page<NoticeSummaryDto> searchByWriter(@RequestParam String writerId, Pageable pageable) {
    return noticeService.searchByWriter(writerId, pageable);
  }

  // 기간별 공지사항 검색
  @GetMapping("/search/date-range")
  public Page<NoticeSummaryDto> searchByDateRange(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate, Pageable pageable) {
    return noticeService.searchByDateRange(startDate, endDate, pageable);
  }
}
