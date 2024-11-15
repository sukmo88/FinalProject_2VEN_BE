package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

  // 제목으로 검색
  @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword%")
  Page<Notice> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

  // 내용으로 검색
  @Query("SELECT n FROM Notice n WHERE n.content LIKE %:keyword%")
  Page<Notice> searchByContent(@Param("keyword") String keyword, Pageable pageable);

  // 제목 또는 내용으로 검색
  @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% OR n.content LIKE %:keyword%")
  Page<Notice> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);
}
