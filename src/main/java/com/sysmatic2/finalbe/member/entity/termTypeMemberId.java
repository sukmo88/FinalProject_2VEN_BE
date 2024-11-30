package com.sysmatic2.finalbe.member.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class termTypeMemberId implements Serializable {

    private String termType; // MemberTermEntity의 @Id 필드 중 하나와 이름 및 타입 일치
    private String member;   // MemberEntity의 @Id와 일치 (ManyToOne 관계)

}