package com.sysmatic2.finalbe.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FollowingStrategyListDto {
    private Long followingStrategyId;
    private Long strategyId;
    private BigDecimal kp_ratio;
    //SM-Score
    private BigDecimal smScore;
    private String strategyTitle;

    //팔로워수
    private Long followersCount;

    //순위
    //분석그래프
    //수익률
    //MDD

    public FollowingStrategyListDto(Long followingStrategyId, Long strategyId, Long followersCount, BigDecimal kp_ratio, BigDecimal smScore, String strategyTitle) {
        this.followingStrategyId = followingStrategyId;
        this.strategyId = strategyId;
        this.followersCount = followersCount;
        this.kp_ratio = kp_ratio;
        this.smScore = smScore;
        this.strategyTitle = strategyTitle;
    }
}
