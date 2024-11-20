package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    Optional<MemberEntity> findByEmail(String email);

    Optional<MemberEntity> findByNickname(String nickname);
}
