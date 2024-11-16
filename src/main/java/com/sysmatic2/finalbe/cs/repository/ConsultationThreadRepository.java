package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationThreadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationThreadRepository extends JpaRepository<ConsultationThreadEntity, Long> {

  // 투자자, 트레이더, 전략 기준으로 상담 스레드 조회
  Optional<ConsultationThreadEntity> findByInvestor_MemberIdAndTrader_MemberIdAndStrategy_StrategyId(
          Long investorId, Long traderId, Long strategyId);

  // 사용자 ID로 상담 스레드 조회 (투자자 또는 트레이더)
  @Query("SELECT c FROM ConsultationThreadEntity c WHERE c.investor.memberId = :userId OR c.trader.memberId = :userId")
  List<ConsultationThreadEntity> findByInvestor_MemberIdOrTrader_MemberId(@Param("userId") Long userId);
}
