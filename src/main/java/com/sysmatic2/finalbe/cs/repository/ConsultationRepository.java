package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상담 레포지토리
 */
public interface ConsultationRepository extends JpaRepository<ConsultationEntity, Long> {

  // 투자자가 요청한 상담 조회
  Page<ConsultationEntity> findAllByInvestor_MemberId(String investorId, Pageable pageable);

  // 트레이더가 담당한 상담 조회
  Page<ConsultationEntity> findAllByTrader_MemberId(String traderId, Pageable pageable);

}
