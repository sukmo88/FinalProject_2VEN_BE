package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.dto.StrategyListDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
// 기본 JPA 리포지토리 + 커스텀 QueryDSL 리포지토리 기능 확장
public interface StrategyRepository extends JpaRepository<StrategyEntity, Long>, StrategyRepositoryCustom {
    // 작성자 ID로 전략 목록 조회
    List<StrategyEntity> findByWriterId(String writerId);

    // 전략 상태 코드로 조회
    //List<StrategyEntity> findByStrategyStatusCode(String strategyStatusCode);

    // 작성일을 기준으로 전략 정렬 조회
    List<StrategyEntity> findByOrderByWritedAtDesc();

    // 전략 작성자 id로 전략 목록 조회(페이지네이션)
    Page<StrategyEntity> findByWriterId(String writerId, Pageable pageable);

    // 전략명 기준으로 전략 목록 조회(페이지네이션)
    Page<StrategyEntity> findByStrategyTitleContaining(String strategyTitle, Pageable pageable);
}
