package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.strategy.dto.LiveAccountDataPageResponseDto;
import com.sysmatic2.finalbe.strategy.dto.LiveAccountDataResponseDto;
import com.sysmatic2.finalbe.strategy.service.LiveAccountDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live-account-data")
public class LiveAccountDataController {

    private final LiveAccountDataService liveAccountDataService;

    public LiveAccountDataController(LiveAccountDataService liveAccountDataService) {
        this.liveAccountDataService = liveAccountDataService;
    }

    /**
     * 1. 실계좌 이미지 등록 API
     * 관리자와 해당 전략의 작성자만 등록 가능하다.
     *
     * @param file         등록할 이미지 파일
     * @param strategyId   전략 ID
     * @param userDetails   업로더 ID (JWT 또는 인증정보에서 추출)
     * @return 등록된 실계좌 이미지 정보
     */
    @PostMapping("/{strategyId}")
    public ResponseEntity<LiveAccountDataResponseDto> uploadLiveAccountData(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long strategyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        // 서비스 호출
        LiveAccountDataResponseDto response = liveAccountDataService.uploadLiveAccountData(file, strategyId, memberId, isTrader);

        // 응답 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * 2. 실계좌 이미지 목록 조회 API
     * isPosted = N or isApproved = N인 경우 작성자와 관리자만 조회할 수 있다.
     *
     * @param strategyId 전략 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param pageSize 페이지 크기 (기본값: 8)
     * @return 실계좌 이미지 정보 리스트
     */
    @GetMapping("/{strategyId}/list")
    public ResponseEntity<LiveAccountDataPageResponseDto> getLiveAccountDataList(
            @PathVariable Long strategyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        //접속자 정보
        String memberId = userDetails != null ? userDetails.getMemberId() : null;
        Boolean isTrader = userDetails != null ?
                userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"))
                : false ;

        // 서비스 호출
        LiveAccountDataPageResponseDto response = liveAccountDataService.getLiveAccountDataList(page, pageSize, strategyId, memberId, isTrader);

        // 응답 반환
        return ResponseEntity.ok(response);
    }



    /**
     * 3. 실계좌 이미지 삭제
     *
     * @param strategyId 전략 ID
     * @param liveAccountId pagenation에 page 값(default = 0)
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/{strategyId}")
    public ResponseEntity<Map<String, Object>> deleteLiveAccountData(
            @PathVariable Long strategyId,
            @RequestBody List<Long> liveAccountIds,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        // 실계좌 인증 삭제
        liveAccountDataService.deleteLiveAccountDataList(strategyId, liveAccountIds, memberId, isTrader);

        // 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Live account data successfully deleted.");
        response.put("deletedIds", liveAccountIds);
        response.put("strategyId", strategyId.toString());

        return ResponseEntity.ok(response);
    }
}