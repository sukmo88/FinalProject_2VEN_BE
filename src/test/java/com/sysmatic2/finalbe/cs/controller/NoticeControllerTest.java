package com.sysmatic2.finalbe.cs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.config.TestSecurityConfig;
import com.sysmatic2.finalbe.cs.dto.NoticeDto;
import com.sysmatic2.finalbe.cs.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
@Import(TestSecurityConfig.class)
class NoticeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private NoticeService noticeService;

  @Autowired
  private ObjectMapper objectMapper;

  private NoticeDto testNotice;

  @BeforeEach
  void setUp() {
    testNotice = new NoticeDto();
    testNotice.setId(1L);
    testNotice.setTitle("Test Title");
    testNotice.setContent("Test Content");
  }

  @Test
  void listNotices() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    when(noticeService.getAllNotices(any(Integer.class), any(Integer.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testNotice), pageable, 1));

    mockMvc.perform(get("/api/notices?page=1&size=20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Test Title"));
  }

  @Test
  void viewNotice() throws Exception {
    when(noticeService.getNoticeById(anyLong())).thenReturn(Optional.of(testNotice));

    mockMvc.perform(get("/api/notices/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Title"));
  }

  @Test
  void createNotice() throws Exception {
    when(noticeService.saveNotice(any(NoticeDto.class))).thenReturn(testNotice);

    mockMvc.perform(post("/api/notices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testNotice)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test Title"));
  }

  @Test
  void editNotice() throws Exception {
    when(noticeService.saveNotice(any(NoticeDto.class))).thenReturn(testNotice);

    mockMvc.perform(put("/api/notices/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testNotice)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Title"));
  }

  @Test
  void deleteNotice() throws Exception {
    mockMvc.perform(delete("/api/notices/1"))
            .andExpect(status().isNoContent());
  }

  @Test
  void searchNotices() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    when(noticeService.searchNoticesByTitle(any(String.class), any(Integer.class), any(Integer.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testNotice), pageable, 1));

    mockMvc.perform(get("/api/notices/search?keyword=Test&type=title&page=1&size=20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Test Title"));
  }
}
