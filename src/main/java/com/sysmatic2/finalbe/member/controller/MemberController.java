package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import com.sysmatic2.finalbe.member.dto.*;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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
    @PostMapping("/find-email")
    public ResponseEntity<Map<String, ?>> findEmail(@Valid @RequestBody PhoneNumberDTO phoneNumberDTO) {

        List<EmailResponseDTO> email = memberService.findEmail(phoneNumberDTO.getPhoneNumber());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "이메일 조회에 성공했습니다.",
                "data", email
        ));
    }

    // 비밀번호 변경을 위한 계정 확인 & 이메일 인증 코드 전송
    @PostMapping("/check-account")
    public ResponseEntity<Map<String, String>> checkAccount(@Valid @RequestBody EmailCheckDTO emailCheckDTO, HttpServletRequest req) {

        // 1. 가입된 이메일인지 확인
        String email = emailCheckDTO.getEmail();
        memberService.checkExistEmail(email);

        // 2. 이메일로 인증코드 전송
        HttpSession session = req.getSession(true);
        sendVerificationMail(email, session);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "인증 번호가 이메일로 전송되었습니다."
        ));
    }

    // 비밀번호 재설정
    @PatchMapping("/reset-password")
    public ResponseEntity<Map<String, String>> checkPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO, HttpServletRequest req) {

        // 1. 세션에서 인증여부 확인하여 미인증 시 예외 발생
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("verified") == null || session.getAttribute("verified").equals(false)) {
            throw new EmailVerificationFailedException("이메일 인증이 완료되지 않았습니다.");
        }

        // 2. 인증되었으면 세션에서 email 획득해서 비밀번호 재설정하는 서비스 호출
        String email = session.getAttribute("email").toString();
        memberService.resetPassword(email, passwordResetDTO);

        // 3. 세션 만료 처리
        session.invalidate();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "비밀번호가 성공적으로 재설정되었습니다."
        ));
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

        // 세션 만료 처리
        session.invalidate();

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
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, String>> checkEmail(@Valid @RequestBody EmailCheckDTO emailCheckDTO, HttpServletRequest req) {

        // 1. 이메일 중복 검사
        String email = emailCheckDTO.getEmail();
        memberService.duplicateEmailCheck(email);

        // 2. 이메일로 인증코드 전송
        HttpSession session = req.getSession(true);
        sendVerificationMail(email, session);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "인증 번호가 이메일로 전송되었습니다."
        ));
    }

    //회원탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<Map<String, String>> withdrawal(@AuthenticationPrincipal CustomUserDetails userDetails) {

        String memberId = userDetails.getMemberId();
        memberService.withdrawal(memberId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "회원탈퇴에 성공하였습니다."
        ));
    }

    // 사이드바 프로필 조회
    @GetMapping("/{memberId}/sidebar-profile")
    public ResponseEntity<Map<String, ?>> getSidebarProfile(@PathVariable("memberId") String memberId) {

        SimpleProfileDTO simpleProfile = memberService.getSimpleProfile(memberId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "사이드바 프로필 조회에 성공하였습니다.",
                "data", simpleProfile
        ));
    }

    // 상세 개인정보 조회
    @GetMapping("/details")
    public ResponseEntity<Map<String, ?>> getDetails(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // 로그인 정보로 memberId 가져온 후 상세 개인정보 조회
        String memberId = userDetails.getMemberId();
        DetailedProfileDTO detailedProfile = memberService.getDetailedProfile(memberId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "상세 개인정보 조회에 성공하였습니다.",
                "data", detailedProfile
        ));
    }

    // 상세 개인정보 수정
    @PutMapping("/details")
    public ResponseEntity<Map<String, String>> modifyDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @RequestBody @Valid ProfileUpdateDTO profileUpdateDTO) {

        // 로그인 정보로 memberId 가져온 후 상세 개인정보 조회
        String memberId = userDetails.getMemberId();
        memberService.modifyDetails(memberId, profileUpdateDTO);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "회원정보가 성공적으로 수정되었습니다."
        ));
    }

    // 비밀번호 수정
    @PatchMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody @Valid PasswordUpdateDTO passwordUpdateDTO) {

        // 로그인 정보로 memberId 가져온 후 상세 개인정보 조회
        String memberId = userDetails.getMemberId();
        memberService.changePassword(memberId, passwordUpdateDTO);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "비밀번호가 성공적으로 변경되었습니다."
        ));
    }

    // 인증번호를 메일로 보내고 세션 저장하는 메소드
    private void sendVerificationMail(String email, HttpSession session) {
        // 2-1. 인증코드 생성
        String verificationCode = RandomKeyGenerator.generateVerificationCode(6);

        // 2-2. 인증 이메일 발송
        emailService.sendVerificationMail(email, verificationCode);

        // 2-3. 세션에 이메일, 인증번호, 만료시간 저장
        session.setAttribute("email", email);
        session.setAttribute("verificationCode", verificationCode);
        session.setAttribute("expiryTime", LocalDateTime.now().plusMinutes(5));  // 인증시간 5분 설정
        session.setAttribute("verified", false);
    }
}
