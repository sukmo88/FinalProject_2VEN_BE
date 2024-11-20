package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.admin.repository.TradingTypeRepository;
import com.sysmatic2.finalbe.strategy.dto.StrategyListDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@TestPropertySource(locations = "classpath:application-test.properties")
class StrategyRepositoryTest {

    @Autowired
    private StrategyRepository strategyRepository;

    @Autowired
    private InvestmentAssetClassesRepository investmentAssetClassesRepository;

    @Autowired
    private TradingCycleRepository tradingCycleRepository;

    @Autowired
    private TradingTypeRepository tradingTypeRepository;

    @Autowired
    private StrategyIACRepository strategyIACRepository;

    @BeforeEach
    void setUp() {
        createTradingCycles();
        createInvestmentAssetClasses();
        createTradingTypes();
        createStrategiesAndRelations();
    }

    private void createTradingCycles() {
        tradingCycleRepository.saveAll(List.of(
                new TradingCycleEntity(1, 1, "포지션", "포지션이미지링크", "포지션매매방식입니다.", "Y"),
                new TradingCycleEntity(2, 2, "데이", "데이이미지링크", "데이매매방식입니다.", "Y")
        ));
    }

    private void createInvestmentAssetClasses() {
        investmentAssetClassesRepository.saveAll(List.of(
                new InvestmentAssetClassesEntity(1, 1, "국내주식", "국내주식이미지링크", "국내주식은 국내의 주식입니다.", "Y"),
                new InvestmentAssetClassesEntity(2, 2, "해외주식", "해외주식이미지링크", "해외주식은 해외의 주식입니다.", "Y"),
                new InvestmentAssetClassesEntity(3, 3, "해외주식 옵션", "해외주식옵션이미지링크", "해외주식의 옵션 상품을 다룹니다.", "N"),
                new InvestmentAssetClassesEntity(4, 4, "국내ETF", "국내ETF이미지링크", "국내 ETF는 국내 펀드 종목입니다.", "Y"),
                new InvestmentAssetClassesEntity(5, 5, "해외ETF", "해외ETF이미지링크", "해외 ETF는 해외 펀드 종목입니다.", "Y")
        ));
    }

    private void createTradingTypes() {
        tradingTypeRepository.saveAll(List.of(
                new TradingTypeEntity(1, 3, "자동", "자동이미지링크", "자동매매방식입니다.", "Y"),
                new TradingTypeEntity(2, 2, "하이브리드", "하이브리드이미지링크", "자동과 수동을 섞은 매매방식입니다.", "Y"),
                new TradingTypeEntity(3, 1, "수동", "수동이미지링크", "수동매매방식입니다.", "Y")
        ));
    }

    private void createStrategiesAndRelations() {
        for (int i = 1; i <= 50; i++) {
            StrategyEntity strategy = new StrategyEntity();
            strategy.setStrategyTitle("Strategy " + i);
            strategy.setFollowersCount((long) (i * 10));
            strategy.setMinInvestmentAmount("10000");
//            strategy.setStrategyStatusCode("ACTIVE");
            strategy.setIsPosted("Y");
            strategy.setIsGranted("Y");
            strategy.setWriterId("Writer_" + i);
            strategy.setTradingCycleEntity(tradingCycleRepository.findById((i % 2) + 1).orElseThrow());
            strategy.setTradingTypeEntity(tradingTypeRepository.findById((i % 3) + 1).orElseThrow());
            strategyRepository.save(strategy);

            for (int j = 1; j <= 3; j++) {
                StrategyIACEntity strategyIAC = new StrategyIACEntity();
                strategyIAC.setStrategyEntity(strategy);
                strategyIAC.setInvestmentAssetClassesEntity(investmentAssetClassesRepository.findById(j).orElseThrow());
                strategyIAC.setWritedBy("Writer_" + i + "_IAC_" + j);
                strategyIACRepository.save(strategyIAC);
            }
        }
    }


    @Test
    @DisplayName("필터 조건 및 페이징 테스트 - 기본 필터링")
    void testFindStrategiesByFilters() {
        // Given
        int tradingCycleId = 1;
        int investmentAssetClassesId = 2;
        PageRequest pageable = PageRequest.of(0, 5);

        // When
        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(tradingCycleId, investmentAssetClassesId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isGreaterThan(0);
        assertThat(result.getContent().get(0).getTradingCycleIcon()).contains("포지션이미지링크");
    }

    @Test
    @DisplayName("필터 조건 조합 테스트 - 다양한 조합")
    void testFindStrategiesByVariousFilters() {
        // 필터 조합별로 전략이 올바르게 조회되는지 검증
        PageRequest pageable = PageRequest.of(0, 5);

        // TradingCycleId: 1, InvestmentAssetClassesId: 1
        Page<StrategyListDto> result1 = strategyRepository.findStrategiesByFilters(1, 1, pageable);
        assertThat(result1.getContent()).allMatch(dto -> dto.getTradingCycleIcon().contains("포지션이미지링크"));
        assertThat(result1.getContent()).allMatch(dto -> dto.getInvestmentAssetClassesIcons().contains("국내주식이미지링크"));

        // TradingCycleId: 2, InvestmentAssetClassesId: 2
        Page<StrategyListDto> result2 = strategyRepository.findStrategiesByFilters(2, 2, pageable);
        assertThat(result2.getContent()).allMatch(dto -> dto.getTradingCycleIcon().contains("데이이미지링크"));
        assertThat(result2.getContent()).allMatch(dto -> dto.getInvestmentAssetClassesIcons().contains("해외주식이미지링크"));

        // TradingCycleId: 3, InvestmentAssetClassesId: 3 (조건에 맞는 데이터가 없는 경우)
        Page<StrategyListDto> result3 = strategyRepository.findStrategiesByFilters(3, 3, pageable);
        assertThat(result3.getContent()).isEmpty(); // 조건에 맞는 데이터가 없는 경우
    }

    @Test
    @DisplayName("필터 조건 테스트 - TradingCycleId만 제공")
    void testFindStrategiesWithOnlyTradingCycleId() {
        // Given: 페이징 설정
        PageRequest pageable = PageRequest.of(0, 5);

        // When: TradingCycleId만 제공
        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(1, null, pageable);

        // Then: TradingCycleId에 맞는 데이터만 필터링되었는지 확인
        assertThat(result.getContent()).allMatch(dto -> dto.getTradingCycleIcon().contains("포지션이미지링크"));
    }

    @Test
    @DisplayName("필터 조건 테스트 - InvestmentAssetClassesId만 제공")
    void testFindStrategiesWithOnlyInvestmentAssetClassesId() {
        // Given: 페이징 설정
        PageRequest pageable = PageRequest.of(0, 5);

        // When: InvestmentAssetClassesId만 제공
        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(null, 2, pageable);

        // Then: InvestmentAssetClassesId에 맞는 데이터만 필터링되었는지 확인
        assertThat(result.getContent()).allMatch(dto -> dto.getInvestmentAssetClassesIcons().contains("해외주식이미지링크"));
    }

    @Test
    @DisplayName("필터 조건 테스트 - 조건이 없는 경우")
    void testFindStrategiesWithoutFilters() {
        // Given: 페이징 설정
        PageRequest pageable = PageRequest.of(0, 5);

        // When: 필터 조건 없이 호출
        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(null, null, pageable);

        // Then: 모든 데이터가 반환되었는지 확인
        assertThat(result.getContent()).hasSize(5); // 페이징에 따라 5개씩 반환
        assertThat(result.getTotalElements()).isGreaterThan(0); // 데이터가 존재
    }

    @Test
    @DisplayName("팔로워 수 조건으로 전략 조회")
    void testFindByFollowersCountGreaterThanEqual() {
        // When
        List<StrategyEntity> strategies = strategyRepository.findByFollowersCountGreaterThanEqual(200L);

        // Then
        assertThat(strategies).isNotEmpty();
        assertThat(strategies).allMatch(strategy -> strategy.getFollowersCount() >= 200L);
        assertThat(strategies.get(0).getFollowersCount()).isGreaterThanOrEqualTo(200L);
    }

    @Test
    @DisplayName("전략 삭제 테스트")
    void testDeleteStrategy() {
        // Given
        StrategyEntity strategy = strategyRepository.findAll().get(0);

        // When
        strategyRepository.delete(strategy);

        // Then
        assertThat(strategyRepository.findById(strategy.getStrategyId())).isEmpty();
    }

    @Test
    @DisplayName("작성일 기준 정렬 테스트")
    void testFindByOrderByWritedAtDesc() {
        // When
        List<StrategyEntity> strategies = strategyRepository.findByOrderByWritedAtDesc();

        // Then
        assertThat(strategies).isNotEmpty();
        assertThat(strategies.get(0).getWritedAt()).isAfter(strategies.get(1).getWritedAt());
    }
}