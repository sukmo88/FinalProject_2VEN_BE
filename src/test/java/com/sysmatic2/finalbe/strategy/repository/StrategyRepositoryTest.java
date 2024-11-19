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
@TestPropertySource(locations = "classpath:application-test.properties")
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
                new InvestmentAssetClassesEntity(3, 3, "해외주식 옵션", "해외주식옵션이미지링크", "해외주식의 옵션 상품을 다룹니다.", "N")
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
        for (int i = 1; i <= 20; i++) {
            StrategyEntity strategy = new StrategyEntity();
            strategy.setStrategyTitle("Strategy " + i);
            strategy.setFollowersCount((long) (i * 10));
            strategy.setMinInvestmentAmount("10000");
            strategy.setStrategyStatusCode("ACTIVE");
            strategy.setIsPosted("Y");
            strategy.setIsGranted("Y");
            strategy.setWriterId("Writer_" + i);
            strategy.setTradingCycleEntity(tradingCycleRepository.findById((i % 2) + 1).orElseThrow());
            strategy.setTradingTypeEntity(tradingTypeRepository.findById((i % 3) + 1).orElseThrow());
            strategyRepository.save(strategy);

            for (int j = 1; j <= 2; j++) {
                StrategyIACEntity strategyIAC = new StrategyIACEntity();
                strategyIAC.setStrategyEntity(strategy);
                strategyIAC.setInvestmentAssetClassesEntity(investmentAssetClassesRepository.findById(j).orElseThrow());
                strategyIAC.setIsActive("Y");
                strategyIAC.setWritedBy("Writer_" + i + "_IAC_" + j);
                strategyIACRepository.save(strategyIAC);
            }
        }
    }

    @Test
    @DisplayName("필터 조건 및 페이징 테스트 - 중복 데이터 없이 반환")
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
        assertThat(result.getContent().get(0).getTradingCycleIcon()).contains("포지션이미지링크");
        assertThat(result.getContent().get(0).getInvestmentAssetClassesIcons()).contains("해외주식이미지링크");
    }

    @Test
    @DisplayName("다양한 필터 조건 조합 테스트")
    void testFindStrategiesByVariousFilters() {
        PageRequest pageable = PageRequest.of(0, 5);

        // 조건 1: TradingCycleId=1, InvestmentAssetClassesId=1
        Page<StrategyListDto> result1 = strategyRepository.findStrategiesByFilters(1, 1, pageable);
        assertThat(result1.getContent()).allMatch(dto -> dto.getTradingCycleIcon().contains("포지션이미지링크"));
        assertThat(result1.getContent().get(0).getInvestmentAssetClassesIcons()).contains("국내주식이미지링크");

        // 조건 2: TradingCycleId=2, InvestmentAssetClassesId=2
        Page<StrategyListDto> result2 = strategyRepository.findStrategiesByFilters(2, 2, pageable);
        assertThat(result2.getContent()).allMatch(dto -> dto.getTradingCycleIcon().contains("데이이미지링크"));
        assertThat(result2.getContent().get(0).getInvestmentAssetClassesIcons()).contains("해외주식이미지링크");

        // 조건 3: 필터가 없는 경우
        Page<StrategyListDto> result3 = strategyRepository.findStrategiesByFilters(null, null, pageable);
        assertThat(result3.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("필터 조건 - TradingCycleId만 사용")
    void testFindStrategiesWithOnlyTradingCycleId() {
        PageRequest pageable = PageRequest.of(0, 5);

        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(1, null, pageable);

        assertThat(result.getContent()).allMatch(dto -> dto.getTradingCycleIcon().contains("포지션이미지링크"));
    }

    @Test
    @DisplayName("필터 조건 - InvestmentAssetClassesId만 사용")
    void testFindStrategiesWithOnlyInvestmentAssetClassesId() {
        PageRequest pageable = PageRequest.of(0, 5);

        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(null, 1, pageable);

        assertThat(result.getContent()).allMatch(dto -> dto.getInvestmentAssetClassesIcons().contains("국내주식이미지링크"));
    }

    @Test
    @DisplayName("필터 조건 - 데이터가 없는 경우")
    void testFindStrategiesWithoutResults() {
        PageRequest pageable = PageRequest.of(0, 5);

        Page<StrategyListDto> result = strategyRepository.findStrategiesByFilters(999, 999, pageable);

        assertThat(result.getContent()).isEmpty();
    }
}