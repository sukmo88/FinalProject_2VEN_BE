package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import jakarta.validation.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDataPayloadDto {
    @Valid
    @Size(max = 5, message = "수기 데이터는 최대 5개까지 등록 가능합니다.")
    private List<DailyStatisticsReqDto> payload; // 수기 데이터 목록
}