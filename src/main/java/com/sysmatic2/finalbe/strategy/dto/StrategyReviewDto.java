package com.sysmatic2.finalbe.strategy.dto;


import com.sysmatic2.finalbe.strategy.entity.StrategyReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyReviewDto {

    private Long strategyReviewId;
    private String writerId;
    private String nickName;
    private String profileUrl;
    private String content;
    private LocalDateTime writedAt;

    public static StrategyReviewDto fromEntity(StrategyReviewEntity entity) {
        return new StrategyReviewDto(
                entity.getStrategyReviewId(),
                entity.getWriterId().getMemberId(),
                entity.getWriterId().getNickname(),
                entity.getWriterId().getProfilePath(),
                entity.getContent(),
                entity.getWritedAt()
        );
    }
}