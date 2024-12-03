package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.strategy.entity.StrategyReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyReviewPageResponseDto {
    private List<StrategyReviewDto> data; // 리뷰 데이터 리스트
    private int pageSize;                 // 페이지 크기
    private int totalPages;               // 총 페이지 수
    private boolean isLastPage;           // 마지막 페이지 여부
    private long totalElements;           // 전체 요소 수
    private boolean isSorted;             // 정렬 여부
    private boolean isFirstPage;          // 첫 번째 페이지 여부
    private int currentPage;              // 현재 페이지 번호
    private String timestamp;             // 현재 타임스탬프

    /**
     * Page<StrategyReviewEntity>를 받아 DTO로 변환
     */
    public static StrategyReviewPageResponseDto fromEntity(Page<StrategyReviewEntity> reviewPage) {
        // 리뷰 엔티티를 DTO 리스트로 변환
        List<StrategyReviewDto> reviewDtos = reviewPage.getContent()
                .stream()
                .map(StrategyReviewDto::fromEntity)
                .toList();

        // 페이지 정보를 활용해 DTO 생성
        return new StrategyReviewPageResponseDto(
                reviewDtos,
                reviewPage.getSize(),
                reviewPage.getTotalPages(),
                reviewPage.isLast(),
                reviewPage.getTotalElements(),
                reviewPage.getSort().isSorted(),
                reviewPage.isFirst(),
                reviewPage.getNumber(),
                Instant.now().toString() // 현재 타임스탬프
        );
    }
}