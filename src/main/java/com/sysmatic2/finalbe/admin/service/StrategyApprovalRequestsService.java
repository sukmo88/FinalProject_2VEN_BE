package com.sysmatic2.finalbe.admin.service;

import com.sysmatic2.finalbe.admin.dto.ApprovalRequestResponseDto;
import com.sysmatic2.finalbe.admin.entity.StrategyApprovalRequestsEntity;
import com.sysmatic2.finalbe.admin.repository.StrategyApprovalRequestsRepository;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyHistoryEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyIACRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.sysmatic2.finalbe.util.CreatePageResponse.createPageResponse;

@Service
@RequiredArgsConstructor
public class StrategyApprovalRequestsService {
    private final StrategyApprovalRequestsRepository strategyApprovalRequestsRepository;
    private final StrategyIACRepository strategyIACRepository;
    private final MemberRepository memberRepository;
    private final StrategyHistoryRepository strategyHistoryRepository;
    private final StrategyRepository strategyRepository;

    //1. 전략 승인 요청 목록
    // 주기, 매매유형, 투자자산 분류, 전략명, 운용여부, 요청일시, 공개여부
    @Transactional(readOnly = true)
    public Map<String, Object> getList(int page, int size){
        //현재 페이지, 페이지 사이즈, 요청 일시 최근순으로 내림차순 정렬 - pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDatetime").descending());

        //페이지 객체를 넣고 요청 엔티티 목록을 가져온다.
        Page<StrategyApprovalRequestsEntity> requestsEntityPage = strategyApprovalRequestsRepository.findAll(pageable);

        //엔티티 목록을 dto목록으로 변환한다.
        Page<ApprovalRequestResponseDto> requestResponseDtos = requestsEntityPage.map(DtoEntityConversionUtils::convertToApprovalDto);

        //Dto 목록을 돌면서 아이콘리스트를 추가해준다.
        for(ApprovalRequestResponseDto requestResponseDto : requestResponseDtos){
            //아이콘 리스트 생성
            List<String> iconList = new ArrayList<>();

            //전략 id로 관계테이블엔티티 리스트 가져온다.
            List<StrategyIACEntity> relationalEntity = strategyIACRepository.findByStrategyEntity_StrategyId(requestResponseDto.getStrategyId());
            //아이콘 리스트에 아이콘 링크를 저장한다.
            for(StrategyIACEntity strategyIACEntity : relationalEntity){
                iconList.add(strategyIACEntity.getInvestmentAssetClassesEntity().getInvestmentAssetClassesIcon());
            }

            //아이콘 리스트를 dto에 저장한다.
            requestResponseDto.setInvestmentAssetClassesIcons(iconList);
        }

        return createPageResponse(requestResponseDtos);
    }

    //2. 전략 승인 요청 수락
    @Transactional
    public void approveStrategy(Long id, String adminId){
        //관리자정보
        MemberEntity admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("관리자 정보가 존재하지 않습니다."));

        //해당 전략요청을 id로 가져온다.
        StrategyApprovalRequestsEntity requestEntity = strategyApprovalRequestsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당되는 승인 요청이 존재하지 않습니다."));

        //전략엔티티 도출
        StrategyEntity strategyEntity = requestEntity.getStrategy();

        //전략의 is_Approved값을 Y, 수정자, 수정일시를 등록한다.
        LocalDateTime changeStartDatetime = LocalDateTime.now(); //수정 시작시간
        strategyEntity.setIsApproved("Y");
        strategyEntity.setUpdaterId(adminId);
        strategyEntity.setUpdatedAt(LocalDateTime.now());

        //전략의 수정 이력을 기록한다.
        StrategyHistoryEntity strategyHistoryEntity = new StrategyHistoryEntity(strategyEntity, "STRATEGY_STATUS_UPDATED", changeStartDatetime);
        strategyHistoryRepository.save(strategyHistoryEntity);

        //전략 승인 요청 목록 테이블의 승인여부를 Y, 담당자ID, 승인일시를 등록한다.
        requestEntity.setIsApproved("Y");
        requestEntity.setAdmin(admin);
        requestEntity.setApprovalDatetime(LocalDateTime.now());
        //저장
        strategyApprovalRequestsRepository.save(requestEntity);
    }

    //3. 전략 승인 요청 반려
    public void rejectStrategy(Long requestId, String adminId, String rejectionReason){
        //담당자 정보 가져오기
        MemberEntity admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("관리자 정보가 존재하지 않습니다."));

        //해당 전략요청을 id로 가져온다.
        StrategyApprovalRequestsEntity requestEntity = strategyApprovalRequestsRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("해당되는 승인 요청이 존재하지 않습니다."));

        //해당 전략의 승인 flag를 P에서 N으로 변경한다.
        LocalDateTime changeStartDatetime = LocalDateTime.now();
        StrategyEntity strategyEntity = requestEntity.getStrategy();
        strategyEntity.setIsApproved("N");
        strategyEntity.setUpdaterId(adminId);
        strategyEntity.setUpdatedAt(LocalDateTime.now());
        strategyRepository.save(strategyEntity);

        //해당 전략 수정 이력 저장
        StrategyHistoryEntity historyEntity = new StrategyHistoryEntity(strategyEntity, "STRATEGY_STATUS_UPDATED", changeStartDatetime);
        strategyHistoryRepository.save(historyEntity);

        //전략 승인 요청 목록 테이블의 담당자ID, 거부사유, 거부일시를 등록한다.
        requestEntity.setAdmin(admin);
        requestEntity.setIsApproved("N");
        requestEntity.setRejectionReason(rejectionReason);
        requestEntity.setRejectionDatetime(LocalDateTime.now());
        strategyApprovalRequestsRepository.save(requestEntity);
    }
}
