package com.sysmatic2.finalbe.cs.controller;

import com.sysmatic2.finalbe.cs.dto.AdminFAQDto;
import com.sysmatic2.finalbe.cs.service.FAQService;
import com.sysmatic2.finalbe.common.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/faqs")
@Tag(name = "FAQ Controller", description = "FAQ를 관리하는 컨트롤러")
public class FAQController {

    private final FAQService faqService;

    public FAQController(FAQService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public ResponseEntity<?> getAllFAQs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                          @RequestHeader(value = "Authorization", required = false) String role) {
        try {
            // role이 null인 경우 접근 제한
            if (role == null) {
                return ResponseUtils.buildErrorResponse(
                        "UNAUTHORIZED",
                        "SecurityException",
                        "User is not authenticated",
                        HttpStatus.UNAUTHORIZED
                );
            }

            // JSON 반환값 Map으로 받아오기
            Map<String, Object> response = faqService.getAllFAQs(page, pageSize, role);

            // JSON 형태로 반환. 상태값 200
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseUtils.buildErrorResponse(
                    "FORBIDDEN",
                    e.getClass().getSimpleName(),
                    "Access Denied: Security exception occurred",
                    HttpStatus.FORBIDDEN
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> createFAQ(@RequestBody AdminFAQDto faq,
                                       @RequestHeader(value = "Authorization", required = false) String role) {
        try {
            System.out.println("role : " + role);

            // role이 null이거나 ROLE_ADMIN이 아닌 경우 접근 제한
            if (role == null) {
                return ResponseUtils.buildErrorResponse(
                        "UNAUTHORIZED",
                        "SecurityException",
                        "User is not authenticated",
                        HttpStatus.UNAUTHORIZED
                );
            } else if (!role.equals("ROLE_ADMIN")) {
                return ResponseUtils.buildErrorResponse(
                        "FORBIDDEN",
                        "SecurityException",
                        "Access Denied: User does not have sufficient permissions",
                        HttpStatus.FORBIDDEN
                );
            }

            // faq 객체의 필수 필드가 유효하지 않은 경우 예외 처리
            if (faq.getWriterId() == null || faq.getQuestion() == null || faq.getQuestion().isEmpty()) {
                return ResponseUtils.buildErrorResponse(
                        "BAD_REQUEST",
                        "IllegalArgumentException",
                        "Invalid input: Missing required fields",
                        HttpStatus.BAD_REQUEST
                );
            }

            AdminFAQDto createdFAQ = faqService.createFAQ(faq);
            return ResponseEntity.ok(createdFAQ);

        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateFAQ(@PathVariable Long id,
                                       @RequestBody AdminFAQDto faq,
                                       @RequestHeader(value = "Authorization", required = false) String role) {
        try {
            // role이 null이거나 ROLE_ADMIN이 아닌 경우 접근 제한
            if (role == null) {
                return ResponseUtils.buildErrorResponse(
                        "UNAUTHORIZED",
                        "SecurityException",
                        "User is not authenticated",
                        HttpStatus.UNAUTHORIZED
                );
            } else if (!role.equals("ROLE_ADMIN")) {
                return ResponseUtils.buildErrorResponse(
                        "FORBIDDEN",
                        "SecurityException",
                        "Access Denied: User does not have sufficient permissions",
                        HttpStatus.FORBIDDEN
                );
            }

            // faq 객체의 필수 필드가 유효하지 않은 경우 예외 처리
            if (faq.getWriterId() == null || faq.getQuestion() == null || faq.getQuestion().isEmpty()) {
                return ResponseUtils.buildErrorResponse(
                        "BAD_REQUEST",
                        "IllegalArgumentException",
                        "Invalid input: Missing required fields",
                        HttpStatus.BAD_REQUEST
                );
            }

            AdminFAQDto updatedFAQ = faqService.updateFAQ(id, faq, role);
            return ResponseEntity.ok(updatedFAQ);

        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFAQ(@PathVariable Long id,
                                       @RequestHeader(value = "Authorization", required = false) String role) {
        try {
            // role이 null이거나 ROLE_ADMIN이 아닌 경우 접근 제한
            if (role == null) {
                return ResponseUtils.buildErrorResponse(
                        "UNAUTHORIZED",
                        "SecurityException",
                        "User is not authenticated",
                        HttpStatus.UNAUTHORIZED
                );
            } else if (!role.equals("ROLE_ADMIN")) {
                return ResponseUtils.buildErrorResponse(
                        "FORBIDDEN",
                        "SecurityException",
                        "Access Denied: User does not have sufficient permissions",
                        HttpStatus.FORBIDDEN
                );
            }

            faqService.deleteFAQ(id, role);
            return ResponseEntity.ok(Map.of("message", "FAQ successfully deleted"));

        }  catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFAQs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                          @RequestParam(required = false) String keyword) {
        try {
            // JSON 반환값 Map으로 받아오기
            Map<String, Object> response = faqService.searchFAQs(page, pageSize, keyword);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseUtils.buildErrorResponse(
                    "FORBIDDEN",
                    e.getClass().getSimpleName(),
                    "Access Denied: Security exception occurred",
                    HttpStatus.FORBIDDEN
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
