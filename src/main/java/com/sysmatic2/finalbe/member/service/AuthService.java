package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    // 인증번호 검증 메소드
    public void validateVerificationCode(String inputVerificationCode, String verificationCode, LocalDateTime expiryTime) {

        // 인증 시간 만료 여부 체크 -> 인증시간 만료 시 EmailVerificationFailedException 예외 발생
        // 인증코드가 동일한지 체크 -> 인증코드 틀리면 EmailVerificationFailedException 예외 발생
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiryTime)) {
            throw new EmailVerificationFailedException("인증시간이 만료되었습니다");
        }

        if (!verificationCode.equals(inputVerificationCode)) {
            throw new EmailVerificationFailedException("인증번호가 일치하지 않습니다.");
        }
    }

}
