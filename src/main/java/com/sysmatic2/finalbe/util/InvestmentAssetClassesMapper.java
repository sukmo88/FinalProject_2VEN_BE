package com.sysmatic2.finalbe.util;

import com.sysmatic2.finalbe.strategy.dto.InvestmentAssetClassesDto;
import com.sysmatic2.finalbe.strategy.entity.InvestmentAssetClassesEntity;
import org.springframework.stereotype.Component;

public class InvestmentAssetClassesMapper {
    // 엔티티를 응답 DTO로 변환하는 메서드
    public static InvestmentAssetClassesDto toDto(InvestmentAssetClassesEntity iacEntity) {
        InvestmentAssetClassesDto iacDto = new InvestmentAssetClassesDto();
        iacDto.setInvestmentAssetClassesId(iacEntity.getInvestmentAssetClassesId());
        iacDto.setOrder(iacEntity.getOrder());
        iacDto.setInvestmentAssetClassesName(iacEntity.getInvestmentAssetClassesName());
        iacDto.setInvestmentAssetClassesIcon(iacEntity.getInvestmentAssetClassesIcon());
        iacDto.setIsActive(iacEntity.getIsActive());
        return iacDto;
    }

    // 요청 DTO를 엔티티로 변환하는 메서드
    public static InvestmentAssetClassesEntity toEntity(InvestmentAssetClassesDto iacDto) {
        InvestmentAssetClassesEntity iacEntity = new InvestmentAssetClassesEntity();
        iacEntity.setOrder(iacDto.getOrder());
        iacEntity.setInvestmentAssetClassesName(iacDto.getInvestmentAssetClassesName());
        iacEntity.setInvestmentAssetClassesIcon(iacDto.getInvestmentAssetClassesIcon());
        iacEntity.setIsActive(iacDto.getIsActive());
        return iacEntity;
    }
}
