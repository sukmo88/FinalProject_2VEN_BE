package com.sysmatic2.finalbe.strategy.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentAssetClassesPayloadDto {
    //등록용 DTO
    private Integer order;
    private String investmentAssetClassesName;
    private String investmentAssetClassesIcon;
    private Character isActive;
}
