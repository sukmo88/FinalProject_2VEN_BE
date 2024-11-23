package com.sysmatic2.finalbe.admin.service;

import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesDto;
import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesPayloadDto;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACHistoryEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyIACHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyIACRepository;
import com.sysmatic2.finalbe.strategy.service.StrategyService;
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.sysmatic2.finalbe.util.CreatePageResponse.createPageResponse;
import static com.sysmatic2.finalbe.util.DtoEntityConversionUtils.toDto;

//관리자 페이지 - 투자자산 분류 관리
@Service
@RequiredArgsConstructor
public class InvestmentAssetClassesService {
    private final InvestmentAssetClassesRepository iacRepository;
    private final StrategyIACRepository strategyIACRepository;
    private final StrategyIACHistoryRepository strategyIACHistoryRepository;

    //1. 투자자산 분류 전체목록 메서드 페이지네이션, 소팅 적용
    @Transactional(readOnly = true)
    public Map<String, Object> getList(int page, int size) throws Exception{
        //현재 페이지, 페이지 사이즈, 정렬 정보 담은 pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("order").ascending());

        //페이지 객체 리스트에 DB 데이터 엔티티들을 가져와서 넣는다.
        Page<InvestmentAssetClassesEntity> pageEntityList = iacRepository.findAll(pageable);
        //페이지 객체 리스트의 타입 변경
        Page<InvestmentAssetClassesDto> pageDtoList = pageEntityList.map(DtoEntityConversionUtils::toDto);

        return createPageResponse(pageDtoList);
    }

    //1-1. 투자자산 분류 상세 조회 메서드
    @Transactional(readOnly = true)
    public InvestmentAssetClassesDto getById(Integer id) throws Exception{
        //id 값으로 optional 객체 얻기
        Optional<InvestmentAssetClassesEntity> iacOptional = iacRepository.findById(id);
        //저장할 엔티티 객체 생성
        InvestmentAssetClassesEntity iacEntity;

        //만약 찾으면 엔티티 객체에 저장
        if(iacOptional.isPresent()){
            iacEntity = iacOptional.get();
        }else{
            //못찾으면 에러 메시지 보내기
            throw new NoSuchElementException();
        }

        //엔티티 객체를 DTO에 담아서 보낸다.
        return toDto(iacEntity);
    }

    //1-2. 투자자산 분류 목록(isActive = Y 인것만)
    @Transactional(readOnly = true)
    public List<InvestmentAssetClassesDto> getActiveList() throws Exception {
        List<InvestmentAssetClassesEntity> iacList = iacRepository.findByIsActiveOrderByOrderAsc("Y");
        List<InvestmentAssetClassesDto> resultList = new ArrayList<>();

        for(InvestmentAssetClassesEntity iacEntity : iacList){
            resultList.add(toDto(iacEntity));
        }

        return resultList;
    }


    //2. 투자자산 분류 생성
    //페이로드 DTO를 받아서 엔티티에 넣는다. 만들고 나면 반환용 DTO를 반환한다.
    @Transactional
    public void register(InvestmentAssetClassesPayloadDto iacPayloadDto) throws Exception{
        //엔티티 객체 생성
        InvestmentAssetClassesEntity iacEntity = new InvestmentAssetClassesEntity();

        //받아온 payloadDTO값을 엔티티 객체에 넣는다.
        //만약 이미 있는 순번이면 에러 발생
        if(iacRepository.existsByOrder(iacPayloadDto.getOrder())){
            throw new DataIntegrityViolationException("Order already exists");
        }

        //order 빈 값이면 order 최대값에서 +1 한 값으로 설정
        if(iacPayloadDto.getOrder() == null){
            iacEntity.setOrder(iacRepository.getMaxOrder().orElse(0)+1); //orElse 자체가 Optional을 벗겨줌
        } else {
            iacEntity.setOrder(iacPayloadDto.getOrder());
        }

        iacEntity.setInvestmentAssetClassesName(iacPayloadDto.getInvestmentAssetClassesName());
        iacEntity.setInvestmentAssetClassesIcon(iacPayloadDto.getInvestmentAssetClassesIcon());
        iacEntity.setIsActive(iacPayloadDto.getIsActive());

        //최초 작성자, 최초 작성일시, 최종 수정자, 최종 수정일시 넣기 - 시스템컬럼
        //TODO) User 객체 받아오기 - spring security
//        iacEntity.setCreatedBy(100L);
        iacEntity.setCreatedAt(LocalDateTime.now());
//        iacEntity.setModifiedBy(100L);
        iacEntity.setModifiedAt(LocalDateTime.now());

        //save()
        iacRepository.save(iacEntity);
    }

    //3. 투자자산 분류 삭제
    //삭제시 관계테이블의 데이터만 삭제된다.
    @Transactional
    public void delete(Integer id, String memberId) throws Exception{
        //id로 존재 확인
        InvestmentAssetClassesEntity iacEntity = iacRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException());

        //관계테이블에서 해당 iacEntity와 관련된 모든 데이터를 찾는다.
        List<StrategyIACEntity> relatedStrategyIACs = strategyIACRepository.findByInvestmentAssetClassesEntity(iacEntity);

        //삭제 이력 테이블에 해당 데이터를 저장한다.
        for(StrategyIACEntity strategyIACEntity : relatedStrategyIACs){
            StrategyIACHistoryEntity historyEntity = new StrategyIACHistoryEntity(strategyIACEntity, "STRATEGYIAC_STATUS_DELETED");
            historyEntity.setUpdatedAt(LocalDateTime.now());
            historyEntity.setUpdaterId(memberId);
            strategyIACHistoryRepository.save(historyEntity);
        }

        //관계테이블에서 엔티티 삭제
        strategyIACRepository.deleteAll(relatedStrategyIACs);

        //투자자산 분류 삭제
        iacRepository.delete(iacEntity);
    }

    //4. 투자자산 분류 수정
    @Transactional
    public  void update(Integer id, InvestmentAssetClassesPayloadDto iacPayloadDto) throws Exception{
        //수정한 내용을 덮어씌우는 느낌.
        Optional<InvestmentAssetClassesEntity> optionalIac = iacRepository.findById(id);
        InvestmentAssetClassesEntity iacEntity;

        if(optionalIac.isPresent()){
            //있으면 엔티티 객체에 저장
            iacEntity = optionalIac.get();
        }else{
            throw new NoSuchElementException();
        }

        //엔티티에 페이로드 내용 저장
        //받아온 payloadDTO값을 엔티티 객체에 넣는다.
        //자신의 order값이 아니고 존재하는 order값이면 에러 발생
        if((iacPayloadDto.getOrder() != iacEntity.getOrder()) && (iacRepository.existsByOrder(iacPayloadDto.getOrder()))){
            throw new DataIntegrityViolationException("Order already exists");
        }
        //order 빈 값이면 값 변경 X
        if(iacPayloadDto.getOrder() != null){
            iacEntity.setOrder(iacPayloadDto.getOrder());
        }
        iacEntity.setInvestmentAssetClassesName(iacPayloadDto.getInvestmentAssetClassesName());
        iacEntity.setInvestmentAssetClassesIcon(iacPayloadDto.getInvestmentAssetClassesIcon());
        iacEntity.setIsActive(iacPayloadDto.getIsActive());
        //시스템컬럼 수정
        iacEntity.setModifiedBy("100");
        iacEntity.setModifiedAt(LocalDateTime.now());

        //save()
        iacRepository.save(iacEntity);
    }
}
