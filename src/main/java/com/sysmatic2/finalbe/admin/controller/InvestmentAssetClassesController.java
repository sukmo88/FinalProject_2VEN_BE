package com.sysmatic2.finalbe.admin.controller;

import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesDto;
import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesPayloadDto;
import com.sysmatic2.finalbe.admin.service.InvestmentAssetClassesService;
import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/api/admin/inv-asset-classes")
@RequiredArgsConstructor
@Tag(name = "Admin InvestmentAssetClasses Controller", description = "관리자가 투자자산 분류를 관리하는 컨트롤러")
@Validated
public class InvestmentAssetClassesController {
    private final InvestmentAssetClassesService iacService;
    private final FileService fileService;

    //1. 투자자산분류 목록 - pagination
    @Operation(summary = "투자자산 분류 목록")
    @GetMapping(value="", produces="application/json")
    @ApiResponse(responseCode="200", description = "List of Investment Asset Classes")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> getAllInvestmentAssetClasses(@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
                                                             @RequestParam(value = "pageSize", defaultValue = "10") @Positive(message = "Page size must be greater than zero") int pageSize) throws Exception{
        //TODO)관리자 판별
        Map pageList = iacService.getList(page, pageSize);

        return ResponseEntity.status(HttpStatus.OK).body(pageList);
    }

    //1-1. 투자자산분류 상세
    @Operation(summary = "투자자산 분류 상세")
    @GetMapping(value="/{id}", produces="application/json")
    @ApiResponse(responseCode="200", description = "Return Investment Asset Classes find by Id")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="404", description = "NOT EXIST")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> getInvestmentAssetClasses(@PathVariable("id") @Positive Integer id) throws Exception {
        //TODO)관리자 판별
        InvestmentAssetClassesDto iasDto = iacService.getById(id);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", iasDto);

        FileMetadataDto iconMetadata = fileService.getFileMetadataByFilePath(iasDto.getInvestmentAssetClassesIcon());
        // 이미지 displayname 조회 및 response 추가
        responseMap.put("displayName", iconMetadata.getDisplayName());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    //1-2. 투자자산 분류 is_active Y 리스트(테스트)
//    @GetMapping(value="/isactive")
//    public ResponseEntity<Map> getIACActive() throws Exception {
//        List<InvestmentAssetClassesDto> iasResult = iacService.getActiveList();
//        Map<String, Object> responseMap = new HashMap<>();
//        responseMap.put("data", iasResult);
//        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
//    }

    //2. 투자자산분류 등록
    @Operation(summary = "투자자산 분류 등록")
    @PostMapping(value="", consumes = "application/json", produces="application/json")
    @ApiResponse(responseCode="201", description = "Register inv asset classes")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> addInvestmentAssetClass(@Valid @RequestBody InvestmentAssetClassesPayloadDto iacPayloadDto) throws Exception {
        //TODO)관리자 판별
        //데이터 저장
        iacService.register(iacPayloadDto);

        //해쉬맵에 성공 메시지 저장
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("msg", "CREATE_SUCCESS");

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    //3-1. 투자자산분류 삭제
    @Operation(summary = "투자자산 분류 삭제")
    @DeleteMapping(value="/{id}")
    @ApiResponse(responseCode="200", description = "Delete Investment Asset Classes by Id")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="404", description = "NOT EXIST")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> deleteInvestmentAssetClass(@PathVariable("id") @Positive Integer id) throws Exception {
        //TODO) 접속한 사람의 토큰,권한 확인하기
        String memberId = "71-88RZ_QQ65hMGknyWKLA";

        iacService.delete(id, memberId);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("msg", "DELETE_SUCCESS");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    //4. 투자자산분류 수정
    @Operation(summary = "투자자산 분류 수정")
    @PutMapping(value="/{id}")
    @ApiResponse(responseCode="200", description = "Delete Investment Asset Classes by Id")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="404", description = "NOT EXIST")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> updateInvestmentAssetClass(@PathVariable("id") @Positive Integer id,
                                                          @Valid @RequestBody InvestmentAssetClassesPayloadDto iacPayloadDto) throws Exception {
        //TODO) 관리자 판별
        iacService.update(id, iacPayloadDto);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("msg", "UPDATE_SUCCESS");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }
}
