package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.dto.DetailedProfileDTO;
import com.sysmatic2.finalbe.member.dto.EmailResponseDTO;
import com.sysmatic2.finalbe.member.dto.SimpleProfileDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {

    // 이메일로 회원 조회
    Optional<MemberEntity> findByEmail(String email);

    // 닉네임으로 회원 조회
    Optional<MemberEntity> findByNickname(String nickname);

    // 간단한 프로필 조회
    @Query("SELECT new com.sysmatic2.finalbe.member.dto.SimpleProfileDTO(m.nickname, m.memberGradeCode, m.introduction, m.fileId) " +
            "FROM MemberEntity m " +
            "WHERE m.memberId = :memberId")
    Optional<SimpleProfileDTO> findSimpleProfileByMemberId(@Param("memberId") String memberId);

    // 상세 프로필 조회
    @Query("SELECT new com.sysmatic2.finalbe.member.dto.DetailedProfileDTO(m.fileId, m.email, m.nickname, m.phoneNumber, m.introduction, " +
            "CASE WHEN (SELECT COUNT(mt) FROM MemberTermEntity mt WHERE mt.member = m AND mt.termType = 'MARKETING_AGREEMENT' AND mt.isTermAgreed = 'Y') > 0 THEN true ELSE false END) " +
            "FROM MemberEntity m " +
            "WHERE m.memberId = :memberId")
    Optional<DetailedProfileDTO> findDetailedProfileByMemberId(@Param("memberId") String memberId);

    // 핸드폰 번호로 이메일 조회
    @Query("SELECT new com.sysmatic2.finalbe.member.dto.EmailResponseDTO(m.email) " +
            "FROM MemberEntity m " +
            "WHERE m.phoneNumber = :phoneNumber")
    List<EmailResponseDTO> findEmailByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}