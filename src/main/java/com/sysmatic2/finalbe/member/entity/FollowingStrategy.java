package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.strategy.entity.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "following_strategy")
@Getter
@Setter
@ToString
public class FollowingStrategy extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "following_strategy_id")
    private Long followingStrategyId;

    // Strategy 엔티티 클래스 없어서 임시 주석 처리
//    @ManyToOne
//    @JoinColumn(name = "strategy_id", nullable = false)
//    private Strategy strategy;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "following_strategy_folder_id", nullable = false)
    private FollowingStrategyFolder followingStrategyFolder;

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt;
}