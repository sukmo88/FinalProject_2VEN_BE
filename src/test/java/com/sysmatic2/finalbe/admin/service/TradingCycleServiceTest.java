package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.admin.service.TradingCycleService;
import com.sysmatic2.finalbe.exception.DuplicateTradingCycleOrderException;
import com.sysmatic2.finalbe.exception.TradingCycleNotFoundException;
import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminRequestDto;
import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminResponseDto;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradingCycleServiceTest {

    @InjectMocks
    private TradingCycleService tradingCycleService;

    @Mock
    private TradingCycleRepository tradingCycleRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. findAllTradingCycles 메서드 테스트
    @Test
    @DisplayName("전체 목록 조회 - isActive가 null일 때 모든 데이터를 반환")
    void findAllTradingCycles_shouldReturnAllWhenIsActiveIsNull() {
        TradingCycleEntity entity1 = new TradingCycleEntity();
        entity1.setTradingCycleId(1);
        entity1.setTradingCycleOrder(1);
        entity1.setTradingCycleName("Cycle 1");
        entity1.setTradingCycleIcon("Icon 1");
        entity1.setIsActive("Y");

        TradingCycleEntity entity2 = new TradingCycleEntity();
        entity2.setTradingCycleId(2);
        entity2.setTradingCycleOrder(2);
        entity2.setTradingCycleName("Cycle 2");
        entity2.setTradingCycleIcon("Icon 2");
        entity2.setIsActive("Y");

        Page<TradingCycleEntity> tradingCycles = new PageImpl<>(List.of(entity1, entity2));
        when(tradingCycleRepository.findAll(any(Pageable.class))).thenReturn(tradingCycles);

        Map<String, Object> result = tradingCycleService.findAllTradingCycles(0, 10, null);

        assertNotNull(result);
        assertEquals(2, ((List<TradingCycleAdminResponseDto>)result.get("data")).size());
        verify(tradingCycleRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("활성 상태가 'Y'일 때 활성화된 투자주기만 반환")
    void findAllTradingCycles_shouldReturnOnlyActiveWhenIsActiveIsY() {
        TradingCycleEntity entity1 = new TradingCycleEntity();
        entity1.setTradingCycleId(1);
        entity1.setTradingCycleOrder(1);
        entity1.setTradingCycleName("Active Cycle");
        entity1.setTradingCycleIcon("Icon A");
        entity1.setIsActive("Y");

        Page<TradingCycleEntity> tradingCycles = new PageImpl<>(List.of(entity1));
        when(tradingCycleRepository.findByIsActive(eq("Y"), any(Pageable.class))).thenReturn(tradingCycles);

        Map<String, Object> result = tradingCycleService.findAllTradingCycles(0, 10, "Y");

        assertNotNull(result);
        assertEquals(1, ((List<TradingCycleAdminResponseDto>) result.get("data")).size());
        assertEquals("Active Cycle", ((List<TradingCycleAdminResponseDto>) result.get("data")).get(0).getTradingCycleName());
        verify(tradingCycleRepository, times(1)).findByIsActive(eq("Y"), any(Pageable.class));
    }

    @Test
    @DisplayName("활성 상태가 'N'일 때 비활성화된 투자주기만 반환")
    void findAllTradingCycles_shouldReturnOnlyInactiveWhenIsActiveIsN() {
        TradingCycleEntity entity1 = new TradingCycleEntity();
        entity1.setTradingCycleId(2);
        entity1.setTradingCycleOrder(2);
        entity1.setTradingCycleName("Inactive Cycle");
        entity1.setTradingCycleIcon("Icon B");
        entity1.setIsActive("N");

        Page<TradingCycleEntity> tradingCycles = new PageImpl<>(List.of(entity1));
        when(tradingCycleRepository.findByIsActive(eq("N"), any(Pageable.class))).thenReturn(tradingCycles);

        Map<String, Object> result = tradingCycleService.findAllTradingCycles(0, 10, "N");

        assertNotNull(result);
        assertEquals(1, ((List<TradingCycleAdminResponseDto>) result.get("data")).size());
        assertEquals("Inactive Cycle", ((List<TradingCycleAdminResponseDto>) result.get("data")).get(0).getTradingCycleName());
        verify(tradingCycleRepository, times(1)).findByIsActive(eq("N"), any(Pageable.class));
    }

    @Test
    @DisplayName("ID로 투자주기를 조회 - 정상 조회")
    void findTradingCycleById_shouldReturnTradingCycleWhenIdExists() {
        TradingCycleEntity entity = new TradingCycleEntity();
        entity.setTradingCycleId(1);
        entity.setTradingCycleName("Cycle 1");
        when(tradingCycleRepository.findById(1)).thenReturn(Optional.of(entity));

        TradingCycleAdminResponseDto result = tradingCycleService.findTradingCycleById(1);

        assertNotNull(result);
        assertEquals("Cycle 1", result.getTradingCycleName());
    }

    @Test
    @DisplayName("ID로 투자주기를 조회 - 존재하지 않는 ID로 조회 시 예외 발생")
    void findTradingCycleById_shouldThrowExceptionWhenIdNotFound() {
        when(tradingCycleRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(TradingCycleNotFoundException.class, () -> tradingCycleService.findTradingCycleById(999));
    }

    @Test
    @DisplayName("투자주기를 등록 - 순서가 null일 때 최대 순서 + 1로 설정")
    void createTradingCycle_shouldSetOrderWhenOrderIsNull() {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleName("New Cycle");
        when(tradingCycleRepository.findMaxTradingCycleOrder()).thenReturn(Optional.of(2));

        tradingCycleService.createTradingCycle(requestDto);

        assertEquals(3, requestDto.getTradingCycleOrder());
        verify(tradingCycleRepository, times(1)).save(any(TradingCycleEntity.class));
    }

    @Test
    @DisplayName("투자주기를 등록 - 중복된 순서로 등록 시 예외 발생")
    void createTradingCycle_shouldThrowExceptionWhenOrderIsDuplicate() {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleOrder(1);
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(1);
        when(tradingCycleRepository.findByTradingCycleOrder(1)).thenReturn(Optional.of(tradingCycleEntity));

        assertThrows(DuplicateTradingCycleOrderException.class, () -> tradingCycleService.createTradingCycle(requestDto));
    }

    @Test
    @DisplayName("투자주기를 등록 - 정상적으로 등록")
    void createTradingCycle_shouldSaveWhenOrderIsValid() {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleOrder(1);
        requestDto.setTradingCycleName("Valid Cycle");

        tradingCycleService.createTradingCycle(requestDto);

        verify(tradingCycleRepository, times(1)).save(any(TradingCycleEntity.class));
    }

    @Test
    @DisplayName("투자주기를 삭제 - 정상 삭제")
    void deleteTradingCycle_shouldDeleteWhenIdExists() {
        TradingCycleEntity entity = new TradingCycleEntity();
        when(tradingCycleRepository.findById(1)).thenReturn(Optional.of(entity));

        tradingCycleService.deleteTradingCycle(1);

        verify(tradingCycleRepository, times(1)).delete(entity);
    }

    @Test
    @DisplayName("투자주기를 논리적 삭제 - 정상적으로 논리적 삭제")
    void softDeleteTradingCycle_shouldSoftDeleteWhenIdExists() {
        TradingCycleEntity entity = new TradingCycleEntity();
        when(tradingCycleRepository.findById(1)).thenReturn(Optional.of(entity));

        tradingCycleService.softDeleteTradingCycle(1);

        assertEquals("N", entity.getIsActive());
        verify(tradingCycleRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("투자주기를 수정 - 정상적으로 업데이트")
    void updateTradingCycle_shouldUpdateWhenIdExistsAndOrderIsValid() {
        TradingCycleEntity entity = new TradingCycleEntity();
        entity.setTradingCycleOrder(1);
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleOrder(2);
        requestDto.setTradingCycleName("Updated Cycle");

        when(tradingCycleRepository.findById(1)).thenReturn(Optional.of(entity));
        when(tradingCycleRepository.findByTradingCycleOrder(2)).thenReturn(Optional.empty());

        tradingCycleService.updateTradingCycle(1, requestDto);

        assertEquals(2, entity.getTradingCycleOrder());
        assertEquals("Updated Cycle", entity.getTradingCycleName());
        verify(tradingCycleRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("투자주기를 수정 - 존재하지 않는 ID로 수정 시 예외 발생")
    void updateTradingCycle_shouldThrowExceptionWhenIdNotFound() {
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        when(tradingCycleRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(TradingCycleNotFoundException.class, () -> tradingCycleService.updateTradingCycle(999, requestDto));
    }

    @Test
    @DisplayName("투자주기를 수정 - 중복된 순서로 수정 시 예외 발생")
    void updateTradingCycle_shouldThrowExceptionWhenOrderIsDuplicate() {
        TradingCycleEntity entity = new TradingCycleEntity();
        entity.setTradingCycleOrder(1);
        TradingCycleAdminRequestDto requestDto = new TradingCycleAdminRequestDto();
        requestDto.setTradingCycleOrder(2);

        when(tradingCycleRepository.findById(1)).thenReturn(Optional.of(entity));
        when(tradingCycleRepository.findByTradingCycleOrder(2)).thenReturn(Optional.of(new TradingCycleEntity()));

        assertThrows(DuplicateTradingCycleOrderException.class, () -> tradingCycleService.updateTradingCycle(1, requestDto));
    }
}