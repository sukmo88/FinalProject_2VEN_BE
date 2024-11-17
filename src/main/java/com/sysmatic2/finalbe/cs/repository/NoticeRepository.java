package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

  // 제목으로 검색 (정렬 옵션은 Pageable에서 제공)
  @Query("SELECT n FROM NoticeEntity n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<NoticeEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

  // 내용으로 검색 (정렬 옵션은 Pageable에서 제공)
  @Query("SELECT n FROM NoticeEntity n WHERE LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<NoticeEntity> searchByContent(@Param("keyword") String keyword, Pageable pageable);

  // 제목 또는 내용으로 검색 (정렬 옵션은 Pageable에서 제공)
  @Query("SELECT n FROM NoticeEntity n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
          "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<NoticeEntity> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);

  // 특정 상태의 공지 검색 (정렬 옵션은 Pageable에서 제공)
  @Query("SELECT n FROM NoticeEntity n WHERE n.noticeStatus = :status")
  Page<NoticeEntity> findByStatus(@Param("status") String status, Pageable pageable);

  // 특정 작성자 공지 검색 (정렬 옵션은 Pageable에서 제공)
  @Query("SELECT n FROM NoticeEntity n WHERE n.writer.id = :writerId")
  Page<NoticeEntity> findByWriter(@Param("writerId") String writerId, Pageable pageable);

  // 일정 기간 내 작성된 공지 검색 (정렬 옵션은 Pageable에서 제공)
  @Query("SELECT n FROM NoticeEntity n WHERE n.postedAt BETWEEN :startDate AND :endDate")
  Page<NoticeEntity> findByPostedAtBetween(@Param("startDate") String startDate, @Param("endDate") String endDate, Pageable pageable);
}
