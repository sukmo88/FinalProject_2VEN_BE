package com.sysmatic2.finalbe.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import com.sysmatic2.finalbe.member.dto.AdminSessionDTO;
import com.sysmatic2.finalbe.member.dto.EmailVerificationDTO;
import com.sysmatic2.finalbe.member.service.AuthService;
import com.sysmatic2.finalbe.member.service.EmailService;
import com.sysmatic2.finalbe.util.RandomKeyGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;
    private final EmailService emailService;

    //이메일로 인증번호 전송
    //관리자 인증번호 전송
    @PostMapping("/admin/send-verification-code")
    public ResponseEntity<Map<String, String>>  sendVerificationCode(HttpServletRequest req) {
        //이메일을 바디로 보내주는걸로 알고 있겠지?
        BufferedReader reader = null;
        String bodyEmail = null;
        try {
            // 요청 바디에서 JSON 읽기
            reader = req.getReader();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestBody = objectMapper.readValue(reader, Map.class);
            bodyEmail = (String) requestBody.get("email");
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse request body", e);
        }
        //관리자 인증번호 전송
        // 2-1. 인증코드 생성
        String verificationCode = RandomKeyGenerator.generateVerificationCode(6);

        // 2-2. 인증 이메일 발송
        emailService.sendVerificationMail(bodyEmail, verificationCode);
        // 2-3. 인증코드 저장 (Session -> Redis 저장하도록 수정 필요!!!)
        HttpSession session = req.getSession(true);
        // 이메일, 인증번호, 만료시간 설정
        session.setAttribute("email", bodyEmail);
        session.setAttribute("verificationCode", verificationCode);
        session.setAttribute("expiryTime", LocalDateTime.now().plusMinutes(5));  // 인증시간 5분 설정
        session.setAttribute("verified", false);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "인증 번호가 이메일로 전송되었습니다."
        ));
    }

    //인증번호 검증 api(관리자 포함)
    @PostMapping("/check-verification-code")
    public ResponseEntity<Map<String, String>> checkVerificationCode(
            @Valid @RequestBody
            EmailVerificationDTO emailVerificationDTO, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
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

    //관리자 인증번호 검증
    @PostMapping("/admin/check-verification-code")
    public ResponseEntity<Map<String, Object>> adminCheckVerificationCode(
            @Valid @RequestBody
            EmailVerificationDTO emailVerificationDTO, HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        AdminSessionDTO adminSessionDTO = (AdminSessionDTO) session.getAttribute("admin_info");
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
        // 인증 완료 처리

        adminSessionDTO = new AdminSessionDTO();
        adminSessionDTO.setAuthorized(true);
        adminSessionDTO.setAuthorizationStatus("AUTHORIZED");
        adminSessionDTO.setAuthorizedAt(LocalDateTime.now().toString());
        adminSessionDTO.setExpiresAt(LocalDateTime.now().plusMinutes(30).toString());

        session.setAttribute("admin_info", adminSessionDTO);
        // 응답코드 200번, 성공 메시지 전송
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "이메일 인증에 성공하였습니다.",
                "admin_info", adminSessionDTO
        ));
    }

    // 인증 상태 확인
    @GetMapping("/admin/status")
    public AdminSessionDTO  adminStatus(HttpSession session) {
        AdminSessionDTO adminSessionDTO = (AdminSessionDTO) session.getAttribute("admin_info");
        if (adminSessionDTO == null) {
            throw new IllegalStateException("Admin session not found");
        }
        if (adminSessionDTO.getAuthorizationStatus().equals("AUTHORIZED")) {
            String expiresAt = (String) adminSessionDTO.getExpiresAt();
            LocalDateTime expirationTime = LocalDateTime.parse(expiresAt);

            if (LocalDateTime.now().isAfter(expirationTime)) {
                adminSessionDTO.setAuthorizationStatus("EXPIRED");
                adminSessionDTO.setAuthorized(false);
                session.setAttribute("admin_info", adminSessionDTO);
            }
        }
        return adminSessionDTO;
    }

    //관리자 로그아웃
    @PostMapping("/admin/logout")
    public ResponseEntity<Map<String, String>> adminLogout(HttpSession session){
        session.invalidate();
        // 응답코드 200번, 성공 메시지 전송
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "관리자 로그아웃에 성공하였습니다."
        ));
    }
}
