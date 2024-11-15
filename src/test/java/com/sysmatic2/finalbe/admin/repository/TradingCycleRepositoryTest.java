package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradingCycleRepositoryTest {

    @Autowired
    TradingCycleRepository tradingCycleRepository;

    @BeforeEach
    public void resetDatabase() {
        tradingCycleRepository.deleteAll();
    }

    @Test
    public void createTradingCycleList() {
        for (int i = 1; i <= 10; i++) {
            TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
            tradingCycleEntity.setTradingCycleOrder(i);
            tradingCycleEntity.setTradingCycleName("Test Cycle " + i);
            tradingCycleEntity.setTradingCycleIcon("test" + i + ".png");
            tradingCycleEntity.setIsActive(i % 2 == 0 ? "N" : "Y"); // 홀수일 때 "Y", 짝수일 때 "N"

            tradingCycleRepository.save(tradingCycleEntity);
        }
    }

    @Test
    @DisplayName("투자주기 등록 테스트")
    public void registerTradingCycleTest() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(1);
        tradingCycleEntity.setTradingCycleName("Monthly Cycle");
        tradingCycleEntity.setTradingCycleIcon("icon_monthly.png");
        tradingCycleEntity.setTradingCycleDescription("Trades every month.");
        tradingCycleEntity.setIsActive("Y");

        TradingCycleEntity savedTradingCycleEntity = tradingCycleRepository.save(tradingCycleEntity);

        Assertions.assertEquals(tradingCycleEntity.getTradingCycleId(), savedTradingCycleEntity.getTradingCycleId());
        Assertions.assertEquals(tradingCycleEntity.getTradingCycleOrder(), savedTradingCycleEntity.getTradingCycleOrder());
        Assertions.assertEquals(tradingCycleEntity.getTradingCycleName(), savedTradingCycleEntity.getTradingCycleName());
        Assertions.assertEquals(tradingCycleEntity.getTradingCycleIcon(), savedTradingCycleEntity.getTradingCycleIcon());
        Assertions.assertEquals(tradingCycleEntity.getTradingCycleDescription(), savedTradingCycleEntity.getTradingCycleDescription());
        Assertions.assertEquals(tradingCycleEntity.getIsActive(), savedTradingCycleEntity.getIsActive());
    }

    @Test
    @DisplayName("unique 중복 검증")
    public void uniqueDuplicateValidation() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(1);
        tradingCycleEntity.setTradingCycleName("Monthly Cycle");
        tradingCycleEntity.setTradingCycleIcon("icon_monthly.png");
        tradingCycleEntity.setTradingCycleDescription("Trades every month.");
        tradingCycleEntity.setIsActive("Y");

        tradingCycleRepository.save(tradingCycleEntity);

        TradingCycleEntity duplicateTradingCycleEntity = new TradingCycleEntity();
        duplicateTradingCycleEntity.setTradingCycleOrder(1); // 중복된 order
        duplicateTradingCycleEntity.setTradingCycleName("Weekly Cycle");
        duplicateTradingCycleEntity.setTradingCycleIcon("icon_weekly.png");
        duplicateTradingCycleEntity.setTradingCycleDescription("Trades every week.");
        duplicateTradingCycleEntity.setIsActive("N");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingCycleRepository.save(duplicateTradingCycleEntity);
        });
    }

    @Test
    @DisplayName("투자주기 조회 테스트")
    public void retrieveTradingCycleTest() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(1);
        tradingCycleEntity.setTradingCycleName("Monthly Cycle");
        tradingCycleEntity.setTradingCycleIcon("icon_monthly.png");
        tradingCycleEntity.setTradingCycleDescription("Trades every month.");
        tradingCycleEntity.setIsActive("Y");

        TradingCycleEntity savedTradingCycleEntity = tradingCycleRepository.save(tradingCycleEntity);
        TradingCycleEntity retrievedTradingCycleEntity = tradingCycleRepository.findByTradingCycleName(savedTradingCycleEntity.getTradingCycleName());

        assertNotNull(retrievedTradingCycleEntity);
        assertEquals(savedTradingCycleEntity.getTradingCycleName(), retrievedTradingCycleEntity.getTradingCycleName());
    }

    @Test
    @DisplayName("투자주기 수정 테스트")
    public void updateTradingCycleTest() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(1);
        tradingCycleEntity.setTradingCycleName("Monthly Cycle");
        tradingCycleEntity.setTradingCycleIcon("icon_monthly.png");
        tradingCycleEntity.setTradingCycleDescription("Trades every month.");
        tradingCycleEntity.setIsActive("Y");

        TradingCycleEntity savedTradingCycleEntity = tradingCycleRepository.save(tradingCycleEntity);
        savedTradingCycleEntity.setTradingCycleName("Updated Cycle");

        TradingCycleEntity updatedTradingCycleEntity = tradingCycleRepository.save(savedTradingCycleEntity);
        assertEquals(savedTradingCycleEntity.getTradingCycleName(), updatedTradingCycleEntity.getTradingCycleName());
    }

    @Test
    @DisplayName("투자주기 삭제 테스트")
    public void deleteTradingCycleTest() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(1);
        tradingCycleEntity.setTradingCycleName("Monthly Cycle");
        tradingCycleEntity.setTradingCycleIcon("icon_monthly.png");
        tradingCycleEntity.setTradingCycleDescription("Trades every month.");
        tradingCycleEntity.setIsActive("Y");

        TradingCycleEntity savedTradingCycleEntity = tradingCycleRepository.save(tradingCycleEntity);
        tradingCycleRepository.delete(savedTradingCycleEntity);

        Optional<TradingCycleEntity> deletedTradingCycle = tradingCycleRepository.findById(savedTradingCycleEntity.getTradingCycleId());
        assertTrue(deletedTradingCycle.isEmpty());
    }

    @Test
    @DisplayName("isActive 필수값 검증 테스트")
    public void isActiveNotNullValidation() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(2);
        tradingCycleEntity.setTradingCycleName("Weekly Cycle");
        tradingCycleEntity.setTradingCycleIcon("icon_weekly.png");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingCycleRepository.save(tradingCycleEntity);
        });
    }

    @Test
    @DisplayName("tradingCycleIcon 필수값 검증 테스트")
    public void tradingCycleIconNotNullValidation() {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(3);
        tradingCycleEntity.setTradingCycleName("Position Trading");
        tradingCycleEntity.setIsActive("Y");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingCycleRepository.save(tradingCycleEntity);
        });
    }

    @Test
    @DisplayName("tradingCycleName 고유성 검증 테스트")
    public void duplicateTradingCycleNameTest() {
        TradingCycleEntity tradingCycleEntity1 = new TradingCycleEntity();
        tradingCycleEntity1.setTradingCycleOrder(4);
        tradingCycleEntity1.setTradingCycleName("Monthly Cycle");
        tradingCycleEntity1.setTradingCycleIcon("icon_monthly.png");
        tradingCycleEntity1.setIsActive("Y");
        tradingCycleRepository.save(tradingCycleEntity1);

        TradingCycleEntity tradingCycleEntity2 = new TradingCycleEntity();
        tradingCycleEntity2.setTradingCycleOrder(5);
        tradingCycleEntity2.setTradingCycleName("Monthly Cycle"); // 중복된 이름
        tradingCycleEntity2.setTradingCycleIcon("icon_weekly.png");
        tradingCycleEntity2.setIsActive("N");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingCycleRepository.save(tradingCycleEntity2);
        });
    }

    @Test
    @DisplayName("isActive 상태별 페이지네이션 조회 테스트")
    public void findByIsActiveWithPaginationTest() {
        createTradingCycleList();

        Pageable pageable = PageRequest.of(0, 2); // 첫 페이지, 페이지 크기 2
        Page<TradingCycleEntity> activeTypesPage = tradingCycleRepository.findByIsActive("Y", pageable);

        assertEquals(2, activeTypesPage.getSize());
        assertEquals(3, activeTypesPage.getTotalPages());
        assertEquals(5, activeTypesPage.getTotalElements());
        assertEquals("Test Cycle 1", activeTypesPage.getContent().get(0).getTradingCycleName());
        assertEquals("Test Cycle 3", activeTypesPage.getContent().get(1).getTradingCycleName());

        assertTrue(activeTypesPage.isFirst());
        assertFalse(activeTypesPage.isLast());
        assertTrue(activeTypesPage.hasNext());

        Pageable secondPage = PageRequest.of(1, 2);
        Page<TradingCycleEntity> secondActiveTypesPage = tradingCycleRepository.findByIsActive("Y", secondPage);

        assertEquals(2, secondActiveTypesPage.getSize());
        assertEquals("Test Cycle 5", secondActiveTypesPage.getContent().get(0).getTradingCycleName());
        assertEquals("Test Cycle 7", secondActiveTypesPage.getContent().get(1).getTradingCycleName());

        Page<TradingCycleEntity> inactiveTypesPage = tradingCycleRepository.findByIsActive("N", pageable);

        assertEquals(2, inactiveTypesPage.getSize());
        assertEquals(3, inactiveTypesPage.getTotalPages());
        assertEquals(5, inactiveTypesPage.getTotalElements());
        assertEquals("Test Cycle 2", inactiveTypesPage.getContent().get(0).getTradingCycleName());
        assertEquals("Test Cycle 4", inactiveTypesPage.getContent().get(1).getTradingCycleName());
    }
}