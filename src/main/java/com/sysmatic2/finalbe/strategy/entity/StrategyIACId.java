package com.sysmatic2.finalbe.strategy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StrategyIACId implements Serializable {
    //전략 - 투자자산 분류 복합키 클래스
    private Long strategyEntity;
    private Integer investmentAssetClassesEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrategyIACId that = (StrategyIACId) o;
        return Objects.equals(strategyEntity, that.strategyEntity) && Objects.equals(investmentAssetClassesEntity, that.investmentAssetClassesEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategyEntity, investmentAssetClassesEntity);
    }
}
