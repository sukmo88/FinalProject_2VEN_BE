package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.common.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationMail(String toEmail, String verificationCode) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Load the HTML template and replace the verification code dynamically
        String htmlContent = EmailTemplate.getVerificationHtmlTemplate(verificationCode);

        helper.setTo(toEmail);
        helper.setSubject("[시스메틱] 인증 번호");
        helper.setText(htmlContent, true); // Enable HTML

        // Send the email
        mailSender.send(message);
    }
}
