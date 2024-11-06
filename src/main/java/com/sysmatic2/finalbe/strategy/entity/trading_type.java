package com.sysmatic2.finalbe.strategy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "trading_type")
@Getter
@Setter
@ToString
public class trading_type {

    @Id
    @Column(name="trading_type_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int tradingTypeId; // 매매유형 ID

    @Column(nullable = false,unique = true)
    private int order; // 매매유형명

    @Column(name="trading_type_name",nullable = false)
    private String tradingTypeName; // 매매유형아이콘

    @Column(name="trading_type_icon")
    private String tradingTypeIcon; // 매매유형카테고리설명

    @Column(name="is_active", nullable = false)
    private char isActive; // 사용유무

    @CreatedBy
    @Column(name="created_by", nullable = false, updatable = false)
    private long createdBy; // 최초작성자 ID

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 최초등록일시

    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    private long modifiedBy; // 최종수정자 ID

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 최종수정일시
}
