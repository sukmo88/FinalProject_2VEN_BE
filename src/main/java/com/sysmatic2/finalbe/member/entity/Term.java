package com.sysmatic2.finalbe.member.entity;

import com.sysmatic2.finalbe.StandardCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "term")
@Getter
@Setter
@ToString
public class Term {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "term_id")
    private Integer itermId;

    @Column(name = "term_title", nullable = false)
    private String termTitle;

    @Column(name = "term_content", nullable = false)
    private String termContent;

    @Column(name = "is_required", nullable = false)
    private String isRequired;

    @ManyToOne
    @JoinColumn(name = "target_member_grade_code", nullable = false)
    private StandardCode targetMemberGradeCode;

    @Column(name = "version")
    private String version;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private String isActive;
}