package com.sysmatic2.finalbe.cs.controlleer;

import com.sysmatic2.finalbe.cs.entity.Notice;
import com.sysmatic2.finalbe.cs.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

  @Autowired
  private NoticeService noticeService;

  // 페이징된 공지사항 목록 조회
  @GetMapping
  public Page<Notice> listNotices(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
    return noticeService.getAllNotices(page, size); // 페이징된 공지사항 반환
  }

  // 공지사항 상세 조회
  @GetMapping("/{id}")
  public Notice viewNotice(@PathVariable Long id) {
    return noticeService.getNoticeById(id).orElseThrow(() -> new RuntimeException("Notice not found"));
  }

  // 공지사항 작성 (관리자만 접근 가능)
//  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Notice createNotice(@RequestBody Notice notice) {
    return noticeService.saveNotice(notice);
  }

  // 공지사항 수정 (관리자만 접근 가능)
//  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Notice editNotice(@PathVariable Long id, @RequestBody Notice notice) {
    notice.setId(id);
    return noticeService.saveNotice(notice);
  }

  // 공지사항 삭제 (관리자만 접근 가능)
//  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public void deleteNotice(@PathVariable Long id) {
    noticeService.deleteNotice(id);
  }
}
