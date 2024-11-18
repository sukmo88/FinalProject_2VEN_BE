package com.sysmatic2.finalbe.admin.controller;

import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminRequestDto;
import com.sysmatic2.finalbe.admin.dto.TradingCycleAdminResponseDto;
import com.sysmatic2.finalbe.admin.service.TradingCycleService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin TradingCycle Controller", description = "관리자가 매매주기를 관리하는 컨트롤러")
public class TradingCycleController {

    private final TradingCycleService tradingCycleService;

    // 1. 투자주기 목록
    @GetMapping("/trading-cycles")
    @ApiResponse(responseCode = "200", description = "List of Trading Cycles")
    public ResponseEntity<Map<String, Object>> getAllTradingCycles(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be greater than zero") int pageSize,
            @RequestParam(required = false) @Pattern(regexp = "Y|N", message = "isActive must be 'Y', 'N', or null") String isActive) {
        Map<String, Object> response = tradingCycleService.findAllTradingCycles(page, pageSize, isActive);
        return ResponseEntity.ok(response);
    }

    // 1-1. 투자주기 상세 조회 메서드
    @GetMapping("/trading-cycles/{id}")
    @ApiResponse(responseCode = "200", description = "Get Trading Cycle by ID")
    public ResponseEntity<Map<String, Object>> getTradingCycleById(@PathVariable("id") @Positive Integer id) {
        TradingCycleAdminResponseDto tradingCycleAdminResponseDto = tradingCycleService.findTradingCycleById(id);
        Instant timestamp = Instant.now();
        return ResponseEntity.ok(Map.of(
                "data", tradingCycleAdminResponseDto,
                "timestamp", timestamp.toString()
        ));
    }

    // 2. 투자주기 등록
    @PostMapping("/trading-cycles")
    @ApiResponse(responseCode = "201", description = "Create Trading Cycle")
    public ResponseEntity<Map<String, String>> createTradingCycle(@Valid @RequestBody TradingCycleAdminRequestDto tradingCycleAdminRequestDto) {
        tradingCycleService.createTradingCycle(tradingCycleAdminRequestDto);
        Instant timestamp = Instant.now();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "msg", "CREATE_SUCCESS",
                "timestamp", timestamp.toString()
        ));
    }

    // 3. 투자주기 삭제
    @DeleteMapping("/trading-cycles/{id}")
    @ApiResponse(responseCode = "200", description = "Delete Trading Cycle")
    public ResponseEntity<Map<String, String>> deleteTradingCycle(@PathVariable @Positive Integer id) {
        tradingCycleService.deleteTradingCycle(id);
        Instant timestamp = Instant.now();
        return ResponseEntity.ok(Map.of(
                "msg", "DELETE_SUCCESS",
                "timestamp", timestamp.toString()
        ));
    }

    // 3-1. 투자주기 논리적 삭제
    @PatchMapping("/trading-cycles/{id}")
    @ApiResponse(responseCode = "200", description = "Soft Delete Trading Cycle")
    public ResponseEntity<Map<String, String>> softDeleteTradingCycle(@PathVariable @Positive Integer id) {
        tradingCycleService.softDeleteTradingCycle(id);
        Instant timestamp = Instant.now();
        return ResponseEntity.ok(Map.of(
                "msg", "DELETE_SUCCESS",
                "timestamp", timestamp.toString()
        ));
    }

    // 4. 투자주기 수정
    @PutMapping("/trading-cycles/{id}")
    @ApiResponse(responseCode = "200", description = "Update Trading Cycle")
    public ResponseEntity<Map<String, String>> updateTradingCycle(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody TradingCycleAdminRequestDto tradingCycleAdminRequestDto) {
        System.out.println("id = " + id);
        System.out.println("tradingCycleService = " + tradingCycleAdminRequestDto);
        tradingCycleService.updateTradingCycle(id, tradingCycleAdminRequestDto);
        Instant timestamp = Instant.now();
        return ResponseEntity.ok(Map.of(
                "msg", "UPDATE_SUCCESS",
                "timestamp", timestamp.toString()
        ));
    }
}