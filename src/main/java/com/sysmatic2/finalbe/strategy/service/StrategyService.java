package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.admin.dto.TradingCycleRegistrationDto;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.exception.TradingCycleNotFoundException;
import com.sysmatic2.finalbe.exception.TradingTypeNotFoundException;
import com.sysmatic2.finalbe.admin.dto.InvestmentAssetClassesRegistrationDto;
import com.sysmatic2.finalbe.strategy.dto.*;
import com.sysmatic2.finalbe.admin.dto.TradingTypeRegistrationDto;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyHistoryEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyIACEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyHistoryRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyIACRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyStandardCodeRepository;
import com.sysmatic2.finalbe.admin.repository.TradingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.sysmatic2.finalbe.util.CreatePageResponse.createPageResponse;
import static com.sysmatic2.finalbe.util.DtoEntityConversionUtils.*;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepo;
    private final InvestmentAssetClassesRepository iacRepo;
    private final TradingTypeRepository ttRepo;
    private final TradingCycleRepository tcRepo;
    private final StrategyIACRepository strategyIACRepository;
    private final TradingCycleRepository tradingCycleRepository;
    private final StrategyHistoryRepository strategyHistoryRepo;

    /**
     * 1. 전략을 생성하는 Service
     */
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
        //페이로드에서 가져온 주기 id로 해당 주기 엔티티 가져오기
        TradingCycleEntity tradingCycleEntity = tradingCycleRepository.findById(strategyPayloadDto.getTradingCycleId())
                .orElseThrow(() -> new TradingCycleNotFoundException(strategyPayloadDto.getTradingCycleId()));

        //payload내용을 엔티티에 담기
        strategyEntity.setStrategyTitle(strategyPayloadDto.getStrategyTitle());
        strategyEntity.setTradingTypeEntity(ttEntity);
        strategyEntity.setTradingCycleEntity(tradingCycleEntity);
        strategyEntity.setMinInvestmentAmount(strategyPayloadDto.getMinInvestmentAmount());
        strategyEntity.setStrategyOverview(strategyPayloadDto.getStrategyOverview());
        strategyEntity.setIsPosted(strategyPayloadDto.getIsPosted());

        //전략 상태 공통코드
        strategyEntity.setStrategyStatusCode("STRATEGY_STATUS_UNDER_MANAGEMENT");

        //TODO) 작성자 설정
        strategyEntity.setWriterId("101");

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
            strategyIACEntity.setWritedBy("101");
            strategyIACEntity.setWritedAt(LocalDateTime.now());

            strategyIACRepository.save(strategyIACEntity);
        }
    }

    /**
     * 2. 사용자 전략 등록 폼에 필요한 정보를 제공하는 메서드.
     *
     * @return StrategyRegistrationDto 전략 등록에 필요한 DTO
     */
    @Transactional
    public StrategyRegistrationDto getStrategyRegistrationForm() {
        // TradingType, InvestmentAssetClass 및 TradingCycle 데이터를 각각 DTO 리스트로 변환
        List<TradingTypeRegistrationDto> tradingTypeDtos = convertToTradingTypeDtos(ttRepo.findByIsActiveOrderByTradingTypeOrderAsc("Y"));
        List<InvestmentAssetClassesRegistrationDto> investmentAssetClassDtos = convertToInvestmentAssetClassDtos(iacRepo.findByIsActiveOrderByOrderAsc("Y"));
        List<TradingCycleRegistrationDto> tradingCycleDtos = convertToTradingCycleDtos(tcRepo.findByIsActiveOrderByTradingCycleOrderAsc("Y"));

        // DTO 설정 및 반환
        StrategyRegistrationDto strategyRegistrationDto = new StrategyRegistrationDto();
        strategyRegistrationDto.setTradingTypeRegistrationDtoList(tradingTypeDtos);
        strategyRegistrationDto.setInvestmentAssetClassesRegistrationDtoList(investmentAssetClassDtos);
        strategyRegistrationDto.setTradingCycleRegistrationDtoList(tradingCycleDtos); // 매매주기 데이터 설정

        return strategyRegistrationDto;
    }

    /**
     * 3. 전략 상세페이지 기본정보 조회 메서드
     * @return StrategyResponseDto - 전략 기본정보 DTO
     * TODO) 트레이더는 비공개한 자신의 전략상세를 볼 수 있다. 관리자는 모든 전략의 상세를 볼 수 있다. 유저는 공개만 볼 수 있다.
     */
    @Transactional
    public StrategyResponseDto getStrategyDetails(Long id){
        //id값으로 해당 전략 조회
        StrategyEntity strategyEntity = strategyRepo.findById(id).orElseThrow(() ->
                new NoSuchElementException());

        //기본정보 dto담기
        StrategyResponseDto responseDto = convertToStrategyDto(strategyEntity);

        //매매유형 dto담기
        responseDto.setTradingTypeName(strategyEntity.getTradingTypeEntity().getTradingTypeName());
        responseDto.setTradingTypeIcon(strategyEntity.getTradingTypeEntity().getTradingTypeIcon());

        //주기 dto 담기
        responseDto.setTradingCycleName(strategyEntity.getTradingCycleEntity().getTradingCycleName());
        responseDto.setTradingCycleIcon(strategyEntity.getTradingCycleEntity().getTradingCycleIcon());

        //투자자산 분류 dto 담기
        //전략 - 투자자산 분류 관계 테이블 조회
        List<StrategyIACEntity> strategyIACEntities = strategyIACRepository.findByStrategyEntity_StrategyId(strategyEntity.getStrategyId());

        //엔티티의 내용을 DTO에 담는다.
        List<StrategyIACResponseDto> strategyIACDtos = strategyIACEntities.stream()
                .map(iacDto -> new StrategyIACResponseDto(
                        iacDto.getInvestmentAssetClassesEntity().getInvestmentAssetClassesId(),                 // 투자자산 분류 ID
                        iacDto.getInvestmentAssetClassesEntity().getInvestmentAssetClassesName(),               // 투자자산 분류 이름
                        iacDto.getInvestmentAssetClassesEntity().getInvestmentAssetClassesIcon()                // 투자자산 분류 아이콘
                ))
                .collect(Collectors.toList());

        // 변환된 투자자산 분류 데이터를 ResponseDto에 추가
        responseDto.setStrategyIACEntities(strategyIACDtos);

        //TODO)트레이더 정보 넣기
        responseDto.setTraderId("1");
        responseDto.setTraderName("곽두팔");
        responseDto.setTraderImage("트레이더프로필이미지");

        return responseDto;
    }

    /**
     * 4. 전략을 삭제하는 메서드
     * 전략테이블에선 삭제가 되고 전략 이력 테이블에선 삭제 이력 데이터가 추가된다.
     */
    @Transactional
    public void deleteStrategy(Long id){
        //TODO) 관리자 or 작성한 트레이더 판별
        StrategyHistoryEntity strategyHistoryEntity = new StrategyHistoryEntity();
        strategyHistoryEntity.setChangeStartDate(LocalDateTime.now());

        //1. 전략의 id를 검색해서 유무 판별
        StrategyEntity strategyEntity = strategyRepo.findById(id).orElseThrow(
                () -> new NoSuchElementException());

        //TODO) 메서드로 빼기
        //2. 해당 전략의 정보를 전략 이력엔티티에 담는다.
        strategyHistoryEntity.setStrategyId(strategyEntity.getStrategyId());
        strategyHistoryEntity.setTradingTypeId(strategyEntity.getTradingTypeEntity().getTradingTypeId());
        strategyHistoryEntity.setStrategyStatusCode(strategyEntity.getStrategyStatusCode());
        strategyHistoryEntity.setTradingCycle(strategyEntity.getTradingCycleEntity().getTradingCycleId());
        strategyHistoryEntity.setStrategyHistoryStatusCode("STRATEGY_STATUS_DELETED");
        strategyHistoryEntity.setMinInvestmentAmount(strategyEntity.getMinInvestmentAmount());
        strategyHistoryEntity.setFollowersCount(strategyEntity.getFollowersCount());
        strategyHistoryEntity.setStrategyTitle(strategyEntity.getStrategyTitle());
        strategyHistoryEntity.setWriterId(strategyEntity.getWriterId());
        strategyHistoryEntity.setIsPosted(strategyEntity.getIsPosted());
        strategyHistoryEntity.setIsGranted(strategyEntity.getIsGranted());
        strategyHistoryEntity.setWritedAt(strategyEntity.getWritedAt());
        strategyHistoryEntity.setStrategyOverview(strategyEntity.getStrategyOverview());
        strategyHistoryEntity.setUpdaterId(strategyEntity.getUpdaterId());
        strategyHistoryEntity.setUpdatedAt(strategyEntity.getUpdatedAt());
        strategyHistoryEntity.setExitDate(strategyEntity.getExitDate());

        //3. 해당 전략을 삭제한다.
        strategyRepo.deleteById(strategyEntity.getStrategyId());

        //4. 이력엔티티의 내용을 전략 이력 테이블에 저장한다.
        strategyHistoryEntity.setChangeEndDate(LocalDateTime.now());
        strategyHistoryRepo.save(strategyHistoryEntity);
    }


    /**
     * 5. 전략을 수정하는 메서드
     *
     * @return StrategyPayloadDto 전략 수정에 필요한 DTO
     */

    /**
     * 6. 필터 조건에 따라 전략 목록을 반환 (페이징 포함)
     *
     * @param tradingCycleId 투자주기 ID (nullable)
     * @param investmentAssetClassesId 투자자산 분류 ID (nullable)
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param pageSize 페이지당 데이터 개수
     * @return 필터링된 전략 목록 및 페이징 정보를 포함한 Map 객체
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStrategies(Integer tradingCycleId, Integer investmentAssetClassesId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize); // 페이지 요청 객체 생성
        Page<StrategyListDto> strategyPage = strategyRepo.findStrategiesByFilters(tradingCycleId, investmentAssetClassesId, pageable);
        return createPageResponse(strategyPage); // 유틸 메서드를 사용해 Map 형태로 변환
    }
}

//이미지 링크는 이미지 링크+{imageId}의 형태라서 imageId만 DB에 저장