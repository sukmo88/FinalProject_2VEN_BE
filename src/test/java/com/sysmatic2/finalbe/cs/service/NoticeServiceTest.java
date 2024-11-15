package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.NoticeDto;
import com.sysmatic2.finalbe.cs.entity.Notice;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import com.sysmatic2.finalbe.cs.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoticeServiceTest {

  @Mock
  private NoticeRepository noticeRepository;

  @InjectMocks
  private NoticeService noticeService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getAllNotices_shouldReturnPagedNotices() {
    // Given
    Notice notice = createSampleNotice(NoticeStatus.PUBLISHED);
    Page<Notice> noticePage = new PageImpl<>(Arrays.asList(notice));
    when(noticeRepository.findAll(any(PageRequest.class))).thenReturn(noticePage);

    // When
    Page<NoticeDto> result = noticeService.getAllNotices(0, 10);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Sample Title", result.getContent().get(0).getTitle());
    assertEquals(NoticeStatus.PUBLISHED, result.getContent().get(0).getNoticeStatus());
    verify(noticeRepository, times(1)).findAll(any(PageRequest.class));
  }

  @Test
  void getNoticeById_shouldReturnNoticeDTOWhenFound() {
    // Given
    Notice notice = createSampleNotice(NoticeStatus.SCHEDULED);
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

    // When
    Optional<NoticeDto> result = noticeService.getNoticeById(1L);

    // Then
    assertTrue(result.isPresent());
    assertEquals("Sample Title", result.get().getTitle());
    assertEquals(NoticeStatus.SCHEDULED, result.get().getNoticeStatus());
    verify(noticeRepository, times(1)).findById(1L);
  }

  @Test
  void saveNotice_shouldSaveAndReturnNoticeDTO() {
    // Given
    Notice notice = createSampleNotice(NoticeStatus.DRAFT);
    NoticeDto noticeDTO = noticeService.convertToDTO(notice);
    when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

    // When
    NoticeDto result = noticeService.saveNotice(noticeDTO);

    // Then
    assertNotNull(result);
    assertEquals("Sample Title", result.getTitle());
    assertEquals(NoticeStatus.DRAFT, result.getNoticeStatus());
    verify(noticeRepository, times(1)).save(any(Notice.class));
  }

  @Test
  void deleteNotice_shouldDeleteNoticeById() {
    // Given
    Long id = 1L;
    doNothing().when(noticeRepository).deleteById(id);

    // When
    noticeService.deleteNotice(id);

    // Then
    verify(noticeRepository, times(1)).deleteById(id);
  }

  private Notice createSampleNotice(NoticeStatus status) {
    Notice notice = new Notice();
    notice.setId(1L);
    notice.setNoticeStatus(status);
    notice.setTitle("Sample Title");
    notice.setContent("Sample Content");
    notice.setPostedAt(LocalDateTime.now());
    notice.setUpdatedAt(LocalDateTime.now());
    notice.setScheduledAt(LocalDateTime.now().plusDays(1));
    notice.setViewCount(10L);
    notice.setWriterId(1L);
    return notice;
  }
}
