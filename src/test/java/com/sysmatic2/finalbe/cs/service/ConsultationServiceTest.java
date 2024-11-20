package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
import com.sysmatic2.finalbe.cs.mapper.ConsultationMapper;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ConsultationServiceTest {

  private ConsultationRepository consultationRepository;
  private MemberRepository memberRepository;
  private ConsultationMapper consultationMapper;
  private ConsultationService consultationService;

  @BeforeEach
  void setUp() {
    consultationRepository = mock(ConsultationRepository.class);
    memberRepository = mock(MemberRepository.class);
    consultationMapper = mock(ConsultationMapper.class);
    consultationService = new ConsultationService(consultationRepository, memberRepository, consultationMapper);
  }

  @Test
  void testCreateConsultation() {
    // Mock input DTO
    ConsultationCreateDto dto = ConsultationCreateDto.builder()
            .investorId("investor123")
            .traderId("trader123")
            .title("Test Title")
            .content("Test Content")
            .build();

    // Mock dependencies
    MemberEntity investor = createMockMember("investor123", "InvestorNickname");
    MemberEntity trader = createMockMember("trader123", "TraderNickname");

    ConsultationEntity entity = createMockConsultationEntity(1L, investor, trader, "Test Title", "Test Content");

    ConsultationDetailResponseDto responseDto = ConsultationDetailResponseDto.builder()
            .id(1L)
            .title("Test Title")
            .content("Test Content")
            .build();

    // Mock behavior
    when(memberRepository.findById("investor123")).thenReturn(Optional.of(investor));
    when(memberRepository.findById("trader123")).thenReturn(Optional.of(trader));
    when(consultationMapper.toEntityFromCreateDto(eq(dto), eq(investor), eq(trader), isNull()))
            .thenReturn(entity); // Mocking StrategyEntity as null
    when(consultationRepository.save(any())).thenReturn(entity);
    when(consultationMapper.toDetailResponseDto(any())).thenReturn(responseDto);

    // Execute
    Optional<ConsultationDetailResponseDto> result = consultationService.createConsultation(dto);

    // Verify
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    assertEquals("Test Title", result.get().getTitle());
    verify(consultationRepository, times(1)).save(any());
    verify(consultationMapper, times(1)).toEntityFromCreateDto(eq(dto), eq(investor), eq(trader), isNull());
  }

  @Test
  void testGetConsultationById() {
    // Mock input entity
    MemberEntity investor = createMockMember("investor123", "InvestorNickname");
    MemberEntity trader = createMockMember("trader123", "TraderNickname");

    ConsultationEntity entity = createMockConsultationEntity(1L, investor, trader, "Test Title", "Test Content");

    ConsultationDetailResponseDto responseDto = ConsultationDetailResponseDto.builder()
            .id(1L)
            .title("Test Title")
            .content("Test Content")
            .build();

    // Mock behavior
    when(consultationRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(consultationMapper.toDetailResponseDto(entity)).thenReturn(responseDto);

    // Execute
    Optional<ConsultationDetailResponseDto> result = consultationService.getConsultationById(1L);

    // Verify
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    assertEquals("Test Title", result.get().getTitle());
    verify(consultationRepository, times(1)).findById(1L);
    verify(consultationMapper, times(1)).toDetailResponseDto(entity);
  }

  @Test
  void testDeleteConsultation() {
    // Mock behavior
    when(consultationRepository.existsById(1L)).thenReturn(true);

    // Execute
    boolean result = consultationService.deleteConsultation(1L);

    // Verify
    assertTrue(result);
    verify(consultationRepository, times(1)).deleteById(1L);
  }

  // Helper method to create a mock MemberEntity
  private MemberEntity createMockMember(String memberId, String nickname) {
    MemberEntity member = new MemberEntity();
    member.setMemberId(memberId);
    member.setNickname(nickname);
    member.setEmail(memberId + "@example.com");
    member.setPassword("encryptedPassword");
    member.setPhoneNumber("010-1234-5678");
    member.setMemberGradeCode("G001");
    member.setMemberStatusCode("ACTIVE");
    member.setLoginFailCount(0);
    member.setIsLoginLocked('N');
    return member;
  }

  // Helper method to create a mock ConsultationEntity
  private ConsultationEntity createMockConsultationEntity(Long id, MemberEntity investor, MemberEntity trader, String title, String content) {
    ConsultationEntity entity = new ConsultationEntity();
    entity.setId(id);
    entity.setInvestor(investor);
    entity.setTrader(trader);
    entity.setTitle(title);
    entity.setContent(content);
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    return entity;
  }
}
