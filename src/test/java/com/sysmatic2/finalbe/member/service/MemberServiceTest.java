package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("닉네임 중복 체크 - 닉네임이 이미 존재하는 경우 MemberAlreadyExistException 발생")
    public void duplicateNickname_nicknameExists() {
        String existingNickname = "nickname";
        MemberEntity mockMember = new MemberEntity();
        mockMember.setNickname(existingNickname);

        // 디비에 닉네임이 이미 존재 -> MemberRepository.findByNickname() 시 Member 객체 반환
        when(memberRepository.findByNickname(existingNickname)).thenReturn(mockMember);

        // 존재하는 닉네임으로 검증 시도하면 예외 발생
        assertThrows(MemberAlreadyExistsException.class,
                () -> memberService.duplicateNicknameCheck(existingNickname));

        verify(memberRepository, times(1)).findByNickname(existingNickname);
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 닉네임이 존재하지 않으면 예외 발생하지 않음")
    public void duplicateNickname_nicknameNotExists() {

        // DB에 존재하지 않는 닉네임으로 중복 검사 시행
        String newNickname = "uniqueNickname";
        when(memberRepository.findByNickname(newNickname)).thenReturn(null);

        memberService.duplicateNicknameCheck(newNickname);

        // 메소드 제대로 호출되었는지 확인
        verify(memberRepository, times(1)).findByNickname(newNickname);
    }

    @Test
    public void createUUID() {
        // UUID 생성 메소드 테스트 필요!! -> private인데 어떻게 테스트하지?
    }
}