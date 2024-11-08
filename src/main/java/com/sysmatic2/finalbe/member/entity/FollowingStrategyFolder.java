package com.sysmatic2.finalbe.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "following_strategy_folder")
@Getter
@Setter
@ToString
public class FollowingStrategyFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "following_strategy_folder_id")
    private Long followingStrategyFolderId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

    @Column(name = "folder_creation_date", nullable = false)
    private LocalDateTime folderCreationDate;

    @Column(name = "is_active", nullable = false)
    private String isActive;

    @Column(name = "folder_order")
    private Integer folderOrder;

    @OneToMany(mappedBy = "followingStrategyFolder")
    private List<FollowingStrategy> followingStrategyList = new ArrayList<>();
}