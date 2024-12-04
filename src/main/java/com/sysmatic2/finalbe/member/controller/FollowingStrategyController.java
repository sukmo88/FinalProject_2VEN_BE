package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.member.dto.*;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.member.service.FollowingStrategyService;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor

public class FollowingStrategyController {
    private final FollowingStrategyService followingStrategyService;

    //관심 전략 등록
    @PostMapping("/following-strategy")
    public ResponseEntity<Map<String,Object>> createFollwingStrategy(@RequestBody FollowingStrategyRequestDto followingStrategyDto,
                                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails){
        FollowingStrategyResponseDto responseDto = followingStrategyService.createFollowingStrategy(followingStrategyDto,customUserDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message","관심전략이 정상적으로 등록되었습니다.",
                "data",responseDto
        ));
    }

    //관심 전략 목록 조회
    @GetMapping("/following-strategy/list/{folderId}")
    public ResponseEntity<Map<String,Object>> getListFollowingStrategy(@PathVariable Long folderId, FollowingStrategyRequestDto followingStrategyRequestDto, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        //ArrayList<FollowingStrategyListDto> list = followingStrategyService.getListFollowingStrategy(followingStrategyRequestDto, customUserDetails);

        List<FollowingStrategyListDto> list = followingStrategyService.getListFollowingStrategy1(folderId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message","관심전략 목록이 정상적으로 조회되었습니다.",
                "data", list
        ));
    }
    //관심 전략 목록 조회(페이징처리)
    @GetMapping("/following-strategy/page2/{folderId}")
    public ResponseEntity<Map<String,Object>> getListFollowingStrategyPage(@PathVariable Long folderId, FollowingStrategyRequestDto followingStrategyRequestDto, @AuthenticationPrincipal CustomUserDetails customUserDetails, Pageable pageable){

        Page<FollowingStrategyListDto> page  = followingStrategyService.getListFollowingStrategyPage(pageable,10,folderId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message","관심전략 목록이 정상적으로 조회되었습니다.",
                "data", page.getContent(),       // 페이징된 데이터
                "currentPage", page.getNumber(), // 현재 페이지
                "totalItems", page.getTotalElements(), // 전체 데이터 수
                "totalPages", page.getTotalPages() // 전체 페이지 수
        ));
    }

    //관심 전략 목록 조회(searchOptionsPayload 호출)
    @GetMapping("/following-strategy/page/{folderId}")
    public ResponseEntity<Map<String, Object>> getStrategiesByFolder(
            @PathVariable Long folderId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        List<Long> strategyIds = followingStrategyService.getListFollowingStrategyList(folderId);
        Map<String, Object> response = followingStrategyService.getStrategiesByFolder(strategyIds, page, pageSize);
        return ResponseEntity.ok(response);
    }



    //관심 전략 삭제
    @DeleteMapping("/following-strategy/{followingStrategyId}")
    public ResponseEntity<Map<String,String>> deleteFollowingStrategy(@PathVariable Long followingStrategyId, FollowingStrategyRequestDto followingStrategyRequestDto, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        followingStrategyService.deleteFollowingStrategy(followingStrategyRequestDto,customUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message","관심전략이 정상적으로 삭제되었습니다."
        ));
    }

    //관심 전략 이동

    //관심전략폴더ID별 관심 전략 갯수 count
    @GetMapping("/following-strategy/{folderId}")
    public ResponseEntity<Map<String,Object>> getCountFollowingStrategy(@PathVariable Long folderId, FollowingStrategyRequestDto followingStrategyRequestDto, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        int count = followingStrategyService.countFollowingStrategy(folderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message","정상적으로 조회 성공했습니다.",
                "count",count
        ));
    }

}
