package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.common.Auditable;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "following_strategy")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FollowingStrategyEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "following_strategy_id")
    private Long followingStrategyId;

    @ManyToOne
    @JoinColumn(name = "strategy_id", nullable = false)
    private StrategyEntity strategy;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private FollowingStrategyFolderEntity followingStrategyFolder;

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt;

    public FollowingStrategyEntity(FollowingStrategyFolderEntity followingStrategyFolder, MemberEntity member, StrategyEntity strategy, LocalDateTime followedAt) {

        this.followingStrategyFolder = followingStrategyFolder;
        this.member = member;
        this.strategy = strategy;
        this.followedAt = followedAt;
    }
}