package com.sysmatic2.finalbe.cs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.cs.dto.*;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import com.sysmatic2.finalbe.cs.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private NoticeService noticeService;

  @Autowired
  private ObjectMapper objectMapper;

  private NoticeDto noticeDto;

  @BeforeEach
  void setUp() {
    noticeDto = new NoticeDto();
    noticeDto.setId(1L);
    noticeDto.setTitle("Test Notice");
    noticeDto.setContent("This is a test notice.");
    noticeDto.setNoticeStatus(NoticeStatus.PUBLISHED);
    noticeDto.setPostedAt(LocalDateTime.now());
    noticeDto.setWriterId("writer-id");
  }

  @Test
  void createNotice_ShouldReturnCreatedNotice() throws Exception {
    CreateNoticeDto createDto = new CreateNoticeDto();
    createDto.setTitle("Test Notice");
    createDto.setContent("This is a test notice.");
    createDto.setWriterId("writer-id");
    createDto.setNoticeStatus(NoticeStatus.DRAFT);

    Mockito.when(noticeService.createNotice(any(CreateNoticeDto.class))).thenReturn(noticeDto);

    mockMvc.perform(post("/api/notices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("Test Notice")))
            .andExpect(jsonPath("$.writerId", is("writer-id")));
  }

  @Test
  void updateNotice_ShouldReturnUpdatedNotice() throws Exception {
    UpdateNoticeDto updateDto = new UpdateNoticeDto();
    updateDto.setTitle("Updated Notice");
    updateDto.setContent("Updated content.");
    updateDto.setWriterId("writer-id");
    updateDto.setNoticeStatus(NoticeStatus.PUBLISHED);

    Mockito.when(noticeService.updateNotice(any(UpdateNoticeDto.class))).thenReturn(noticeDto);

    mockMvc.perform(put("/api/notices/{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("Test Notice")));
  }

  @Test
  void deleteNotice_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/notices/{id}", 1))
            .andExpect(status().isOk());
    Mockito.verify(noticeService, Mockito.times(1)).deleteNotice(1L);
  }

  @Test
  void getNoticeById_ShouldReturnNotice() throws Exception {
    Mockito.when(noticeService.getNoticeById(1L)).thenReturn(noticeDto);

    mockMvc.perform(get("/api/notices/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("Test Notice")));
  }

  @Test
  void searchByTitle_ShouldReturnPageOfNotices() throws Exception {
    NoticeSummaryDto summaryDto = new NoticeSummaryDto();
    summaryDto.setId(1L);
    summaryDto.setTitle("Test Notice");
    summaryDto.setPostedAt(LocalDateTime.now());
    summaryDto.setViewCount(0L);

    Page<NoticeSummaryDto> page = new PageImpl<>(Collections.singletonList(summaryDto), PageRequest.of(0, 10), 1);

    Mockito.when(noticeService.searchByTitle(eq("Test"), any(PageRequest.class))).thenReturn(page);

    mockMvc.perform(get("/api/notices/search/title")
                    .param("keyword", "Test")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title", is("Test Notice")));
  }

  @Test
  void searchByDateRange_ShouldReturnPageOfNotices() throws Exception {
    NoticeSummaryDto summaryDto = new NoticeSummaryDto();
    summaryDto.setId(1L);
    summaryDto.setTitle("Test Notice");
    summaryDto.setPostedAt(LocalDateTime.now());
    summaryDto.setViewCount(0L);

    Page<NoticeSummaryDto> page = new PageImpl<>(Collections.singletonList(summaryDto), PageRequest.of(0, 10), 1);

    Mockito.when(noticeService.searchByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
            .thenReturn(page);

    mockMvc.perform(get("/api/notices/search/date-range")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-12-31T23:59:59")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title", is("Test Notice")));
  }
}
