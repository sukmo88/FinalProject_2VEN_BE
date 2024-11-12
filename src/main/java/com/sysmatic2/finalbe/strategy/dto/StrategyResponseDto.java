package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.StandardCodeEntity;
import com.sysmatic2.finalbe.strategy.entity.TradingTypeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StrategyResponseDto {
    private Long strategyId; // 전략 ID
    private TradingTypeEntity tradingTypeEntity; // 매매유형 ID
    private StandardCodeEntity strategyStatusCode; // 전략상태코드(공통 코드)
    private StandardCodeEntity tradingCycleCode; // 매매주기코드(공통 코드)
    private Long followersCount; // 팔로워수
    private String strategyTitle; // 전략명
    private String isPosted; // 공개여부
}
