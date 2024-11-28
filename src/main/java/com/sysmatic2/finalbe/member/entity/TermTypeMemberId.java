package com.sysmatic2.finalbe.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TermTypeMemberId implements Serializable {

    private String termType; // 복합 키의 첫 번째 필드
    private String member;     // 복합 키의 두 번째 필드 (MemberEntity의 PK 타입)

    // equals() and hashCode() - JPA에서 필수
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermTypeMemberId that = (TermTypeMemberId) o;
        return Objects.equals(termType, that.termType) &&
                Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(termType, member);
    }
}