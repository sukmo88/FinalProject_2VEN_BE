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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("닉네임 중복 체크 - 닉네임이 이미 존재하는 경우 MemberAlreadyExistException 발생")
    public void duplicateNickname_nicknameExists() {
        String existingNickname = "nickname";
        Optional<MemberEntity> mockMember = Optional.of(new MemberEntity());
        mockMember.get().setNickname(existingNickname);

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
        when(memberRepository.findByNickname(newNickname)).thenReturn(Optional.empty());

        memberService.duplicateNicknameCheck(newNickname);

        // 메소드 제대로 호출되었는지 확인
        verify(memberRepository, times(1)).findByNickname(newNickname);
    }

    @Test
    @DisplayName("이메일 중복 체크 - 이메일이 이미 존재하는 경우 MemberAlreadyExistException 발생")
    public void duplicateEmail_emailExists() {
        String existingEmail = "email@email.com";
        Optional<MemberEntity> mockMember = Optional.of(new MemberEntity());
        mockMember.get().setEmail(existingEmail);

        // 디비에 이메일이 이미 존재 -> MemberRepository.findByEmail() 시 Member 객체 반환
        when(memberRepository.findByEmail(existingEmail)).thenReturn(mockMember);

        // 존재하는 이메일로 검증 시도하면 예외 발생
        assertThrows(MemberAlreadyExistsException.class,
                () -> memberService.duplicateEmailCheck(existingEmail));

        verify(memberRepository, times(1)).findByEmail(existingEmail);
    }

    @Test
    @DisplayName("이메일 중복 체크 - 이메일이 존재하지 않으면 예외 발생하지 않음")
    public void duplicateEmail_emailNotExists() {

        // DB에 존재하지 않는 이메일로 중복 검사 시행
        String newEmail = "email@email.com";
        when(memberRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        memberService.duplicateEmailCheck(newEmail);

        // 메소드 제대로 호출되었는지 확인
        verify(memberRepository, times(1)).findByEmail(newEmail);
    }
}