package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 상담 레포지토리
 */
public interface ConsultationRepository extends JpaRepository<ConsultationEntity, Long> {

  // 투자자가 요청한 상담 조회
  Page<ConsultationEntity> findAllByInvestor_MemberId(String investorId, Pageable pageable);

  // 트레이더가 담당한 상담 조회
  Page<ConsultationEntity> findAllByTrader_MemberId(String traderId, Pageable pageable);

  // 투자자가 요청한 상담 전체 조회
  List<ConsultationEntity> findAllByInvestor(MemberEntity investor);

  // 트레이더가 담당한 상담 전체 조회
  List<ConsultationEntity> findAllByTrader(MemberEntity trader);

  // 전략에 해당하는 상담 모두 삭제
  void deleteAllByStrategy(StrategyEntity strategy);


}
