package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sysmatic2.finalbe.cs.dto.NoticeDTO;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

  @Autowired
  private NoticeService noticeService;

  // 페이징된 공지사항 목록 조회
  @GetMapping
  public Page<NoticeDTO> listNotices(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
    return noticeService.getAllNotices(page, size);
  }

  // 공지사항 상세 조회
  @GetMapping("/{id}")
  public NoticeDTO viewNotice(@PathVariable Long id) {
    return noticeService.getNoticeById(id)
            .orElseThrow(() -> new RuntimeException("Notice not found"));
  }

  // 공지사항 작성 (관리자만 접근 가능)
//    @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public NoticeDTO createNotice(@RequestBody NoticeDTO noticeDTO) {
    return noticeService.saveNotice(noticeDTO);
  }

  // 공지사항 수정 (관리자만 접근 가능)
//    @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public NoticeDTO editNotice(@PathVariable Long id, @RequestBody NoticeDTO noticeDTO) {
    noticeDTO.setId(id);
    return noticeService.saveNotice(noticeDTO);
  }

  // 공지사항 삭제 (관리자만 접근 가능)
//    @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public void deleteNotice(@PathVariable Long id) {
    noticeService.deleteNotice(id);
  }
}
