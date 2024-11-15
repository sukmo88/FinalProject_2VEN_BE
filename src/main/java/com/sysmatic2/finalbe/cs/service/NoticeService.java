package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.NoticeDto;
import com.sysmatic2.finalbe.cs.entity.Notice;
import com.sysmatic2.finalbe.cs.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class NoticeService {

  @Autowired
  private NoticeRepository noticeRepository;

  // 페이징 처리된 공지사항 목록 조회
  public Page<NoticeDto> getAllNotices(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    return noticeRepository.findAll(pageRequest).map(this::convertToDTO);
  }

  public Optional<NoticeDto> getNoticeById(Long id) {
    return noticeRepository.findById(id).map(this::convertToDTO);
  }

  public NoticeDto saveNotice(NoticeDto noticeDTO) {
    Notice notice = convertToEntity(noticeDTO);
    Notice savedNotice = noticeRepository.save(notice);
    return convertToDTO(savedNotice);
  }

  public void deleteNotice(Long id) {
    noticeRepository.deleteById(id);
  }

  public Page<NoticeDto> searchNoticesByTitle(String keyword, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<Notice> notices = noticeRepository.searchByTitle(keyword, pageRequest);
    return notices.map(this::convertToDTO);
  }

  public Page<NoticeDto> searchNoticesByContent(String keyword, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<Notice> notices = noticeRepository.searchByContent(keyword, pageRequest);
    return notices.map(this::convertToDTO);
  }

  public Page<NoticeDto> searchNoticesByTitleOrContent(String keyword, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<Notice> notices = noticeRepository.searchByTitleOrContent(keyword, pageRequest);
    return notices.map(this::convertToDTO);
  }

  protected NoticeDto convertToDTO(Notice notice) {
    NoticeDto dto = new NoticeDto();
    dto.setId(notice.getId());
    dto.setNoticeStatus(notice.getNoticeStatus());
    dto.setTitle(notice.getTitle());
    dto.setContent(notice.getContent());
    dto.setPostedAt(notice.getPostedAt());
    dto.setUpdatedAt(notice.getUpdatedAt());
    dto.setScheduledAt(notice.getScheduledAt());
    dto.setViewCount(notice.getViewCount());
    dto.setWriterId(notice.getWriterId());
    return dto;
  }

  protected Notice convertToEntity(NoticeDto dto) {
    Notice notice = new Notice();
    notice.setId(dto.getId());
    notice.setNoticeStatus(dto.getNoticeStatus());
    notice.setTitle(dto.getTitle());
    notice.setContent(dto.getContent());
    notice.setPostedAt(dto.getPostedAt());
    notice.setUpdatedAt(dto.getUpdatedAt());
    notice.setScheduledAt(dto.getScheduledAt());
    notice.setViewCount(dto.getViewCount());
    notice.setWriterId(dto.getWriterId());
    return notice;
  }
}
