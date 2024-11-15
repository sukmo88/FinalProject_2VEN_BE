package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.exception.TradingTypeNotFoundException;
import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesRegistrationDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyPayloadDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyRegistrationDto;
import com.sysmatic2.finalbe.admin.dto.TradingTypeRegistrationDto;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.strategy.dto.StrategyResponseDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyIACRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyStandardCodeRepository;
import com.sysmatic2.finalbe.admin.repository.TradingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static com.sysmatic2.finalbe.util.DtoEntityConversionUtils.*;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepo;
    private final InvestmentAssetClassesRepository iacRepo;
    private final TradingTypeRepository ttRepo;
    private final TradingTypeRepository tradingTypeRepository;
    private final StrategyIACRepository strategyIACRepository;

    //1. 전략 생성
    @Transactional
    public void register(StrategyPayloadDto strategyPayloadDto) throws Exception {
        //TODO) 트레이더 판별

        //전략 엔티티 생성
        StrategyEntity strategyEntity = new StrategyEntity();

        //페이로드에서 가져온 매매유형 id로 해당 매매유형 엔티티가져오기
        TradingTypeEntity ttEntity = ttRepo.findById(strategyPayloadDto.getTradingTypeId())
                .orElseThrow(() -> new TradingTypeNotFoundException(strategyPayloadDto.getTradingTypeId()));
        //페이로드에서 가져온 투자자산분류 id로 해당 투자자산 분류 엔티티들 가져오기
        List<Integer> iacIds = strategyPayloadDto.getInvestmentAssetClassesIdList();
        List<InvestmentAssetClassesEntity> iacEntities = iacRepo.findAllById(iacIds);

        //payload내용을 엔티티에 담기
        strategyEntity.setStrategyTitle(strategyPayloadDto.getStrategyTitle());
        strategyEntity.setTradingTypeEntity(ttEntity);
        strategyEntity.setTradingCycleCode(strategyPayloadDto.getTradingCycleCode());
        strategyEntity.setMinInvestmentAmount(strategyPayloadDto.getMinInvestmentAmount());
        strategyEntity.setStrategyOverview(strategyPayloadDto.getStrategyOverview());
        strategyEntity.setIsPosted(strategyPayloadDto.getIsPosted());

        //전략 상태 공통코드
        strategyEntity.setStrategyStatusCode("STRATEGY_STATUS_UNDER_MANAGEMENT");

        //TODO) 작성자 설정
        strategyEntity.setWriterId(101L);

        //save() - 저장후 저장한 엔티티 바로 가져옴
        StrategyEntity createdEntity = strategyRepo.save(strategyEntity);

        //전략 - 투자자산 분류 관계 데이터 생성
        //투자자산 분류 리스트 for문 돌리기
        for(int i = 0; i < iacEntities.size(); i++) {
            //전략 엔티티 생성 및 관계엔티티에 넣기
            StrategyIACEntity strategyIACEntity = new StrategyIACEntity();
            strategyIACEntity.setStrategyEntity(createdEntity);
            strategyIACEntity.setInvestmentAssetClassesEntity(iacEntities.get(i));

            //TODO) 작성자 설정
            strategyIACEntity.setWritedBy(101L);
            strategyIACEntity.setWritedAt(LocalDateTime.now());

            strategyIACRepository.save(strategyIACEntity);
        }
    }

    /**
     2. 사용자 전략 등록 폼에 필요한 정보를 제공하는 메서드
     * @return StrategyRegistrationDto 전략 등록에 필요한 DTO
     */
    @Transactional
    public StrategyRegistrationDto getStrategyRegistrationForm() {
        // TradingType 및 InvestmentAssetClass 데이터를 각각 DTO 리스트로 변환
        List<TradingTypeRegistrationDto> tradingTypeDtos = convertToTradingTypeDtos(ttRepo.findByIsActiveOrderByTradingTypeOrderAsc("Y"));
        List<InvestmentAssetClassesRegistrationDto> investmentAssetClassDtos = convertToInvestmentAssetClassDtos(iacRepo.findByIsActiveOrderByOrderAsc("Y"));

        // DTO 설정 및 반환
        StrategyRegistrationDto strategyRegistrationDto = new StrategyRegistrationDto();
        strategyRegistrationDto.setTradingTypeRegistrationDtoList(tradingTypeDtos);
        strategyRegistrationDto.setInvestmentAssetClassesRegistrationDtoList(investmentAssetClassDtos);

        return strategyRegistrationDto;
    }

    /**
     * 3. 전략 상세페이지 기본정보 조회 메서드
     * @return StrategyResponseDto - 전략 기본정보 DTO
     * TODO) 트레이더는 비공개한 자신의 전략상세를 볼 수 있다. 관리자는 모든 전략의 상세를 볼 수 있다. 유저는 공개만 볼 수 있다.
     */
//    @Transactional
//    public StrategyResponseDto getStrategyDetails(Long id){
//        //id값으로 해당 전략 조회
//        StrategyEntity strategyEntity = strategyRepo.findById(id).orElseThrow(() ->
//                new NoSuchElementException());
//
//        //기본정보 dto담기
//        StrategyResponseDto responseDto = convertToStrategyDto(strategyEntity);
//        //매매유형 dto담기
//        responseDto.setTradingTypeName(strategyEntity.getTradingTypeEntity().getTradingTypeName());
//        responseDto.setTradingTypeIcon(strategyEntity.getTradingTypeEntity().getTradingTypeIcon());
//        //
//    }
}

//이미지 링크는 이미지 링크+{imageId}의 형태라서 imageId만 DB에 저장