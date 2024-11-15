package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesRegistrationDto;
import com.sysmatic2.finalbe.admin.dto.TradingCycleRegistrationDto;
import com.sysmatic2.finalbe.admin.dto.TradingTypeRegistrationDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class StrategyRegistrationDto {
    private List<TradingTypeRegistrationDto> tradingTypeRegistrationDtoList;
    private List<InvestmentAssetClassesRegistrationDto> investmentAssetClassesRegistrationDtoList;
    private List<TradingCycleRegistrationDto> tradingCycleRegistrationDtoList;
}
