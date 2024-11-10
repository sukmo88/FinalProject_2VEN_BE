package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyUpdateService {
    private final StrategyRepository strategyRepository;

    // 매일 자정에 운용 일수만 업데이트
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateStrategyOperationPeriods() {
        List<StrategyEntity> strategies = strategyRepository.findAll();
        for (StrategyEntity strategy : strategies) {
            strategy.updateOperationPeriod();
        }
        strategyRepository.saveAll(strategies);
    }

    // 상태가 "STRATEGY_STATUS_INACTIVE"로 변경될 때 종료일 업데이트
    @Transactional
    public void updateExitDateIfInactive(StrategyEntity strategy) {
        if ("STRATEGY_STATUS_INACTIVE".equals(strategy.getStrategyStatusCode().getCode()) && strategy.getExitDate() == null) {
            strategy.setExitDate(LocalDateTime.now());
            strategyRepository.save(strategy);
        }
    }
}
