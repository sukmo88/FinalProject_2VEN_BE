package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.MemberNotFoundException;
import com.sysmatic2.finalbe.exception.ReviewNotFoundException;
import com.sysmatic2.finalbe.exception.StrategyNotFoundException;
import com.sysmatic2.finalbe.exception.StrategyReviewFailedException;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.member.service.MemberService;
import com.sysmatic2.finalbe.strategy.dto.LiveAccountDataResponseDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyReviewDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyReviewPageResponseDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyReviewEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StrategyReviewService {

    private final StrategyReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final StrategyRepository strategyRepository;

    /**
     * 1. 리뷰 생성
     */
    @Transactional
    public StrategyReviewDto createReview(Long strategyId, String writerId, String content) {
        // 전략 및 작성자 확인
        StrategyEntity strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in review "));

        MemberEntity writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new MemberNotFoundException("Writer not found with ID: " + writerId + "in review"));

        // 리뷰 엔티티 생성
        StrategyReviewEntity review = new StrategyReviewEntity();
        review.setStrategy(strategy);
        review.setWriterId(writer);
        review.setContent(content);
        review.setWritedAt(LocalDateTime.now());
        review.setUpdatedAt(null);

        // 저장 및 DTO 변환
        StrategyReviewEntity savedReview;
        try {
            savedReview = reviewRepository.save(review);
        } catch (Exception e) {
            throw new StrategyReviewFailedException("Failed to create the strategy review due to unexpected error.", e);
        }

        return StrategyReviewDto.fromEntity(savedReview);
    }

    /**
     * 2. 리뷰 조회 (단일)
     */
    public StrategyReviewDto getReviewById(Long reviewId) {
        StrategyReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        return StrategyReviewDto.fromEntity(review);
    }

    /**
     * 3. 리뷰 조회 (리스트)
     */
    public StrategyReviewPageResponseDto getReviewsByStrategyId(Long strategyId, int page, int pageSize) {
        // 1. 페이지네이션 요청 값 검증 및 페이지 객체 생성
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }

        // 페이지 요청 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        // 2. strategy가 유효한 전략인지 확인하고 조회
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in live account data uploading."));

        // 3. 전략의 리뷰들 조회
        Page<StrategyReviewEntity> reviewPage = reviewRepository.findAllByStrategyOrderByWritedAtDesc(strategyEntity, pageable);

        return StrategyReviewPageResponseDto.fromEntity(reviewPage);
    }

    /**
     * 4. 리뷰 수정
     */
    @Transactional
    public StrategyReviewDto updateReview(Long reviewId, String updaterId, String updatedContent) {
        // 리뷰 및 수정자 확인
        StrategyReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        MemberEntity updater = memberRepository.findById(updaterId)
                .orElseThrow(() -> new MemberNotFoundException("Updater not found with ID: " + updaterId));

        //리뷰 수정은 작성자만 가능하다.
        if(!review.getUpdaterId().equals(updaterId)){
            throw new AccessDeniedException("리뷰 수정은 작성자만 가능합니다.");
        }

        // 수정
        review.setUpdaterId(updater);
        review.setContent(updatedContent);
        review.setUpdatedAt(LocalDateTime.now());
        review.setModifiedAt(LocalDateTime.now());

        try {
            // 저장 및 DTO 변환
            StrategyReviewEntity updatedReview = reviewRepository.save(review);
            return StrategyReviewDto.fromEntity(updatedReview);
        } catch (Exception e) {
            throw new StrategyReviewFailedException("Failed to update the strategy review due to unexpected error.", e);
        }
    }

    /**
     * 5-1. 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long strategyId, Long reviewId, String memberId, Boolean isAdmin, Boolean isTrader) {
        StrategyReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        //관리자인 경우
        if(isAdmin){
            reviewRepository.delete(review);
            return;
        }

        //전략 정보
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                        .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in review "));

        //트레이더인 경우 : 전략의 작성자
        if(isTrader && strategyEntity.getWriterId().equals(memberId)){
            reviewRepository.delete(review);
            return;
        }

        //일반 유저인 경우 : 리뷰의 작성자
        if(review.getWriterId().equals(memberId)){
            reviewRepository.delete(review);
            return;
        }

        throw new AccessDeniedException("리뷰 삭제 권한이 없습니다.");
    }

    /**
     * 5-2. 리뷰 삭제 (전략이 삭제되었을 때)
     */
    @Transactional
    public void deleteAllReviewsByStrategy(Long strategyId) {
        // 전략 확인
        StrategyEntity strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId));

        // 전략에 포함된 모든 리뷰 조회
        List<StrategyReviewEntity> reviews = reviewRepository.findAllByStrategy(strategy);

        if (reviews.isEmpty()) {
            throw new ReviewNotFoundException("No reviews found for strategy ID: " + strategyId);
        }

        try {
            // 리뷰 삭제
            reviewRepository.deleteAll(reviews);
        } catch (Exception e) {
            throw new StrategyReviewFailedException("Failed to delete all reviews for strategy ID: " + strategyId, e);
        }
    }

    /**
     * 5-3. 전략에 해당하는 리뷰 삭제 (트레이더 회원 탈퇴로 인한 전략 삭제 시)
     */
    @Transactional
    public void deleteReviewsByStrategy(StrategyEntity strategy) {
        try {
            reviewRepository.deleteAllByStrategy(strategy);
        } catch (Exception e) {
            throw new StrategyReviewFailedException("Failed to delete all reviews for strategy ID: " + strategy.getStrategyId(), e);
        }
    }

    /**
     * 5-4. 리뷰 작성자로 리뷰 삭제 (회원 탈퇴 시)
     */
    @Transactional
    public void deleteReviewsByWriter(MemberEntity writer) {
        try {
            reviewRepository.deleteAllByWriterId(writer);
        } catch (Exception e) {
            throw new StrategyReviewFailedException("Failed to delete all reviews for writer ID: " + writer.getMemberId(), e);
        }
    }

}
