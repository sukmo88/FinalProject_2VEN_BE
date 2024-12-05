package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.service.ExcelUploadService;
import com.sysmatic2.finalbe.exception.ExcelValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/strategies")
public class ExcelUploadController {

  private final ExcelUploadService excelUploadService;

  public ExcelUploadController(ExcelUploadService excelUploadService) {
    this.excelUploadService = excelUploadService;
  }

  @Operation(summary = "엑셀 파일 업로드 및 데이터 저장", description = "특정 전략 ID와 연동된 엑셀 파일을 업로드하고 데이터를 추출하여 저장합니다.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "엑셀 데이터 추출 및 저장 성공",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(example = "{ \"msg\": \"CREATE_SUCCESS\", \"data\": [ { \"date\": \"2024-01-01\", \"depWdPrice\": 1000, \"dailyProfitLoss\": 200 } ] }"))),
          @ApiResponse(responseCode = "400", description = "잘못된 엑셀 파일 형식 또는 유효하지 않은 전략 ID",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(example = "{ \"msg\": \"EXCEL_VALIDATION_FAILED\", \"error\": \"Invalid Excel file format\" }"))),
          @ApiResponse(responseCode = "500", description = "서버 오류",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(example = "{ \"msg\": \"EXCEL_UPLOAD_FAILED\", \"error\": \"Unexpected error occurred\" }")))
  })
  @PostMapping(value = "/{strategyId}/upload", consumes = "multipart/form-data", produces = "application/json")
  public ResponseEntity<Map<String, Object>> uploadExcelFile(
          @PathVariable Long strategyId,
          @RequestParam("file") MultipartFile file,
          @AuthenticationPrincipal CustomUserDetails userDetails) {
    try {
      //접속자 정보
      String memberId = userDetails.getMemberId();
      Boolean isTrader = userDetails.getAuthorities().stream()
              .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

      // 엑셀 파일에서 데이터를 추출하고 저장 (엔티티 리스트 반환)
      List<DailyStatisticsEntity> entities = excelUploadService.extractAndSaveData(file, strategyId, memberId, isTrader);



      // 엔티티 리스트를 DTO 리스트로 변환
      List<DailyStatisticsReqDto> dtos = entities.stream()
              .map(entity -> DailyStatisticsReqDto.builder()
                      .date(entity.getDate())
                      .depWdPrice(entity.getDepWdPrice())
                      .dailyProfitLoss(entity.getDailyProfitLoss())
                      .build())
              .collect(Collectors.toList());

      // 성공적인 응답
      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
              "msg", "CREATE_SUCCESS",
              "data", dtos
      ));

    } catch (ExcelValidationException e) {
      // 엑셀 파일 유효성 검증 실패
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
              "msg", "EXCEL_VALIDATION_FAILED",
              "error", e.getMessage()
      ));
    } catch (IllegalArgumentException e) {
      // 유효하지 않은 전략 ID
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
              "msg", "EXCEL_UPLOAD_FAILED",
              "error", e.getMessage()
      ));
    } catch (Exception e) {
      // 서버 내부 오류
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
              "msg", "EXCEL_UPLOAD_FAILED",
              "error", e.getMessage()
      ));
    }
  }
}
