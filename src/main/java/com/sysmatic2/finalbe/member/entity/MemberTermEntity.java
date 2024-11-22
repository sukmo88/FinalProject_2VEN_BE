package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_term")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MemberTermEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_term_id")
    private Long memberTermId;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private TermEntity term;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "is_term_agreed", nullable = false)
    @Pattern(regexp = "Y|N", message = "isTermAgreed는 'Y' 또는 'N'만 허용됩니다.")
    private String isTermAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Column(name = "decline_date")
    private LocalDateTime declineDate;
}