package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
import com.sysmatic2.finalbe.cs.repository.NoticeRepository;
import com.sysmatic2.finalbe.cs.util.NoticeMapper;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {

  @Autowired
  private NoticeRepository noticeRepository;

  @Autowired
  private MemberRepository memberRepository;

  // 공지사항 생성
  @Transactional
  public NoticeDto createNotice(CreateNoticeDto dto) {
    // 작성자 조회
    MemberEntity writer = memberRepository.findById(dto.getWriterId())
            .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다. ID: " + dto.getWriterId()));

    // NoticeEntity 생성 및 저장
    NoticeEntity entity = NoticeMapper.toEntity(dto, writer);
    NoticeEntity savedEntity = noticeRepository.save(entity);

    return NoticeMapper.toDto(savedEntity);
  }

  // 공지사항 수정
  @Transactional
  public NoticeDto updateNotice(UpdateNoticeDto dto) {
    // 기존 공지사항 조회
    NoticeEntity existingNotice = noticeRepository.findById(dto.getId())
            .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + dto.getId()));

    // 작성자 조회
    MemberEntity writer = memberRepository.findById(dto.getWriterId())
            .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다. ID: " + dto.getWriterId()));

    // NoticeEntity 업데이트
    NoticeEntity updatedEntity = NoticeMapper.toEntity(dto, writer);
    updatedEntity.setPostedAt(existingNotice.getPostedAt()); // 기존 작성 날짜 유지
    updatedEntity.setViewCount(existingNotice.getViewCount()); // 기존 조회수 유지

    NoticeEntity savedEntity = noticeRepository.save(updatedEntity);

    return NoticeMapper.toDto(savedEntity);
  }

  // 공지사항 상세 조회
  public NoticeDto getNoticeById(Long id) {
    NoticeEntity notice = noticeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id));

    return NoticeMapper.toDto(notice);
  }

  // 공지사항 삭제
  @Transactional
  public void deleteNotice(Long id) {
    if (!noticeRepository.existsById(id)) {
      throw new IllegalArgumentException("삭제할 공지사항이 존재하지 않습니다. ID: " + id);
    }
    noticeRepository.deleteById(id);
  }

  // 제목으로 공지사항 검색
  public Page<NoticeSummaryDto> searchByTitle(String keyword, Pageable pageable) {
    Page<NoticeEntity> notices = noticeRepository.searchByTitle(keyword, pageable);
    return notices.map(NoticeMapper::toSummaryDto);
  }

  // 내용으로 공지사항 검색
  public Page<NoticeSummaryDto> searchByContent(String keyword, Pageable pageable) {
    Page<NoticeEntity> notices = noticeRepository.searchByContent(keyword, pageable);
    return notices.map(NoticeMapper::toSummaryDto);
  }

  // 제목 또는 내용으로 공지사항 검색
  public Page<NoticeSummaryDto> searchByTitleOrContent(String keyword, Pageable pageable) {
    Page<NoticeEntity> notices = noticeRepository.searchByTitleOrContent(keyword, pageable);
    return notices.map(NoticeMapper::toSummaryDto);
  }

  // 상태별 공지사항 검색
  public Page<NoticeSummaryDto> searchByStatus(String status, Pageable pageable) {
    Page<NoticeEntity> notices = noticeRepository.findByStatus(status, pageable);
    return notices.map(NoticeMapper::toSummaryDto);
  }

  // 특정 작성자별 공지사항 검색
  public Page<NoticeSummaryDto> searchByWriter(String writerId, Pageable pageable) {
    Page<NoticeEntity> notices = noticeRepository.findByWriter(writerId, pageable);
    return notices.map(NoticeMapper::toSummaryDto);
  }

  // 기간별 공지사항 검색
  public Page<NoticeSummaryDto> searchByDateRange(String startDate, String endDate, Pageable pageable) {
    Page<NoticeEntity> notices = noticeRepository.findByPostedAtBetween(startDate, endDate, pageable);
    return notices.map(NoticeMapper::toSummaryDto);
  }
}
