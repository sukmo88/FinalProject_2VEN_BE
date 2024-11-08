package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.StandardCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_code")
@Getter
@Setter
@ToString
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "verification_code_id")
    private Long verificationCodeId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "last_attempt_time")
    private LocalDateTime lastAttemptTime;

    @ManyToOne
    @JoinColumn(name = "verification_reason_code", nullable = false)
    private StandardCode verificationReasonCode;
}