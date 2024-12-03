package com.sysmatic2.finalbe.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowingStrategyRequestDto {
    private Long folderId;
    private Long strategyId;
    private Long followingStrategyId;
}
