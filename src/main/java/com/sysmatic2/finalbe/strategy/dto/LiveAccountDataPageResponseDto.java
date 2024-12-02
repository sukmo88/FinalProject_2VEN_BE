package com.sysmatic2.finalbe.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveAccountDataPageResponseDto {
    private List<LiveAccountDataResponseDto> data;
    private int pageSize;
    private int totalPages;
    private boolean isLastPage;
    private long totalElements;
    private boolean isSorted;
    private boolean isFirstPage;
    private int currentPage;
    private String timestamp;
}