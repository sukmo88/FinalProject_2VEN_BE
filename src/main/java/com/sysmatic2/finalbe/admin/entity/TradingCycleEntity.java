package com.sysmatic2.finalbe.admin.entity;

import com.sysmatic2.finalbe.common.Auditable;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "trading_cycle")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TradingCycleEntity extends Auditable {
    @Id
    @Column(name="trading_cycle_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tradingCycleId; // 투자주기 ID

    @Column(name="trading_cycle_order", nullable = false,unique = true)
    private Integer tradingCycleOrder; // 투자주기순서

    @Column(name="trading_cycle_name",nullable = false, unique = true)
    private String tradingCycleName; // 투자주기명

    @Column(name="trading_cycle_icon", nullable = false)
    private String tradingCycleIcon; // 투자주기아이콘

    @Column(name="trading_cycle_description")
    private String tradingCycleDescription; // 투자주기 설명

    @Column(name="is_active", nullable = false)
    private String isActive; // 사용유무

    //전략(N) : 주기(1)
//    @OneToMany(mappedBy = "tradingCycleEntity")
//    private List<StrategyEntity> strategyEntities;
}
