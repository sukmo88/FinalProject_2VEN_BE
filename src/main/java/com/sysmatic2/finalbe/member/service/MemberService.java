package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignupDTO signupDTO) {
        // email 인증 여부 확인 - Redis 서버에서 인증여부 확인

        // nickname 중복 여부 확인
        duplicateNicknameCheck(signupDTO.getNickname());

        // 비밀번호 동열여부 확인
        if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match"); // 사용자 예외(또는 시큐리티 예외)로 변경 필요
        }

        // SignupDto -> MemberEntity로 변환
        MemberEntity member = convertToMemberEntity(signupDTO);

        // MemberEntity -> save() : 가입에 실패하면 Exception 발생
            memberRepository.save(member);
    }

    // email 중복 여부 확인
    public void duplicateEmailCheck(String email) {
        // email로 디비에서 데이터 조회 -> 존재하면 중복 예외 발생
        // 탈퇴 시 개인정보 바로 삭제하므로 회원 상태 비교 불필요
        if (memberRepository.findByEmail(email) != null) {
            throw new MemberAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }
    }

    // nickname 중복 여부 확인
    public void duplicateNicknameCheck(String nickname) {
        // email로 디비에서 데이터 조회 -> 존재하면 중복 예외 발생
        // 탈퇴 시 개인정보 바로 삭제하므로 회원 상태 비교 불필요
        if (memberRepository.findByNickname(nickname) != null) {
            throw new MemberAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }
    }

    // SignupDTO를 MemberEntity로 변환하는 메소드
    private MemberEntity convertToMemberEntity(SignupDTO signupDTO) {
        // memberType 넘어온 값에 "MEMBER_ROLE_" 붙여서 -> memberGradeCode
        // "MEMBER_STATUS_ACTIVE" -> memberStatusCode
        // email Redis에서 가져와서 -> email
        // password 해싱 인코딩해서 -> password
        // nickname 그대로 -> nickname
        // phoneNumber 그대로 -> phoneNumber
        // 마케팅,광고성 정보 수신 여부 -> Y
        // 가입일자는 auditing 사용??

        MemberEntity member = new MemberEntity();
        member.setMemberGradeCode("MEMBER_GRADE_" + signupDTO.getMemberType());
        member.setMemberStatusCode("MEMBER_STATUS_ACTIVE");
        member.setEmail(signupDTO.getEmail()); // Redis 에서 가져오는 걸로 변경 필요
        member.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        member.setNickname(signupDTO.getNickname());
        member.setPhoneNumber(signupDTO.getPhoneNumber());
        member.setIsAgreedMarketingAd('Y');

        return member;
    }
}
