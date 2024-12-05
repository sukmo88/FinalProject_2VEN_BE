package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.dto.*;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.strategy.service.DailyStatisticsService;
import com.sysmatic2.finalbe.strategy.service.MonthlyStatisticsService;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import com.sysmatic2.finalbe.util.CreatePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Strategy Controller", description = "전략 컨트롤러")
public class StrategyController {
    private final StrategyService strategyService;
    private final DailyStatisticsService dailyStatisticsService;
    private final MonthlyStatisticsService monthlyStatisticsService;
    private final MemberRepository memberRepository;
    private final StrategyRepository strategyRepository;

    // 1. 전략 생성페이지(GET)
    // 관리자와 트레이더만 전략을 생성할 수 있다.
    @Operation(summary = "전략 생성페이지 필요 정보")
    @GetMapping("/registration-form")
    @ApiResponse(responseCode = "200", description = "Get Strategy Registration Form")
    public ResponseEntity<Map<String, Object>> getStrategyRegistrationForm() {

        //서비스 메서드를 호출하여 StrategyRegistrationDto 생성
        StrategyRegistrationDto strategyRegistrationDto = strategyService.getStrategyRegistrationForm();

        // 타임스탬프 추가
        Instant timestamp = Instant.now();

        // JSON 형태로 응답 반환 (상태 코드 200)
        return ResponseEntity.ok(Map.of(
                "data", strategyRegistrationDto,
                "timestamp", timestamp.toString()
        ));
    }

    // 2. 전략 생성(POST)
    // 관리자와 트레이더만 전략을 생성할 수 있다.
    @Operation(summary = "전략 생성")
    @PostMapping(produces="application/json")
    public ResponseEntity<Map> createStrategy(@Valid @RequestBody StrategyPayloadDto strategyPayloadDto,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{

        //Authentication에서 JWT토큰을 가져와 회원id를 가져옴
        String memberId = userDetails.getMemberId();

        //데이터 저장
        Map<String, Long> responseData = strategyService.register(strategyPayloadDto, memberId);

        //해쉬맵에 성공 메시지 저장
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");
        responseMap.put("data", responseData);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    // 3. 전략 목록 - 랭킹
    /**
     * 3. 필터 조건에 따라 전략 목록 반환 (페이징 포함) - 랭킹
     *
     * @param tradingCycleId           투자주기 ID (nullable)
     * @param investmentAssetClassesId 투자자산 분류 ID (nullable)
     * @param page                     현재 페이지 번호 (0부터 시작)
     * @param pageSize                 페이지당 데이터 개수
     * @return 필터링된 전략 목록 및 페이징 정보를 포함한 Map 객체
     */
    @GetMapping
    @Operation(summary = "필터 조건으로 전략 목록 조회 - 전략 랭킹",
            description = "투자주기와 투자자산 분류로 전략을 필터링하여 조회합니다. 페이징을 지원합니다. " +
                    "각 전략에 대해 누적 손익률, 최근 1년 손익률, 최대 자본 인하율(MDD)을 포함한 상세 정보를 제공합니다.")
    public ResponseEntity<Map<String, Object>> getStrategies(
            @RequestParam(required = false) @Positive(message = "tradingCycleId는 양수여야 합니다.") Integer tradingCycleId,
            @RequestParam(required = false) @Positive(message = "investmentAssetClassesId는 양수여야 합니다.") Integer investmentAssetClassesId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) int pageSize) {

        // 1. 서비스 메서드 호출
        // 필터 조건 및 페이징 정보를 전달하여 전략 목록 데이터를 가져옴
        Map<String, Object> response = strategyService.getStrategies(tradingCycleId, investmentAssetClassesId, page, pageSize);

        // 2. 응답 데이터 반환
        // 200 OK 응답과 함께 결과 데이터를 반환
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 3. 승인 받은 전략의 갯수와 트레이더 수를 반환 - 메인페이지
     *
     * @return 승인 받은 전략수, 트레이더수
     */
    @Operation(
            summary = "승인된 전략 수 및 트레이더 수 조회",
            description = "승인된 전략의 총 개수와 'MEMBER_ROLE_TRADER' 역할을 가진 트레이더의 총 수를 반환합니다."
    )
    @GetMapping(value = "strategy-trader-count", produces = "application/json")
    public ResponseEntity<Map<String, Long>> strategyTraderCount() throws Exception {
        Long approvedCnt = strategyRepository.countByIsApproved("Y");
        Long traderCnt = memberRepository.countBymemberGradeCode("MEMBER_ROLE_TRADER");

        Map<String, Long> responseMap = new HashMap<>();
        responseMap.put("strategyCnt", approvedCnt);
        responseMap.put("traderCnt", traderCnt);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    // 4. 전략 상세 - 전략 기본정보
    @Operation(summary = "전략 상세")
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map> getStrategyById(@PathVariable("id") Long strategyId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
        //팔로워 조회용 - 미로그인시 null
        String memberId = userDetails != null ? userDetails.getMemberId() : null;

        //전략 기본정보 데이터를 가져온다.
        StrategyResponseDto strategyResponseDto = strategyService.getStrategyDetails(strategyId, memberId);

        //isPosted=N or isApproved=N인 경우 관리자와 작성트레이더만 볼 수 있다.
        if(strategyResponseDto.getIsPosted().equals("N") || strategyResponseDto.getIsApproved().equals("N")){
            //비로그인 상태인 경우
            if (userDetails == null) {
                throw new AccessDeniedException("조회 권한이 없습니다.");
            }

            //관리자인 경우 검증없이 if문 종료
            Boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if(isAdmin) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("data", strategyResponseDto);
                return ResponseEntity.status(HttpStatus.OK).body(responseMap);
            }

            //트레이더면 작성자 검증
            Boolean isTrader = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

            //로그인한 일반 회원인 경우
            if(!isTrader)
                throw new AccessDeniedException("비공개 전략은 작성자와 관리자만 조회할 수 있습니다.");

            //트레이더면서 작성자가 아닌 경우
            if(!userDetails.getMemberId().equals(strategyResponseDto.getMemberId()))
                throw new AccessDeniedException("전략 조회 권한이 없습니다.");
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", strategyResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    // 5. 전략 삭제
    // 관리자와 작성 트레이더만 삭제할 수 있다.
    @Operation(summary = "전략 삭제")
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map> deleteStrategy(@PathVariable("id") Long strategyId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        strategyService.deleteStrategy(strategyId, memberId, isTrader);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("msg", "DELETE_SUCCESS");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    //6. 전략 수정 페이지(GET)
    // 관리자와 작성 트레이더만 수정할 수 있다.
    @Operation(summary = "전략 수정페이지에 필요한 정보 반환")
    @GetMapping(value = "/{id}/update-form", produces = "application/json")
    public ResponseEntity<Map> updateStrategyForm(@PathVariable("id") Long strategyId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        Map<String, Object> dataMap = strategyService.getStrategyUpdateForm(strategyId, memberId, isTrader);

        return ResponseEntity.status(HttpStatus.OK).body(dataMap);
    }

    //7. 전략 수정(PUT)
    // 관리자와 작성 트레이더만 수정할 수 있다.
    @Operation(summary = "전략 수정")
    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map> updateStrategy(@PathVariable("id") Long strategyId, @RequestBody StrategyPayloadDto strategyPayloadDto,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        Map<String, Long> dataMap = strategyService.updateStrategy(strategyId, memberId, isTrader, strategyPayloadDto);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "UPDATE_SUCCESS");
        responseMap.put("data", dataMap);
        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    //8. 전략 승인 요청(POST)
    // 관리자와 작성 트레이더만 요청할 수 있다.
    @Operation(summary = "전략 승인 요청")
    @PostMapping(value = "/{id}/approval-request", produces = "application/json")
    public ResponseEntity<Map> requestStrategyApproval(@PathVariable("id") Long strategyId,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        Map<String, Long> dataMap = strategyService.approvalRequest(strategyId, memberId, isTrader);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");
        responseMap.put("data", dataMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    //8-1. 전략 승인 요청 거절 정보(GET)
    // 해당 전략 작성자와 관리자만 볼 수 있다.
    @Operation(summary = "해당 전략의 거절 정보를 반환")
    @GetMapping(value = "/{id}/rejection-info", produces = "application/json")
    public ResponseEntity<Map> rejectionInfo(@PathVariable("id") Long strategyId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        Map<String, Object> dataMap = strategyService.findRejectionByStrategyId(strategyId, memberId, isTrader);
        return ResponseEntity.status(HttpStatus.OK).body(dataMap);
    }

    //9. 전략 운용 종료(PATCH)
    //관리자와 작성 트레이더만 운용종료할 수 있다.
    @Operation(summary = "전략 운용 종료")
    @PatchMapping(value="/{id}/termination", produces = "application/json")
    public ResponseEntity<Map> terminateStrategy(@PathVariable("id") Long strategyId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        Map<String, Long> dataMap = strategyService.terminateStrategy(strategyId, memberId, isTrader);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "TERMINATE_SUCCESS");
        responseMap.put("data", dataMap);
        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    // 10. 전략 수기 데이터 등록
    @Operation(summary = "전략 수기 데이터 등록", description = "날짜, 입출금, 일손익을 최대 5행까지 입력받아 전략 데이터를 등록합니다.")
    @PostMapping(value = "/{id}/daily-data", produces = "application/json")
    public ResponseEntity<Map<String, Object>> registerManualDailyData(
            @PathVariable("id") Long strategyId,
            @RequestBody @Valid DailyDataPayloadDto payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        StrategyEntity strategyEntity = strategyRepository.findById(strategyId).orElseThrow(
                () -> new NoSuchElementException("Strategy not found"));

        // 트레이더면 작성자 판별
        if(isTrader && !strategyEntity.getWriterId().equals(memberId)) {
            throw new AccessDeniedException("수기 데이터 등록 권한이 없습니다.");
        }

        // 1. 데이터 유효성 검사
        // 수기 데이터가 비어있는지 확인
        if (payload.getPayload() == null || payload.getPayload().isEmpty()) {
            // 수기 데이터가 없으면 BAD_REQUEST 상태로 오류 응답
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수기 데이터는 최소 1개 이상 필요합니다.");
        }
        // 수기 데이터가 5개를 초과하는지 확인
        if (payload.getPayload().size() > 5) {
            // 5개를 초과하면 BAD_REQUEST 상태로 오류 응답
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수기 데이터는 최대 5개까지 등록 가능합니다.");
        }

        // 2. 수기 데이터를 저장
        // 수기 데이터를 하나씩 처리하여 저장
        payload.getPayload().forEach(entry -> {
            /// 각 데이터 항목을 기반으로 수기 데이터를 처리하는 서비스 메서드 호출
            dailyStatisticsService.registerDailyStatistics(
                    strategyId,  // 전략 ID를 서비스 메서드에 전달
                    DailyStatisticsReqDto.builder()
                            .date(entry.getDate())  // 수기 데이터의 날짜
                            .dailyProfitLoss(entry.getDailyProfitLoss())  // 일손익
                            .depWdPrice(entry.getDepWdPrice())  // 입출금 금액
                            .build()
            );
        });

        // 3. 응답 데이터 구성
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");
        responseMap.put("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    //11. 전략 수기 데이터 수정

    /**
     * 전략 수기 데이터 수정 API
     *
     * @param strategyId  수정할 전략 ID
     * @param dailyDataId 수정할 데이터 ID
     * @param reqDto      수정 요청 데이터 (날짜, 입출금, 일손익)
     * @return 성공 메시지
     */
    @Operation(summary = "전략 수기 데이터 수정", description = "수정된 날짜 이후 데이터까지 재등록하여 지표를 갱신합니다.")
    @PutMapping("/{strategyId}/daily-data/{dailyDataId}")
    public ResponseEntity<Map<String, String>> updateDailyData(
            @PathVariable Long strategyId,
            @PathVariable Long dailyDataId,
            @RequestBody DailyStatisticsReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        // 서비스 호출하여 수정 로직 수행
        dailyStatisticsService.updateDailyData(strategyId, dailyDataId, memberId, isTrader, reqDto);

        // 성공 응답
        Map<String, String> response = Map.of("msg", "UPDATE_SUCCESS");
        return ResponseEntity.ok(response);
    }


    //13. 전략 수기 데이터 삭제
    /**
     * 특정 전략의 일간 분석 데이터를 삭제하고 필요한 데이터를 재계산합니다.
     *
     * @param strategyId        삭제할 데이터가 포함된 전략의 ID
     * @param requestDto        삭제할 일간 분석 데이터 ID 리스트
     * @return 삭제 및 재계산 결과
     */
    @Operation(
            summary = "특정 전략의 일간 분석 데이터 삭제",
            description = "특정 전략의 일간 분석 데이터를 삭제하고, 삭제된 데이터 이후의 데이터들을 재계산합니다."
    )
    @PostMapping("/{strategyId}/daily-analyses/delete")
    public ResponseEntity<?> deleteDailyAnalyses(
            @PathVariable Long strategyId,
            @RequestBody DeleteDailyStatisticsRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        //접속자 정보
        String memberId = userDetails.getMemberId();
        Boolean isTrader = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRADER"));

        // 요청 데이터에서 ID 리스트 추출
        List<Long> dailyStatisticsIds = requestDto.getDailyStatisticsId();

        // 서비스 호출: 삭제 및 재계산
        dailyStatisticsService.deleteAndRecalculate(strategyId, memberId, isTrader, dailyStatisticsIds);

        // 성공 응답 반환
        return ResponseEntity.ok(Map.of(
                "msg", "DELETE_SUCCESS",
                "timestamp", Instant.now()
        ));
    }

    //14. 일간 분석 데이터 목록 조회
    //isPosted = N or isApproved = N인 경우 작성자와 관리자만 조회할 수 있다.
    /**
     * 특정 전략의 일간 분석 데이터를 최신일자순으로 페이징하여 반환합니다.
     *
     * @param strategyId 전략 ID.
     * @param page       페이지 번호 (기본값: 0).
     * @param pageSize   페이지 크기 (기본값: 5).
     * @return 페이징된 일간 통계 데이터를 포함한 Map.
     */
    @Operation(
            summary = "특정 전략의 일간 분석 데이터 조회",
            description = "특정 전략 ID에 대한 일간 분석 데이터를 최신일자순으로 페이징하여 반환합니다."
    )
    @GetMapping("/{strategyId}/daily-analyses")
    public ResponseEntity<Map<String, Object>> getDailyAnalyses(
            @PathVariable Long strategyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        // 서비스에서 페이징된 결과를 가져옴
        Page<DailyStatisticsResponseDto> result = dailyStatisticsService.getDailyStatisticsByStrategy(strategyId, page, pageSize);

        // CreatePageResponse 유틸리티를 사용하여 결과를 Map 형태로 변환
        Map<String, Object> response = CreatePageResponse.createPageResponse(result);

        // 변환된 Map 반환
        return ResponseEntity.ok(response);
    }

    //15. 전략 통계
    /**
     * 전략 통계를 반환합니다.
     *
     * @param strategyId 전략 ID
     * @return 전략 통계 데이터
     */
    @Operation(
            summary = "특정 전략의 통계 데이터 조회",
            description = "특정 전략 ID에 대한 최신 통계 데이터를 반환합니다."
    )
    @GetMapping("/{strategyId}/statistics")
    public ResponseEntity<Map<String, Object>> getStrategyStatistics(
            @PathVariable Long strategyId) {
        // 서비스 호출: Map<String, Object> 형태의 통계 데이터 반환
        Map<String, Object> statistics = dailyStatisticsService.getDailyStatistics(strategyId);

        // 응답 데이터 포맷
        Map<String, Object> response = Map.of(
                "data", statistics,
                "timestamp", Instant.now().toString() // 현재 타임스탬프 추가
        );

        return ResponseEntity.ok(response); // HTTP 200 상태로 응답 반환
    }

    //16. 필터링
    /**
     * 16-1. 전략 상세 필터링
     *
     * @param investmentAssetClassesList   투자자산 분류 id 목록(1,2,3)
     * @param strategyOperationStatusList  전략 운용 상태 코드 목록
     * @param tradingTypeList              매매유형 id 목록
     * @param operationDaysList            총운용일수 목록
     * @param tradingCycleList             매매주기 id 목록
     * @param minInvestmentAmount          최소운용가능금액
     * @param minPrincipal                 원금 필터 최소값
     * @param maxPrincipal                 원금 필터 최대값
     * @param minSmscore                   SM-score 필터 최소값
     * @param maxSmscore                   SM-score 필터 최대값
     * @param minMdd                       Mdd 필터 최소값
     * @param maxMdd                       Mdd 필터 최대값
     * @param startDate                    수익률 기간 시작일
     * @param EndDate                      수익률 기간 종료일
     * @param returnRateList               수익률 범위 목록
     * @param page                         현재 페이지
     * @param pageSize                     페이지 사이즈
     *
     * @return 필터링된 전략 리스트, 필터링되는 전략 갯수
     */
    @GetMapping("/advanced-search")
    @Operation(summary = "필터링 적용한 전략 목록 조회")
    public ResponseEntity<Map<String, Object>> advancedSearchStrategies(
            @RequestParam(required = false) String investmentAssetClassesList,
            @RequestParam(required = false) String strategyOperationStatusList,
            @RequestParam(required = false) String tradingTypeList,
            @RequestParam(required = false) String operationDaysList,
            @RequestParam(required = false) String tradingCycleList,
            @RequestParam(required = false) String minInvestmentAmount,
            @RequestParam(required = false) BigDecimal minPrincipal,
            @RequestParam(required = false) BigDecimal maxPrincipal,
            @RequestParam(required = false) Integer minSmscore,
            @RequestParam(required = false) Integer maxSmscore,
            @RequestParam(required = false) BigDecimal minMdd,
            @RequestParam(required = false) BigDecimal maxMdd,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate EndDate,
            @RequestParam(required = false) String returnRateList,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "30") @Min(1) Integer pageSize)
    {
        //쿼리파라미터 옵션들 Dto에 담기
        SearchOptionsPayloadDto optionsPayload = new SearchOptionsPayloadDto();
        optionsPayload.setInvestmentAssetClassesIdList(investmentAssetClassesList);
        optionsPayload.setStrategyOperationStatusList(strategyOperationStatusList);
        optionsPayload.setTradingTypeIdList(tradingTypeList);
        optionsPayload.setOperationDaysList(operationDaysList);
        optionsPayload.setTradingCylcleIdList(tradingCycleList);
        optionsPayload.setMinInvestmentAmount(minInvestmentAmount);
        optionsPayload.setMinPrincipal(minPrincipal);
        optionsPayload.setMaxPrincipal(maxPrincipal);
        optionsPayload.setMinSmscore(minSmscore);
        optionsPayload.setMaxSmscore(maxSmscore);
        optionsPayload.setMinMdd(minMdd);
        optionsPayload.setMaxMdd(maxMdd);
        optionsPayload.setStartDate(startDate);
        optionsPayload.setEndDate(EndDate);
        optionsPayload.setReturnRateList(returnRateList);
        optionsPayload.setKeyword(keyword);

        //상세 검색 실행
        Map<String, Object> resultData = strategyService.advancedSearch(optionsPayload, page, pageSize);

        //가변맵으로 변경
        Map<String, Object> responseData = new HashMap<>(resultData);
        responseData.put("keyword", keyword);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }


    /**
     * 16-2. 전략 키워드 검색.
     *
     * @param keyword                              검색 키워드
     * @return ResponseEntity<Map<String, Object>> 검색 결과 전략 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "키워드를 입력하여 전략명을 검색하는 메서드")
    public ResponseEntity<Map<String, Object>> searchStrategy(@RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "0") Integer page,
                                                              @RequestParam(defaultValue = "6") Integer pageSize){
        Map<String, Object> responseData = strategyService.getStrategyListByKeyword(keyword, page, pageSize);

        //불변맵을 가변맵으로 변환
        responseData = new HashMap<>(responseData);
        responseData.put("keyword", keyword);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    // 17. 월간 분석 목록
    /**
     * 전략의 월간 분석 목록을 페이징 처리하여 반환하는 API.
     *
     * @param strategyId 전략 ID
     * @param page       페이지 번호 (기본값: 0)
     * @param pageSize   페이지 크기 (기본값: 5)
     * @return 월간 분석 데이터가 담긴 페이징 응답
     */
    @Operation(
            summary = "특정 전략의 월간 분석 데이터 조회",
            description = "특정 전략 ID에 대한 월간 분석 데이터를 최신 월 순으로 페이징하여 반환합니다."
    )
    @GetMapping("/{strategyId}/monthly-analysis")
    public Map<String, Object> getMonthlyAnalysis(
            @PathVariable Long strategyId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) int pageSize) {

        // 월간 분석 서비스 호출 및 결과 반환
        return monthlyStatisticsService.getMonthlyAnalysis(strategyId, page, pageSize);
    }

    // 18. 전략 상세 차트 조회 API
    /**
     * 전략 상세 차트 데이터를 조회하는 API
     *
     * @param strategyId 차트 데이터를 조회할 전략 ID
     * @param option1 첫 번째 데이터 옵션 (예: "referencePrice", "balance" 등)
     * @param option2 두 번째 데이터 옵션 (예: "dailyProfitLoss", "cumulativeProfitLoss" 등)
     * @return ResponseEntity<DailyStatisticsChartResponseDto> (전략 차트 데이터와 타임스탬프)
     */
    @Operation(
            summary = "전략 상세 차트 데이터 조회",
            description = "특정 전략 ID와 선택된 데이터 옵션(예: referencePrice, balance 등)을 날짜순으로 조회합니다."
    )
    @GetMapping("/{id}/details-chart")
    public ResponseEntity<DailyStatisticsChartResponseDto> getStrategyChartDetails(
            @PathVariable("id") Long strategyId,
            @RequestParam("option1") String option1,
            @RequestParam("option2") String option2) {
        // Service에서 데이터와 타임스탬프 포함한 DTO 생성
        DailyStatisticsChartResponseDto responseDto = strategyService.getStrategyChartDetails(strategyId, option1, option2);

        // 바로 반환
        return ResponseEntity.ok(responseDto);
    }

    // 19. SM Score 기반 상위 5개 전략 리스트
    @Operation(
            summary = "SM SCORE 상위 5개 전략 조회",
            description = "SM SCORE 기준 상위 5개의 전략 리스트를 반환합니다. 응답에는 전략 ID, 전략명, 작성자 프로필, 작성자 닉네임, 전일대비, 누적 수익률의 정보가 포함됩니다."
    )
    @GetMapping("/top5-sm-score")
    public ResponseEntity<Map<String, Object>> getTop5SmScoreStrategies() {
        Map<String, Object> response = strategyService.getSmScoreTop5Strategies();
        return ResponseEntity.ok(response);
    }
}
