package com.sysmatic2.finalbe.member.dto;

import com.sysmatic2.finalbe.strategy.dto.SearchOptionsDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class StrategyIdsDto extends SearchOptionsDto {
    List<Long> strategyIds;
}
