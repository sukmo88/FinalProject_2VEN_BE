package com.sysmatic2.finalbe.common;

import com.sysmatic2.finalbe.common.StandardCodeEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyStandardCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StandardCodeRepositoryTest {
    @Autowired
    private StrategyStandardCodeRepository standardCodeRepository;

    @Test
    public void testData(){
        StandardCodeEntity scEntity1 = new StandardCodeEntity();
        scEntity1.setCode("STRATEGY_STATUS_UNDER_MANAGEMENT");
        scEntity1.setCodeTypeName("전략상태");
        scEntity1.setCodeName("UNDER_MANAGEMENT");
        scEntity1.setCodeDescription("전략 운용중");
        scEntity1.setSortOrder(Short.valueOf("1"));
        scEntity1.setIsUse("Y");
        standardCodeRepository.save(scEntity1);
        StandardCodeEntity scEntity2 = new StandardCodeEntity();
        scEntity2.setCode("STRATEGY_STATUS_TERMINATED");
        scEntity2.setCodeTypeName("전략상태");
        scEntity2.setCodeName("TERMINATED");
        scEntity2.setCodeDescription("전략 운용 종료");
        scEntity2.setSortOrder(Short.valueOf("2"));
        scEntity2.setIsUse("Y");
        standardCodeRepository.save(scEntity2);
        StandardCodeEntity scEntity3 = new StandardCodeEntity();
        scEntity3.setCode("STRATEGY_TRADINGCYCLE_DAY");
        scEntity3.setCodeTypeName("주기");
        scEntity3.setCodeName("DAY");
        scEntity3.setCodeDescription("데이");
        scEntity3.setSortOrder(Short.valueOf("1"));
        scEntity3.setIsUse("Y");
        standardCodeRepository.save(scEntity3);
        StandardCodeEntity scEntity4 = new StandardCodeEntity();
        scEntity4.setCode("STRATEGY_TRADINGCYCLE_POSITION");
        scEntity4.setCodeTypeName("주기");
        scEntity4.setCodeName("POSITION");
        scEntity4.setCodeDescription("포지션");
        scEntity4.setSortOrder(Short.valueOf("2"));
        scEntity4.setIsUse("Y");
        standardCodeRepository.save(scEntity4);

    }
}
