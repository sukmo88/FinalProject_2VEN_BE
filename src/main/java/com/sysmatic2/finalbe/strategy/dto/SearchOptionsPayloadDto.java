package com.sysmatic2.finalbe.strategy.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SearchOptionsPayloadDto {
    //프론트에서 날리는 옵션 쿼리파라미터를 받을 Dto
    @Pattern(regexp = "^\\d+(,\\d+)*$", message = "investment asset ids must contain numbers separated by commas (1,2,3...)")
    private String investmentAssetClassesIdList; //투자자산분류id 문자열 1,2,3
    @Pattern(regexp = "^\\s*\\w+(\\s*,\\s*\\w+)*\\s*$", message = "operation status ids must contain string separated by commas")
    private String strategyOperationStatusList;  //전략운용코드 STRATEGY_OPERATION_UNDER_MANAGEMENT,STRATEGY_OPERATION_TERMINATED
    @Pattern(regexp = "^\\d+(,\\d+)*$", message = "trading type ids must contain numbers separated by commas (1,2,3...)")
    private String tradingTypeIdList;            //매매유형id 문자열 1,2,3
    @Pattern(regexp = "^\\d+(,\\d+)*$", message = "operation status ids must contain numbers separated by commas (1,2,3...)")
    private String operationDaysList;            //총운용일수 목록 문자열 0,1,2,3
    @Pattern(regexp = "^\\d+(,\\d+)*$", message = "trading cycle ids must contain string separated by commas (1,2,3...)")
    private String tradingCylcleIdList;          //매매주기코드 목록 문자열 1,2

    private String minInvestmentAmount;          //최소운용가능금액

    @DecimalMin(value = "0", message = "Investment amount must be more than 0")
    @DecimalMax(value = "100000000000.0", message = "Investment amount must be less than 100,000,000,000")
    private BigDecimal minPrincipal;             //원금 필터 최소값
    @DecimalMin(value = "0", message = "Investment amount must be more than 0")
    @DecimalMax(value = "100000000000.0", message = "Investment amount must be less than 100,000,000,000")
    private BigDecimal maxPrincipal;             //원금 필터 최대값

    @Min(0)
    @Max(100)
    private Integer minSmscore;                  //SM-score 필터 최소값
    @Min(0)
    @Max(100)
    private Integer maxSmscore;                  //SM-score 필터 최대값

    @DecimalMax(value = "0", message = "MDD must be less than 0")
    @DecimalMin(value = "-100.0000", message = "MDD must be more than -100")
    private BigDecimal minMdd;                   //MDD(최대자본인하율) 필터 최소값
    @DecimalMax(value = "0", message = "MDD must be less than 0")
    @DecimalMin(value = "-100.0000", message = "MDD must be more than -100")
    private BigDecimal maxMdd;                   //MDD(최대자본인하율) 필터 최대값

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;                 //기간 시작일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;                   //기간 종료일
    @Pattern(regexp = "^\\d+(,\\d+)*$", message = "returnRate ids must contain numbers separated by commas (1,2,3...)")
    private String returnRateList;               //수익률 리스트

    //원금 최대값 >= 원금 최소값 검증
    @AssertTrue(message = "maxPrincipal must be greater or equal to minPrincipal")
    public boolean isPrincipalRangeValid(){
        if(minPrincipal==null || maxPrincipal==null){
            return true;
        }
        return minPrincipal.compareTo(maxPrincipal)<=0;
    }

    //Sm-score 최대값 >= Sm-score 최소값
    @AssertTrue(message = "maxSmscore must be greater or equal to minSmscore")
    public boolean isSmscoreRangeValid(){
        if(minSmscore==null || maxSmscore==null){
            return true;
        }
        return minSmscore.compareTo(maxSmscore)<=0;
    }

    //maxMdd 최대값 >= MinMdd 최소값
    @AssertTrue(message = "minMdd must be greater or equal to maxMdd")
    public boolean isMddRangeValid(){
        if(minMdd==null || maxMdd==null){
            return true;
        }
        return minMdd.compareTo(maxMdd)<=0;
    }

    //시작일이 종료일보다 이전인지 판별
    @AssertTrue(message = "end date must be the same or after start date")
    public boolean isDateRangeValid(){
        if(startDate==null || endDate==null){
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    // startDate와 endDate가 있으면 returnRateList가 비어있지 않아야 함
    // returnRateList가 있으면 startDate와 endDate가 모두 비어있지 않아야 함
    @AssertTrue(message = "If startDate and endDate are provided, returnRateList must not be empty, and vice versa.")
    public boolean isDateAndReturnRateValid() {
        boolean isDateProvided = (startDate != null && endDate != null);
        boolean isReturnRateProvided = (returnRateList != null && !returnRateList.trim().isEmpty());

        // 둘 중 하나라도 있으면 서로 비어있으면 안 됨
        return !(isDateProvided && !isReturnRateProvided || !isDateProvided && isReturnRateProvided);
    }

}
