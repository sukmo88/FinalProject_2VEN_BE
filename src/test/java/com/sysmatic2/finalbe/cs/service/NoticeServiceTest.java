package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import com.sysmatic2.finalbe.cs.repository.NoticeRepository;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NoticeServiceTest {

  @InjectMocks
  private NoticeService noticeService;

  @Mock
  private NoticeRepository noticeRepository;

  @Mock
  private MemberRepository memberRepository;

  private MemberEntity writer;
  private NoticeEntity noticeEntity;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Mock 데이터 초기화
    writer = new MemberEntity();
    writer.setMemberId("writer-id");
    writer.setNickname("Test Writer");

    noticeEntity = new NoticeEntity();
    noticeEntity.setId(1L);
    noticeEntity.setTitle("Test Notice");
    noticeEntity.setContent("This is a test notice.");
    noticeEntity.setNoticeStatus(NoticeStatus.PUBLISHED);
    noticeEntity.setPostedAt(LocalDateTime.now());
    noticeEntity.setViewCount(0L);
    noticeEntity.setWriter(writer);
  }

  @Test
  void createNotice_ShouldReturnCreatedNoticeDto() {
    // Given
    CreateNoticeDto dto = new CreateNoticeDto();
    dto.setWriterId(writer.getMemberId());
    dto.setTitle("New Notice");
    dto.setContent("This is a new notice.");
    dto.setNoticeStatus(NoticeStatus.DRAFT);
    dto.setScheduledAt(LocalDateTime.now().plusDays(1));

    when(memberRepository.findById(writer.getMemberId())).thenReturn(Optional.of(writer));
    when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(noticeEntity);

    // When
    NoticeDto result = noticeService.createNotice(dto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Notice");
    assertThat(result.getWriterId()).isEqualTo(writer.getMemberId());
    verify(memberRepository, times(1)).findById(writer.getMemberId());
    verify(noticeRepository, times(1)).save(any(NoticeEntity.class));
  }

  @Test
  void createNotice_ShouldThrowException_WhenWriterNotFound() {
    // Given
    CreateNoticeDto dto = new CreateNoticeDto();
    dto.setWriterId("non-existent-writer");

    when(memberRepository.findById("non-existent-writer")).thenReturn(Optional.empty());

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> noticeService.createNotice(dto));
    verify(memberRepository, times(1)).findById("non-existent-writer");
    verify(noticeRepository, never()).save(any(NoticeEntity.class));
  }

  @Test
  void updateNotice_ShouldReturnUpdatedNoticeDto() {
    // Given
    UpdateNoticeDto dto = new UpdateNoticeDto();
    dto.setId(1L);
    dto.setWriterId(writer.getMemberId());
    dto.setTitle("Updated Notice");
    dto.setContent("Updated content.");
    dto.setNoticeStatus(NoticeStatus.PUBLISHED);

    when(noticeRepository.findById(1L)).thenReturn(Optional.of(noticeEntity));
    when(memberRepository.findById(writer.getMemberId())).thenReturn(Optional.of(writer));
    when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(noticeEntity);

    // When
    NoticeDto result = noticeService.updateNotice(dto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Notice");
    verify(noticeRepository, times(1)).findById(1L);
    verify(memberRepository, times(1)).findById(writer.getMemberId());
    verify(noticeRepository, times(1)).save(any(NoticeEntity.class));
  }

  @Test
  void getNoticeById_ShouldReturnNoticeDto() {
    // Given
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(noticeEntity));

    // When
    NoticeDto result = noticeService.getNoticeById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Notice");
    verify(noticeRepository, times(1)).findById(1L);
  }

  @Test
  void deleteNotice_ShouldCallRepositoryDelete() {
    // Given
    when(noticeRepository.existsById(1L)).thenReturn(true);

    // When
    noticeService.deleteNotice(1L);

    // Then
    verify(noticeRepository, times(1)).deleteById(1L);
  }

  @Test
  void searchByTitle_ShouldReturnPageOfNotices() {
    // Given
    PageRequest pageable = PageRequest.of(0, 10);
    Page<NoticeEntity> noticePage = new PageImpl<>(List.of(noticeEntity));
    when(noticeRepository.searchByTitle("Test", pageable)).thenReturn(noticePage);

    // When
    Page<NoticeSummaryDto> result = noticeService.searchByTitle("Test", pageable);

    // Then
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Notice");
    verify(noticeRepository, times(1)).searchByTitle("Test", pageable);
  }
}
