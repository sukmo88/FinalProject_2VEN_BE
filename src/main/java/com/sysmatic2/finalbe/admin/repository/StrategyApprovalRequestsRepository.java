package com.sysmatic2.finalbe.admin.repository;

import com.sysmatic2.finalbe.admin.entity.StrategyApprovalRequestsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyApprovalRequestsRepository extends JpaRepository<StrategyApprovalRequestsEntity, Long> {
    //Pagination 적용한 리스트
    Page<StrategyApprovalRequestsEntity> findAll(Pageable pageable);

    //전략 id값으로 해당 객체 가져오기
    @Query("SELECT requestEntity FROM StrategyApprovalRequestsEntity requestEntity " +
            "WHERE requestEntity.strategy.strategyId = :strategyId AND requestEntity.isApproved = 'N'")
    List<StrategyApprovalRequestsEntity> findRejectedRequestsByStrategyId(@Param("strategyId") Long strategyId);

    //전략 id값, isApproved N인 값중 제일 최근값
    @Query(value = "SELECT * FROM strategy_approval_requests " +
            "WHERE strategy_id = :strategyId AND is_approved = 'N' " +
            "ORDER BY rejection_datetime DESC LIMIT 1",
            nativeQuery = true)
    Optional<StrategyApprovalRequestsEntity> findLatestRejectedRequestByStrategyId(@Param("strategyId") Long strategyId);
}
