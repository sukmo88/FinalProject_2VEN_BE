package com.sysmatic2.finalbe.admin.repository;

import com.sysmatic2.finalbe.admin.entity.StrategyApprovalRequestsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyApprovalRequestsRepository extends JpaRepository<StrategyApprovalRequestsEntity, Long> {
    //Pagination 적용한 리스트
    Page<StrategyApprovalRequestsEntity> findAll(Pageable pageable);
}
