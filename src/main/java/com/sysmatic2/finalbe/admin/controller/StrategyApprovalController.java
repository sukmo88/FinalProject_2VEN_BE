package com.sysmatic2.finalbe.admin.controller;

import com.sysmatic2.finalbe.admin.service.StrategyApprovalRequestsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/approval-requests")
@RequiredArgsConstructor
@Tag(name = "Admin strategy Approval Controller", description = "관리자 승인 요청 관리 컨트롤러")
@Validated
public class StrategyApprovalController {
    private final StrategyApprovalRequestsService strategyApprovalRequestsService;

    //1. 전략 승인 요청 목록 - pagination(page size = 10)
    //TODO) 관리자만 확인할 수 있다.
    @Operation(summary = "전략 승인 요청 목록")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<Map> getAllStrategyApprovalRequests(@RequestParam(value = "page", defaultValue = "0")
                                                                  @Min(value = 0, message = "Page number must be 0 or greater") int page,
                                                              @RequestParam(value = "pageSize", defaultValue = "10")
                                                              @Positive(message = "Page size must be greater than zero") int pageSize) throws Exception {

        Map pageList = strategyApprovalRequestsService.getList(page, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(pageList);
    }

    //2. 전략 승인 api
    //TODO) 관리자만 전략 승인할 수 있다.
    @Operation(summary = "전략 승인 요청 승인")
    @PatchMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, String>> approveStrategy(@PathVariable("id") Long id){
        //TODO) 접속자 토큰 권한 판별
        String adminId = "4w_qd34STqeIAd7fndHLf4";

        strategyApprovalRequestsService.approveStrategy(id, adminId);

        Map<String, String> response = new HashMap<>();
        response.put("msg", "APPROVAL_SUCCESS");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //3. 전략 승인 반려 api
    //TODO) 관리자만 전략 승인 반려할 수 있다.
    @Operation(summary = "전략 승인 요청 반려")
    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, String>> rejectStrategy(@PathVariable("id") Long requestId, @RequestBody String rejectionReason){
        //TODO)접속자 토큰 권한 판별
        String adminId = "4w_qd34STqeIAd7fndHLf4";

        strategyApprovalRequestsService.rejectStrategy(requestId, adminId, rejectionReason);

        Map<String, String> response = new HashMap<>();
        response.put("msg", "REJECTION_SUCCESS");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
