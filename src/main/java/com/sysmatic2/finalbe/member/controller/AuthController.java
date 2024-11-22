package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import com.sysmatic2.finalbe.member.dto.EmailVerificationDTO;
import com.sysmatic2.finalbe.member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    //이메일로 인증번호 전송
    //관리자 인증번호 전송
    @PostMapping("/send-verification-code")
    public String sendVerificationCode() {
        return "send-verification-code";
    }

    //인증번호 검증 api(관리자 포함)
    @PostMapping("/check-verification-code")
    public ResponseEntity<Map<String, String>> checkVerificationCode(
            @Valid @RequestBody
            EmailVerificationDTO emailVerificationDTO, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        System.out.println("session=" + session);

        // 세션, 만료시간, 인증코드 값이 null이면 이메일 인증 불가 (잘못된 접근이라고 예외를 세분화해야 하나?)
        if (session == null || session.getAttribute("expiryTime") == null || session.getAttribute("verificationCode") == null) {
            throw new EmailVerificationFailedException("이메일 인증에 실패하였습니다.");
        }

        String savedVerificationCode = (String) session.getAttribute("verificationCode");
        LocalDateTime expiryTime = (LocalDateTime) session.getAttribute("expiryTime");

        // 인증번호 검증 메소드 호출
        authService.validateVerificationCode(emailVerificationDTO.getVerificationCode(), savedVerificationCode, expiryTime);

        // 세션에 인증 성공 정보 추가
        session.setAttribute("verified", true);

        // 응답코드 200번, 성공 메시지 전송
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "이메일 인증에 성공하였습니다."
        ));
    }
}
