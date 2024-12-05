package com.sysmatic2.finalbe.strategy.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SearchOptionsDto {
    //Service에서 Repository로 날리는 dto
    private List<Integer> investmentAssetClassesIdList; //투자자산 분류 ID 리스트
    private List<String>  strategyOperationStatusList;  //전략 운용 상태 코드 리스트
    private List<Integer> tradingTypeIdList;            //매매유형 ID 리스트
    private List<Integer> operationDaysList;            //총운용일수 리스트(0년,1년,2년,3년이상)
    private List<Integer>  tradingCylcleIdList;          //매매주기코드 리스트

    private String minInvestmentAmount;          //최소운용가능금액

    private BigDecimal minPrincipal;             //원금 필터 최소값
    private BigDecimal maxPrincipal;             //원금 필터 최대값

    private Integer minSmscore;                  //SM-score 필터 최소값
    private Integer maxSmscore;                  //SM-score 필터 최대값

    private BigDecimal minMdd;                   //MDD(최대자본인하율) 필터 최소값
    private BigDecimal maxMdd;                   //MDD(최대자본인하율) 필터 최대값

    private LocalDate startDate;                 //기간 시작일
    private LocalDate endDate;                   //기간 종료일
    private List<Integer> returnRateList;        //수익률 리스트

    private String keyword;                      //검색 키워드
}
