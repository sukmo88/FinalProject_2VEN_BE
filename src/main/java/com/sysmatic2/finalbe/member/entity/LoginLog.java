package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.strategy.entity.Auditable;
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
public class LoginLog extends Auditable {
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

}
