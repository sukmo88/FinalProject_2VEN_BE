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
    Optional<MemberEntity> findByEmail(String email);

    Optional<MemberEntity> findByNickname(String nickname);

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.SimpleProfileDTO(m.nickname, m.memberGradeCode, m.introduction, m.fileId) FROM MemberEntity m WHERE m.memberId = :memberId")
    Optional<SimpleProfileDTO> findSimpleProfileByMemberId(@Param("memberId") String memberId);

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.DetailedProfileDTO(m.fileId, m.email, m.nickname, m.phoneNumber, m.introduction, m.isAgreedMarketingAd) FROM MemberEntity m WHERE m.memberId = :memberId")
    Optional<DetailedProfileDTO> findDetailedProfileByMemberId(@Param("memberId") String memberId);

    // 핸드폰 번호 일치하는 email 찾기
    @Query("SELECT new com.sysmatic2.finalbe.member.dto.EmailResponseDTO(m.email) FROM MemberEntity m WHERE m.phoneNumber = :phoneNumber")
    List<EmailResponseDTO> findEmailByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
