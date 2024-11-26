package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.admin.repository.StrategyApprovalRequestsRepository;
import com.sysmatic2.finalbe.strategy.dto.*;
import com.sysmatic2.finalbe.strategy.service.DailyStatisticsService;
import com.sysmatic2.finalbe.strategy.service.ExcelGeneratorService;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import com.sysmatic2.finalbe.util.CreatePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.core.io.InputStreamResource; // 추가
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Strategy Controller", description = "전략 컨트롤러")
public class StrategyController {
    private final StrategyService strategyService;
    private final StrategyApprovalRequestsRepository strategyApprovalRequestsRepository;
    private final DailyStatisticsService dailyStatisticsService;

    // 1. 전략 생성페이지(GET)
    //TODO) 관리자와 트레이더만 수정할 수 있다.
    @Operation(summary = "전략 생성페이지 필요 정보")
    @GetMapping("/registration-form")
    @ApiResponse(responseCode = "200", description = "Get Strategy Registration Form")
    public ResponseEntity<Map<String, Object>> getStrategyRegistrationForm() {
        //TODO) 전략 생성 권한 판별
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
    //TODO) 관리자와 트레이더만 수정할 수 있다.
    @Operation(summary = "전략 생성")
    @PostMapping(produces="application/json")
    public ResponseEntity<Map> createStrategy(@Valid @RequestBody StrategyPayloadDto strategyPayloadDto) throws Exception{
        //TODO) 접속자 토큰 권한 판별
        String adminId = "4w_qdODSTqeIAd7fndHLfg";

        //데이터 저장
        Map<String, Long> responseData = strategyService.register(strategyPayloadDto, adminId);

        //해쉬맵에 성공 메시지 저장
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");
        responseMap.put("data", responseData);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    // 3. 전략 목록
    /**
     * 3. 필터 조건에 따라 전략 목록 반환 (페이징 포함)
     *
     * @param tradingCycleId 투자주기 ID (nullable)
     * @param investmentAssetClassesId 투자자산 분류 ID (nullable)
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param pageSize 페이지당 데이터 개수
     * @return 필터링된 전략 목록 및 페이징 정보를 포함한 Map 객체
     */
    @GetMapping
    @Operation(summary = "필터 조건으로 전략 목록 조회",
            description = "투자주기와 투자자산 분류로 전략을 필터링하여 조회합니다. 페이징을 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전략 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 값"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Map<String, Object>> getStrategies(
            @RequestParam(required = false) @Positive(message = "tradingCycleId는 양수여야 합니다.") Integer tradingCycleId,
            @RequestParam(required = false) @Positive(message = "investmentAssetClassesId는 양수여야 합니다.") Integer investmentAssetClassesId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) int pageSize) {

        Map<String, Object> response = strategyService.getStrategies(tradingCycleId, investmentAssetClassesId, page, pageSize);

        // 200 OK 응답과 함께 반환
        return ResponseEntity.ok(response);
    }

    // 4. 전략 상세
    //TODO) isPosted=N인 경우 관리자와 작성트레이더만 볼 수 있다.
    @Operation(summary = "전략 상세")
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map> getStrategyById(@PathVariable("id") Long id) throws Exception{
        StrategyResponseDto strategyResponseDto = strategyService.getStrategyDetails(id);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", strategyResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    // 5. 전략 삭제
    //TODO) 관리자와 작성 트레이더만 삭제할 수 있다.
    @Operation(summary = "전략 삭제")
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map> deleteStrategy(@PathVariable("id") Long id) throws Exception{
        strategyService.deleteStrategy(id);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("msg", "DELETE_SUCCESS");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    //6. 전략 수정 페이지(GET)
    //TODO) 관리자와 작성 트레이더만 수정할 수 있다.
    @Operation(summary = "전략 수정페이지에 필요한 정보 반환")
    @GetMapping(value = "/update-form/{id}", produces = "application/json")
    public ResponseEntity<Map> updateStrategyForm(@PathVariable("id") Long id) throws Exception{
        Map<String, Object> dataMap = strategyService.getStrategyUpdateForm(id);

        return ResponseEntity.status(HttpStatus.OK).body(dataMap);
    }

    //7. 전략 수정(POST)
    //TODO) 관리자와 작성 트레이더만 수정할 수 있다.
    @Operation(summary = "전략 수정")
    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map> updateStrategy(@PathVariable("id") Long id, @RequestBody StrategyPayloadDto strategyPayloadDto) throws Exception{
        Map<String, Long> dataMap = strategyService.updateStrategy(id, strategyPayloadDto);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "UPDATE_SUCCESS");
        responseMap.put("data", dataMap);
        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    //8. 전략 승인 요청(POST)
    //TODO) 관리자와 작성 트레이더만 요청할 수 있다.
    @Operation(summary = "전략 승인 요청")
    @PostMapping(value = "/{id}/approval-request", produces = "application/json")
    public ResponseEntity<Map> requestStrategyApproval(@PathVariable("id") Long id) throws Exception{
        //TODO) 접속한 사람의 토큰 확인하기
        String applicantId = "71-88RZ_QQ65hMGknyWKLA";

        Map<String, Long> dataMap = strategyService.approvalRequest(id, applicantId);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");
        responseMap.put("data", dataMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    //8-1. 전략 승인 요청 거절 정보(GET)
    //TODO) 해당 전략 작성자와 관리자만 볼 수 있다.
    @Operation(summary = "해당 전략의 거절 정보를 반환")
    @GetMapping(value = "/{id}/rejection-info", produces = "application/json")
    public ResponseEntity<Map> rejectionInfo(@PathVariable("id") Long strategyId) throws Exception{
        //TODO) 접속한 사람의 토큰 확인하기

        Map<String, Object> dataMap = strategyService.findRequestByStrategyId(strategyId);
        return ResponseEntity.status(HttpStatus.OK).body(dataMap);
    }

    //9. 전략 운용 종료(PATCH)
    //TODO) 관리자와 작성 트레이더만 운용종료할 수 있다.
    @Operation(summary = "전략 운용 종료")
    @PatchMapping(value="/{id}/termination", produces = "application/json")
    public ResponseEntity<Map> terminateStrategy(@PathVariable("id") Long id) throws Exception{
        //TODO) 접속한 사람의 토큰 확인하기
        String applicantId = "71-88RZ_QQ65hMGknyWKLA";

        Map<String, Long> dataMap = strategyService.terminateStrategy(id, applicantId);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "TERMINATE_SUCCESS");
        responseMap.put("data", dataMap);
        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    // 10. 전략 수기 데이터 등록
    @Operation(summary = "전략 수기 데이터 등록", description = "날짜, 입출금, 일손익을 최대 5행까지 입력받아 전략 데이터를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "수기 데이터 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "전략 ID를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping(value = "/{id}/daily-data", produces = "application/json")
    public ResponseEntity<Map<String, Object>> registerManualDailyData(
            @PathVariable("id") Long strategyId,
            @RequestBody @Valid DailyDataPayloadDto payload) {

        // 디버깅 로그 추가
        System.out.println("Received strategyId: " + strategyId);

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
        List<Long> savedIds = payload.getPayload().stream().map(entry -> {
            try {
                /// 각 데이터 항목을 기반으로 수기 데이터를 처리하는 서비스 메서드 호출
                dailyStatisticsService.processDailyStatistics(
                        strategyId,  // 전략 ID를 서비스 메서드에 전달
                        DailyStatisticsReqDto.builder()
                                .date(entry.getDate())  // 수기 데이터의 날짜
                                .dailyProfitLoss(entry.getDailyProfitLoss())  // 일손익
                                .depWdPrice(entry.getDepWdPrice())  // 입출금 금액
                                .build()
                );

                return strategyId; // 실제로 저장된 데이터의 ID를 반환하도록 수정 가능
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.", e);
            }
        }).collect(Collectors.toList());

        // 3. 응답 데이터 구성
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");
        responseMap.put("data", savedIds.stream()
                .map(id -> Map.of("dailyDataId", id))
                .collect(Collectors.toList()));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    // 11. 일간 지표 다운로드
    @Operation(summary = "일간 지표 다운로드", description = "일간 지표를 엑셀 파일로 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "엑셀 파일 다운로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/download/daily-indicators")
    public ResponseEntity<byte[]> downloadDailyStatisticsExcel() {
        try {
            ByteArrayInputStream in = excelGeneratorService.generateDailyStatisticsExcel();
            byte[] bytes = in.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=daily_statistics.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    // 12. 일간 분석 지표 다운로드
    @Operation(summary = "일간 분석 지표 다운로드", description = "일간 분석 지표를 포함한 엑셀 파일을 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "엑셀 파일 다운로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/download/daily-analysis-indicators")
    public ResponseEntity<byte[]> downloadDailyAnalysisIndicatorsExcel() {
        try {
            ByteArrayInputStream in = excelGeneratorService.generateDailyAnalysisIndicatorsExcel();
            byte[] bytes = in.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=daily_analysis_indicators.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    // 13. 월간 지표 다운로드
    @Operation(summary = "월간 지표 다운로드", description = "월간 지표를 엑셀 파일로 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "엑셀 파일 다운로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/download/monthly-indicators")
    public ResponseEntity<byte[]> downloadMonthlyStatisticsExcel() {
        try {
            ByteArrayInputStream in = excelGeneratorService.generateMonthlyStatisticsExcel();
            byte[] bytes = in.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=monthly_statistics.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }
}
