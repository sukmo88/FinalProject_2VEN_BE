package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.StandardCodeEntity;
import com.sysmatic2.finalbe.strategy.dto.StrategyPayloadDto;
import com.sysmatic2.finalbe.strategy.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.TradingTypeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StrategyService {
    //1. 전략 등록
    // 1) 전략테이블 -  매매유형ID, 전략상태공통코드(운용중), 주기공통코드, 최소운용가능금액, Follower수(0), 전략명, 작성자ID, 공개여부(N), 승인여부(N), 작성일지(now), 전략소개, 총전략운용일수(1)
    // 1-1) 전략 테이블에 save(통계값은 null)
    // 1-2) 제안서 테이블 저장(추후 구현)
    // 2) 전략 내 투자자산 분류 테이블 - 전략ID(전략테이블 생성된 ID), 투자자산분류ID(전략테이블에서 가져오기), 사용유무(Y), 작성일시, 수정일시
    // 2-1) 전략 내 투자자산 분류 테이블 save
    @Transactional
    public void createStrategy(StrategyPayloadDto strategyPayloadDto) throws Exception{
        //전략 엔티티 객체 생성
        StrategyEntity strategyEntity = new StrategyEntity();
        //매매유형, 투자자산분류 객체 생성
        TradingTypeEntity tradingTypeEntity = new TradingTypeEntity();
        InvestmentAssetClassesEntity iacEntity = new InvestmentAssetClassesEntity();
        //공통코드 객체 생성
        StandardCodeEntity standardCodeEntity = new StandardCodeEntity();




        //받아온 payloadDTO 값을 엔티티 객체에 저장한다.
        //



    }
}

//이미지 링크는 이미지 링크+{imageId}의 형태라서 imageId만 DB에 저장