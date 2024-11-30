package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.strategy.service.ExcelGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/strategies/export")
@RequiredArgsConstructor
@Tag(name = "Strategy Export Controller", description = "전략 관련 데이터 엑셀 다운로드 컨트롤러")
@Validated // 메서드 파라미터 유효성 검사를 활성화
public class StrategyExportController {

  private final ExcelGeneratorService excelGeneratorService;

  /**
   * 일간 통계 엑셀 다운로드 API (페이징 지원)
   *
   * @param strategyId      전략 ID
   * @param includeAnalysis 분석 지표 포함 여부
   * @param pageNumber      페이지 번호 (기본값: 0)
   * @param pageSize        페이지 크기 (기본값: 10, 최대: 100)
   * @return 엑셀 파일 다운로드
   */
  @Operation(summary = "일간 통계 엑셀 다운로드", description = "특정 전략의 일간 통계 데이터를 엑셀 파일로 다운로드합니다.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "엑셀 다운로드 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "404", description = "전략을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/daily")
  public ResponseEntity<InputStreamResource> downloadDailyStatistics(
          @RequestParam Long strategyId,
          @RequestParam(defaultValue = "false") boolean includeAnalysis,
          @RequestParam(defaultValue = "0") @Min(value = 0, message = "pageNumber는 0 이상이어야 합니다.") int pageNumber,
          @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize는 최소 1 이상이어야 합니다.")
          @Max(value = 100, message = "pageSize는 최대 100까지 허용됩니다.") int pageSize) throws IOException {

    byte[] excelBytes = excelGeneratorService.exportDailyStatisticsToExcel(strategyId, includeAnalysis, pageNumber, pageSize);
    ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=daily_statistics_page_" + (pageNumber + 1) + ".xlsx");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(bis));
  }

  /**
   * 월간 통계 엑셀 다운로드 API (페이징 지원)
   *
   * @param strategyId 전략 ID
   * @param pageNumber 조회할 페이지 번호 (기본값: 0)
   * @param pageSize   페이지 크기 (기본값: 10, 최대: 100)
   * @return 엑셀 파일 다운로드
   */
  @Operation(summary = "월간 통계 엑셀 다운로드", description = "특정 전략의 월간 통계 데이터를 엑셀 파일로 다운로드합니다.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "엑셀 다운로드 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "404", description = "전략을 찾을 수 없음"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/monthly")
  public ResponseEntity<InputStreamResource> downloadMonthlyStatistics(
          @RequestParam Long strategyId,
          @RequestParam(defaultValue = "0") @Min(value = 0, message = "pageNumber는 0 이상이어야 합니다.") int pageNumber,
          @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize는 최소 1 이상이어야 합니다.")
          @Max(value = 100, message = "pageSize는 최대 100까지 허용됩니다.") int pageSize) throws IOException {

    byte[] excelBytes = excelGeneratorService.exportMonthlyStatisticsToExcel(strategyId, pageNumber, pageSize);
    ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=monthly_statistics_page_" + (pageNumber + 1) + ".xlsx");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(bis));
  }

//  /**
//   * 일간 분석 지표 엑셀 다운로드 API (페이징 지원)
//   *
//   * @param strategyId 전략 ID
//   * @param pageNumber 조회할 페이지 번호 (기본값: 0)
//   * @param pageSize   페이지 크기 (기본값: 10, 최대: 100)
//   * @return 엑셀 파일 다운로드
//   */
//  @Operation(summary = "일간 분석 지표 엑셀 다운로드", description = "특정 전략의 일간 분석 지표 데이터를 엑셀 파일로 다운로드합니다.")
//  @ApiResponses(value = {
//          @ApiResponse(responseCode = "200", description = "엑셀 다운로드 성공"),
//          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
//          @ApiResponse(responseCode = "404", description = "전략을 찾을 수 없음"),
//          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//  })
//  @GetMapping("/daily-analysis")
//  public ResponseEntity<InputStreamResource> downloadDailyAnalysisIndicators(
//          @RequestParam Long strategyId,
//          @RequestParam(defaultValue = "0") @Min(value = 0, message = "pageNumber는 0 이상이어야 합니다.") int pageNumber,
//          @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize는 최소 1 이상이어야 합니다.")
//          @Max(value = 100, message = "pageSize는 최대 100까지 허용됩니다.") int pageSize) throws IOException {
//
//    byte[] excelBytes = excelGeneratorService.exportDailyAnalysisIndicatorsToExcel(strategyId, pageNumber, pageSize);
//    ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
//
//    HttpHeaders headers = new HttpHeaders();
//    headers.add("Content-Disposition", "attachment; filename=daily_analysis_page_" + (pageNumber + 1) + ".xlsx");
//
//    return ResponseEntity.ok()
//            .headers(headers)
//            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
//            .body(new InputStreamResource(bis));
//  }
}
