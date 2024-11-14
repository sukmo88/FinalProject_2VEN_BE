package com.sysmatic2.finalbe.strategy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
public class StrategyFormDto {
    @NotBlank(message = "전략명은 필수 입력 값입니다.")
    private String strategyTitle; // 전략명

    @NotNull(message = "매매유형 ID는 필수 입력 값입니다.")
    private Integer tradingTypeId; // 매매유형 ID

    @NotNull(message = "매매주기 코드 ID는 필수 입력 값입니다.")
    private String tradingCycleCode; // 매매주기코드(공통 코드)

    @NotNull(message = "원금은 필수 입력 값입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "원금은 0이상이어야 합니다.")
    private BigDecimal principal; // 원금

    @NotBlank(message = "전략 소개는 필수 입력 값입니다.")
    private String strategyOverview; // 전략소개

    @NotEmpty(message = "투자자산 분류 ID 목록은 비어 있을 수 없습니다.")
    private List<@NotNull(message = "투자자산 분류 ID는 null일 수 없습니다.") Integer> InvestmentAssetClassesIdList; //투자자자산 분류 id 목록

    @NotBlank(message = "공개 여부는 필수 입력 값입니다.")
    private String isPosted; // 공개여부

    @NotNull(message = "전략제안서 링크는 필수 입력 값입니다.")
    private String strategyProposalLink; // 전략제안서 링크
}