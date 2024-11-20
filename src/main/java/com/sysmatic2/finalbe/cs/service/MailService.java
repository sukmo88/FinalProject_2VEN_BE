//package com.sysmatic2.finalbe.cs.service;
//
//import com.sysmatic2.finalbe.cs.dto.EmailNotificationDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//import org.springframework.scheduling.annotation.Async;
//
//@Service
//public class MailService {
//
//  private final JavaMailSender mailSender;
//
//  @Autowired
//  public MailService(JavaMailSender mailSender) {
//    this.mailSender = mailSender;
//  }
//
//  /**
//   * 이메일 전송
//   *
//   * @param emailDto 이메일 알림 DTO
//   */
//  @Async
//  public void sendEmail(EmailNotificationDto emailDto) {
//    SimpleMailMessage message = new SimpleMailMessage();
//    message.setTo(emailDto.getRecipientEmail());
//    message.setSubject(emailDto.getSubject());
//    message.setText(emailDto.getBody());
//    mailSender.send(message);
//  }
//}
