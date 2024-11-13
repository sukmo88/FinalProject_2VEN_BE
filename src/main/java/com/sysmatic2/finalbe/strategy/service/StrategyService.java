package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.dto.StrategyCreateRequestDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyPayloadDto;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StrategyService {
    private final StrategyRepository strategyRepository;
    /* 1. 전략 등록
        1) 전략테이블 - 매매유형ID(자동/반자동/수동), 매매주기공통코드(데이/포지션), 전략명, 공개여부(N), 승인여부(N), 작성일지(now), 전략소개, 총전략운용일수(1)
            1-1) 전략 테이블에 save(통계값은 null)
            1-2) 제안서 테이블 저장(추후 구현)
            1-3) 전략상태공통코드(STRATEGY_STATUS_UNDER_MANAGEMENT)는 서비스에서 기본값 설정
            1-4) 팔로워수(0)는 엔티티에서 기본값 설정
        2) 전략 내 투자자산 분류 테이블 - 전략ID(전략테이블 생성된 ID), 투자자산분류ID(전략테이블에서 가져오기), 사용유무(Y), 작성일시, 수정일시
            2-1) 전략 내 투자자산 분류 테이블 save
    */
    @Transactional
    public void createStrategy(StrategyCreateRequestDto strategyCreateRequestDto) {

    }


    /* 2. 전략 수정 - 전략 데이터 등록
       1) 전략테이블 기본정보 수정
       2) 매매일지 등록/삭제 수정?
           2-1) 데일리 리뷰 등록/삭제 수정?
       3) 입출금 관리 등록/삭제 수정?
       4) 실계좌 인증 등록/삭제
       5) 제안서 등록/삭제
     */

    /* 3. 전략 삭제 - soft delete
       1) 전략 이력테이블 로그 저장 (삭제 상태코드)
       2) 전략 일간 데이터, 전략 월간 데이터, 전략 누적 데이터 이력 테이블 삭제 로그 등록
       3) 전략 일간 데이터, 전략 월간 데이터, 전략 누적 데이터 테이블 삭제
       3) 입출금 내역 이력, 매매일지 이력, 실계좌 정보 이력 테이블 삭제 로그 등록
       3) 입출금 내역, 매매일지, 실계좌 정보 테이블 삭제
       4) 주식잔고, 전략 제안서, 전략 리뷰 삭제
       5) 전략 테이블 데이터 삭제
       6) 전략내 투자자산 분류 테이블 삭제
     */

    //4. 전략 목록
    // 1) 전략 랭킹 목록
    //5. 전략 상세
    // 1) 전략 상세

}

//이미지 링크는 이미지 링크+{imageId}의 형태라서 imageId만 DB에 저장