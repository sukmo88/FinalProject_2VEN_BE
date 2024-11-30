package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.strategy.service.ExcelUploadService;
import com.sysmatic2.finalbe.exception.ExcelValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strategies")  // 동일한 엔드포인트 경로
public class ExcelUploadController {

  private final ExcelUploadService excelUploadService;

  public ExcelUploadController(ExcelUploadService excelUploadService) {
    this.excelUploadService = excelUploadService;
  }

  @Operation(summary = "엑셀 파일 업로드 및 데이터 추출", description = "엑셀 파일을 업로드하고 데이터를 추출하여 반환합니다.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "엑셀 데이터 추출 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 엑셀 파일 형식"),
          @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping(value = "/upload", produces = "application/json") // 같은 엔드포인트 "/upload"
  public ResponseEntity<Map<String, Object>> uploadExcelFile(@RequestParam("file") MultipartFile file) {
    try {
      // 엑셀 파일에서 데이터를 추출하고 검증
      List<DailyStatisticsReqDto> result = excelUploadService.extractAndValidateData(file);

      // 성공적인 응답
      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
              "msg", "CREATE_SUCCESS",
              "data", result
      ));

    } catch (ExcelValidationException e) {
      // 엑셀 파일 유효성 검증 실패
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
              "msg", "EXCEL_VALIDATION_FAILED",
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
