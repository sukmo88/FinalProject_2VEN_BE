package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.InvalidPasswordException;
import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.exception.MemberNotFoundException;
import com.sysmatic2.finalbe.member.dto.DetailedProfileDTO;
import com.sysmatic2.finalbe.member.dto.PasswordUpdateDTO;
import com.sysmatic2.finalbe.member.dto.ProfileUpdateDTO;
import com.sysmatic2.finalbe.member.dto.SimpleProfileDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    // SimpleProfile 조회하는 메소드 테스트 - 없으면 예외 발생
    @Test
    @DisplayName("memberId로 SimpleProfile 조회해서 없으면 예외 발생")
    public void getSimpleProfile_simpleProfileNotExists() {

        // 존재하지 않는 id로 찾으면 null 반환하도록 mock 설정
        String notExistMemberId = "notExistMemberId";
        when(memberRepository.findSimpleProfileByMemberId(notExistMemberId)).thenReturn(Optional.empty());

        // service 호출 시 member 존재하지 않으므로 예외 발생
        assertThrows(MemberNotFoundException.class, () -> memberService.getSimpleProfile(notExistMemberId), "존재하지 않는 회원입니다.");
    }

    // SimpleProfile 조회하는 메소드 테스트 - 있으면 SimpleProfileDTO 반환
    @Test
    @DisplayName("memberId로 SimpleProfile 조회해서 있으면 DTO 반환")
    public void getSimpleProfile_simpleProfileExists() {
        // memberId로 조회 시 SimpleProfileDTO 반환하도록 mock 설정
        String existMemberId = "existMemberId";
        SimpleProfileDTO simpleProfileDTO  = new SimpleProfileDTO();
        when(memberRepository.findSimpleProfileByMemberId(existMemberId))
                .thenReturn(Optional.of(simpleProfileDTO));

        // 예외 발생 X, 메서드 1회 호출되었는지 확인
        assertDoesNotThrow(() ->  memberService.getSimpleProfile(existMemberId));
    }

    // DetailedProfile 조회하는 메소드 테스트 - 없으면 예외 발생
    @Test
    @DisplayName("memberId로 DetailedProfile 조회해서 없으면 예외 발생")
    public void getDetailedProfile_detailedProfileNotExists() {

        // 존재하지 않는 id로 찾으면 null 반환하도록 mock 설정
        String notExistMemberId = "notExistMemberId";
        when(memberRepository.findDetailedProfileByMemberId(notExistMemberId)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.getDetailedProfile(notExistMemberId), "존재하지 않는 회원입니다.");
    }

    // DetailedProfile 조회하는 메소드 테스트 - 있으면 DetailedProfileDTO 반환
    @Test
    @DisplayName("memberId로 DetailedProfile 조회해서 있으면 DTO 반환")
    public void getDetailedProfile_detailedProfileExists() {
        // memberId로 조회 시 DetailedProfileDTO 반환하도록 mock 설정
        String existMemberId = "existMemberId";
        DetailedProfileDTO detailedProfileDTO = new DetailedProfileDTO();
        when(memberRepository.findDetailedProfileByMemberId(existMemberId))
                .thenReturn(Optional.of(detailedProfileDTO));

        // 예외 발생 X, 메서드 1회 호출되었는지 확인
        assertDoesNotThrow(() ->  memberService.getDetailedProfile(existMemberId));
    }

    // 상세 개인정보 수정하는 테스트 - memberId 존재하지 않으면 MemberNotFoundException 발생
    @Test
    @DisplayName("상세 개인정보 수정 - member 존재하지 않아 예외 발생")
    public void modifyDetails_memberNotExists() {
        String notExistMemberId = "notExistMemberId";
        when(memberRepository.findById(notExistMemberId)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.modifyDetails(notExistMemberId, new ProfileUpdateDTO()));
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    public void changePassword_Success() {
        // Arrange
        String memberId = "validMemberId";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setOldPassword(oldPassword);
        passwordUpdateDTO.setNewPassword(newPassword);
        passwordUpdateDTO.setConfirmPassword(newPassword);

        MemberEntity mockMember = new MemberEntity();
        mockMember.setMemberId(memberId);
        mockMember.setPassword("encodedOldPassword");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches(oldPassword, mockMember.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        assertDoesNotThrow(() -> memberService.changePassword(memberId, passwordUpdateDTO));

        // Assert
        verify(memberRepository, times(1)).findById(memberId);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(memberRepository, times(1)).save(mockMember);

        assertEquals("encodedNewPassword", mockMember.getPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 - 존재하지 않는 회원")
    public void changePassword_MemberNotFound() {
        // Arrange
        String invalidMemberId = "invalidMemberId";

        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setOldPassword("oldPassword");
        passwordUpdateDTO.setNewPassword("newPassword");
        passwordUpdateDTO.setConfirmPassword("newPassword");

        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MemberNotFoundException.class, () -> memberService.changePassword(invalidMemberId, passwordUpdateDTO));

        verify(memberRepository, times(1)).findById(invalidMemberId);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호 불일치")
    public void changePassword_InvalidOldPassword() {
        // Arrange
        String memberId = "validMemberId";
        String oldPassword = "wrongPassword";

        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setOldPassword(oldPassword);
        passwordUpdateDTO.setNewPassword("newPassword");
        passwordUpdateDTO.setConfirmPassword("newPassword");

        MemberEntity mockMember = new MemberEntity();
        mockMember.setMemberId(memberId);
        mockMember.setPassword("encodedOldPassword");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches(oldPassword, mockMember.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> memberService.changePassword(memberId, passwordUpdateDTO));

        verify(memberRepository, times(1)).findById(memberId);
        verify(passwordEncoder, times(1)).matches(oldPassword, mockMember.getPassword());
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호와 확인 비밀번호 불일치")
    public void changePassword_NewPasswordsMismatch() {
        // Arrange
        String memberId = "validMemberId";

        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setOldPassword("oldPassword");
        passwordUpdateDTO.setNewPassword("newPassword");
        passwordUpdateDTO.setConfirmPassword("differentPassword");

        MemberEntity mockMember = new MemberEntity();
        mockMember.setMemberId(memberId);
        mockMember.setPassword("encodedOldPassword");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches("oldPassword", mockMember.getPassword())).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> memberService.changePassword(memberId, passwordUpdateDTO));

        verify(memberRepository, times(1)).findById(memberId);
        verify(passwordEncoder, times(1)).matches("oldPassword", mockMember.getPassword());
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("회원가입 성공")
    public void signup_Success() {
        // given : signupDTO를 넘겨주고
        //
        // when : 회원가입 메소드 실행하면
        //
        // then : 응답코드 200, 응답메시지 "회원가입에 성공했습니다."

    }
}