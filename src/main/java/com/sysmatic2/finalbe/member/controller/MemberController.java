package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.service.EmailService;
import com.sysmatic2.finalbe.member.service.MemberService;
import com.sysmatic2.finalbe.util.RandomKeyGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    //로그인
    @PostMapping("/login")
    public String login(HttpServletRequest request) {
        return "login";
    }
    //로그아웃
    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();

        return "logout";
    }

    //이메일찾기
    @GetMapping("/check-phone")
    public String checkPhone(HttpServletRequest request) {
        return "check-phone";
    }

    //비밀번호 재설정
    @PostMapping("/password")
    public String password(HttpServletRequest request) {
        return "password";
    }

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupDTO signupDTO, HttpServletRequest req) {

        // session 에서 이메일 인증 성공 여부 확인

        // 인증 X -> 예외 발생
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("verified") == null || session.getAttribute("verified").equals(false)) {
            // 이메일 인증이 완료되지 않았다는 예외 발생 (세션 없는 경우와 세분화해야 하나?)
            throw new EmailVerificationFailedException("이메일 인증이 완료되지 않았습니다.");
        }

        // 인증 OK -> signupDTO 에 email 설정
        signupDTO.setEmail(session.getAttribute("email").toString());

        // 회원가입 로직 진행
        memberService.signup(signupDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "success",
                "message", "회원가입에 성공했습니다."
        ));
    }

    //닉네임 중복 체크
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, String>> checkNickname(
            @Pattern(
            regexp = "^[A-Za-z\\d가-힣]{2,10}$",
            message = "닉네임은 2~10자 이내의 문자(한글, 영어, 숫자)여야 합니다."
            )
            String nickname) {

        memberService.duplicateNicknameCheck(nickname);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "사용 가능한 닉네임입니다."
        ));
    }

    //이메일 체크(중복체크) & 이메일 인증 코드 전송
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, String>> checkEmail(
            @Email(message = "이메일 형식에 맞게 입력되어야 합니다.") String email, HttpServletRequest req) {

        // 1. 이메일 중복 검사
        memberService.duplicateEmailCheck(email);

        // 2. 이메일로 인증코드 전송
        // 2-1. 인증코드 생성
        String verificationCode = RandomKeyGenerator.generateVerificationCode(6);

        // 2-2. 인증 이메일 발송
        emailService.sendVerificationMail(email, verificationCode);

        // 2-3. 인증코드 저장 (Session -> Redis 저장하도록 수정 필요!!!)
        HttpSession session = req.getSession(true);
        // 이메일, 인증번호, 만료시간 설정
        session.setAttribute("email", email);
        session.setAttribute("verificationCode", verificationCode);
        session.setAttribute("expiryTime", LocalDateTime.now().plusMinutes(5));  // 인증시간 5분 설정
        session.setAttribute("verified", false);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "인증 번호가 이메일로 전송되었습니다."
        ));
    }

    //회원탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<Map<String, String>> delete() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "회원탈퇴에 성공하였습니다."
        ));
    }

    // 사이드바 프로필 조회
    @GetMapping("/{memberId}/sidebar-profile")
    public ResponseEntity<Map<String, String>> asdfasdfasdf(

    )

    //개인정보상세조회
    @GetMapping("/details")
    public String details(HttpServletRequest request) {
        return "details";
    }
    //회원정보수정
    @PutMapping("/details")
    public String updateDetails(HttpServletRequest request) {
        return "updateDetails";
    }
    //비밀번호 확인
    @PostMapping("/check-password")
    public String checkPassword(HttpServletRequest request) {
        return "check-password";
    }
    //비밀번호 변경
    @PatchMapping("/change-password")
    public String changePassword(HttpServletRequest request) {
        return "change-password";
    }
}
