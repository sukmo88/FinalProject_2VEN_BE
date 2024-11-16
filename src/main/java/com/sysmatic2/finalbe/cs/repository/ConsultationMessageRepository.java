package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessageEntity, Long> {

  // 특정 스레드의 메시지 목록 조회 (시간 순)
  List<ConsultationMessageEntity> findByThread_IdOrderBySentAtAsc(Long threadId);

  // 특정 사용자가 보낸 메시지 조회 (페이징 및 정렬 포함)
  @Query("SELECT m FROM ConsultationMessageEntity m WHERE m.sender.memberId = :userId ORDER BY m.sentAt DESC")
  Page<ConsultationMessageEntity> findSentMessagesByUserId(@Param("userId") Long userId, Pageable pageable);

  // 특정 사용자가 받은 메시지 조회 (페이징 및 정렬 포함)
  @Query("SELECT m FROM ConsultationMessageEntity m " +
          "WHERE (m.thread.investor.memberId = :userId OR m.thread.trader.memberId = :userId) " +
          "AND m.sender.memberId != :userId ORDER BY m.sentAt DESC")
  Page<ConsultationMessageEntity> findReceivedMessagesByUserId(@Param("userId") Long userId, Pageable pageable);

  // 키워드로 메시지 검색 (페이징 및 정렬 포함)
  @Query("SELECT m FROM ConsultationMessageEntity m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.sentAt DESC")
  Page<ConsultationMessageEntity> searchMessagesByKeyword(@Param("keyword") String keyword, Pageable pageable);

  // 날짜 범위로 메시지 검색 (페이징 및 정렬 포함)
  @Query("SELECT m FROM ConsultationMessageEntity m WHERE m.sentAt BETWEEN :startDate AND :endDate ORDER BY m.sentAt DESC")
  Page<ConsultationMessageEntity> searchMessagesByDateRange(@Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate,
                                                            Pageable pageable);
}
