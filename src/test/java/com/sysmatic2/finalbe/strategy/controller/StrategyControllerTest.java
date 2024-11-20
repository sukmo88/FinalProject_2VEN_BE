package com.sysmatic2.finalbe.strategy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyIACRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.admin.repository.TradingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 컨텍스트 초기화
class StrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        // 초기화
        strategyIACRepository.deleteAll();
        strategyRepository.deleteAll();
        investmentAssetClassesRepository.deleteAll();
        tradingTypeRepository.deleteAll();
        tradingCycleRepository.deleteAll();

        // 데이터 삽입
        createTestTradingCycles();
        createTestInvestmentAssetClasses();
        createTestTradingTypes();
        createTestStrategies();
    }

    private void createTestInvestmentAssetClasses() {
        investmentAssetClassesRepository.saveAll(List.of(
                new InvestmentAssetClassesEntity(1, 1, "국내주식", "국내주식이미지링크", "국내주식은 국내의 주식입니다.", "Y"),
                new InvestmentAssetClassesEntity(2, 2, "해외주식", "해외주식이미지링크", "해외주식은 해외의 주식입니다.", "Y")
        ));
    }

    private void createTestTradingCycles() {
        tradingCycleRepository.saveAll(List.of(
                new TradingCycleEntity(1, 1, "포지션", "포지션이미지링크", "포지션매매방식입니다.", "Y"),
                new TradingCycleEntity(2, 2, "데이", "데이이미지링크", "데이매매방식입니다.", "Y")
        ));
        // 데이터 검증 출력
        System.out.println("Trading Cycles: " + tradingCycleRepository.findAll());
    }

    private void createTestTradingTypes() {
        tradingTypeRepository.saveAll(List.of(
                new TradingTypeEntity(1, 1, "자동", "자동이미지링크", "자동매매방식입니다.", "Y"),
                new TradingTypeEntity(2, 2, "수동", "수동이미지링크", "수동매매방식입니다.", "Y")
        ));
    }

    private void createTestStrategies() {
        TradingCycleEntity tradingCycle = tradingCycleRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("TradingCycle with ID 1 not found"));
        TradingTypeEntity tradingType = tradingTypeRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("TradingType with ID 1 not found"));
        InvestmentAssetClassesEntity assetClass = investmentAssetClassesRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("InvestmentAssetClasses with ID 1 not found"));

        for (int i = 1; i <= 5; i++) {
            StrategyEntity strategy = new StrategyEntity();
            strategy.setStrategyTitle("Strategy " + i);
            strategy.setFollowersCount((long) (i * 10));
            strategy.setMinInvestmentAmount("10000");
//            strategy.setStrategyStatusCode("ACTIVE");
            strategy.setIsPosted("Y");
            strategy.setIsGranted("Y");
            strategy.setWriterId("Writer_" + i);
            strategy.setTradingCycleEntity(tradingCycle);
            strategy.setTradingTypeEntity(tradingType);
            strategyRepository.save(strategy);

            StrategyIACEntity strategyIAC = new StrategyIACEntity();
            strategyIAC.setStrategyEntity(strategy);
            strategyIAC.setInvestmentAssetClassesEntity(assetClass);
            strategyIAC.setWritedBy("Writer_" + i);
            strategyIACRepository.save(strategyIAC);
        }
    }

    @Test
    @DisplayName("전략 등록 폼 조회 - 성공")
    void testGetStrategyRegistrationForm_Success() throws Exception {
        mockMvc.perform(get("/api/strategies/registration-form"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tradingTypeRegistrationDtoList", hasSize(2)))
                .andExpect(jsonPath("$.data.investmentAssetClassesRegistrationDtoList", hasSize(2)))
                .andExpect(jsonPath("$.data.tradingCycleRegistrationDtoList", hasSize(2)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("전략 목록 조회 - 성공")
    void testGetStrategies_Success() throws Exception {
        mockMvc.perform(get("/api/strategies")
                        .param("tradingCycleId", "1")
                        .param("investmentAssetClassesId", "1")
                        .param("page", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("전략 목록 조회 - 잘못된 필터링")
    void testGetStrategies_InvalidFilter() throws Exception {
        mockMvc.perform(get("/api/strategies")
                        .param("tradingCycleId", "999")
                        .param("investmentAssetClassesId", "999")
                        .param("page", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}