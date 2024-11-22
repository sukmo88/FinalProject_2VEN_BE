package com.sysmatic2.finalbe.admin.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestResponseDto {
    //프론트 반환용
    private Long strategyApprovalRequestId; //요청 id
    private LocalDateTime requestDatetime;  //요청 일시
    private String isApproved;              //승인 여부(결과)
    private Long strategyId;                //요청 전략id
    private String strategyTitle;           //요청 전략명
    private String isPosted;                //요청 전략 공개여부
    private String strategyStatus;          //요청 전략 상태(운용중/운용종료)
    private String tradingTypeIcon;         //매매 유형 아이콘
    private String tradingCycleIcon;        //전략 주기 아이콘
    private List<String> investmentAssetClassesIcons; //투자자산 분류 아이콘 리스트

}
