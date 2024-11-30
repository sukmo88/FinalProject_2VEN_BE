package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
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
public class FollowingStrategyFolderEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long folderId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

    @Column(name = "folder_creation_date", nullable = false)
    private LocalDateTime folderCreationDate;

    @Column(name = "is_active", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "is_active 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isActive;

    @Column(name = "is_default_folder", nullable = false, columnDefinition = "CHAR(1)")
    @Pattern(regexp = "Y|N", message = "is_default_folder 필드는 'Y' 또는 'N'만 허용됩니다.")
    private String isDefaultFolder ;

    @Column(name = "folder_order")
    private Integer folderOrder;

    @OneToMany(mappedBy = "followingStrategyFolder")
    private List<FollowingStrategyEntity> followingStrategyList = new ArrayList<>();

}