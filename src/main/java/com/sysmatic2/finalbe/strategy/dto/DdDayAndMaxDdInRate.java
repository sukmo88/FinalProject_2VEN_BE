package com.sysmatic2.finalbe.strategy.dto;

import java.math.BigDecimal;

public class DdDayAndMaxDdInRate {
    private final Integer ddDay;
    private final BigDecimal maxDdInRate;

    public DdDayAndMaxDdInRate(Integer ddDay, BigDecimal maxDdInRate) {
        this.ddDay = ddDay;
        this.maxDdInRate = maxDdInRate;
    }

    public Integer getDdDay() {
        return ddDay;
    }

    public BigDecimal getMaxDdInRate() {
        return maxDdInRate;
    }
}
