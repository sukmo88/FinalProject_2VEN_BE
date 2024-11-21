package com.sysmatic2.finalbe.cs.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponseDto<T> {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
