package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationMessageEntity;
import com.sysmatic2.finalbe.cs.entity.ConsultationThreadEntity;
import com.sysmatic2.finalbe.cs.repository.ConsultationMessageRepository;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ConsultationMessageRepositoryUnitTest {

  @Mock
  private ConsultationMessageRepository consultationMessageRepository;

  @InjectMocks
  private ConsultationMessageRepositoryUnitTest testSubject;

  private ConsultationThreadEntity thread;
  private MemberEntity sender;
  private ConsultationMessageEntity message1;
  private ConsultationMessageEntity message2;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    // Mock Thread
    thread = new ConsultationThreadEntity();
    thread.setId(1L);

    // Mock Sender
    sender = new MemberEntity();
    sender.setMemberId("user-001");

    // Mock Messages
    message1 = new ConsultationMessageEntity();
    message1.setId(1L);
    message1.setThread(thread);
    message1.setSender(sender);
    message1.setContent("Message content 1");
    message1.setSentAt(LocalDateTime.now().minusDays(1));

    message2 = new ConsultationMessageEntity();
    message2.setId(2L);
    message2.setThread(thread);
    message2.setSender(sender);
    message2.setContent("Message content 2");
    message2.setSentAt(LocalDateTime.now());
  }

  @Test
  public void testFindByThreadIdOrderBySentAtAsc() {
    // Mock repository response
    when(consultationMessageRepository.findByThread_IdOrderBySentAtAsc(anyLong()))
            .thenReturn(Arrays.asList(message1, message2));

    // Call repository method
    List<ConsultationMessageEntity> result = consultationMessageRepository.findByThread_IdOrderBySentAtAsc(1L);

    // Verify results
    assertEquals(2, result.size());
    assertEquals("Message content 1", result.get(0).getContent());
    assertEquals("Message content 2", result.get(1).getContent());
  }

  @Test
  public void testFindSentMessagesByUserId() {
    // Mock repository response
    PageRequest pageable = PageRequest.of(0, 10);
    when(consultationMessageRepository.findSentMessagesByUserId(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(message1, message2)));

    // Call repository method
    Page<ConsultationMessageEntity> result = consultationMessageRepository
            .findSentMessagesByUserId("user-001", pageable);

    // Verify results
    assertEquals(2, result.getContent().size());
    assertEquals("Message content 1", result.getContent().get(0).getContent());
  }

  @Test
  public void testFindReceivedMessagesByUserId() {
    // Mock repository response
    PageRequest pageable = PageRequest.of(0, 10);
    when(consultationMessageRepository.findReceivedMessagesByUserId(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(message1)));

    // Call repository method
    Page<ConsultationMessageEntity> result = consultationMessageRepository
            .findReceivedMessagesByUserId("user-001", pageable);

    // Verify results
    assertEquals(1, result.getContent().size());
    assertEquals("Message content 1", result.getContent().get(0).getContent());
  }

  @Test
  public void testSearchMessagesByKeyword() {
    // Mock repository response
    PageRequest pageable = PageRequest.of(0, 10);
    when(consultationMessageRepository.searchMessagesByKeyword(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(message2)));

    // Call repository method
    Page<ConsultationMessageEntity> result = consultationMessageRepository
            .searchMessagesByKeyword("content 2", pageable);

    // Verify results
    assertEquals(1, result.getContent().size());
    assertEquals("Message content 2", result.getContent().get(0).getContent());
  }

  @Test
  public void testSearchMessagesByDateRange() {
    // Mock repository response
    PageRequest pageable = PageRequest.of(0, 10);
    when(consultationMessageRepository.searchMessagesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(message1, message2)));

    // Call repository method
    Page<ConsultationMessageEntity> result = consultationMessageRepository
            .searchMessagesByDateRange(LocalDateTime.now().minusDays(2), LocalDateTime.now(), pageable);

    // Verify results
    assertEquals(2, result.getContent().size());
  }
}
