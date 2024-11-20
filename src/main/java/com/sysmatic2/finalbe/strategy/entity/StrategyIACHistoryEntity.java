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

import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_investment_asset_classes_history")
@Getter
@Setter
@ToString
public class StrategyIACHistoryEntity extends Auditable {
    @Id
    @Column(name = "strategy_investment_asset_classes_history_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId; //전략Id

    @Column(name = "investment_asset_classes_id", nullable = false)
    private Integer investmentAssetClassId; //투자자산 분류 Id

    @CreatedBy
    @Column(name = "writer_id", nullable = false)
    private String writerId; //작성자 Id

    @LastModifiedBy
    @Column(name = "updater_id")
    private String updaterId; //수정자 Id

    @Column(name = "status", nullable = false)
    private String status; //상태(삭제/수정)

    @CreatedDate
    @Column(name = "writed_at", nullable = false)
    private LocalDateTime writtenAt; //작성일시

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시
}
