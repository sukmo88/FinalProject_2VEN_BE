package com.sysmatic2.finalbe.util;

import com.sysmatic2.finalbe.strategy.dto.TradingTypeRequestDto;
import com.sysmatic2.finalbe.strategy.dto.TradingTypeResponseDto;
import com.sysmatic2.finalbe.strategy.entity.TradingType;
import org.springframework.stereotype.Component;

@Component
public class TradingTypeMapper {
    // 엔티티를 응답 DTO로 변환하는 메서드
    public TradingTypeResponseDto toDto(TradingType tradingType) {
        TradingTypeResponseDto dto = new TradingTypeResponseDto();
        dto.setTradingTypeId(tradingType.getTradingTypeId());
        dto.setTradingTypeOrder(tradingType.getTradingTypeOrder());
        dto.setTradingTypeName(tradingType.getTradingTypeName());
        dto.setTradingTypeIcon(tradingType.getTradingTypeIcon());
        dto.setIsActive(tradingType.getIsActive());
        return dto;
    }

    // 요청 DTO를 엔티티로 변환하는 메서드
    public TradingType toEntity(TradingTypeRequestDto dto) {
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(dto.getTradingTypeOrder());
        tradingType.setTradingTypeName(dto.getTradingTypeName());
        tradingType.setTradingTypeIcon(dto.getTradingTypeIcon());
        tradingType.setIsActive(dto.getIsActive());
        return tradingType;
    }
}
