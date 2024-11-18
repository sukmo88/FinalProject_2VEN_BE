package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyHistoryEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class StrategyDeleteMockTest {

    @Mock
    private StrategyRepository strategyRepo;

    @Mock
    private StrategyHistoryRepository strategyHistoryRepo;

    @InjectMocks
    private StrategyService strategyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteStrategy_success() {
        // Given
        Long strategyId = 1L;

        // Mocking existing StrategyEntity
        StrategyEntity mockStrategyEntity = new StrategyEntity();
        mockStrategyEntity.setStrategyId(strategyId);
        TradingTypeEntity mockTradingTypeEntity = new TradingTypeEntity();
        mockTradingTypeEntity.setTradingTypeId(1);
        mockStrategyEntity.setTradingTypeEntity(mockTradingTypeEntity); // Assume TradingTypeEntity with ID 1

        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleId(2);
        mockStrategyEntity.setTradingCycleEntity(tradingCycleEntity); // Assume TradingCycleEntity with ID 2

        mockStrategyEntity.setStrategyStatusCode("ACTIVE");
        mockStrategyEntity.setMinInvestmentAmount("100000");
        mockStrategyEntity.setFollowersCount(50L);
        mockStrategyEntity.setStrategyTitle("Test Strategy");
        mockStrategyEntity.setWriterId("test_writer");
        mockStrategyEntity.setIsPosted("Y");
        mockStrategyEntity.setIsGranted("N");
        mockStrategyEntity.setWritedAt(LocalDateTime.now().minusDays(10));
        mockStrategyEntity.setStrategyOverview("This is a test strategy.");
        mockStrategyEntity.setUpdaterId("updater_test");
        mockStrategyEntity.setUpdatedAt(LocalDateTime.now().minusDays(5));
        mockStrategyEntity.setExitDate(null);

        when(strategyRepo.findById(strategyId)).thenReturn(Optional.of(mockStrategyEntity));

        // When
        strategyService.deleteStrategy(strategyId);

        // Then
        verify(strategyRepo, times(1)).findById(strategyId);
        verify(strategyRepo, times(1)).deleteById(strategyId);
        verify(strategyHistoryRepo, times(1)).save(any(StrategyHistoryEntity.class));
    }

    @Test
    void deleteStrategy_strategyNotFound() {
        // Given
        Long strategyId = 1L;
        when(strategyRepo.findById(strategyId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> strategyService.deleteStrategy(strategyId));

        verify(strategyRepo, times(1)).findById(strategyId);
        verify(strategyRepo, times(0)).deleteById(anyLong());
        verify(strategyHistoryRepo, times(0)).save(any(StrategyHistoryEntity.class));
    }
}
