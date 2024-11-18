package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.EmailNotificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MailServiceTest {

  private MailService mailService;
  private JavaMailSender mailSender;

  @BeforeEach
  void setUp() {
    // Mock JavaMailSender
    mailSender = Mockito.mock(JavaMailSender.class);
    mailService = new MailService(mailSender);
  }

  @Test
  void testSendEmail() {
    // Arrange: Prepare test data
    EmailNotificationDto emailDto = new EmailNotificationDto();
    emailDto.setRecipientEmail("test@example.com");
    emailDto.setSubject("Test Subject");
    emailDto.setBody("Test Body");

    // Act: Call the method
    mailService.sendEmail(emailDto);

    // Assert: Capture the argument passed to JavaMailSender
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender, times(1)).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertThat(capturedMessage.getTo()).containsExactly(emailDto.getRecipientEmail());
    assertThat(capturedMessage.getSubject()).isEqualTo(emailDto.getSubject());
    assertThat(capturedMessage.getText()).isEqualTo(emailDto.getBody());
  }
}
