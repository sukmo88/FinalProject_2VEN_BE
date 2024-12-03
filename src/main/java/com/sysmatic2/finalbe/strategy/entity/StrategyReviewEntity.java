package com.sysmatic2.finalbe.strategy.entity;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "strategy_review")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
public class StrategyReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strategy_review_id")
    private Long strategyReviewId; // 전략 리뷰 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private StrategyEntity strategy; // 전략 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private MemberEntity writerId; // 작성자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updater_id")
    private MemberEntity updaterId; // 수정자 ID

    @Column(name = "content")
    private String content;

    @Column(name = "writed_at")
    private LocalDateTime writedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    private String createdBy; // 작성자 ID

    @LastModifiedBy
    @Column(name="modified_by")
    private String modifiedBy; // 수정자 ID

    @CreatedDate
    @Column(name="created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 작성일시

    @LastModifiedDate
    @Column(name="modified_at")
    private LocalDateTime modifiedAt; // 수정일시

}
