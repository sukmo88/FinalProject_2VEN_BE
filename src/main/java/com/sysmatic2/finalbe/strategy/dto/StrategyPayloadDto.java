package com.sysmatic2.finalbe.strategy.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StrategyPayloadDto {
    //전략 등록용 DTO
    //TODO) validation
    private String strategyTitle; //전략명
    private Integer tradingTypeId; //매매유형 id(자동/반자동/수동)
    private String tradingCycleCode; //주기 공통코드(데이/포지션)
    private String minInvestmentAmount; //최소운용가능금액
    private String strategyOverview; //전략 소개
    private List<Integer> InvestmentAssetClassesIdList; //투자자자산 분류 id 목록
    private String isPosted;

    //제안서 파일
    //private String strategyProposalLink;
}
