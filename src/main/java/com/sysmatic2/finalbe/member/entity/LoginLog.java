package com.sysmatic2.finalbe.member.entity;

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
@Table(name="login_log")
@Getter
@Setter
@ToString
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="login_log_id")
    private int loginLogId;

    @Column(name="member_id", nullable = false)
    private Long memberId;

    @Column(name="login_at")
    private LocalDateTime LoginAt;

    @Column(name="ip")
    private String ip;

    @Column(name="user_agent")
    private String userAgent;

    @Column(name="is_success", nullable = false)
    private char isSuccess;

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
