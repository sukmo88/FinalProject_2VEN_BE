//package com.sysmatic2.finalbe.cs.service;
//
//import com.sysmatic2.finalbe.cs.dto.ConsultationDto;
//import com.sysmatic2.finalbe.cs.dto.NotificationDto;
//import com.sysmatic2.finalbe.cs.dto.SendMessageDto;
//import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;
//import com.sysmatic2.finalbe.cs.entity.ConsultationThreadEntity;
//import com.sysmatic2.finalbe.cs.entity.MessageRecipientEntity;
//import com.sysmatic2.finalbe.cs.repository.ConsultationMessageRepository;
//import com.sysmatic2.finalbe.cs.repository.ConsultationThreadRepository;
//import com.sysmatic2.finalbe.cs.repository.MessageRecipientRepository;
//import com.sysmatic2.finalbe.cs.dto.EmailNotificationDto;
//import com.sysmatic2.finalbe.member.entity.MemberEntity;
//import com.sysmatic2.finalbe.member.repository.MemberRepository;
//import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
//import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class ConsultationServiceTest {
//
//  @Mock
//  private ConsultationThreadRepository threadRepository;
//
//  @Mock
//  private ConsultationMessageRepository messageRepository;
//
//  @Mock
//  private MessageRecipientRepository messageRecipientRepository;
//
//  @Mock
//  private MemberRepository memberRepository;
//
//  @Mock
//  private StrategyRepository strategyRepository;
//
//  @Mock
//  private SimpMessagingTemplate messagingTemplate;
//
//  @Mock
//  private MailService mailService; // Add mock for MailService
//
//  @InjectMocks
//  private ConsultationService consultationService;
//
//  private MemberEntity investor;
//  private MemberEntity trader;
//  private StrategyEntity strategy;
//  private ConsultationThreadEntity thread;
//  private ConsultationMessageEntity message;
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//
//    // Mock Investor
//    investor = new MemberEntity();
//    investor.setMemberId("investor-001");
//    investor.setNickname("Investor");
//
//    // Mock Trader
//    trader = new MemberEntity();
//    trader.setMemberId("trader-001");
//    trader.setNickname("Trader");
//
//    // Mock Strategy
//    strategy = new StrategyEntity();
//    strategy.setStrategyId(1L);
//    strategy.setStrategyTitle("Sample Strategy");
//
//    // Mock Consultation Thread
//    thread = new ConsultationThreadEntity();
//    thread.setId(1L);
//    thread.setInvestor(investor);
//    thread.setTrader(trader);
//    thread.setStrategy(strategy);
//    thread.setConsultationTitle("Sample Consultation");
//
//    // Mock Message
//    message = new ConsultationMessageEntity();
//    message.setId(1L);
//    message.setThread(thread);
//    message.setSender(investor);
//    message.setContent("Sample Message");
//    message.setSentAt(LocalDateTime.now());
//  }
//
//  @Test
//  void testCreateConsultation() {
//    // Mock repository responses
//    when(memberRepository.findById("investor-001")).thenReturn(Optional.of(investor));
//    when(memberRepository.findById("trader-001")).thenReturn(Optional.of(trader));
//    when(strategyRepository.findById(1L)).thenReturn(Optional.of(strategy));
//    when(threadRepository.findByInvestor_MemberIdAndTrader_MemberIdAndStrategy_StrategyId(anyString(), anyString(), anyLong()))
//            .thenReturn(Optional.empty());
//    when(threadRepository.save(any(ConsultationThreadEntity.class))).thenReturn(thread);
//    when(messageRepository.save(any(ConsultationMessageEntity.class))).thenReturn(message);
//
//    doNothing().when(mailService).sendEmail(any(EmailNotificationDto.class)); // Mock MailService
//
//    // Call service method
//    ConsultationDto result = consultationService.createConsultation(
//            "investor-001", "trader-001", 1L, "Sample Consultation", "Sample Content");
//
//    // Verify repository interactions
//    verify(threadRepository, times(1)).save(any(ConsultationThreadEntity.class));
//    verify(messageRepository, times(1)).save(any(ConsultationMessageEntity.class));
//    verify(messageRecipientRepository, times(1)).save(any(MessageRecipientEntity.class));
//    verify(mailService, times(1)).sendEmail(any(EmailNotificationDto.class)); // Verify MailService
//
//    // Verify result
//    assertEquals("Sample Consultation", result.getConsultationTitle());
//  }
//
//  @Test
//  void testSendMessage() {
//    // Mock repository responses
//    when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
//    when(memberRepository.findById("investor-001")).thenReturn(Optional.of(investor));
//    when(memberRepository.findById("trader-001")).thenReturn(Optional.of(trader)); // 추가: 수신자 설정
//    when(messageRepository.save(any(ConsultationMessageEntity.class))).thenReturn(message);
//
//    // Prepare DTO
//    SendMessageDto dto = new SendMessageDto();
//    dto.setThreadId(1L);
//    dto.setContent("Sample Message");
//
//    // Call service method
//    consultationService.sendMessage("investor-001", dto);
//
//    // Verify repository interactions
//    verify(messageRepository, times(1)).save(any(ConsultationMessageEntity.class));
//    verify(messageRecipientRepository, times(1)).save(any(MessageRecipientEntity.class));
//    verify(messagingTemplate, times(1))
//            .convertAndSend(eq("/topic/notifications/trader-001"), (Object) any(NotificationDto.class));
//  }
//}
