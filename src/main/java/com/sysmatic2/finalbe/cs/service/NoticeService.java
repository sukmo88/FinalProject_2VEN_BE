package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.entity.Notice;
import com.sysmatic2.finalbe.cs.repository.NoticeRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class NoticeService {

  @Autowired
  private NoticeRepository noticeRepository;

  // 페이징 처리된 공지사항 목록 조회
  public Page<Notice> getAllNotices(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size); // PageRequest 생성
    return noticeRepository.findAll(pageRequest); // Page<Notice> 반환
  }

  public Optional<Notice> getNoticeById(Long id) {
    return noticeRepository.findById(id);
  }

  public Notice saveNotice(Notice notice) {
    return noticeRepository.save(notice);
  }

  public void deleteNotice(Long id) {
    noticeRepository.deleteById(id);
  }
}