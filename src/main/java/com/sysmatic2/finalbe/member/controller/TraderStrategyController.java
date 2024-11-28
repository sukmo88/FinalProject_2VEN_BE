package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.service.MemberService;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/traders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Trader Controller", description = "트레이더 관련 ")
public class TraderStrategyController {
    private final StrategyService strategyService;
    private final MemberService memberService;

    // 1. 트레이더 나의 전략
    @GetMapping("/{traderId}/strategies")
    @Operation(summary = "트레이더 마이페이지내 나의 전략 목록들을 보여주는 메서드")
    public ResponseEntity<Map<String, Object>> traderStrategy(@PathVariable String traderId,
                                              @RequestParam(defaultValue = "0") @Min(0) Integer page,
                                              @RequestParam(defaultValue = "10") @Min(1) Integer pageSize){
        //TODO) 로그인정보 권한 확인

        Map<String, Object> responseData = strategyService.getStrategyListbyTraderId(traderId, page, pageSize);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    // 2. 트레이더 키워드 검색
    @GetMapping("/search")
    @Operation(summary = "키워드로 검색한 트레이더 목록을 보여주는 메서드")
    public ResponseEntity<Map<String, Object>> traderKeywordSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "4") Integer pageSize){

        Map<String, Object> responseData = memberService.getTraderListByKeyword(keyword, page, pageSize);
        //가변맵 변경
        responseData = new HashMap<>(responseData);
        responseData.put("keyword", keyword);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

}
