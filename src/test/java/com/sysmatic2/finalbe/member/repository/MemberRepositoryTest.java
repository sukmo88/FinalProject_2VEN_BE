package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.dto.DetailedProfileDTO;
import com.sysmatic2.finalbe.member.dto.SimpleProfileDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.util.RandomKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    // memberEntity 생성하는 메소드
    private MemberEntity createMemberEntity() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberId(RandomKeyGenerator.createUUID());
        memberEntity.setMemberGradeCode("MEMBER_ROLE_TRADER");
        memberEntity.setMemberStatusCode("MEMBER_STATUS_ACTIVE");
        memberEntity.setEmail("test@test.com");
        memberEntity.setPassword("qwer1234!");
        memberEntity.setNickname("testNick");
        memberEntity.setPhoneNumber("01012345678");
        memberEntity.setIntroduction("testIntroduction");
        memberEntity.setFileId("fileId1");
        return memberEntity;
    };

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    // memberId로 member 찾아 simpleProfileDTO 반환하는 기능 테스트

    // 1. memberId로 저장된 member가 없으면 반환된 DTO가 없다. (빈 Optional 반환)
    @Test
    @DisplayName("없는 memberId로 simpleProfile 조회하면 null")
    public void findSimpleProfileTest_1() {
        // 존재하지 않는 memberId로 조회 시, 반환값이 없다.
        String notExistMemberId = "notExistMemberId";
        Optional<SimpleProfileDTO> simpleProfileByMemberId = memberRepository.findSimpleProfileByMemberId(notExistMemberId);
        assertTrue(simpleProfileByMemberId.isEmpty());
    }

    // 2. memberId로 저장된 member가 있으면 하나의 DTO가 반환된다.
    // 3. memberId로 저장된 member가 있으면 반환된 DTO 내 필드값이 저장한 값과 동일해야 한다.
    @Test
    @DisplayName("존재하는 memberId로 simpleProfile 조회하면 하나의 DTO의 반환")
    public void findSimpleProfileTest_2() {
        MemberEntity member = createMemberEntity();
        memberRepository.save(member);

        // 조회하면 값 존재
        Optional<SimpleProfileDTO> simpleProfileByMemberId = memberRepository.findSimpleProfileByMemberId(member.getMemberId());
        assertTrue(simpleProfileByMemberId.isPresent());

        // 저장한 필드값과 반환된 필드값 동일
        SimpleProfileDTO simpleProfileDTO = simpleProfileByMemberId.get();
        assertEquals(simpleProfileDTO.getNickname(), member.getNickname());
        assertEquals(simpleProfileDTO.getMemberType(), member.getMemberGradeCode().replace("MEMBER_ROLE_", ""));
        assertEquals(simpleProfileDTO.getIntroduction(), member.getIntroduction());
        assertEquals(simpleProfileDTO.getFileId(), member.getFileId());
    }

    // memberId로 member 찾아 detailedProfileDTO 반환하는 기능 테스트

    // 1. memberId로 저장된 member가 없으면 반환된 DTO가 없다. (빈 Optional 반환)
    @Test
    @DisplayName("없는 memberId로 detailedProfile 조회하면 null")
    public void findDetailedProfileTest_1() {
        // 존재하지 않는 memberId로 조회 시, 반환값이 없다.
        String notExistMemberId = "notExistMemberId";
        Optional<DetailedProfileDTO> detailedProfileByMemberId = memberRepository.findDetailedProfileByMemberId(notExistMemberId);
        assertTrue(detailedProfileByMemberId.isEmpty());
    }

    // 2. memberId로 저장된 member가 있으면 하나의 DTO가 반환된다.
    // 3. memberId로 저장된 member가 있으면 반환된 DTO 내 필드값이 저장한 값과 동일해야 한다.
    @Test
    @DisplayName("존재하는 memberId로 detailedProfile 조회하면 하나의 DTO의 반환")
    public void findDetailedProfileTest_2() {
        MemberEntity member = createMemberEntity();
        memberRepository.save(member);

        // 조회하면 값 존재
        Optional<DetailedProfileDTO> detailedProfileByMemberId = memberRepository.findDetailedProfileByMemberId(member.getMemberId());
        assertTrue(detailedProfileByMemberId.isPresent());

        // 저장한 필드값과 반환된 필드값 동일
        DetailedProfileDTO detailedProfileDTO = detailedProfileByMemberId.get();
        assertEquals(detailedProfileDTO.getEmail(), member.getEmail());
        assertEquals(detailedProfileDTO.getNickname(), member.getNickname());
        assertEquals(detailedProfileDTO.getPhoneNumber(), member.getPhoneNumber());
        assertEquals(detailedProfileDTO.getIntroduction(), member.getIntroduction());
        assertEquals(detailedProfileDTO.getFileId(), member.getFileId());
    }
}