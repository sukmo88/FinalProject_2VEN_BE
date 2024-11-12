package com.sysmatic2.finalbe.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    //이메일로 인증번호 전송
    //관리자 인증번호 전송
    @PostMapping("/send-verification-code")
    public String sendVerificationCode() {
        return "send-verification-code";
    }
    //인증번호 검증 api(관리자 포함)
    @PostMapping("/check-verification-code")
    public String checkVerificationCode() {
        return "check-verification-code";
    }

}
