package com.sysmatic2.finalbe.admin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TradingCycleRegistrationDto {
    private Integer tradingCycleId; // 투자주기 ID
    private String tradingCycleName; // 투자주기명
    private String tradingCycleIcon; // 투자주기 아이콘 이미지 URL
}