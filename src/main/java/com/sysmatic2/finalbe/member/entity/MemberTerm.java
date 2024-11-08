package com.sysmatic2.finalbe.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_term")
@Getter
@Setter
@ToString
public class MemberTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_term_id")
    private Long memberTermId;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "is_term_agreed", nullable = false)
    private String isTermAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Column(name = "decline_date")
    private LocalDateTime declineDate;
}