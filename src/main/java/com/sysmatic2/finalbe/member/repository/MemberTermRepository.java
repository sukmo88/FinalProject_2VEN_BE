package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.entity.MemberTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberTermRepository extends JpaRepository<MemberTermEntity, Long> {
}
