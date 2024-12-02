package com.sysmatic2.finalbe.strategy.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "live_account_data_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString

// Todo) 실계좌 인증 이력
public class LiveAccountDataHistoryEntity {

    @Id
    @Column(name = "live_account_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long liveAccountId;

    //@OneToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "strategy_id", unique = true, nullable = false)
    //StrategyEntity strategy; // 전략 ID
    @Column(name = "strategy_id", nullable = false)
    Long strategyId;

    @Column(name = "file_name", nullable = false)
    String fileName;

    @Column(name = "file_link", nullable = false, unique = true)
    String fileLink;

    @Column(name = "file_size")
    Integer fileSize;

    @Column(name = "file_introduce")
    String fileIntroduce;

    @Column(name = "file_type")
    String fileType;

    @CreatedBy
    @Column(name = "writer_id", updatable = false, nullable = false)
    private String writerId; // 작성자 ID

    @CreatedDate
    @Column(name="writed_at", updatable = false, nullable = false)
    private LocalDateTime writedAt; // 작성일시

    @Column(name = "is_active")
    String isActive;
}
