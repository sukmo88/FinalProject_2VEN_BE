package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    // 인증코드 일치하고 만료시간 전이면 인증 OK
    public void VerificationCodeCheckTest_success() {
        String inputVerificationCode = "123456";
        String savedVerificationCode = "123456";
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(3);

        assertDoesNotThrow(() -> authService.validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime));
    }

    @Test
    // 인증코드 만료시간 후면 EmailVerificationFailedException 발생
    public void VerificationCodeCheckTest_failure1() {
        String inputVerificationCode = "123456";
        String savedVerificationCode = "123456";
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(30);

        assertThrows(EmailVerificationFailedException.class,
                () -> authService.validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime));
    }

    @Test
    // 인증코드 일치하지 않으면 EmailVerificationFailedException 발생
    public void VerificationCodeCheckTest_failure2() {
        String inputVerifyCode = "123456";
        String savedVerificationCode = "123451";
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(30);

        assertThrows(EmailVerificationFailedException.class,
                () -> authService.validateVerificationCode(inputVerifyCode, savedVerificationCode, expiryTime));
    }
}