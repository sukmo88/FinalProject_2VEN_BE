package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StrategyIACResponseDto {
    //전략 - 투자자산 분류 관계 테이블 데이터 응답용 dto
    //하나의 투자자산 분류에 대한 내용
    private Integer investmentAssetClassesId;
    private String investmentAssetClassesName;
    private String investmentAssetClassesIcon;
}
