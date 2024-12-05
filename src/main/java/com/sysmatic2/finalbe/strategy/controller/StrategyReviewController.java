package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.strategy.dto.StrategyReviewDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyReviewPageResponseDto;
import com.sysmatic2.finalbe.strategy.service.StrategyReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategies/{strategyId}/reviews")
@RequiredArgsConstructor
public class StrategyReviewController {

    private final StrategyReviewService strategyReviewService;

    /**
     * 1. 리뷰 생성
     */
    @PostMapping
    public ResponseEntity<Map<String,Object>> createReview(
            @PathVariable Long strategyId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String content = requestBody.get("content");
        StrategyReviewDto review = strategyReviewService.createReview(strategyId, userDetails.getMemberId(), content);

        // 메시지와 리뷰 데이터를 포함한 응답 생성
        Map<String, Object> response = Map.of(
                "msg", "Review created successfully."
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 리뷰 조회 (단일)
     */
//    @GetMapping("/{reviewId}")
//    public ResponseEntity<StrategyReviewDto> getReviewById(@PathVariable Long strategyId, @PathVariable Long reviewId) {
//        StrategyReviewDto review = strategyReviewService.getReviewById(reviewId);
//        return ResponseEntity.ok(review);
//    }

    /**
     * 3. 리뷰 조회 (리스트)
     */
    @GetMapping
    public ResponseEntity<StrategyReviewPageResponseDto> getReviewsByStrategyId(
            @PathVariable Long strategyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        StrategyReviewPageResponseDto reviewsPage = strategyReviewService.getReviewsByStrategyId(strategyId, page, pageSize);

        return ResponseEntity.ok(reviewsPage);
    }

    /**
     * 4. 리뷰 수정
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long strategyId,
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String updatedContent = requestBody.get("content");
        StrategyReviewDto updatedReview = strategyReviewService.updateReview(reviewId, userDetails.getMemberId(), updatedContent);

        // 메시지와 리뷰 데이터를 포함한 응답 생성
        Map<String, Object> response = Map.of(
                "msg", "Review updated successfully."
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 5-1. 리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long strategyId, @PathVariable Long reviewId,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        //접속자 정보
        //관리자와 전략 작성자, 리뷰 작성자는 해당 리뷰를 삭제할 수 있다.
        String memberId = userDetails.getMemberId();
        Boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        strategyReviewService.deleteReview(strategyId, reviewId, memberId, isAdmin, isTrader);

        return ResponseEntity.ok(Map.of("msg", "Review successfully deleted"));
    }

}