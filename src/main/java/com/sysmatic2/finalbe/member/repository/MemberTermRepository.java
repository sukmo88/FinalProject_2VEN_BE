package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.entity.MemberTermEntity;
import com.sysmatic2.finalbe.member.entity.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTermRepository extends JpaRepository<MemberTermEntity, Long> {

    List<MemberTermEntity> findByTerm(TermEntity testTerm);

    List<MemberTermEntity> findByMember(MemberEntity testMember);
}
