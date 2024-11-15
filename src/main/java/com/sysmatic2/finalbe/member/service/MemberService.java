package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

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

        MemberEntity member = new MemberEntity();
        member.setMemberId(createUUID());
        member.setMemberGradeCode("MEMBER_GRADE_" + signupDTO.getMemberType());
        member.setMemberStatusCode("MEMBER_STATUS_ACTIVE");
        member.setEmail(signupDTO.getEmail()); // Redis 에서 가져오는 걸로 변경 필요
        member.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        member.setNickname(signupDTO.getNickname());
        member.setPhoneNumber(signupDTO.getPhoneNumber());
        member.setIsAgreedMarketingAd('Y');
        // 가입일자는 auditing 사용?? -> 석모님 확인 필요

        return member;
    }

    // UUID 생성
    private String createUUID() {
        UUID uuid = UUID.randomUUID();

        // UUID를 바이트 배열로 변환
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (msb & 0xFF);
            msb >>= 8;
            bytes[8+i] = (byte) (lsb & 0xFF);
            lsb >>= 8;
        }

        // Base64로 인코딩하고 패딩 제거
        String base64UUID = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return base64UUID;
    }
}
