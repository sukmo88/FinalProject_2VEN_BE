//package com.sysmatic2.finalbe.cs.service;
//
//import com.mysql.cj.protocol.x.Notice;
//import com.sysmatic2.finalbe.cs.dto.NoticeCreateDto;
//import com.sysmatic2.finalbe.cs.dto.NoticeResponseDto;
//import com.sysmatic2.finalbe.cs.dto.NoticeUpdateDto;
//import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
//import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
//import com.sysmatic2.finalbe.cs.mapper.NoticeMapper;
//import com.sysmatic2.finalbe.cs.repository.NoticeRepository;
//import com.sysmatic2.finalbe.cs.specification.NoticeSpecification;
//import com.sysmatic2.finalbe.member.entity.MemberEntity;
//import com.sysmatic2.finalbe.member.repository.MemberRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.data.domain.*;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import org.springframework.data.jpa.domain.Specification;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//public class NoticeServiceTest {
//
//  @InjectMocks
//  private NoticeService noticeService;
//
//  @Mock
//  private NoticeRepository noticeRepository;
//
//  @Mock
//  private NoticeMapper noticeMapper;
//
//  @Mock
//  private MemberRepository memberRepository;
//
//  private MemberEntity writer;
//  private NoticeCreateDto createDto;
//  private NoticeEntity noticeEntity;
//  private NoticeResponseDto responseDto;
//  private NoticeUpdateDto updateDto;
//
//  @BeforeEach
//  public void setUp() {
//    MockitoAnnotations.openMocks(this);
//
//    writer = MemberEntity.builder()
//            .memberId("admin1")
//            .nickname("Admin One")
//            .email("admin1@example.com")
//            .build();
//
//    createDto = NoticeCreateDto.builder()
//            .title("Test Notice")
//            .content("This is a test notice.")
//            .build();
//
//    noticeEntity = NoticeEntity.builder()
//            .id(1L)
//            .title("Test Notice")
//            .content("This is a test notice.")
//            .noticeStatus(NoticeStatus.PUBLISHED)
//            .postedAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .writer(writer)
//            .build();
//
//    responseDto = NoticeResponseDto.builder()
//            .id(1L)
//            .title("Test Notice")
//            .content("This is a test notice.")
//            .noticeStatus(NoticeStatus.PUBLISHED)
//            .postedAt(noticeEntity.getPostedAt())
//            .updatedAt(noticeEntity.getUpdatedAt())
//            .writerId("admin1")
//            .build();
//
//    updateDto = NoticeUpdateDto.builder()
//            .title("Updated Test Notice")
//            .content("This is an updated test notice.")
//            .noticeStatus(NoticeStatus.DRAFT)
//            .build();
//  }
//
//  @Test
//  @DisplayName("공지사항 생성 성공")
//  public void testCreateNotice_Success() {
//    when(memberRepository.findById("admin1")).thenReturn(Optional.of(writer));
//    when(noticeMapper.toEntity(createDto, writer)).thenReturn(noticeEntity);
//    when(noticeRepository.save(noticeEntity)).thenReturn(noticeEntity);
//    when(noticeMapper.toResponseDto(noticeEntity)).thenReturn(responseDto);
//
//    Optional<NoticeResponseDto> result = noticeService.createNotice(createDto, "admin1");
//
//    assertThat(result).isPresent();
//    assertThat(result.get().getTitle()).isEqualTo("Test Notice");
//    assertThat(result.get().getNoticeStatus()).isEqualTo(NoticeStatus.PUBLISHED);
//
//    verify(memberRepository, times(1)).findById("admin1");
//    verify(noticeMapper, times(1)).toEntity(createDto, writer);
//    verify(noticeRepository, times(1)).save(noticeEntity);
//    verify(noticeMapper, times(1)).toResponseDto(noticeEntity);
//  }
//
//  @Test
//  @DisplayName("공지사항 생성 실패 - 작성자 없음")
//  public void testCreateNotice_Failure_NoWriter() {
//    when(memberRepository.findById("admin2")).thenReturn(Optional.empty());
//
//    Optional<NoticeResponseDto> result = noticeService.createNotice(createDto, "admin2");
//
//    assertThat(result).isNotPresent();
//
//    verify(memberRepository, times(1)).findById("admin2");
//    verify(noticeMapper, never()).toEntity(any(), any());
//    verify(noticeRepository, never()).save(any());
//    verify(noticeMapper, never()).toResponseDto(any());
//  }
//
//  @Test
//  @DisplayName("공지사항 조회 성공")
//  public void testGetNoticeById_Success() {
//    when(noticeRepository.findById(1L)).thenReturn(Optional.of(noticeEntity));
//    when(noticeMapper.toResponseDto(noticeEntity)).thenReturn(responseDto);
//
//    Optional<NoticeResponseDto> result = noticeService.getNoticeById(1L);
//
//    assertThat(result).isPresent();
//    assertThat(result.get().getId()).isEqualTo(1L);
//    assertThat(result.get().getTitle()).isEqualTo("Test Notice");
//
//    verify(noticeRepository, times(1)).findById(1L);
//    verify(noticeMapper, times(1)).toResponseDto(noticeEntity);
//  }
//
//  @Test
//  @DisplayName("공지사항 조회 실패 - 존재하지 않는 공지사항")
//  public void testGetNoticeById_Failure_NotFound() {
//    when(noticeRepository.findById(2L)).thenReturn(Optional.empty());
//
//    Optional<NoticeResponseDto> result = noticeService.getNoticeById(2L);
//
//    assertThat(result).isNotPresent();
//
//    verify(noticeRepository, times(1)).findById(2L);
//    verify(noticeMapper, never()).toResponseDto(any());
//  }
//
//  @Test
//  @DisplayName("공지사항 업데이트 성공")
//  public void testUpdateNotice_Success() {
//    NoticeEntity updatedEntity = NoticeEntity.builder()
//            .id(1L)
//            .title("Updated Test Notice")
//            .content("This is an updated test notice.")
//            .noticeStatus(NoticeStatus.DRAFT)
//            .postedAt(noticeEntity.getPostedAt())
//            .updatedAt(LocalDateTime.now())
//            .writer(writer)
//            .build();
//
//    NoticeResponseDto updatedResponseDto = NoticeResponseDto.builder()
//            .id(1L)
//            .title("Updated Test Notice")
//            .content("This is an updated test notice.")
//            .noticeStatus(NoticeStatus.DRAFT)
//            .postedAt(noticeEntity.getPostedAt())
//            .updatedAt(updatedEntity.getUpdatedAt())
//            .writerId("admin1")
//            .build();
//
//    when(noticeRepository.findById(1L)).thenReturn(Optional.of(noticeEntity));
//    doNothing().when(noticeMapper).updateEntity(updateDto, noticeEntity);
//    when(noticeRepository.save(noticeEntity)).thenReturn(updatedEntity);
//    when(noticeMapper.toResponseDto(updatedEntity)).thenReturn(updatedResponseDto);
//
//    Optional<NoticeResponseDto> result = noticeService.updateNotice(1L, updateDto);
//
//    assertThat(result).isPresent();
//    assertThat(result.get().getTitle()).isEqualTo("Updated Test Notice");
//    assertThat(result.get().getNoticeStatus()).isEqualTo(NoticeStatus.DRAFT);
//
//    verify(noticeRepository, times(1)).findById(1L);
//    verify(noticeMapper, times(1)).updateEntity(updateDto, noticeEntity);
//    verify(noticeRepository, times(1)).save(noticeEntity);
//    verify(noticeMapper, times(1)).toResponseDto(updatedEntity);
//  }
//
//  @Test
//  @DisplayName("공지사항 업데이트 실패 - 존재하지 않는 공지사항")
//  public void testUpdateNotice_Failure_NotFound() {
//    when(noticeRepository.findById(2L)).thenReturn(Optional.empty());
//
//    Optional<NoticeResponseDto> result = noticeService.updateNotice(2L, updateDto);
//
//    assertThat(result).isNotPresent();
//
//    verify(noticeRepository, times(1)).findById(2L);
//    verify(noticeMapper, never()).updateEntity(any(), any());
//    verify(noticeRepository, never()).save(any());
//    verify(noticeMapper, never()).toResponseDto(any());
//  }
//
//  @Test
//  @DisplayName("공지사항 삭제 성공")
//  public void testDeleteNotice_Success() {
//    when(noticeRepository.findById(1L)).thenReturn(Optional.of(noticeEntity));
//
//    boolean result = noticeService.deleteNotice(1L);
//
//    assertThat(result).isTrue();
//
//    verify(noticeRepository, times(1)).findById(1L);
//    verify(noticeRepository, times(1)).delete(noticeEntity);
//  }
//
//  @Test
//  @DisplayName("공지사항 삭제 실패 - 존재하지 않는 공지사항")
//  public void testDeleteNotice_Failure_NotFound() {
//    when(noticeRepository.findById(2L)).thenReturn(Optional.empty());
//
//    boolean result = noticeService.deleteNotice(2L);
//
//    assertThat(result).isFalse();
//
//    verify(noticeRepository, times(1)).findById(2L);
//    verify(noticeRepository, never()).delete(any(NoticeEntity.class));
//  }
//
//  @Test
//  @DisplayName("공지사항 목록 조회 성공")
//  public void testGetNotices_Success() {
//    String titleKeyword = "Test";
//    String contentKeyword = "notice";
//    String status = "PUBLISHED";
//    int page = 0;
//
//    NoticeEntity notice1 = noticeEntity;
//
//    NoticeResponseDto response1 = responseDto;
//
//    Page<NoticeEntity> entityPage = new PageImpl<>(List.of(notice1));
//    Page<NoticeResponseDto> dtoPage = new PageImpl<>(List.of(response1));
//
//    when(noticeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
//    when(noticeMapper.toResponseDto(notice1)).thenReturn(response1);
//
//    Page<NoticeResponseDto> result = noticeService.getNotices(titleKeyword, contentKeyword, status, page);
//
//    assertThat(result).isNotNull();
//    assertThat(result.getContent()).hasSize(1);
//    assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
//    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Notice");
//
//    verify(noticeRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
//    verify(noticeMapper, times(1)).toResponseDto(notice1);
//  }
//
//  @Test
//  @DisplayName("공지사항 목록 조회 실패 - 조회 결과 없음")
//  public void testGetNotices_Failure_NoResults() {
//    String titleKeyword = "Nonexistent";
//    String contentKeyword = "nothing";
//    String status = "DRAFT";
//    int page = 0;
//
//    Page<NoticeEntity> entityPage = new PageImpl<>(Collections.emptyList());
//
//    when(noticeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
//
//    Page<NoticeResponseDto> result = noticeService.getNotices(titleKeyword, contentKeyword, status, page);
//
//    assertThat(result).isNotNull();
//    assertThat(result.getContent()).isEmpty();
//
//    verify(noticeRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
//    verify(noticeMapper, never()).toResponseDto(any());
//  }
//}
