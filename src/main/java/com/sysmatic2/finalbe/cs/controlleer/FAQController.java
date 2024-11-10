package com.sysmatic2.finalbe.cs.controlleer;

import com.sysmatic2.finalbe.cs.dto.AdminFAQDto;
import com.sysmatic2.finalbe.cs.dto.UserFAQDto;
import com.sysmatic2.finalbe.cs.entity.FAQ;
import com.sysmatic2.finalbe.cs.service.FAQService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/faqs")
public class FAQController {

    private final FAQService faqService;

    public FAQController(FAQService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public ResponseEntity<?> getAllFAQs(@RequestParam(value = "role", required = false) String role){
        try {
            List<?> faqs = faqService.getAllFAQs(role);
            return ResponseEntity.ok(faqs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "READ_FAILED", "message", "An unexpected error occurred while fetching FAQs"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createFAQ(@RequestBody FAQ faq, @RequestParam(value = "role", required = false) String role) {
        try {
            AdminFAQDto createdFAQ = faqService.createFAQ(faq, role);
            return ResponseEntity.ok(createdFAQ);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "REGISTRATION_FAILED", "message", "Access Denied"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "REGISTRATION_FAILED", "message", "An unexpected error occurred during registration"));
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFAQ(@PathVariable Long id, @RequestBody FAQ faq, @RequestParam(value = "role", required = false) String role) {
        try {
            AdminFAQDto updatedFAQ = faqService.updateFAQ(id, faq, role);
            return ResponseEntity.ok(updatedFAQ);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "UPDATE_FAILED", "message", "Access Denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "UPDATE_FAILED", "message", "FAQ with id " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "UPDATE_FAILED", "message", "An unexpected error occurred while updating FAQ"));
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFAQ(@PathVariable Long id, @RequestParam(value = "role", required = false) String role) {
        try {
            faqService.deleteFAQ(id, role);
            return ResponseEntity.ok(Map.of("message", "FAQ successfully deleted")); // 200 OK 상태 반환
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "DELETE_FAILED", "message", "Access Denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "DELETE_FAILED", "message", "FAQ with id " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "DELETE_FAILED", "message", "An unexpected error occurred while deleting FAQ"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserFAQDto>> searchFAQs(
            @RequestParam("keyword") String keyword,
            Pageable pageable) {
        Page<UserFAQDto> faqPage = faqService.searchFAQs(keyword, pageable);
        return ResponseEntity.ok(faqPage);
    }


}
