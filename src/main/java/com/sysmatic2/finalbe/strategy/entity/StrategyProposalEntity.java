package com.sysmatic2.finalbe.strategy.entity;

import com.sysmatic2.finalbe.common.Auditable;
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

@Entity
@Table(name = "strategy_proposal")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
public class StrategyProposalEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strategy_proposal_id")
    private Long strategyProposalId; // 전략제안서 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", unique = true, nullable = false)
    private StrategyEntity strategy; // 전략 ID

    @Column(name="file_title", nullable = false)
    private String fileTitle = "전략제안서"; // 전략제안서 제목

    @Column(name="file_link", unique = true, nullable = false)
    private String fileLink; // 전략제안서 링크

    @Column(name="file_size")
    private Integer fileSize; // 전략제안서 사이즈

    @Column(name="file_introduce")
    private String fileIntroduce; // 제안서 설명

    @Column(name="file_type")
    private String fileType; // 제안서 파일 확장자

    @CreatedBy
    @Column(name = "writer_id", updatable = false, nullable = false)
    private String writerId; // 작성자 ID

    @LastModifiedBy
    @Column(name="updater_id")
    private String updaterId; // 수정자 ID

    @CreatedDate
    @Column(name="writed_at", updatable = false, nullable = false)
    private LocalDateTime writedAt; // 작성일시

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt; // 수정일시
}
