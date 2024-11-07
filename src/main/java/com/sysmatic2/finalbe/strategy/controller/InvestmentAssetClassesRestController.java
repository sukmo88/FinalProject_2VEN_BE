package com.sysmatic2.finalbe.strategy.controller;

import com.sysmatic2.finalbe.strategy.dto.InvestmentAssetClassesDto;
import com.sysmatic2.finalbe.strategy.dto.InvestmentAssetClassesPayloadDto;
import com.sysmatic2.finalbe.strategy.service.InvestmentAssetClassesService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.MethodNotAllowedException;

import java.util.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin InvestmentAssetClasses Controller", description = "관리자가 투자자산 분류를 관리하는 컨트롤러")
public class InvestmentAssetClassesRestController {
    private final InvestmentAssetClassesService iacService;

    //1. 투자자산분류 목록
    @GetMapping(value="/inv-asset-classes", produces="application/json")
    @ApiResponse(responseCode="200", description = "List of Investment Asset Classes")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> getAllInvestmentAssetClasses() {
        //TODO) 에러 처리, pagination(10)
        try {
            List<InvestmentAssetClassesDto> dtoList = iacService.getList();
            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("count", dtoList.size());
            responseMap.put("data", dtoList);

            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } catch(MethodNotAllowedException e){ //405
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "METHOD_NOT_ALLOWED");
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMap);
        } catch(Exception e){ //500
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    //1-1. 투자자산분류 상세
    @GetMapping(value="/inv-asset-classes/{id}", produces="application/json")
    @ApiResponse(responseCode="200", description = "Return Investment Asset Classes find by Id")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="404", description = "NOT EXIST")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> getInvestmentAssetClasses(@PathVariable("id") Integer id) {
        //TODO) 에러 처리
        try {
            InvestmentAssetClassesDto iasDto = iacService.getById(id);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", iasDto);

            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } catch(NoSuchElementException e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Investment_Asset_Classes_WITH_ID_" + id + "_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);
        } catch(MethodNotAllowedException e){ //405
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "METHOD_NOT_ALLOWED");
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMap);
        } catch(Exception e){ //500
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }


    //2. 투자자산분류 등록
    @PostMapping(value="/inv-asset-classes", consumes = "application/json", produces="application/json")
    @ApiResponse(responseCode="201", description = "Register inv asset classes")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> addInvestmentAssetClass(@RequestBody InvestmentAssetClassesPayloadDto iacPayloadDto){
        //TODO)에러처리
        //반환할 객체 만들기
        InvestmentAssetClassesDto iacDto = new InvestmentAssetClassesDto();

        //서비스 메서드 호출
        iacDto = iacService.register(iacPayloadDto);

        //해쉬맵에 저장
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", iacDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    //투자자산분류 삭제
    @DeleteMapping(value="/inv-asset-classes/{id}")
    @ApiResponse(responseCode="200", description = "Delete Investment Asset Classes by Id")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="404", description = "NOT EXIST")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> deleteInvestmentAssetClass(@PathVariable("id") Integer id){
        //TODO) 에러처리
        try{
            iacService.delete(id);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("msg", "DELETE_SUCCESS");
            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } catch(NoSuchElementException e){
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Investment_Asset_Classes_WITH_ID_" + id + "_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);
        } catch(Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }


    //투자자산분류 수정
    @PutMapping(value="/inv-asset-classes/{id}")
    @ApiResponse(responseCode="200", description = "Delete Investment Asset Classes by Id")
    @ApiResponse(responseCode="400", description = "Wrong Request URL")
    @ApiResponse(responseCode="401", description = "Unauthorized")
    @ApiResponse(responseCode="404", description = "NOT EXIST")
    @ApiResponse(responseCode="405", description = "Wrong Request Method")
    @ApiResponse(responseCode="500", description = "Other Errors")
    public ResponseEntity<Map> updateInvestmentAssetClass(@PathVariable("id") Integer id, @RequestBody InvestmentAssetClassesPayloadDto iacPayloadDto){
        try{
            iacService.update(id, iacPayloadDto);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("msg", "UPDATE_SUCCESS");
            return ResponseEntity.status(HttpStatus.OK).body(responseMap);
        } catch(NoSuchElementException e){
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Investment_Asset_Classes_WITH_ID_" + id + "_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);
        } catch(Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }
}
