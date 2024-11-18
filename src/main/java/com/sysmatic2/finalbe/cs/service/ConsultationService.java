package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.ConsultationDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationMessageDto;
import com.sysmatic2.finalbe.cs.dto.ConsultationSummaryDto;
import com.sysmatic2.finalbe.cs.dto.SendMessageDto;
import com.sysmatic2.finalbe.cs.dto.NotificationDto;
import com.sysmatic2.finalbe.cs.dto.EmailNotificationDto;
import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;
import com.sysmatic2.finalbe.cs.entity.ConsultationThreadEntity;
import com.sysmatic2.finalbe.cs.entity.MessageRecipientEntity;
import com.sysmatic2.finalbe.cs.entity.MessageRecipientId;
import com.sysmatic2.finalbe.cs.repository.ConsultationMessageRepository;
import com.sysmatic2.finalbe.cs.repository.ConsultationThreadRepository;
import com.sysmatic2.finalbe.cs.repository.MessageRecipientRepository;
import com.sysmatic2.finalbe.cs.util.ConsultationMapper;
import com.sysmatic2.finalbe.cs.util.ConsultationMessageMapper;
import com.sysmatic2.finalbe.cs.util.EmailNotificationMapper;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultationService {

  private final ConsultationThreadRepository threadRepository;
  private final ConsultationMessageRepository messageRepository;
  private final MessageRecipientRepository messageRecipientRepository;
  private final MemberRepository memberRepository;
  private final StrategyRepository strategyRepository;
  private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송을 위한 템플릿
  private final MailService mailService; // 이메일 전송을 위한 서비스

  @Autowired
  public ConsultationService(ConsultationThreadRepository threadRepository,
                             ConsultationMessageRepository messageRepository,
                             MessageRecipientRepository messageRecipientRepository,
                             MemberRepository memberRepository,
                             StrategyRepository strategyRepository,
                             SimpMessagingTemplate messagingTemplate,
                             MailService mailService) {
    this.threadRepository = threadRepository;
    this.messageRepository = messageRepository;
    this.messageRecipientRepository = messageRecipientRepository;
    this.memberRepository = memberRepository;
    this.strategyRepository = strategyRepository;
    this.messagingTemplate = messagingTemplate;
    this.mailService = mailService;
  }

  /**
   * 상담 생성
   *
   * @param investorId        투자자 ID
   * @param traderId          트레이더 ID
   * @param strategyId        전략 ID
   * @param consultationTitle 상담 제목
   * @param content           상담 내용
   * @return ConsultationDto
   */
  @Transactional
  public ConsultationDto createConsultation(String investorId, String traderId, Long strategyId, String consultationTitle, String content) {
    MemberEntity investor = memberRepository.findById(investorId)
            .orElseThrow(() -> new IllegalArgumentException("투자자를 찾을 수 없습니다."));
    MemberEntity trader = memberRepository.findById(traderId)
            .orElseThrow(() -> new IllegalArgumentException("트레이더를 찾을 수 없습니다."));
    StrategyEntity strategy = strategyRepository.findById(strategyId)
            .orElseThrow(() -> new IllegalArgumentException("전략을 찾을 수 없습니다."));

    // 기존 상담 스레드가 있는지 확인 (투자자, 트레이더, 전략 기준)
    ConsultationThreadEntity thread = threadRepository.findByInvestor_MemberIdAndTrader_MemberIdAndStrategy_StrategyId(
            investorId, traderId, strategyId).orElse(null);

    if (thread == null) {
      // 새로운 상담 스레드 생성
      thread = ConsultationThreadEntity.builder()
              .investor(investor)
              .trader(trader)
              .strategy(strategy)
              .consultationTitle(consultationTitle) // 상담 제목 설정
              .createdAt(LocalDateTime.now())
              .build();
      thread = threadRepository.save(thread);
    } else {
      // 기존 상담 스레드에 새로운 상담 제목을 설정하거나 다른 로직 적용 가능
      // 여기서는 기존 스레드에 제목을 변경하지 않고 그대로 유지합니다.
    }

    // 초기 상담 메시지 생성
    ConsultationMessageEntity initialMessage = ConsultationMessageEntity.builder()
            .thread(thread)
            .sender(investor)
            .content(content)
            .sentAt(LocalDateTime.now())
            .isRead(false)
            .build();

    ConsultationMessageEntity savedMessage = messageRepository.save(initialMessage);

    // 수신자 (트레이더)에 대한 MessageRecipient 생성
    MessageRecipientEntity recipient = MessageRecipientEntity.builder()
            .id(new MessageRecipientId(savedMessage.getId(), traderId))
            .message(savedMessage)
            .recipient(trader)
            .isRead(false)
            .isDeleted(false)
            .build();
    messageRecipientRepository.save(recipient);

    // 실시간 알림 전송 (트레이더에게 메시지가 도착했음을 알림)
    NotificationDto notification = new NotificationDto();
    notification.setType("message_received");
    notification.setMessageId(savedMessage.getId());
    notification.setSenderNickname(investor.getNickname());
    notification.setContent(content);
    notification.setSentAt(savedMessage.getSentAt());

    messagingTemplate.convertAndSend("/topic/notifications/" + traderId, notification);

    // 이메일 알림 전송 (트레이더에게 이메일을 통해 메시지가 도착했음을 알림)
    EmailNotificationDto emailNotification = EmailNotificationMapper.toEmailDto(savedMessage, trader);
    mailService.sendEmail(emailNotification);

    // 상담 DTO 반환
    return ConsultationMapper.toDto(thread);
  }

  /**
   * 메시지 전송
   *
   * @param senderId 송신자 ID
   * @param dto      SendMessageDto
   * @return ConsultationMessageDto
   */
  @Transactional
  public ConsultationMessageDto sendMessage(String senderId, SendMessageDto dto) {
    ConsultationThreadEntity thread = threadRepository.findById(dto.getThreadId())
            .orElseThrow(() -> new IllegalArgumentException("상담 스레드를 찾을 수 없습니다."));
    MemberEntity sender = memberRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));

    // 발신자가 상담 스레드의 참여자인지 확인
    boolean isParticipant = thread.getInvestor().getMemberId().equals(senderId) ||
            thread.getTrader().getMemberId().equals(senderId);
    if (!isParticipant) {
      throw new IllegalArgumentException("상담 스레드의 참여자가 아닙니다.");
    }

    ConsultationMessageEntity message = ConsultationMessageEntity.builder()
            .thread(thread)
            .sender(sender)
            .content(dto.getContent())
            .sentAt(LocalDateTime.now())
            .isRead(false)
            .build();

    ConsultationMessageEntity savedMessage = messageRepository.save(message);

    // 수신자 (상대방 사용자)에 대한 MessageRecipient 생성
    String recipientId = thread.getInvestor().getMemberId().equals(senderId) ?
            thread.getTrader().getMemberId() :
            thread.getInvestor().getMemberId();

    MemberEntity recipient = memberRepository.findById(recipientId)
            .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

    MessageRecipientEntity recipientEntity = MessageRecipientEntity.builder()
            .id(new MessageRecipientId(savedMessage.getId(), recipientId))
            .message(savedMessage)
            .recipient(recipient)
            .isRead(false)
            .isDeleted(false)
            .build();
    messageRecipientRepository.save(recipientEntity);

    // 실시간 알림 전송 (수신자에게 메시지가 도착했음을 알림)
    NotificationDto notification = new NotificationDto();
    notification.setType("message_received");
    notification.setMessageId(savedMessage.getId());
    notification.setSenderNickname(sender.getNickname());
    notification.setContent(dto.getContent());
    notification.setSentAt(savedMessage.getSentAt());

    messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, notification);

    // 이메일 알림 전송 (수신자에게 이메일을 통해 메시지가 도착했음을 알림)
    EmailNotificationDto emailNotification = EmailNotificationMapper.toEmailDto(savedMessage, recipient);
    mailService.sendEmail(emailNotification);

    return ConsultationMessageMapper.toDto(savedMessage);
  }

  /**
   * 사용자별 메시지 목록 조회
   *
   * @param userId   사용자 ID
   * @param sent     보낸 메시지 여부 (true: 보낸 메시지, false: 받은 메시지)
   * @param pageable 페이징 및 정렬 정보
   * @return Page<ConsultationMessageDto>
   */
  public Page<ConsultationMessageDto> getUserMessages(String userId, boolean sent, Pageable pageable) {
    Page<ConsultationMessageEntity> messages = sent
            ? messageRepository.findSentMessagesByUserId(userId, pageable)
            : messageRepository.findReceivedMessagesByUserId(userId, pageable);
    return messages.map(ConsultationMessageMapper::toDto);
  }

  /**
   * 메시지 키워드 검색
   *
   * @param keyword  검색 키워드
   * @param pageable 페이징 및 정렬 정보
   * @return Page<ConsultationMessageDto>
   */
  public Page<ConsultationMessageDto> searchMessagesByKeyword(String keyword, Pageable pageable) {
    return messageRepository.searchMessagesByKeyword(keyword, pageable)
            .map(ConsultationMessageMapper::toDto);
  }

  /**
   * 메시지 날짜 범위 검색
   *
   * @param startDate 시작 날짜
   * @param endDate   종료 날짜
   * @param pageable  페이징 및 정렬 정보
   * @return Page<ConsultationMessageDto>
   */
  public Page<ConsultationMessageDto> searchMessagesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return messageRepository.searchMessagesByDateRange(startDate, endDate, pageable)
            .map(ConsultationMessageMapper::toDto);
  }

  /**
   * 메시지 읽음 상태 업데이트
   *
   * @param messageId 메시지 ID
   * @param userId    사용자 ID
   */
  @Transactional
  public void markMessageAsRead(Long messageId, String userId) {
    ConsultationMessageEntity message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

    // 메시지를 받은 사용자인지 확인
    boolean isReceiver = (message.getThread().getInvestor().getMemberId().equals(userId) ||
            message.getThread().getTrader().getMemberId().equals(userId)) &&
            !message.getSender().getMemberId().equals(userId);

    if (!isReceiver) {
      throw new IllegalArgumentException("메시지를 읽을 권한이 없습니다.");
    }

    // MessageRecipient 업데이트
    MessageRecipientEntity recipient = messageRecipientRepository.findById(new MessageRecipientId(messageId, userId))
            .orElseThrow(() -> new IllegalArgumentException("메시지 수신 정보를 찾을 수 없습니다."));

    if (!recipient.getIsRead()) {
      recipient.setIsRead(true);
      recipient.setReadAt(LocalDateTime.now());
      messageRecipientRepository.save(recipient);
    }
  }

  /**
   * 사용자별 상담 요약 목록 조회
   *
   * @param userId 사용자 ID
   * @return List<ConsultationSummaryDto>
   */
  public List<ConsultationSummaryDto> getUserConsultations(String userId) {
    List<ConsultationThreadEntity> threads = threadRepository.findByInvestor_MemberIdOrTrader_MemberId(userId);
    return threads.stream()
            .map(ConsultationMapper::toSummaryDto)
            .collect(Collectors.toList());
  }

  // TODO: 인증(Authentication) 및 권한 부여(Authorization) 로직 추가
}
