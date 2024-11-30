package com.sysmatic2.finalbe.member.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailService emailService;

    @Test
    public void verificationCodeSenderTest_success() {
        String toMail = "test@test.com";
        String verificationCode = "123123";

        // 지정 수신자로 인증코드 전송
        emailService.sendVerificationMail(toMail, verificationCode);

        // ArgumentCaptor를 사용해 메일 메시지 캡처
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture()); // send() 메서드가 호출되었는지 검증

        // 캡처된 메일 메시지 검증
        SimpleMailMessage capturedMessage = captor.getValue();
        assertEquals(toMail, capturedMessage.getTo()[0]); // 수신자 이메일 확인
        assertEquals("[시스메틱] 인증 번호", capturedMessage.getSubject()); // 제목 확인
        assertEquals(
                "인증을 완료하려면 5분 내에 인증번호를 입력하세요! \n\n인증코드: " + verificationCode,
                capturedMessage.getText()
        );
    }

    @Test
    // 유효하지 않은 이메일로 메일 발송 시, MailSendException 발생
    public void verificationCodeSenderTest_failure() {
        String toMail = "test";
        String verificationCode = "123123";

        doThrow(MailSendException.class).when(mailSender).send(any(SimpleMailMessage.class)); // 실제 mailSender 사용 시에 대한 테스트 필요? 통합테스트?

        assertThrows(MailSendException.class, () -> emailService.sendVerificationMail(toMail, verificationCode));
    }
}

/*
* 메시지 전송 실패 시 발생할 수 있는 예외
* 1. MailException : 모든 메일 관련 예외의 최상위 클래스
* 2. MailAuthenticationException : SMTP 인증 문제 (계정 주소 혹은 비밀번호 오류)
* 3. MailSendException : 메일 발송 실패와 관련된 일반적인 예외 (유효하지 않은 이메일, SMTP 서버 통신 불가 등)
* 4. MailParseException : 메일 메시지 구문 문제
* 5. MailPreparationException : 메일 준비 과정 문제
* 6. MailMessagingException : 메일 전송과 관련된 전반적인 메시지 관련 예외
* */