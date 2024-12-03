package com.sysmatic2.finalbe.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FollowingStrategyResponseDto {
    private Long folderId;
    private Long strategyId;
    private Long followingStrategyId;
    private String strategyName;
}
