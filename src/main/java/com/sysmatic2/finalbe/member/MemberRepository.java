package com.sysmatic2.finalbe.member;

import com.sysmatic2.finalbe.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
}
