package com.sysmatic2.finalbe.cs.service;
import com.sysmatic2.finalbe.cs.dto.NoticeDTO;
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
  public Page<NoticeDTO> getAllNotices(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    return noticeRepository.findAll(pageRequest).map(this::convertToDTO);
  }

  public Optional<NoticeDTO> getNoticeById(Long id) {
    return noticeRepository.findById(id).map(this::convertToDTO);
  }

  public NoticeDTO saveNotice(NoticeDTO noticeDTO) {
    Notice notice = convertToEntity(noticeDTO);
    Notice savedNotice = noticeRepository.save(notice);
    return convertToDTO(savedNotice);
  }

  public void deleteNotice(Long id) {
    noticeRepository.deleteById(id);
  }

  protected NoticeDTO convertToDTO(Notice notice) {
    NoticeDTO dto = new NoticeDTO();
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

  protected Notice convertToEntity(NoticeDTO dto) {
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
