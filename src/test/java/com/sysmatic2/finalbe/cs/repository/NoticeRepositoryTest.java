package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class NoticeRepositoryTest {

  @Autowired
  private NoticeRepository noticeRepository;
  @Autowired
  private EntityManager em;
  private MemberEntity writer;

  @BeforeEach
  void setUp() {
    // 가상 작성자 생성 (UUID 사용)
    writer = new MemberEntity();
    writer.setMemberId(UUID.randomUUID().toString());
    writer.setMemberGradeCode("GRADE_1");
    writer.setMemberStatusCode("ACTIVE");
    writer.setEmail("test@example.com");
    writer.setPassword("password123");
    writer.setNickname("test_writer");
    writer.setPhoneNumber("123-456-7890");
    em.persist(writer);
  }

  @Test
  void saveAndRetrieveNotice() {
    // Given
    NoticeEntity notice = new NoticeEntity();
    notice.setTitle("Test Notice");
    notice.setContent("This is a test notice.");
    notice.setNoticeStatus(NoticeStatus.PUBLISHED);
    notice.setWriter(writer);
    notice.setPostedAt(LocalDateTime.now());
    notice.setViewCount(0L);

    // When
    NoticeEntity savedNotice = noticeRepository.save(notice);
    NoticeEntity foundNotice = noticeRepository.findById(savedNotice.getId()).orElse(null);

    // Then
    assertThat(foundNotice).isNotNull();
    assertThat(foundNotice.getTitle()).isEqualTo("Test Notice");
    assertThat(foundNotice.getWriter().getMemberId()).isEqualTo(writer.getMemberId());
  }

  @Test
  void searchByTitle_ShouldReturnMatchingResults() {
    // Given
    NoticeEntity notice = createNotice("Important Title", "Content for testing", NoticeStatus.PUBLISHED);
    noticeRepository.save(notice);

    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<NoticeEntity> results = noticeRepository.searchByTitle("Important", pageable);

    // Then
    assertThat(results.getTotalElements()).isEqualTo(1);
    assertThat(results.getContent().get(0).getTitle()).isEqualTo("Important Title");
  }

  @Test
  void searchByContent_ShouldReturnMatchingResults() {
    // Given
    NoticeEntity notice = createNotice("Test Title", "Matching Content", NoticeStatus.PUBLISHED);
    noticeRepository.save(notice);

    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<NoticeEntity> results = noticeRepository.searchByContent("Matching", pageable);

    // Then
    assertThat(results.getTotalElements()).isEqualTo(1);
    assertThat(results.getContent().get(0).getContent()).isEqualTo("Matching Content");
  }

  @Test
  void findByWriter_ShouldReturnNoticesBySpecificWriter() {
    // Given
    NoticeEntity notice = createNotice("Writer Test", "Written by specific writer", NoticeStatus.PUBLISHED);
    notice.setWriter(writer);
    noticeRepository.save(notice);

    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<NoticeEntity> results = noticeRepository.findByWriter(writer.getMemberId(), pageable);

    // Then
    assertThat(results.getTotalElements()).isEqualTo(1);
    assertThat(results.getContent().get(0).getWriter().getMemberId()).isEqualTo(writer.getMemberId());
  }

  @Test
  void findByPostedAtBetween_ShouldReturnNoticesInDateRange() {
    // Given
    NoticeEntity notice = createNotice("Date Test", "Posted recently", NoticeStatus.PUBLISHED);
    notice.setPostedAt(LocalDateTime.now().minusDays(1));
    noticeRepository.save(notice);

    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<NoticeEntity> results = noticeRepository.findByPostedAtBetween(
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now(),
            pageable
    );

    // Then
    assertThat(results.getTotalElements()).isEqualTo(1);
    assertThat(results.getContent().get(0).getPostedAt()).isEqualTo(notice.getPostedAt());
  }

  private NoticeEntity createNotice(String title, String content, NoticeStatus status) {
    NoticeEntity notice = new NoticeEntity();
    notice.setTitle(title);
    notice.setContent(content);
    notice.setNoticeStatus(status);
    notice.setWriter(writer);
    notice.setPostedAt(LocalDateTime.now());
    notice.setViewCount(0L);
    return notice;
  }
}
