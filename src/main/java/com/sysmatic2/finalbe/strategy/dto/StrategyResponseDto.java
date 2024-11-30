package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StrategyResponseDto {
    //전략 기본정보 DTO
    //TODO) validation
    private String tradingTypeName; //매매유형명
    private String tradingTypeIcon; //매매유형아이콘링크
    private List<StrategyIACResponseDto> strategyIACEntities; //투자자산 분류 리스트
    private String tradingCycleName; //주기명
    private String tradingCycleIcon; //주기아이콘링크

    private String memberId;     //트레이더 ID
    private String nickname;     //트레이더별명
    private String profilePath;  //트레이더 이미지

    private Long strategyId;            //전략 Id
    private String strategyTitle;       //전략명
    private String strategyStatusCode;  //전략 운용 코드 - 운용중/운용종료
    private String minInvestmentAmount; //최소운용가능금액
    private String strategyOverview;    //전략설명
    private LocalDateTime writedAt;     //작성일시
    private String isPosted;            //공개여부
    private String isApproved;           //승인여부
    private Long followersCount;       //팔로워수

    private String strategyProposalLink; // 제안서 url
}
