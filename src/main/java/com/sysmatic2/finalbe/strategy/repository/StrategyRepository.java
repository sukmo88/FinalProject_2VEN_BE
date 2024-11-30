package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.dto.StrategyKpDto;
import com.sysmatic2.finalbe.strategy.dto.StrategySmScoreDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
// 기본 JPA 리포지토리 + 커스텀 QueryDSL 리포지토리 기능 확장
public interface StrategyRepository extends JpaRepository<StrategyEntity, Long>, StrategyRepositoryCustom {
    // 작성자 ID로 전략 목록 조회
    List<StrategyEntity> findByWriterId(String writerId);

    // 작성자 ID로 전략 갯수 반환
    Integer countByWriterIdAndIsApproved(String writerId, String isApproved);

    // 전략 상태 코드로 조회
    //List<StrategyEntity> findByStrategyStatusCode(String strategyStatusCode);

    // 작성일을 기준으로 전략 정렬 조회
    List<StrategyEntity> findByOrderByWritedAtDesc();

    // 전략 작성자 id로 전략 목록 조회(페이지네이션)
    Page<StrategyEntity> findByWriterId(String writerId, Pageable pageable);

    // 전략명 기준으로 전략 목록 조회(페이지네이션)
    @Query("SELECT s FROM StrategyEntity s " +
            "WHERE s.strategyTitle LIKE %:keyword% " +
            "AND s.isPosted = :isPosted " +
            "AND s.isApproved = :isApproved")
    Page<StrategyEntity> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("isPosted") String isPosted,
            @Param("isApproved") String isApproved,
            Pageable pageable);

    /**
     * 특정 전략 ID에 대한 FollowersCount를 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return FollowersCount (해당 전략의 팔로워 수)
     */
    @Query("SELECT s.followersCount FROM StrategyEntity s WHERE s.strategyId = :strategyId")
    Long findFollowersCountByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략 ID의 KP-RATIO를 업데이트합니다.
     *
     * @param strategyId 업데이트할 전략 ID
     * @param kpRatio 새로운 KP-RATIO 값
     */
    @Modifying
    @Query("UPDATE StrategyEntity s SET s.kpRatio = :kpRatio WHERE s.strategyId = :strategyId")
    void updateKpRatioByStrategyId(@Param("strategyId") Long strategyId, @Param("kpRatio") BigDecimal kpRatio);


    /**
     * 특정 전략 ID의 KP-RATIO와 SM-SCORE를 동시에 업데이트합니다.
     *
     * @param strategyId 업데이트할 전략 ID
     * @param kpRatio    새로운 KP-RATIO 값
     * @param smScore    새로운 SM-SCORE 값
     */
    @Modifying
    @Query("UPDATE StrategyEntity s SET s.kpRatio = :kpRatio, s.smScore = :smScore WHERE s.strategyId = :strategyId")
    void updateKpRatioAndSmScoreByStrategyId(@Param("strategyId") Long strategyId,
                                             @Param("kpRatio") BigDecimal kpRatio,
                                             @Param("smScore") BigDecimal smScore);

    /**
     * 특정 전략 ID의 SM-SCORE만 업데이트합니다.
     *
     * @param strategyId 업데이트할 전략 ID
     * @param smScore    새로운 SM-SCORE 값
     */
    @Modifying
    @Query("UPDATE StrategyEntity s SET s.smScore = :smScore WHERE s.strategyId = :strategyId")
    void updateSmScoreByStrategyId(@Param("strategyId") Long strategyId, @Param("smScore") BigDecimal smScore);

    /**
     * KP-RATIO가 0보다 큰 전략의 데이터를 페이징 처리하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return KP-RATIO가 0보다 큰 전략 ID와 KP-RATIO 값을 포함하는 페이징된 DTO 리스트
     */
    @Query("""
    SELECT new com.sysmatic2.finalbe.strategy.dto.StrategyKpDto(s.strategyId, s.kpRatio)
    FROM StrategyEntity s
    WHERE s.kpRatio > 0
    ORDER BY s.strategyId ASC
""")
    Page<StrategyKpDto> findByNonZeroKpRatio(Pageable pageable);

    /**
     * 모든 전략의 SM-SCORE 데이터를 페이징하여 조회하는 메서드.
     *
     * - 이 메서드는 StrategyEntity에서 모든 전략 데이터를 조회하여,
     *   전략 ID와 SM-SCORE를 포함하는 DTO 객체로 매핑합니다.
     *
     * @param pageable 페이징 정보를 포함하는 객체 (페이지 번호와 크기 설정)
     * @return 전략 ID와 SM-SCORE를 포함하는 DTO의 페이징된 결과
     */
    @Query("SELECT new com.sysmatic2.finalbe.strategy.dto.StrategySmScoreDto(s.strategyId, s.smScore) " +
            "FROM StrategyEntity s ORDER BY s.strategyId ASC")
    Page<StrategySmScoreDto> findAllStrategySmScores(Pageable pageable);
}
