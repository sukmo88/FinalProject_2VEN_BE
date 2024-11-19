package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.ConfirmPasswordMismatchException;
import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupDTO signupDTO) {

        // nickname 중복 여부 확인
        duplicateNicknameCheck(signupDTO.getNickname());

        // 비밀번호 동열여부 확인
        if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
            throw new ConfirmPasswordMismatchException("확인 비밀번호가 일치하지 않습니다.");
        }

        // SignupDto -> MemberEntity로 변환
        MemberEntity member = DtoEntityConversionUtils.convertToMemberEntity(signupDTO, passwordEncoder);

        // MemberEntity -> save() : 가입에 실패하면 예외 발생
            memberRepository.save(member);
    }

    // email 중복 여부 확인
    public void duplicateEmailCheck(String email) {
        // email로 디비에서 데이터 조회 -> 존재하면 중복 예외 발생 (탈퇴 시 개인정보 바로 삭제하므로 회원 상태 비교 불필요)
        if (memberRepository.findByEmail(email) != null) {
            throw new MemberAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }
    }

    // nickname 중복 여부 확인
    public void duplicateNicknameCheck(String nickname) {
        // email로 디비에서 데이터 조회 -> 존재하면 중복 예외 발생 (탈퇴 시 개인정보 바로 삭제하므로 회원 상태 비교 불필요)
        if (memberRepository.findByNickname(nickname) != null) {
            throw new MemberAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }
    }
}
