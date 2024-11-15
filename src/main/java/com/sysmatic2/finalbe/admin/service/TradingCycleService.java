package com.sysmatic2.finalbe.admin.service;

import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminRequestDto;
import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminResponseDto;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.exception.DuplicateTradingCycleOrderException;
import com.sysmatic2.finalbe.exception.TradingCycleNotFoundException;
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.sysmatic2.finalbe.util.CreatePageResponse.createPageResponse;
import static com.sysmatic2.finalbe.util.DtoEntityConversionUtils.toDto;
import static com.sysmatic2.finalbe.util.DtoEntityConversionUtils.toEntity;

@Service
@RequiredArgsConstructor
public class TradingCycleService {
    private final TradingCycleRepository tradingCycleRepository;

    @Transactional(readOnly = true)
    // 1. 투자주기 전체 목록을 가져오는 메서드
    public Map<String, Object> findAllTradingCycles(int page, int pageSize, String isActive) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("tradingCycleOrder").ascending());

        Page<TradingCycleEntity> tradingCycleList;
        if (isActive == null) {
            tradingCycleList = tradingCycleRepository.findAll(pageable); // 모든 데이터 조회
        } else {
            tradingCycleList = tradingCycleRepository.findByIsActive(isActive, pageable); // 활성 상태에 따른 조회
        }

        Page<TradingCycleAdminResponseDto> pageDtoList = tradingCycleList.map(DtoEntityConversionUtils::toDto);

        return createPageResponse(pageDtoList);
    }

    @Transactional(readOnly = true)
    // 1-1. 투자주기 상세 조회 메서드
    public TradingCycleAdminResponseDto findTradingCycleById(Integer id) {
        TradingCycleEntity tradingCycleEntity = tradingCycleRepository.findById(id)
                .orElseThrow(() -> new TradingCycleNotFoundException(id));

        return toDto(tradingCycleEntity);
    }

    // 2. 투자주기를 등록하는 메서드
    @Transactional
    public void createTradingCycle(TradingCycleAdminRequestDto tradingCycleAdminRequestDto) {
        Integer order = tradingCycleAdminRequestDto.getTradingCycleOrder();

        if (order == null) {
            Integer maxOrder = tradingCycleRepository.findMaxTradingCycleOrder()
                    .orElse(0) + 1; // 최대 순서 +1
            tradingCycleAdminRequestDto.setTradingCycleOrder(maxOrder);
        } else if (tradingCycleRepository.findByTradingCycleOrder(order).isPresent()) {
            throw new DuplicateTradingCycleOrderException(order);
        }

        TradingCycleEntity tradingCycleEntity = toEntity(tradingCycleAdminRequestDto);
        tradingCycleRepository.save(tradingCycleEntity);
    }

    // 3. 투자주기 삭제 메서드
    @Transactional
    public void deleteTradingCycle(Integer id) {
        TradingCycleEntity tradingCycleEntity = tradingCycleRepository.findById(id)
                .orElseThrow(() -> new TradingCycleNotFoundException(id));

        tradingCycleRepository.delete(tradingCycleEntity);
    }

    // 3-1. 투자주기 논리적 삭제 메서드
    @Transactional
    public void softDeleteTradingCycle(Integer id) {
        TradingCycleEntity tradingCycleEntity = tradingCycleRepository.findById(id)
                .orElseThrow(() -> new TradingCycleNotFoundException(id));

        tradingCycleEntity.setIsActive("N");
        tradingCycleRepository.save(tradingCycleEntity);
    }

    // 4. 투자주기 수정 메서드
    @Transactional
    public void updateTradingCycle(Integer id, TradingCycleAdminRequestDto tradingCycleAdminRequestDto) {
        TradingCycleEntity existingTradingCycleEntity = tradingCycleRepository.findById(id)
                .orElseThrow(() -> new TradingCycleNotFoundException(id));

        Integer requestedOrder = tradingCycleAdminRequestDto.getTradingCycleOrder();
        if (requestedOrder != null && !requestedOrder.equals(existingTradingCycleEntity.getTradingCycleOrder())) {
            tradingCycleRepository.findByTradingCycleOrder(requestedOrder)
                    .ifPresent(order -> {
                        throw new DuplicateTradingCycleOrderException(requestedOrder);
                    });
        }

        existingTradingCycleEntity.setTradingCycleOrder(tradingCycleAdminRequestDto.getTradingCycleOrder());
        existingTradingCycleEntity.setTradingCycleName(tradingCycleAdminRequestDto.getTradingCycleName());
        existingTradingCycleEntity.setTradingCycleIcon(tradingCycleAdminRequestDto.getTradingCycleIcon());
        existingTradingCycleEntity.setIsActive(tradingCycleAdminRequestDto.getIsActive());

        tradingCycleRepository.save(existingTradingCycleEntity);
    }
}
