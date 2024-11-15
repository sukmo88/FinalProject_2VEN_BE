package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.service.NoticeService;
import com.sysmatic2.finalbe.cs.dto.NoticeDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

  @Autowired
  private NoticeService noticeService;

  // 페이징된 공지사항 목록 조회
  @GetMapping
  public ResponseEntity<?> listNotices(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
    try {
      Page<NoticeDto> notices = noticeService.getAllNotices(page, size);
      return ResponseEntity.ok(notices);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to list notices", "LIST_NOTICES_ERROR"));
    }
  }

  // 공지사항 상세 조회
  @GetMapping("/{id}")
  public ResponseEntity<?> viewNotice(@PathVariable Long id) {
    try {
      NoticeDto notice = noticeService.getNoticeById(id)
              .orElseThrow(() -> new RuntimeException("Notice not found"));
      return ResponseEntity.ok(notice);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage(), "NOTICE_NOT_FOUND"));
    }
  }

  // 공지사항 작성 (관리자만 접근 가능)
  @PostMapping
  public ResponseEntity<?> createNotice(@RequestBody NoticeDto noticeDTO) {
    try {
      NoticeDto savedNotice = noticeService.saveNotice(noticeDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedNotice);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage(), "INVALID_INPUT"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to create notice", "CREATE_NOTICE_ERROR"));
    }
  }

  // 공지사항 수정 (관리자만 접근 가능)
  @PutMapping("/{id}")
  public ResponseEntity<?> editNotice(@PathVariable Long id, @RequestBody NoticeDto noticeDTO) {
    try {
      noticeDTO.setId(id);
      NoticeDto updatedNotice = noticeService.saveNotice(noticeDTO);
      return ResponseEntity.ok(updatedNotice);
    } catch (IllegalArgumentException e) {
      // 먼저 IllegalArgumentException을 처리
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage(), "INVALID_INPUT"));
    } catch (RuntimeException e) {
      // 그 다음 RuntimeException 처리
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Notice not found", "NOTICE_NOT_FOUND"));
    } catch (Exception e) {
      // 기타 Exception 처리
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to edit notice", "EDIT_NOTICE_ERROR"));
    }
  }

  // 공지사항 삭제 (관리자만 접근 가능)
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
    try {
      noticeService.deleteNotice(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Notice not found", "NOTICE_NOT_FOUND"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to delete notice", "DELETE_NOTICE_ERROR"));
    }
  }

  // 공지사항 검색
  @GetMapping("/search")
  public ResponseEntity<?> searchNotices(@RequestParam String keyword,
                                         @RequestParam String type,
                                         @RequestParam int page,
                                         @RequestParam int size) {
    try {
      Page<NoticeDto> notices;
      switch (type) {
        case "title":
          notices = noticeService.searchNoticesByTitle(keyword, page, size);
          break;
        case "content":
          notices = noticeService.searchNoticesByContent(keyword, page, size);
          break;
        case "titleOrContent":
          notices = noticeService.searchNoticesByTitleOrContent(keyword, page, size);
          break;
        default:
          throw new IllegalArgumentException("Invalid search type");
      }
      return ResponseEntity.ok(notices);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage(), "INVALID_SEARCH_TYPE"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to search notices", "SEARCH_NOTICES_ERROR"));
    }
  }

  // 에러 응답 생성 메서드
  private Map<String, Object> createErrorResponse(String message, String errorCode) {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "error");
    response.put("message", message);
    response.put("errorCode", errorCode);
    return response;
  }
}
