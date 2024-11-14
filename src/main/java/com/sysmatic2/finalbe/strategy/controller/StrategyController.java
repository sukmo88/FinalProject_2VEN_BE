package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.strategy.dto.StrategyRegistrationDto;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
@Validated
public class StrategyController {
    private final StrategyService strategyService;

    // 1. 전략 생성(POST)

    // 2. 전략 생성(GET)
    @GetMapping("/registration-form")
    @ApiResponse(responseCode = "200", description = "Get Strategy Registration Form")
    public ResponseEntity<Map<String, Object>> getStrategyRegistrationForm() {
        // 서비스 메서드를 호출하여 StrategyRegistrationDto 생성
        StrategyRegistrationDto strategyRegistrationDto = strategyService.getStrategyRegistrationForm();

        // 타임스탬프 추가
        Instant timestamp = Instant.now();

        // JSON 형태로 응답 반환 (상태 코드 200)
        return ResponseEntity.ok(Map.of(
                "data", strategyRegistrationDto,
                "timestamp", timestamp.toString()
        ));
    }
}
