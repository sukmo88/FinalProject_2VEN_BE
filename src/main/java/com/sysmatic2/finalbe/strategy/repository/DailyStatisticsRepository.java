package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.dto.DateRange;
import com.sysmatic2.finalbe.strategy.dto.DdDayAndMaxDdInRate;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
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
    @Query("SELECT new com.sysmatic2.finalbe.strategy.dto.DdDayAndMaxDdInRate(d.ddDay, d.maxDdInRate) " +
            "FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<DdDayAndMaxDdInRate> findDdDayAndMaxDdInRateByStrategyIdOrderByDate(@Param("strategyId") Long strategyId);

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


    @Query("""
        SELECT new com.sysmatic2.finalbe.strategy.dto.DateRange(
            MIN(d.date),
            MAX(d.date)
        )
        FROM DailyStatisticsEntity d
        WHERE d.strategyEntity.strategyId = :strategyId
    """)
    Optional<DateRange> findEarliestAndLatestDatesByStrategyId(@Param("strategyId") Long strategyId);

    /**
     * 특정 전략과 특정 날짜에 해당하는 일간 분석 데이터를 조회하는 메서드.
     *
     * - 이 메서드는 DailyStatisticsEntity에서 전략 ID와 날짜를 기준으로 데이터를 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @param date       조회할 날짜
     * @return 해당 전략 ID와 날짜에 해당하는 DailyStatisticsEntity를 Optional 형태로 반환
     */
    @Query("SELECT d FROM DailyStatisticsEntity d WHERE d.strategyEntity.strategyId = :strategyId AND d.date = :date")
    Optional<DailyStatisticsEntity> findByStrategyIdAndDate(@Param("strategyId") Long strategyId, @Param("date") LocalDate date);

    /**
     * 특정 날짜에 데이터가 없는 전략들의 ID를 페이징하여 조회합니다.
     *
     * - 이 메서드는 DailyStatisticsEntity 테이블에서 주어진 날짜에 데이터가 없는 전략 ID를 조회합니다.
     * - 전략 ID는 StrategyEntity와 연관되어 있어야 하며, 해당 날짜에 데이터가 존재하지 않는 경우에만 반환합니다.
     *
     * @param date 조회할 날짜
     * @param pageable 페이징 정보를 포함하는 객체 (페이지 번호와 크기 설정)
     * @return 주어진 날짜에 데이터가 없는 전략 ID의 페이징된 결과
     */
    @Query("""
        SELECT s.strategyId
        FROM StrategyEntity s
        WHERE s.strategyId NOT IN (
            SELECT d.strategyEntity.strategyId
            FROM DailyStatisticsEntity d
            WHERE d.date = :date
        )
        ORDER BY s.strategyId ASC
    """)
    Page<Long> findStrategyIdsWithoutDailyStatistics(@Param("date") LocalDate date, Pageable pageable);

    /**
<<<<<<< HEAD
     * 특정 전략 ID와 연도, 월에 해당하는 모든 원금을 조회하는 메서드.
     *
     * - 일간 분석 데이터(DailyStatisticsEntity) 테이블에서 특정 전략 ID에 대한 데이터만 조회합니다.
     * - 조건: 특정 연도와 월에 해당하는 데이터만 가져옵니다.
     * - 원금 필드(principal)를 리스트 형태로 반환합니다.
     *
     * @param strategyId 전략 ID (DailyStatisticsEntity와 연관된 전략)
     * @param year       조회할 연도 (yyyy)
     * @param month      조회할 월 (1~12)
     * @return 해당 월의 모든 원금(BigDecimal) 리스트
     */
    @Query("""
    SELECT d.principal 
    FROM DailyStatisticsEntity d
    WHERE d.strategyEntity.strategyId = :strategyId 
      AND FUNCTION('YEAR', d.date) = :year
      AND FUNCTION('MONTH', d.date) = :month
""")
    List<BigDecimal> findDailyPrincipalsByStrategyIdAndMonth(@Param("strategyId") Long strategyId,
                                                             @Param("year") int year,
                                                             @Param("month") int month);

    /**
     * 특정 전략의 특정 월의 모든 입출금 금액을 조회하는 메서드.
     *
     * @param strategyId 전략 ID
     * @param year       조회할 연도
     * @param month      조회할 월
     * @return 해당 월의 모든 입출금 금액 리스트 (없으면 빈 리스트 반환)
     */
    @Query("""
    SELECT d.depWdPrice 
    FROM DailyStatisticsEntity d
    WHERE d.strategyEntity.strategyId = :strategyId 
      AND FUNCTION('YEAR', d.date) = :year
      AND FUNCTION('MONTH', d.date) = :month
""")
    List<BigDecimal> findDailyDepWdAmountsByStrategyIdAndMonth(@Param("strategyId") Long strategyId,
                                                               @Param("year") int year,
                                                               @Param("month") int month);

    /**
     * 특정 전략의 특정 월의 모든 손익 데이터를 조회하는 메서드.
     *
     * @param strategyId 전략 ID
     * @param year       조회할 연도
     * @param month      조회할 월
     * @return 해당 월의 모든 손익 리스트 (없으면 빈 리스트 반환)
     */
    @Query("""
    SELECT d.dailyProfitLoss 
    FROM DailyStatisticsEntity d
    WHERE d.strategyEntity.strategyId = :strategyId 
      AND FUNCTION('YEAR', d.date) = :year
      AND FUNCTION('MONTH', d.date) = :month
""")
    List<BigDecimal> findDailyProfitLossesByStrategyIdAndMonth(@Param("strategyId") Long strategyId,
                                                               @Param("year") int year,
                                                               @Param("month") int month);

    /**
     * 특정 전략의 특정 월 마지막 기준가를 조회하는 메서드.
     *
     * - 일간 분석 테이블에서 특정 월의 마지막 기준가를 조회합니다.
     * - 해당 월 데이터가 없으면 빈 리스트를 반환합니다.
     *
     * @param strategyId 전략 ID
     * @param year       조회할 연도
     * @param month      조회할 월
     * @param pageable   페이징 정보 (한 개의 데이터만 가져오기 위해 Pageable 사용)
     * @return 특정 월의 마지막 기준가 리스트 (없으면 빈 리스트 반환)
     */
    @Query("""
    SELECT d.referencePrice
    FROM DailyStatisticsEntity d
    WHERE d.strategyEntity.strategyId = :strategyId
      AND FUNCTION('YEAR', d.date) = :year
      AND FUNCTION('MONTH', d.date) = :month
    ORDER BY d.date DESC
""")
    List<BigDecimal> findLastReferencePriceByStrategyIdAndMonth(
            @Param("strategyId") Long strategyId,
            @Param("year") int year,
            @Param("month") int month,
            Pageable pageable
    );

     /**
     * 시작일과 종료일 사이의 엔티티 갯수 반환
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 엔티티 갯수
     */
    //시작일과 종료일 사이의 엔티티 갯수 반환
    Long countByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 전략 ID에 대한 누적수익률 데이터를 날짜 오름차순으로 조회합니다.
     *
     * @param strategyId 조회할 전략의 ID
     * @return 날짜 오름차순으로 정렬된 누적수익률 리스트
     */
    @Query("SELECT d.cumulativeProfitLossRate FROM DailyStatisticsEntity d " +
            "WHERE d.strategyEntity.strategyId = :strategyId " +
            "ORDER BY d.date ASC")
    List<Double> findCumulativeProfitLossRateByStrategyIdOrderByDate(Long strategyId);

    // strategy id로 일일통계 데이터 모두 삭제
    void deleteAllByStrategyEntity(StrategyEntity strategyEntity);
}