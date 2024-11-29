package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatisticsRepository extends JpaRepository<DailyStatisticsEntity, Long> {

    /**
     * 특정 전략에서 가장 최근의 일일 통계 데이터를 1개 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @param pageable   페이징 객체 (최신 데이터 1개만 조회하도록 설정)
     * @return 최신 일일 통계 데이터 리스트 (크기 1)
     */
    @Query("SELECT d FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date DESC")
    List<DailyStatisticsEntity> findLatestByStrategyId(@Param("strategyId") Long strategyId, Pageable pageable);

    /**
     * 특정 전략의 일 손익 데이터를 날짜 오름차순으로 조회합니다.
     * 전략 id 리스트를 받고 각 전략의 가장 최근의 일일 통계 데이터 1개를 리스트에 담는다.
     *
     * @param strategyIds 조회할 전략의 ID
     * @return 최신 일일 통계 데이터 리스트
     */
    @Query(value = """
        SELECT * FROM daily_statistics ds
        WHERE ds.strategy_id IN :strategyIds
        AND ds.daily_statistics_id = (
            SELECT MAX(ds_inner.daily_statistics_id)
            FROM daily_statistics ds_inner
            WHERE ds_inner.strategy_id = ds.strategy_id
        )
    """, nativeQuery = true)
    List<DailyStatisticsEntity> findLatestStatisticsByStrategyIds(@Param("strategyIds") List<Long> strategyIds);

    /**
     * 특정 전략의 일손익(dailyProfitLoss) 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 일 손익 데이터 리스트
     */
    @Query("SELECT d.dailyProfitLoss FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<BigDecimal> findDailyProfitLossesByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 최근 1년 기준으로 기준가 데이터를 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @param oneYearAgo 1년 전 기준 날짜
     * @return 최근 1년 내 기준가 리스트 (날짜 오름차순으로 정렬)
     */
    @Query("SELECT d.referencePrice FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "AND d.date >= :oneYearAgo " +
            "ORDER BY d.date ASC")
    List<BigDecimal> findReferencePricesOneYearAgo(@Param("strategyId") Long strategyId, @Param("oneYearAgo") LocalDate oneYearAgo);

    /**
     * 특정 전략의 모든 기준가 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 기준가 리스트
     */
    @Query("SELECT ds.referencePrice FROM DailyStatisticsEntity ds WHERE ds.strategyEntity.strategyId = :strategyId ORDER BY ds.date ASC")
    List<BigDecimal> findAllReferencePricesByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 모든 입출금 내역 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 입출금 내역 리스트
     */
    @Query("SELECT ds.depWdPrice FROM DailyStatisticsEntity ds WHERE ds.strategyEntity.strategyId = :strategyId ORDER BY ds.date ASC")
    List<BigDecimal> findDepWdHistoryByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 모든 일 손익률 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 일 손익률 리스트
     */
    @Query("SELECT d.dailyPlRate FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<BigDecimal> findDailyPlRatesByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략 ID와 날짜에 해당하는 데이터가 존재하는지 확인합니다.
     *
     * @param strategyId 전략 ID
     * @param date       확인할 날짜
     * @return 데이터가 존재하면 true, 없으면 false
     */
    @Query("SELECT COUNT(ds) > 0 FROM DailyStatisticsEntity ds WHERE ds.strategyEntity.strategyId = :strategyId AND ds.date = :date")
    boolean existsByStrategyIdAndDate(@Param("strategyId") Long strategyId, @Param("date") LocalDate date);

    /**
     * 특정 전략의 누적 손익 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 누적 손익 리스트
     */
    @Query("SELECT ds.cumulativeProfitLoss FROM DailyStatisticsEntity ds WHERE ds.strategyEntity.strategyId = :strategyId ORDER BY ds.date ASC")
    List<BigDecimal> findCumulativeProfitLossByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 누적 손익률 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 누적 손익률 리스트
     */
    @Query("SELECT ds.cumulativeProfitLossRate FROM DailyStatisticsEntity ds WHERE ds.strategyEntity.strategyId = :strategyId ORDER BY ds.date ASC")
    List<BigDecimal> findCumulativeProfitLossRateByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 일간 통계 데이터를 최신 날짜순으로 페이징 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @param pageable   페이징 정보 (페이지 번호와 크기)
     * @return 최신 날짜순으로 정렬된 일간 통계 데이터 {@link Page}
     */
    Page<DailyStatisticsEntity> findByStrategyEntityStrategyIdOrderByDateDesc(Long strategyId, Pageable pageable);

    /**
     * 특정 전략 ID에 대한 전략 통계 데이터를 조회합니다.
     *
     * @param strategyId 전략 ID
     * @return 해당 전략의 가장 최신 통계 데이터 (Optional로 반환)
     */
    @Query("SELECT d FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date DESC")
    List<DailyStatisticsEntity> findLatestStatisticsByStrategyId(@Param("strategyId") Long strategyId, Pageable pageable);

    /**
     * 특정 전략의 가장 오래된 날짜 데이터를 조회합니다.
     *
     * @param strategyId 전략 ID
     * @return 가장 오래된 날짜 (Optional 반환)
     */
    @Query("SELECT MIN(d.date) FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId")
    Optional<LocalDate> findEarliestDateByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 날짜 기준으로 이전 날짜 중 가장 최신 데이터를 조회합니다.
     *
     * @param strategyId 전략 ID
     * @param fromDate   기준 날짜
     * @param pageable   페이징 객체 (최대 1개의 데이터만 반환)
     * @return 기준 날짜 이전의 가장 최신 데이터 리스트 (최대 1개 데이터 반환)
     */
    @Query("SELECT d FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date < :fromDate ORDER BY d.date DESC")
    List<DailyStatisticsEntity> findLatestBeforeDate(@Param("strategyId") Long strategyId, @Param("fromDate") LocalDate fromDate, Pageable pageable);

    /**
     * 특정 날짜 기준으로 이후 날짜 중 가장 오래된 데이터를 조회합니다.
     *
     * @param strategyId 전략 ID
     * @param fromDate   기준 날짜
     * @param pageable   페이징 객체 (최대 1개의 데이터만 반환)
     * @return 기준 날짜 이전의 가장 최신 데이터 리스트 (최대 1개 데이터 반환)
     */
    @Query("SELECT d FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date > :fromDate ORDER BY d.date ASC")
    List<DailyStatisticsEntity> findOldestAfterDateList(@Param("strategyId") Long strategyId, @Param("fromDate") LocalDate fromDate, Pageable pageable);

    /**
     * 특정 날짜 기준으로 이후 데이터를 조회합니다.
     *
     * @param strategyId 전략 ID
     * @param fromDate   기준 날짜
     * @return 기준 날짜 이후의 데이터 리스트
     */
    @Query("SELECT d FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date >= :fromDate ORDER BY d.date ASC")
    List<DailyStatisticsEntity> findAllAfterDate(@Param("strategyId") Long strategyId, @Param("fromDate") LocalDate fromDate);

    /**
     * 특정 날짜 이후 데이터를 삭제합니다.
     *
     * @param strategyId 전략 ID
     * @param fromDate   기준 날짜
     */
    @Modifying
    @Query("DELETE FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date >= :fromDate")
    void deleteFromDate(@Param("strategyId") Long strategyId, @Param("fromDate") LocalDate fromDate);

    /**
     * 특정 전략의 가장 최근 팔로워 수를 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @param pageable   페이징 객체 (최신 데이터 1개 조회)
     * @return 최신 팔로워 수 (크기 1)
     */
    @Query("SELECT d.followersCount FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date DESC")
    List<Long> findLatestFollowersCountByStrategyId(@Param("strategyId") Long strategyId, Pageable pageable);

    /**
     * 특정 전략의 지정된 날짜 이후의 가장 오래된 날짜를 조회합니다 (페이징 지원).
     *
     * @param strategyId 전략 ID
     * @param date       기준 날짜
     * @param pageable   페이징 객체
     * @return 지정된 조건에 맞는 날짜가 포함된 페이지 객체
     */
    @Query("SELECT d.date FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date > :date ORDER BY d.date ASC")
    Page<LocalDate> findNextDatesAfter(@Param("strategyId") Long strategyId, @Param("date") LocalDate date, Pageable pageable);

    // 특정 날짜 바로 이전 데이터 조회
    @Query("SELECT d FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date < :date ORDER BY d.date DESC")
    Page<DailyStatisticsEntity> findPreviousStates(@Param("strategyId") Long strategyId, @Param("date") LocalDate date, Pageable pageable);

    /**
     * 일간 분석 ID 리스트에 해당하는 데이터를 삭제합니다.
     *
     * @param dailyStatisticsIds 삭제할 일간 분석 ID 리스트
     */
    @Modifying
    @Query("DELETE FROM DailyStatisticsEntity d WHERE d.dailyStatisticsId IN :dailyStatisticsIds")
    void deleteAllById(@Param("dailyStatisticsIds") List<Long> dailyStatisticsIds);

    /**
     * 특정 전략의 ddDay와 maxDdInRate 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 ddDay와 maxDdInRate 데이터 리스트 (Object[] 형태로 반환)
     */
    @Query("SELECT d.ddDay, d.maxDdInRate FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<Object[]> findDdDayAndMaxDdInRateByStrategyIdOrderByDate(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 모든 현재 자본인하율 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 현재 자본인하율 리스트
     */
    @Query("SELECT d.currentDrawdownRate FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<BigDecimal> findAllDrawdownRatesByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략의 모든 자본인하 금액 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 모든 자본인하 금액 데이터 리스트
     */
    @Query("SELECT d.currentDrawdownAmount FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<BigDecimal> findAllDrawdownAmountsByStrategyId(@Param("strategyId") Long strategyId);
}