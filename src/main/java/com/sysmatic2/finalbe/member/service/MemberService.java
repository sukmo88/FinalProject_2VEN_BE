package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.ConfirmPasswordMismatchException;
import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    //로그인 서비스
    public ResponseEntity<Map<String,Object>> login(String email, String password) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElse(null);
        Map<String,Object> response = new HashMap<>();
        if(member == null) {
            //이메일로 사용자를 찾지 못했을 경우(404)
            response.put("status","error");
            response.put("message","해당 계정이 존재하지 않습니다.");
            response.put("errorCode","MEMBER_NOT_FOUND");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if(member.getIsLoginLocked()==('Y')){
            // 계정이 잠금 상태일 경우 (401)
            response.put("status", "error");
            response.put("message", "5회이상 로그인 실패로 잠금처리되었습니다. 이메일인증을 통해 비밀번호를 재설정하세요.");
            response.put("errorCode", "LOGIN_ATTEMPTS_EXCEEDED");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        if (!member.getPassword().equals(password)) {
            // 비밀번호가 일치하지 않는 경우 (401 로그인 실패)
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            response.put("errorCode", "INVALID_PASSWORD");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 로그인 성공 (200)
        response.put("status", "success");
        response.put("message", "로그인에 성공했습니다.");
        Map<String, Object> data = new HashMap<>();
        data.put("email", member.getEmail());
        data.put("nickname", member.getNickname());
        data.put("role", member.getMemberGradeCode());
        //jwt 값을 전달해줘야지 정상적으로 로그인 했으면
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
