package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_term")
@IdClass(TermTypeMemberId.class)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MemberTermEntity extends Auditable {

    @Id
    @Column(name = "term_type", nullable = false)
    private String termType;

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "is_term_agreed", nullable = false)
    @Pattern(regexp = "Y|N", message = "isTermAgreed는 'Y' 또는 'N'만 허용됩니다.")
    private String isTermAgreed;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;
}