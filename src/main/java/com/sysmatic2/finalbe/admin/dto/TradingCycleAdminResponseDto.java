package com.sysmatic2.finalbe.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TradingCycleAdminResponseDto {
    private Integer tradingCycleId; // 투자주기 ID
    private Integer tradingCycleOrder; // 투자주기 순서
    private String tradingCycleName; // 투자주기명
    private String tradingCycleIcon; // 투자주기 아이콘 이미지 URL
    private String tradingCycleDescription; // 투자주기 설명
    private String isActive; // 사용유무
}