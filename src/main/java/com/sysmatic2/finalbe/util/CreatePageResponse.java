package com.sysmatic2.finalbe.util;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

public class CreatePageResponse {
    public static <T> Map<String, Object> createPageResponse(Page<T> page) {
        return Map.of(
                "totalElements", page.getTotalElements(),   // 전체 원소 개수
                "isFirstPage", page.isFirst(),              // 첫 번째 페이지 여부
                "isLastPage", page.isLast(),                // 마지막 페이지 여부
                "totalPages", page.getTotalPages(),         // 전체 페이지 개수
                "isSorted", page.getSort().isSorted(),      // 정렬 여부
                "pageSize", page.getSize(),                 // 한 페이지당 원소 개수
                "currentPage", page.getNumber(),            // 현재 페이지 번호
                "data", page.getContent(),                  // 실제 데이터 리스트
                "timestamp", Instant.now().toString()       // 타임스탬프
        );
    }
}
