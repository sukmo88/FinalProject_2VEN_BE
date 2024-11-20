package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<ConsultationEntity, Long> {

  // 투자자가 요청한 상담 조회
  Page<ConsultationEntity> findAllByInvestorMemberId(String investorId, Pageable pageable);

  // 트레이더가 담당한 상담 조회
  Page<ConsultationEntity> findAllByTraderMemberId(String traderId, Pageable pageable);

}
