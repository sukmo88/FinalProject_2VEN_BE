package com.sysmatic2.finalbe.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationMail(String toEmail, String verificationCode) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("[시스메틱] 인증 번호");
        mailMessage.setText("인증을 완료하려면 인증번호를 입력하세요! \n\n인증코드: " + verificationCode);

        // 이메일 전송 -> 실패하면 MailException 예외 발생
        mailSender.send(mailMessage);
    }

}
