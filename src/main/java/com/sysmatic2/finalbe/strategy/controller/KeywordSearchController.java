package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.member.service.MemberService;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//@RestController
//@RequestMapping("/api/keyword-search")
//@RequiredArgsConstructor
//@Validated
//@Tag(name = "Keyword Search Controller", description = "키워드 통합 검색 컨트롤러")
//public class KeywordSearchController {
//    private final StrategyService strategyService;
//    private final MemberService memberService;
//
//    //1. 키워드 전략 검색(GET)
//    @Operation(summary = "키워드 전략 검색")
//    @GetMapping("/strategies")
//
//}
