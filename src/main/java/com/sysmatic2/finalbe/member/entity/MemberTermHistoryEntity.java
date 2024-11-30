package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity(name = "member_term_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberTermHistoryEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_term_history_id")
    private Long id;

    @Column(name = "term_type", nullable = false)
    private Long termType;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "is_term_agreed", nullable = false)
    @Pattern(regexp = "Y|N", message = "isTermAgreed는 'Y' 또는 'N'만 허용됩니다.")
    private String isTermAgreed;

    @Column(name = "change_start_date", nullable = false)
    private LocalDateTime changeStartDate;

    @Column(name = "chage_start_date", nullable = false)
    private LocalDateTime chageStartDate;

    @Column(name = "change_end_date", nullable = false)
    private LocalDateTime changeEndDate;
}
