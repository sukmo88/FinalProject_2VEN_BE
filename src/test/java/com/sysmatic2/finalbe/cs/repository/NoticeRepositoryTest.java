package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.Notice;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NoticeRepositoryTest {

  @Autowired
  private NoticeRepository noticeRepository;

  private Notice notice;

  @BeforeEach
  void setUp() {
    notice = new Notice();
    notice.setNoticeStatus(NoticeStatus.DRAFT);
    notice.setTitle("Test Notice");
    notice.setContent("This is a test notice content.");
    notice.setPostedAt(LocalDateTime.now());
    notice.setViewCount(0L);
    notice.setWriterId(1L);
  }

  @Test
  @DisplayName("Notice 저장 테스트")
  void saveNotice() {
    Notice savedNotice = noticeRepository.save(notice);
    assertNotNull(savedNotice.getId());
    assertEquals("Test Notice", savedNotice.getTitle());
    assertEquals(NoticeStatus.DRAFT, savedNotice.getNoticeStatus());
  }

  @Test
  @DisplayName("Notice 조회 테스트")
  void findNoticeById() {
    Notice savedNotice = noticeRepository.save(notice);
    Optional<Notice> foundNotice = noticeRepository.findById(savedNotice.getId());

    assertTrue(foundNotice.isPresent());
    assertEquals(savedNotice.getTitle(), foundNotice.get().getTitle());
    assertEquals(savedNotice.getContent(), foundNotice.get().getContent());
  }

  @Test
  @DisplayName("Notice 수정 테스트")
  void updateNotice() {
    Notice savedNotice = noticeRepository.save(notice);
    savedNotice.setTitle("Updated Title");
    savedNotice.setNoticeStatus(NoticeStatus.PUBLISHED);
    noticeRepository.save(savedNotice);

    Notice updatedNotice = noticeRepository.findById(savedNotice.getId()).orElseThrow();
    assertEquals("Updated Title", updatedNotice.getTitle());
    assertEquals(NoticeStatus.PUBLISHED, updatedNotice.getNoticeStatus());
  }

  @Test
  @DisplayName("Notice 삭제 테스트")
  void deleteNotice() {
    Notice savedNotice = noticeRepository.save(notice);
    noticeRepository.deleteById(savedNotice.getId());

    Optional<Notice> deletedNotice = noticeRepository.findById(savedNotice.getId());
    assertFalse(deletedNotice.isPresent());
  }
}
