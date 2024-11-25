package com.sysmatic2.finalbe.admin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RejectionRequestResponseDto {
    //승인 요청 거절 정보 프론트 반환용
    private Long strategyApprovalRequestId;  //요청 id
    private LocalDateTime requestDatetime;   //요청 일시
    private String isApproved;               //승인 여부(결과) - N 만 날라감
    private Long strategyId;                 //요청 전략id
    private String isPosted;                 //요청 전략 공개여부 - Y,N
    private String applicantId;              //요청자 Id
    private String managerNickname;          //담당자 별명
    private String profileImg;               //담당자 프로필사진 링크
    private String rejectionReason;          //거부 사유
    private LocalDateTime rejectionDatetime; //거부일시
}
