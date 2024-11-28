package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.service.MemberService;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/traders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Trader Strategy Controller", description = "트레이더 마이 페이지에서 나의 전략을 보여주는 컨트롤러")
public class TraderStrategyController {
    private final StrategyService strategyService;
    private final MemberService memberService;

    @GetMapping("/{traderId}/strategies")
    public ResponseEntity<Map<String, Object>> traderStrategy(@PathVariable String traderId,
                                              @RequestParam(defaultValue = "0") @Min(0) Integer page,
                                              @RequestParam(defaultValue = "10") @Min(1) Integer pageSize){
        //TODO) 로그인정보 권한 확인

        Map<String, Object> responseData = strategyService.getStrategyListbyTraderId(traderId, page, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

}
