package com.sysmatic2.finalbe.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TradingCycleAdminRequestDto {

    @Positive(message = "투자주기 순서는 양수여야 합니다.")
    @Max(value = 100000000, message = "투자주기 순서는 100,000,000 이하만 가능합니다.")
    private Integer tradingCycleOrder; // 투자주기 순서

    @NotBlank(message = "투자주기명은 필수 입력 값입니다.")
    private String tradingCycleName; // 투자주기명

    @NotBlank(message = "투자주기 아이콘 URL은 필수 입력 값입니다.")
    private String tradingCycleIcon; // 투자주기 아이콘 이미지 URL

    @Pattern(regexp = "[YN]", message = "사용유무는 'Y' 또는 'N'만 허용됩니다.")
    private String isActive; // 사용유무
}