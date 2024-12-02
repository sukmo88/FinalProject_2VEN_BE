package com.sysmatic2.finalbe.strategy.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LiveAccountDataDto {

    private Long liveAccountId; // 실게좌 인증 ID
    private Long strategyId; // 전략 ID
    private String fileTitle; // 실계좌 제목 
    private String fileLink; // 실계좌 링크
    private Integer fileSize; // 실계좌 사이즈
    private String fileIntroduce; // 실계좌 설명
    private String fileType; // 실계좌 파일 확장자
    private String writerId; // 작성자 ID
    private LocalDateTime writedAt; // 작성일시






}
