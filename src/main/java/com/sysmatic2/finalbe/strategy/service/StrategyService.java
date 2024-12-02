package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.admin.dto.*;
import com.sysmatic2.finalbe.admin.entity.StrategyApprovalRequestsEntity;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.repository.StrategyApprovalRequestsRepository;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import com.sysmatic2.finalbe.attachment.service.FileService;
import com.sysmatic2.finalbe.attachment.service.ProposalService;
import com.sysmatic2.finalbe.cs.repository.ConsultationRepository;
import com.sysmatic2.finalbe.exception.*;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.FollowingStrategyRepository;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.dto.*;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.strategy.entity.*;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.strategy.repository.*;
import com.sysmatic2.finalbe.admin.repository.TradingTypeRepository;
import com.sysmatic2.finalbe.util.ParseCsvToList;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.sysmatic2.finalbe.util.CreatePageResponse.createPageResponse;
import static com.sysmatic2.finalbe.common.DtoEntityConversion.*;

@Service
@Validated
@RequiredArgsConstructor
public class StrategyService {
    private final MemberRepository memberRepository;
    private final InvestmentAssetClassesRepository investmentAssetClassesRepository;
    private final TradingCycleRepository tradingCycleRepository;
    private final TradingTypeRepository tradingTypeRepository;
    private final StrategyRepository strategyRepo;
    private final StrategyHistoryRepository strategyHistoryRepo;
    private final StrategyIACRepository strategyIACRepository;
    private final StrategyIACHistoryRepository strategyIACHistoryRepository;
    private final StrategyApprovalRequestsRepository strategyApprovalRequestsRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final StrategyProposalRepository strategyProposalRepository;
    private final StrategyProposalService strategyProposalService;
    private final FileService fileService;
    private final LiveAccountDataService liveAccountDataService;
    private final LiveAccountDataRepository liveAccountDataRepository;
    private final FollowingStrategyRepository followingStrategyRepository;
    private final ConsultationRepository consultationRepository;
    private final MonthlyStatisticsRepository monthlyStatisticsRepository;

    //1. 전략 생성
    /**
     * 1-1. 사용자 전략 등록 폼에 필요한 정보를 제공하는 메서드.
     *
     * @return StrategyRegistrationDto 전략 등록에 필요한 DTO
     *
     */
    @Transactional
    public StrategyRegistrationDto getStrategyRegistrationForm() {
        // TradingType, InvestmentAssetClass 및 TradingCycle 데이터를 각각 DTO 리스트로 변환
        List<TradingTypeRegistrationDto> tradingTypeDtos = convertToTradingTypeDtos(tradingTypeRepository.findByIsActiveOrderByTradingTypeOrderAsc("Y"));
        List<InvestmentAssetClassesRegistrationDto> investmentAssetClassDtos = convertToInvestmentAssetClassDtos(investmentAssetClassesRepository.findByIsActiveOrderByOrderAsc("Y"));
        List<TradingCycleRegistrationDto> tradingCycleDtos = convertToTradingCycleDtos(tradingCycleRepository.findByIsActiveOrderByTradingCycleOrderAsc("Y"));

        // DTO 설정 및 반환
        StrategyRegistrationDto strategyRegistrationDto = new StrategyRegistrationDto();
        strategyRegistrationDto.setTradingTypeRegistrationDtoList(tradingTypeDtos);
        strategyRegistrationDto.setInvestmentAssetClassesRegistrationDtoList(investmentAssetClassDtos);
        strategyRegistrationDto.setTradingCycleRegistrationDtoList(tradingCycleDtos); // 매매주기 데이터 설정

        return strategyRegistrationDto;
    }

    /**
     * 1-2. 전략을 등록하는 메서드
     *
     * @Param strategyPayloadDto : 등록할 전략의 데이터
     *
     */
    @Transactional
    public Map<String, Long> register(StrategyPayloadDto strategyPayloadDto, String memberId) throws Exception {
        //작성자 정보 가져오기, 확인
        MemberEntity traderEntity = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));


        //등록 이력 시작
        StrategyHistoryEntity strategyHistoryEntity = new StrategyHistoryEntity();
        strategyHistoryEntity.setChangeStartDate(LocalDateTime.now());

        //1. 전략 등록
        //전략 엔티티 생성
        StrategyEntity strategyEntity = new StrategyEntity();

        //페이로드에서 가져온 매매유형 id로 해당 매매유형 엔티티가져오기
        TradingTypeEntity ttEntity = tradingTypeRepository.findById(strategyPayloadDto.getTradingTypeId())
                .orElseThrow(() -> new TradingTypeNotFoundException(strategyPayloadDto.getTradingTypeId()));

        //페이로드에서 가져온 주기 id로 해당 주기 엔티티 가져오기
        TradingCycleEntity tradingCycleEntity = tradingCycleRepository.findById(strategyPayloadDto.getTradingCycleId())
                .orElseThrow(() -> new TradingCycleNotFoundException(strategyPayloadDto.getTradingCycleId()));

        //페이로드에서 가져온 투자자산분류 id로 해당 투자자산 분류 엔티티들 가져오기
        List<Integer> iacIds = strategyPayloadDto.getInvestmentAssetClassesIdList();
        List<InvestmentAssetClassesEntity> iacEntities = investmentAssetClassesRepository.findAllById(iacIds);

        //isActive 검증
        List<Integer> inactiveIacIds = iacEntities.stream()
                .filter(iacEntity -> "N".equals(iacEntity.getIsActive()))
                .map(InvestmentAssetClassesEntity::getInvestmentAssetClassesId)
                .toList();
        if(!inactiveIacIds.isEmpty()) {
            throw new InvestmentAssetClassesNotActiveException("isActive가 'N'인 투자자산 분류가 포함되어 있습니다.");
        }

        //요청한 ID와 조회된 ID 비교 - 없는 투자자산 분류인지 검증
        Set<Integer> foundIds = iacEntities.stream()
                .map(InvestmentAssetClassesEntity::getInvestmentAssetClassesId)
                .collect(Collectors.toSet());
        List<Integer> missingIds = iacIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new InvestmentAssetClassesNotFoundException("INVESTMENT_ASSET_CLASSES_NOT_EXIST");
        }

        //payload내용을 엔티티에 담기
        strategyEntity.setStrategyTitle(strategyPayloadDto.getStrategyTitle());
        strategyEntity.setTradingTypeEntity(ttEntity);
        strategyEntity.setTradingCycleEntity(tradingCycleEntity);
        strategyEntity.setStrategyStatusCode("STRATEGY_OPERATION_UNDER_MANAGEMENT"); //운용중인 전략만 등록 가능
        strategyEntity.setMinInvestmentAmount(strategyPayloadDto.getMinInvestmentAmount());
        strategyEntity.setStrategyOverview(strategyPayloadDto.getStrategyOverview());
        strategyEntity.setIsPosted(strategyPayloadDto.getIsPosted());
        strategyEntity.setWriterId(memberId);

        //save() - 저장후 저장한 엔티티 바로 가져옴
        StrategyEntity createdEntity = strategyRepo.save(strategyEntity);

        //2. 전략 - 투자자산 분류 관계 데이터 등록
        for(InvestmentAssetClassesEntity iacEntity : iacEntities) {
            StrategyIACEntity strategyIACEntity = new StrategyIACEntity();
            strategyIACEntity.setStrategyEntity(createdEntity);
            strategyIACEntity.setInvestmentAssetClassesEntity(iacEntity);
            strategyIACEntity.setWritedBy(createdEntity.getWriterId());
            strategyIACEntity.setWritedAt(LocalDateTime.now());

            strategyIACRepository.save(strategyIACEntity);

            //3. 전략 - 투자자산 분류 관계 데이터 등록 이력 추가
            StrategyIACHistoryEntity strategyIACHistoryEntity = new StrategyIACHistoryEntity();
            strategyIACHistoryEntity.setStrategyId(strategyIACEntity.getStrategyEntity().getStrategyId());
            strategyIACHistoryEntity.setInvestmentAssetClassId(strategyIACEntity.getInvestmentAssetClassesEntity().getInvestmentAssetClassesId());
            strategyIACHistoryEntity.setWriterId(strategyEntity.getWriterId());
            strategyIACHistoryEntity.setWritedAt(LocalDateTime.now());
            strategyIACHistoryEntity.setStatus("STRATEGYIAC_STATUS_CREATED");

            strategyIACHistoryRepository.save(strategyIACHistoryEntity);
        }

        //4. 전략 이력 추가
        strategyHistoryEntity.setStrategyId(createdEntity.getStrategyId());
        strategyHistoryEntity.setTradingTypeId(createdEntity.getTradingTypeEntity().getTradingTypeId());
        strategyHistoryEntity.setTradingCycle(createdEntity.getTradingCycleEntity().getTradingCycleId());
        strategyHistoryEntity.setStrategyStatusCode(createdEntity.getStrategyStatusCode());
        strategyHistoryEntity.setStrategyHistoryStatusCode("STRATEGY_STATUS_CREATED");
        strategyHistoryEntity.setMinInvestmentAmount(createdEntity.getMinInvestmentAmount());
        strategyHistoryEntity.setStrategyTitle(createdEntity.getStrategyTitle());
        strategyHistoryEntity.setWriterId(createdEntity.getWriterId());
        strategyHistoryEntity.setIsPosted(createdEntity.getIsPosted());
        strategyHistoryEntity.setIsApproved(createdEntity.getIsApproved());
        strategyHistoryEntity.setWritedAt(createdEntity.getWritedAt());
        strategyHistoryEntity.setStrategyOverview(createdEntity.getStrategyOverview());
        strategyHistoryEntity.setChangeEndDate(LocalDateTime.now());

        strategyHistoryRepo.save(strategyHistoryEntity);

        // 5. 제안서 등록 (sbwoo)
        // strategyPayloadDto.ProposalLink(이하 link)이 null이 아니면, 제안서 등록
        if (strategyPayloadDto.getStrategyProposalLink() != null) {
            strategyProposalService.uploadProposal(strategyPayloadDto.getStrategyProposalLink(), createdEntity.getWriterId(), createdEntity.getStrategyId());
        }

        // 6. 응답
        Map<String, Long> responseMap = new HashMap<>();
        responseMap.put("Strategy_Id", strategyEntity.getStrategyId());
        return responseMap;
    }

    //2. 전략 목록
    /**
     * 2-1. 필터 조건에 따라 전략 목록을 반환 (페이징 포함) - 랭킹
     *
     * @param tradingCycleId           투자주기 ID (nullable)
     * @param investmentAssetClassesId 투자자산 분류 ID (nullable)
     * @param page                     현재 페이지 번호 (0부터 시작)
     * @param pageSize                 페이지당 데이터 개수
     * @return 필터링된 전략 목록 및 페이징 정보를 포함한 Map 객체
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStrategies(Integer tradingCycleId, Integer investmentAssetClassesId, int page, int pageSize) {
        // 1. 페이지 요청 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        // 2. 전략 필터링 데이터 가져오기
        // 투자주기 ID와 투자자산 분류 ID를 기준으로 전략 목록을 페이징 처리하여 조회
        Page<StrategyEntity> findStrategyList = strategyRepo.findStrategiesByFilters(tradingCycleId, investmentAssetClassesId, pageable);

        // 3. 전략 ID 리스트 생성
        // 조회된 전략 목록에서 각 전략의 ID를 추출하여 리스트로 저장
        List<Long> strategyIds = findStrategyList.stream()
                .map(StrategyEntity::getStrategyId)
                .collect(Collectors.toList());

        // 4. 최신 일간 통계 데이터 가져오기
        // 각 전략 ID에 해당하는 최신 일간 통계 데이터를 조회하여 Map으로 변환
        List<DailyStatisticsEntity> latestStatisticsList = dailyStatisticsRepository.findLatestStatisticsByStrategyIds(strategyIds);
        Map<Long, DailyStatisticsEntity> latestStatisticsMap = latestStatisticsList.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getStrategyEntity().getStrategyId(), // 키: 전략 ID
                        stat -> stat // 값: DailyStatisticsEntity
                ));

        // 5. 누적 수익률 데이터 조회
        // 각 전략 ID에 대해 누적 수익률 데이터를 날짜 오름차순으로 조회하여 Map으로 변환
        Map<Long, List<Double>> cumulativeProfitLossRateMap = strategyIds.stream()
                .collect(Collectors.toMap(
                        strategyId -> strategyId, // 키: 전략 ID
                        strategyId -> dailyStatisticsRepository.findCumulativeProfitLossRateByStrategyIdOrderByDate(strategyId) // 값: 누적 수익률 리스트
                ));

        // 6. DTO 생성
        List<AdvancedSearchResultDto> dtoList = findStrategyList.stream()
                .map(strategyEntity -> {
                    // 기본 정보 DTO에 삽입
                    AdvancedSearchResultDto dto = new AdvancedSearchResultDto(
                            strategyEntity.getStrategyId(),        // 전략 ID
                            strategyEntity.getTradingTypeEntity().getTradingTypeIcon(),   // 매매유형 아이콘
                            strategyEntity.getTradingCycleEntity().getTradingCycleIcon(),  // 매매주기 아이콘
                            strategyEntity.getStrategyIACEntities().stream()
                                    .map(iac -> iac.getInvestmentAssetClassesEntity().getInvestmentAssetClassesIcon())
                                    .collect(Collectors.toList()),  // 투자자산 분류 아이콘
                            strategyEntity.getStrategyTitle(),      // 전략명
                            BigDecimal.ZERO,                        // 누적손익률 (초기값)
                            BigDecimal.ZERO,                        // 최근 1년 손익률 (초기값)
                            BigDecimal.ZERO,                        // MDD (초기값)
                            strategyEntity.getSmScore(),            // SM-Score
                            strategyEntity.getFollowersCount(),     // 팔로워 수
                            null                                    // 누적 수익률 리스트 (초기값)
                    );

                    // 최신 일간 통계 데이터 삽입
                    DailyStatisticsEntity latestStatistics = latestStatisticsMap.get(strategyEntity.getStrategyId());
                    if (latestStatistics != null) {
                        dto.setCumulativeProfitLossRate(latestStatistics.getCumulativeProfitLossRate()); // 누적손익률
                        dto.setRecentOneYearReturn(latestStatistics.getRecentOneYearReturn());           // 최근 1년 손익률
                        dto.setMdd(latestStatistics.getMaxDrawdownRate());                               // MDD
                    }

                    // 누적 수익률 전체 데이터 삽입
                    List<Double> cumulativeProfitLossRates = cumulativeProfitLossRateMap.get(strategyEntity.getStrategyId());
                    dto.setCumulativeProfitLossRateList(cumulativeProfitLossRates);

                    return dto;
                }).collect(Collectors.toList());

        // 7. DTO 페이지 객체 생성
        Page<AdvancedSearchResultDto> dtoPage = new PageImpl<>(dtoList, pageable, findStrategyList.getTotalElements());

        // 8. 페이지 응답 생성 및 반환
        return createPageResponse(dtoPage);
    }

    /**
     * 2-2. 상세 필터를 적용한 전략 목록을 반환(페이지네이션)
     *
     * @param searchOptionsPayload 필터 객체 (검색 조건을 포함한 DTO)
     * @param page                 현재 페이지 번호
     * @param pageSize             페이지당 데이터 크기
     * @return 필터링된 전략 목록과 페이징 정보를 포함한 Map 객체
     */
    @Transactional(readOnly = true)
    public Map<String, Object> advancedSearch(@Valid SearchOptionsPayloadDto searchOptionsPayload, Integer page, Integer pageSize) {
        //페이지 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        // 2. 문자열을 리스트로 변환 (필터 조건 처리)
        //투자자산 분류 id 리스트
        List<Integer> iacIds = ParseCsvToList.parseCsvToIntegerList(searchOptionsPayload.getInvestmentAssetClassesIdList());
        //전략 운용코드 리스트
        List<String> operationStatusList = ParseCsvToList.parseCsvToStringList(searchOptionsPayload.getStrategyOperationStatusList());
        //매매유형 id 리스트
        List<Integer> tradingTypeIds = ParseCsvToList.parseCsvToIntegerList(searchOptionsPayload.getTradingTypeIdList());
        //총운용일수 리스트
        List<Integer> operationDays = ParseCsvToList.parseCsvToIntegerList(searchOptionsPayload.getOperationDaysList());
        //매매주기 id 리스트
        List<Integer> tradingCycleIds = ParseCsvToList.parseCsvToIntegerList(searchOptionsPayload.getTradingCylcleIdList());
        //수익률 리스트
        List<Integer> returnRates = ParseCsvToList.parseCsvToIntegerList(searchOptionsPayload.getReturnRateList());

        // 3. 필터 DTO 생성 및 설정
        //Repository 전달용 dto생성
        SearchOptionsDto searchOptionsDto = new SearchOptionsDto();
        //전달용 dto에 값 넣기
        searchOptionsDto.setInvestmentAssetClassesIdList(iacIds);
        searchOptionsDto.setStrategyOperationStatusList(operationStatusList);
        searchOptionsDto.setTradingTypeIdList(tradingTypeIds);
        searchOptionsDto.setOperationDaysList(operationDays);
        searchOptionsDto.setTradingCylcleIdList(tradingCycleIds);
        searchOptionsDto.setMinInvestmentAmount(searchOptionsPayload.getMinInvestmentAmount());
        searchOptionsDto.setMinPrincipal(searchOptionsPayload.getMinPrincipal());
        searchOptionsDto.setMaxPrincipal(searchOptionsPayload.getMaxPrincipal());
        searchOptionsDto.setMinSmscore(searchOptionsPayload.getMinSmscore());
        searchOptionsDto.setMaxSmscore(searchOptionsPayload.getMaxSmscore());
        searchOptionsDto.setMinMdd(searchOptionsPayload.getMinMdd());
        searchOptionsDto.setMaxMdd(searchOptionsPayload.getMaxMdd());
        searchOptionsDto.setStartDate(searchOptionsPayload.getStartDate());
        searchOptionsDto.setEndDate(searchOptionsPayload.getEndDate());
        searchOptionsDto.setReturnRateList(returnRates);

        // 4. Repository 호출로 필터링된 전략 페이지 가져오기
        //2)필터객체, 페이지 객체넣고 db에서 데이터 가져오기
        Page<StrategyEntity> findStrategyPage = strategyRepo.findStrategiesByDetailSearchOptions(searchOptionsDto, pageable);

        //전략 페이지로 일간 데이터들 중 제일 최신값 가져오기
        // 5. 전략 ID 리스트 생성
        List<Long> strategyIds = findStrategyPage.stream()
                .map(StrategyEntity::getStrategyId)
                .collect(Collectors.toList());

        // 6. 각 전략의 최신 일간 통계 데이터 가져오기
        List<DailyStatisticsEntity> latestStatisticsList = dailyStatisticsRepository.findLatestStatisticsByStrategyIds(strategyIds);
        Map<Long, DailyStatisticsEntity> latestStatisticsMap = latestStatisticsList.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getStrategyEntity().getStrategyId(),
                        stat -> stat
                ));

        // 7. 누적 수익률 데이터 가져오기 (날짜 오름차순)
        Map<Long, List<Double>> cumulativeProfitLossRateMap = strategyIds.stream()
                .collect(Collectors.toMap(
                        strategyId -> strategyId, // 전략 ID를 키로 사용
                        strategyId -> dailyStatisticsRepository.findCumulativeProfitLossRateByStrategyIdOrderByDate(strategyId) // 누적 수익률 리스트
                ));

        // 8. DTO 생성
        List<AdvancedSearchResultDto> dtoList = findStrategyPage.stream()
                .map(strategyEntity -> {
                    AdvancedSearchResultDto dto = new AdvancedSearchResultDto(
                            strategyEntity.getStrategyId(),                                // 전략 ID
                            strategyEntity.getTradingTypeEntity().getTradingTypeIcon(),    // 매매 유형 아이콘
                            strategyEntity.getTradingCycleEntity().getTradingCycleIcon(),  // 매매 주기 아이콘
                            strategyEntity.getStrategyIACEntities().stream()
                                    .map(iac -> iac.getInvestmentAssetClassesEntity().getInvestmentAssetClassesIcon())
                                    .collect(Collectors.toList()),                         // 투자 자산 분류 아이콘 리스트
                            strategyEntity.getStrategyTitle(),                             // 전략명
                            BigDecimal.ZERO,                                               // 누적 손익률 (초기값)
                            BigDecimal.ZERO,                                               // 최근 1년 손익률 (초기값)
                            BigDecimal.ZERO,                                               // MDD (초기값)
                            strategyEntity.getSmScore(),                                   // SM-Score
                            strategyEntity.getFollowersCount(),                            // 팔로워 수
                            null                                                           // 누적 수익률 리스트 (초기값)
                    );

                    // 최신 일간 통계 데이터 추가
                    DailyStatisticsEntity latestStatistics = latestStatisticsMap.get(strategyEntity.getStrategyId());
                    if (latestStatistics != null) {
                        dto.setCumulativeProfitLossRate(latestStatistics.getCumulativeProfitLossRate());
                        dto.setRecentOneYearReturn(latestStatistics.getRecentOneYearReturn());
                        dto.setMdd(latestStatistics.getMaxDrawdownRate());
                    }

                    // 누적 수익률 리스트 추가
                    List<Double> cumulativeProfitLossRates = cumulativeProfitLossRateMap.get(strategyEntity.getStrategyId());
                    dto.setCumulativeProfitLossRateList(cumulativeProfitLossRates);

                    return dto;
                }).collect(Collectors.toList());

        // 9. DTO 리스트를 페이지 객체로 변환
        Page<AdvancedSearchResultDto> dtoPage = new PageImpl<>(dtoList, pageable, findStrategyPage.getTotalElements());

        // 10. 페이지 응답 생성 및 반환
        return createPageResponse(dtoPage);
    }

    /**
     * 2-3. 작성자 ID로 필터링한 전략 목록을 반환(페이지네이션) - 나의 전략
     *
     * @param traderId 작성자(트레이더) ID
     * @param page     현재 페이지 번호
     * @param pageSize 페이지당 데이터 크기
     * @return 작성자가 등록한 전략 목록과 페이징 정보를 포함한 Map 객체
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStrategyListbyTraderId(String traderId, Integer page, Integer pageSize) {
        // 1. 페이지 요청 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        // 2. 트레이더 ID로 전략 페이지 가져오기
        Page<StrategyEntity> traderStrategyPage = strategyRepo.findByWriterId(traderId, pageable);

        //전략 페이지로 일간 데이터들 중 제일 최신값으로 가져오기
        // 3. 전략 ID 리스트 생성
        List<Long> strategyIds = traderStrategyPage.stream()
                .map(StrategyEntity::getStrategyId)
                .collect(Collectors.toList());

        // 4. 각 전략의 최신 일간 통계 데이터 가져오기
        List<DailyStatisticsEntity> latestStatisticsList = dailyStatisticsRepository.findLatestStatisticsByStrategyIds(strategyIds);
        Map<Long, DailyStatisticsEntity> latestStatisticsMap = latestStatisticsList.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getStrategyEntity().getStrategyId(),
                        stat -> stat
                ));

        // 5. 누적 수익률 데이터 가져오기 (날짜 오름차순)
        Map<Long, List<Double>> cumulativeProfitLossRateMap = strategyIds.stream()
                .collect(Collectors.toMap(
                        strategyId -> strategyId, // 전략 ID를 키로 사용
                        strategyId -> dailyStatisticsRepository.findCumulativeProfitLossRateByStrategyIdOrderByDate(strategyId) // 누적 수익률 리스트
                ));

        // 6. DTO 생성
        List<AdvancedSearchResultDto> dtoList = traderStrategyPage.stream()
                .map(strategyEntity -> {
                    AdvancedSearchResultDto dto = new AdvancedSearchResultDto(
                            strategyEntity.getStrategyId(),                                // 전략 ID
                            strategyEntity.getTradingTypeEntity().getTradingTypeIcon(),    // 매매 유형 아이콘
                            strategyEntity.getTradingCycleEntity().getTradingCycleIcon(),  // 매매 주기 아이콘
                            strategyEntity.getStrategyIACEntities().stream()
                                    .map(iac -> iac.getInvestmentAssetClassesEntity().getInvestmentAssetClassesIcon())
                                    .collect(Collectors.toList()),                         // 투자 자산 분류 아이콘 리스트
                            strategyEntity.getStrategyTitle(),                             // 전략명
                            BigDecimal.ZERO,                                               // 누적 손익률 (초기값)
                            BigDecimal.ZERO,                                               // 최근 1년 손익률 (초기값)
                            BigDecimal.ZERO,                                               // MDD (초기값)
                            strategyEntity.getSmScore(),                                   // SM-Score
                            strategyEntity.getFollowersCount(),                            // 팔로워 수
                            null                                                           // 누적 수익률 리스트 (초기값)
                    );

                    // 최신 일간 통계 데이터 추가
                    DailyStatisticsEntity latestStatistics = latestStatisticsMap.get(strategyEntity.getStrategyId());
                    if (latestStatistics != null) {
                        dto.setCumulativeProfitLossRate(latestStatistics.getCumulativeProfitLossRate());
                        dto.setRecentOneYearReturn(latestStatistics.getRecentOneYearReturn());
                        dto.setMdd(latestStatistics.getMaxDrawdownRate());
                    }

                    // 누적 수익률 리스트 추가
                    List<Double> cumulativeProfitLossRates = cumulativeProfitLossRateMap.get(strategyEntity.getStrategyId());
                    dto.setCumulativeProfitLossRateList(cumulativeProfitLossRates);

                    return dto;
                }).collect(Collectors.toList());


        // 7. DTO 리스트를 페이지 객체로 변환
        Page<AdvancedSearchResultDto> dtoPage = new PageImpl<>(dtoList, pageable, traderStrategyPage.getTotalElements());

        // 8. 페이지 응답 생성 및 반환
        return createPageResponse(dtoPage);
    }

    /**
     * 2-4. 키워드로 전략명 필터링한 전략 목록을 반환(페이지네이션)
     *
     * @param keyword  검색 키워드
     * @param page     현재 페이지 번호
     * @param pageSize 페이지당 데이터 크기
     * @return 키워드로 필터링된 전략 목록과 페이징 정보를 포함한 Map 객체
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStrategyListByKeyword(String keyword, Integer page, Integer pageSize) {
        // 1. 페이지 요청 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        //TODO)isApproved = Y 설정
        //2. 키워드 검색으로 전략 페이지 가져오기 - isPosted = Y, isApproved = Y
        Page<StrategyEntity> findStrategyPage = strategyRepo.searchByKeyword(keyword, "Y", "N", pageable);

        //전략 페이지로 일간 데이터들 중 제일 최신값 가져오기
        // 3. 전략 ID 리스트 생성
        List<Long> strategyIds = findStrategyPage.stream()
                .map(StrategyEntity::getStrategyId)
                .collect(Collectors.toList());

        // 4. 각 전략의 최신 일간 통계 데이터 가져오기
        List<DailyStatisticsEntity> latestStatisticsList = dailyStatisticsRepository.findLatestStatisticsByStrategyIds(strategyIds);
        Map<Long, DailyStatisticsEntity> latestStatisticsMap = latestStatisticsList.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getStrategyEntity().getStrategyId(),
                        stat -> stat
                ));

        // 5. 누적 수익률 데이터 가져오기 (날짜 오름차순)
        Map<Long, List<Double>> cumulativeProfitLossRateMap = strategyIds.stream()
                .collect(Collectors.toMap(
                        strategyId -> strategyId, // 전략 ID를 키로 사용
                        strategyId -> dailyStatisticsRepository.findCumulativeProfitLossRateByStrategyIdOrderByDate(strategyId) // 누적 수익률 리스트
                ));

        // 6. DTO 생성
        List<AdvancedSearchResultDto> dtoList = findStrategyPage.stream()
                .map(strategyEntity -> {
                    AdvancedSearchResultDto dto = new AdvancedSearchResultDto(
                            strategyEntity.getStrategyId(),                                // 전략 ID
                            strategyEntity.getTradingTypeEntity().getTradingTypeIcon(),    // 매매 유형 아이콘
                            strategyEntity.getTradingCycleEntity().getTradingCycleIcon(),  // 매매 주기 아이콘
                            strategyEntity.getStrategyIACEntities().stream()
                                    .map(iac -> iac.getInvestmentAssetClassesEntity().getInvestmentAssetClassesIcon())
                                    .collect(Collectors.toList()),                         // 투자 자산 분류 아이콘 리스트
                            strategyEntity.getStrategyTitle(),                             // 전략명
                            BigDecimal.ZERO,                                               // 누적 손익률 (초기값)
                            BigDecimal.ZERO,                                               // 최근 1년 손익률 (초기값)
                            BigDecimal.ZERO,                                               // MDD (초기값)
                            strategyEntity.getSmScore(),                                   // SM-Score
                            strategyEntity.getFollowersCount(),                            // 팔로워 수
                            null                                                           // 누적 수익률 리스트 (초기값)
                    );

                    // 최신 일간 통계 데이터 추가
                    DailyStatisticsEntity latestStatistics = latestStatisticsMap.get(strategyEntity.getStrategyId());
                    if (latestStatistics != null) {
                        dto.setCumulativeProfitLossRate(latestStatistics.getCumulativeProfitLossRate());
                        dto.setRecentOneYearReturn(latestStatistics.getRecentOneYearReturn());
                        dto.setMdd(latestStatistics.getMaxDrawdownRate());
                    }

                    // 누적 수익률 리스트 추가
                    List<Double> cumulativeProfitLossRates = cumulativeProfitLossRateMap.get(strategyEntity.getStrategyId());
                    dto.setCumulativeProfitLossRateList(cumulativeProfitLossRates);

                    return dto;
                }).collect(Collectors.toList());

        // 7. DTO 리스트를 페이지 객체로 변환
        Page<AdvancedSearchResultDto> dtoPage = new PageImpl<>(dtoList, pageable, findStrategyPage.getTotalElements());

        // 8. 페이지 응답 생성 및 반환
        return createPageResponse(dtoPage);
    }

    //3. 전략 상세
    /**
     * 3-1. 전략 상세페이지 기본정보 조회 메서드
     *
     * @return StrategyResponseDto - 전략 기본정보 DTO
     * TODO) 트레이더는 비공개한 자신의 전략상세를 볼 수 있다. 관리자는 모든 전략의 상세를 볼 수 있다. 유저는 공개만 볼 수 있다.
     *
     */
    @Transactional
    public StrategyResponseDto getStrategyDetails(Long id) {
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

        //작성자 정보 넣기
        memberRepository.findById(strategyEntity.getWriterId())
                .ifPresentOrElse(
                        memberEntity -> {
                            responseDto.setMemberId(memberEntity.getMemberId());
                            responseDto.setNickname(memberEntity.getNickname());
                            responseDto.setProfilePath(memberEntity.getProfilePath());
                        },
                        () -> {
                            responseDto.setMemberId(null);
                            responseDto.setNickname(null);
                            responseDto.setProfilePath(null);
                        }
                );

        // 최신 팔로워 수 조회
        Long followersCount = strategyRepo.findFollowersCountByStrategyId(id);
        responseDto.setFollowersCount(followersCount);

        // 제안서 정보
        strategyProposalService.getProposalByStrategyId(strategyEntity.getStrategyId())
                .ifPresentOrElse(
                        proposal -> {
                            responseDto.setStrategyProposalFileTitle(proposal.getFileTitle());
                            responseDto.setStrategyProposalLink(proposal.getFileLink());
                        },
                        () -> {
                            responseDto.setStrategyProposalFileTitle(null);
                            responseDto.setStrategyProposalLink(null);
                        }
                );

        return responseDto;
    }

    //4. 전략 삭제
    /**
     * 4-1. 전략을 삭제하는 메서드
     * 전략테이블에선 삭제가 되고 전략 이력 테이블에선 삭제 이력 데이터가 추가된다.
     * 관계테이블도 삭제가 되고 이력테이블에 삭제 이력이 추가된다.
     * <p>
     * 전략 이력 등록시작 -> 전략 관계 테이블 이력 등록 -> 전략 관계 테이블 삭제 -> 전략 삭제 -> 전략 이력 등록끝
     */
    @Transactional
    public void deleteStrategy(Long id, String adminId) {
        //작성자 있는지 확인
        MemberEntity memberEntity = memberRepository.findById(adminId)
                .orElseThrow(() -> new MemberNotFoundException("해당 회원이 존재하지 않습니다."));

        //1. 전략 이력 등록 시작
        //전략 이력 엔티티 생성
        StrategyHistoryEntity strategyHistoryEntity = new StrategyHistoryEntity();
        strategyHistoryEntity.setChangeStartDate(LocalDateTime.now());

        //1. 전략의 id를 검색해서 유무 판별
        StrategyEntity strategyEntity = strategyRepo.findById(id).orElseThrow(
                () -> new NoSuchElementException());

        //TODO) 메서드로 빼기
        //2. 해당 전략의 정보를 전략 이력엔티티에 담는다.
        strategyHistoryEntity.setStrategyId(strategyEntity.getStrategyId());
        strategyHistoryEntity.setTradingTypeId(strategyEntity.getTradingTypeEntity().getTradingTypeId());
        strategyHistoryEntity.setTradingCycle(strategyEntity.getTradingCycleEntity().getTradingCycleId());
        strategyHistoryEntity.setStrategyStatusCode(strategyEntity.getStrategyStatusCode());
        strategyHistoryEntity.setStrategyHistoryStatusCode("STRATEGY_STATUS_DELETED");
        strategyHistoryEntity.setMinInvestmentAmount(strategyEntity.getMinInvestmentAmount());
        strategyHistoryEntity.setStrategyTitle(strategyEntity.getStrategyTitle());
        strategyHistoryEntity.setWriterId(strategyEntity.getWriterId());
        strategyHistoryEntity.setIsPosted(strategyEntity.getIsPosted());
        strategyHistoryEntity.setIsApproved(strategyEntity.getIsApproved());
        strategyHistoryEntity.setWritedAt(strategyEntity.getWritedAt());
        strategyHistoryEntity.setStrategyOverview(strategyEntity.getStrategyOverview());
        strategyHistoryEntity.setUpdaterId(adminId);
        strategyHistoryEntity.setUpdatedAt(LocalDateTime.now());
        strategyHistoryEntity.setExitDate(strategyEntity.getExitDate());

        //3. 전략 관계 테이블 이력 등록
        List<StrategyIACEntity> strategyIACEntities = strategyEntity.getStrategyIACEntities();
        for(StrategyIACEntity strategyIACEntity : strategyIACEntities) {
            StrategyIACHistoryEntity strategyIACHistoryEntity = new StrategyIACHistoryEntity();
            strategyIACHistoryEntity.setStrategyId(strategyIACEntity.getStrategyEntity().getStrategyId());
            strategyIACHistoryEntity.setInvestmentAssetClassId(strategyIACEntity.getInvestmentAssetClassesEntity().getInvestmentAssetClassesId());
            strategyIACHistoryEntity.setWriterId(strategyIACEntity.getWritedBy());
            strategyIACHistoryEntity.setWritedAt(strategyIACEntity.getWritedAt());
            strategyIACHistoryEntity.setUpdaterId(strategyHistoryEntity.getUpdaterId());
            strategyIACHistoryEntity.setUpdatedAt(LocalDateTime.now());
            strategyIACHistoryEntity.setStatus("STRATEGYIAC_STATUS_DELETED");
            strategyIACHistoryRepository.save(strategyIACHistoryEntity);
        }

        //4. 전략 제안서가 있는 경우, 제안서 데이터 삭제 (sbwoo)
        if(strategyProposalService.getProposalByStrategyId(strategyEntity.getStrategyId()).isPresent()){
            strategyProposalService.deleteProposal(strategyEntity.getStrategyId(), strategyEntity.getWriterId());
        }

        //5. 실계좌 인증이 있는 경우, 실계좌 인증 데이터 삭제 (sbwoo)
        if(!liveAccountDataRepository.findAllByStrategy(strategyEntity).isEmpty()){
            liveAccountDataService.deleteAllLiveAccountData(strategyEntity.getStrategyId());
        }

        //6. 해당 전략을 삭제한다. - 관계 테이블도 함께 삭제됨
        strategyRepo.deleteById(strategyEntity.getStrategyId());

        //7. 전략 이력엔티티의 내용을 전략 이력 테이블에 저장한다.
        strategyHistoryEntity.setChangeEndDate(LocalDateTime.now());
        strategyHistoryRepo.save(strategyHistoryEntity);

    }

    //5. 전략 수정
    /**
     * 5-1. 전략 기본정보 수정하는 페이지를 보여주는 메서드
     *
     * 투자주기, 매매유형, 투자자산 분류, 전략 정보 보내기
     * TODO)운용상태 담기, 운용종료한 전략은 수정이 불가능하도록 설정
     *
     */
    @Transactional
    public Map<String, Object> getStrategyUpdateForm(Long id){
        //전략 정보 가져오기
        StrategyEntity strategyEntity = strategyRepo.findById(id).orElseThrow(() ->
                new NoSuchElementException());

        //운용상태 판별
        if (strategyEntity.getStrategyStatusCode().equals("STRATEGY_OPERATION_TERMINATED")){
            throw new StrategyTerminatedException("운용종료된 전략은 수정할 수 없습니다.");
        }

        //TradingType, InvestmentAssetClass 및 TradingCycle 데이터를 각각 DTO 리스트로 변환
        List<TradingTypeRegistrationDto> tradingTypeDtos = convertToTradingTypeDtos(tradingTypeRepository.findByIsActiveOrderByTradingTypeOrderAsc("Y"));
        List<InvestmentAssetClassesRegistrationDto> investmentAssetClassDtos = convertToInvestmentAssetClassDtos(investmentAssetClassesRepository.findByIsActiveOrderByOrderAsc("Y"));
        List<TradingCycleRegistrationDto> tradingCycleDtos = convertToTradingCycleDtos(tradingCycleRepository.findByIsActiveOrderByTradingCycleOrderAsc("Y"));

        // DTO 설정 및 반환
        StrategyRegistrationDto strategyRegistrationDto = new StrategyRegistrationDto();
        strategyRegistrationDto.setTradingTypeRegistrationDtoList(tradingTypeDtos);
        strategyRegistrationDto.setInvestmentAssetClassesRegistrationDtoList(investmentAssetClassDtos);
        strategyRegistrationDto.setTradingCycleRegistrationDtoList(tradingCycleDtos);

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

        //작성자 정보 넣기
        memberRepository.findById(strategyEntity.getWriterId())
                .ifPresentOrElse(
                        memberEntity -> {
                            responseDto.setMemberId(memberEntity.getMemberId());
                            responseDto.setNickname(memberEntity.getNickname());
                            responseDto.setProfilePath(memberEntity.getProfilePath());
                        },
                        () -> {
                            responseDto.setMemberId(null);
                            responseDto.setNickname(null);
                            responseDto.setProfilePath(null);
                        }
                );

        // 등록되어있는 제안서 정보
        strategyProposalService.getProposalByStrategyId(strategyEntity.getStrategyId())
                .ifPresentOrElse(
                        proposal -> {
                            responseDto.setStrategyProposalFileTitle(proposal.getFileTitle());
                            responseDto.setStrategyProposalLink(proposal.getFileLink());
                        },
                        () -> {
                            responseDto.setStrategyProposalFileTitle(null);
                            responseDto.setStrategyProposalLink(null);
                        }
                );

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("Data", responseDto);
        responseMap.put("Requirements", strategyRegistrationDto);

        return responseMap;
    }

    /**
     * 5-2. 전략 기본정보를 수정하는 메서드
     *
     * @param strategyPayloadDto 전략 수정에 필요한 내용을 담은DTO
     * 전략 수정폼 -> 전략 수정 -> 전략 수정 요청
     * 전략 수정 이력 등록 시작 -> 전략 수정 -> 전략 관계 테이블 clear() ->  전략 관계 테이블 수정 이력 등록->
     * 전략 이력 등록 끝
     * 생성자는 안바뀌고 수정자는 현재 수정자로 바뀐다.
     */
    @Transactional
    public Map<String, Long> updateStrategy(String updaterId, Long strategyId, StrategyPayloadDto strategyPayloadDto) {
        //수정자 정보 확인
        MemberEntity memberEntity = memberRepository.findById(updaterId)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));

        //1. 전략 수정 이력 등록 시작
        //전략 이력 엔티티 생성
        StrategyHistoryEntity strategyHistoryEntity = new StrategyHistoryEntity();
        strategyHistoryEntity.setChangeStartDate(LocalDateTime.now());

        //2. 전략의 id를 검색해서 유무 판별
        StrategyEntity strategyEntity = strategyRepo.findById(strategyId).orElseThrow(
                () -> new NoSuchElementException("해당 전략을 찾을 수 없습니다."));

        //2-1. 운용 종료된 전략은 수정불가
        if (strategyEntity.getStrategyStatusCode().equals("STRATEGY_OPERATION_TERMINATED")){
            throw new StrategyTerminatedException("운용종료된 전략은 수정할 수 없습니다.");
        }

        //3. payload의 id값으로 필요한 객체들을 찾아온다.
        //페이로드에서 가져온 매매유형 id로 해당 매매유형 엔티티가져오기
        TradingTypeEntity tradingTypeEntity = tradingTypeRepository.findById(strategyPayloadDto.getTradingTypeId())
                .orElseThrow(() -> new TradingTypeNotFoundException(strategyPayloadDto.getTradingTypeId()));
        //페이로드에서 가져온 주기 id로 해당 주기 엔티티 가져오기
        TradingCycleEntity tradingCycleEntity = tradingCycleRepository.findById(strategyPayloadDto.getTradingCycleId())
                .orElseThrow(() -> new TradingCycleNotFoundException(strategyPayloadDto.getTradingCycleId()));

        //4. 전략테이블의 데이터를 수정한다.(전략엔티티에 payload를 넣고 save)
        strategyEntity.setStrategyTitle(strategyPayloadDto.getStrategyTitle());
        strategyEntity.setTradingTypeEntity(tradingTypeEntity);
        strategyEntity.setTradingCycleEntity(tradingCycleEntity);
        strategyEntity.setMinInvestmentAmount(strategyPayloadDto.getMinInvestmentAmount());
        strategyEntity.setStrategyOverview(strategyPayloadDto.getStrategyOverview());
        strategyEntity.setIsPosted(strategyPayloadDto.getIsPosted());
        strategyEntity.setUpdaterId(updaterId);
        strategyEntity.setUpdatedAt(LocalDateTime.now());

        //변경된 값으로 전략 저장
        strategyRepo.save(strategyEntity);

        //5. 전략 - IAC 테이블 이력등록, 삭제
        //관계테이블 엔티티 리스트 가져오기
        List<StrategyIACEntity> relationList = strategyEntity.getStrategyIACEntities();

        //관계테이블 엔티티 리스트를 관계이력테이블에 저장한다.
        List<StrategyIACHistoryEntity> relationHistoryEntities = new ArrayList<>();
        for(StrategyIACEntity relation : relationList) {
            StrategyIACHistoryEntity strategyIACHistoryEntity = new StrategyIACHistoryEntity();
            strategyIACHistoryEntity.setStrategyId(relation.getStrategyEntity().getStrategyId());
            strategyIACHistoryEntity.setInvestmentAssetClassId(relation.getInvestmentAssetClassesEntity().getInvestmentAssetClassesId());
            strategyIACHistoryEntity.setWriterId(relation.getWritedBy());
            strategyIACHistoryEntity.setWritedAt(relation.getWritedAt());
            strategyIACHistoryEntity.setUpdaterId(relation.getStrategyEntity().getUpdaterId());
            strategyIACHistoryEntity.setUpdatedAt(LocalDateTime.now());
            strategyIACHistoryEntity.setStatus("STRATEGYIAC_STATUS_DELETED");

            relationHistoryEntities.add(strategyIACHistoryEntity);
        }
        strategyIACHistoryRepository.saveAll(relationHistoryEntities);

        //6. 관계테이블 clear()
        relationList.clear();

        //7. 변경된 투자자산 분류 관계테이블 데이터 입력
        //페이로드에서 투자자산 분류 id 가져오기
        List<Integer> newIacIdList = strategyPayloadDto.getInvestmentAssetClassesIdList();
        //해당되는 투자자산 분류 엔티티들 가져오기
        List<InvestmentAssetClassesEntity> newIacList = investmentAssetClassesRepository.findAllById(newIacIdList);

        //isActive 검증
        List<Integer> inactiveIacIds = newIacList.stream()
                .filter(iacEntity -> "N".equals(iacEntity.getIsActive()))
                .map(InvestmentAssetClassesEntity::getInvestmentAssetClassesId)
                .toList();
        if(!inactiveIacIds.isEmpty()) {
            throw new InvestmentAssetClassesNotActiveException("isActive가 'N'인 투자자산 분류가 포함되어 있습니다.");
        }

        //요청한 ID와 조회된 ID 비교 - 없는 투자자산 분류인지 검증
        Set<Integer> foundIds = newIacList.stream()
                .map(InvestmentAssetClassesEntity::getInvestmentAssetClassesId)
                .collect(Collectors.toSet());
        List<Integer> missingIds = newIacIdList.stream()
                .filter(newId -> !foundIds.contains(newId))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new InvestmentAssetClassesNotFoundException("INVESTMENT_ASSET_CLASSES_NOT_EXIST");
        }

        //관계테이블 데이터 저장
        List<StrategyIACEntity> strategyIACEntityList = newIacList.stream().map(strategyIacEntity -> {
            StrategyIACEntity strategyIACEntity = new StrategyIACEntity();
            strategyIACEntity.setStrategyEntity(strategyEntity);
            strategyIACEntity.setInvestmentAssetClassesEntity(strategyIacEntity);
            strategyIACEntity.setWritedBy(strategyEntity.getWriterId()); //전략 생성자id
            strategyIACEntity.setWritedAt(strategyEntity.getWritedAt()); //전략 생성일시
            strategyIACEntity.setUpdatedBy(strategyEntity.getUpdaterId()); //전략 수정자id
            strategyIACEntity.setUpdatedAt(LocalDateTime.now());
            return strategyIACEntity;
        }).collect(Collectors.toList());

        strategyIACRepository.saveAll(strategyIACEntityList);

        //8. 투자자산 분류 관계테이블 생성 이력 추가
        //관계테이블 엔티티 리스트를 관계이력테이블에 저장한다.
        List<StrategyIACHistoryEntity> newRelationHistoryEntities = strategyIACEntityList.stream().map(newRelation -> {
            StrategyIACHistoryEntity newRelationHistoryEntity = new StrategyIACHistoryEntity();
            newRelationHistoryEntity.setStrategyId(newRelation.getStrategyEntity().getStrategyId());
            newRelationHistoryEntity.setInvestmentAssetClassId(newRelation.getInvestmentAssetClassesEntity().getInvestmentAssetClassesId());
            newRelationHistoryEntity.setWriterId(newRelation.getWritedBy());
            newRelationHistoryEntity.setWritedAt(newRelation.getWritedAt());
            newRelationHistoryEntity.setUpdaterId(newRelation.getUpdatedBy());
            newRelationHistoryEntity.setUpdatedAt(newRelation.getUpdatedAt());
            newRelationHistoryEntity.setStatus("STRATEGYIAC_STATUS_UPDATED");
            return newRelationHistoryEntity;
        }).collect(Collectors.toList());

        strategyIACHistoryRepository.saveAll(newRelationHistoryEntities);

        //9. 관계 이력 테이블에 데이터 추가
        strategyHistoryEntity.setStrategyId(strategyEntity.getStrategyId());
        strategyHistoryEntity.setTradingTypeId(strategyEntity.getTradingTypeEntity().getTradingTypeId());
        strategyHistoryEntity.setTradingCycle(strategyEntity.getTradingCycleEntity().getTradingCycleId());
        strategyHistoryEntity.setStrategyStatusCode(strategyEntity.getStrategyStatusCode());
        strategyHistoryEntity.setStrategyHistoryStatusCode("STRATEGY_STATUS_UPDATED");
        strategyHistoryEntity.setMinInvestmentAmount(strategyEntity.getMinInvestmentAmount());
        strategyHistoryEntity.setStrategyTitle(strategyEntity.getStrategyTitle());
        strategyHistoryEntity.setWriterId(strategyEntity.getWriterId());
        strategyHistoryEntity.setIsPosted(strategyEntity.getIsPosted());
        strategyHistoryEntity.setIsApproved(strategyEntity.getIsApproved());
        strategyHistoryEntity.setWritedAt(strategyEntity.getWritedAt());
        strategyHistoryEntity.setStrategyOverview(strategyEntity.getStrategyOverview());
        strategyHistoryEntity.setUpdaterId(strategyEntity.getUpdaterId());
        strategyHistoryEntity.setUpdatedAt(strategyEntity.getUpdatedAt());
        strategyHistoryEntity.setExitDate(strategyEntity.getExitDate());

        strategyHistoryEntity.setChangeEndDate(LocalDateTime.now());
        strategyHistoryRepo.save(strategyHistoryEntity);

        // 10. 제안서 수정 (sbwoo)
        // strategyPayloadDto.ProposalLink(이하 link)이 null이 아니고, 링크의 변화가 있다면 제안서 등록
        if (strategyPayloadDto.getStrategyProposalLink() != null) {

            // 파일이 업로드 되어 있는 링크라면
            if (fileService.getFileMetadataByFilePath(strategyPayloadDto.getStrategyProposalLink()) != null) {

                Optional<StrategyProposalEntity> existingProposal = strategyProposalRepository.findByStrategy(strategyEntity);

                // strategyProposal이 있으면 수정
                if (existingProposal.isPresent()) {
                    String existingFileLink = existingProposal.get().getFileLink();

                    // 기존 파일 링크와 같으면 수정하지 않음
                    if (!existingFileLink.equals(strategyPayloadDto.getStrategyProposalLink())) {
                        strategyProposalService.modifyProposal(strategyPayloadDto.getStrategyProposalLink(), strategyEntity.getWriterId(), strategyEntity.getStrategyId());
                    }
                } else {
                    // 없으면 새로 등록
                    strategyProposalService.uploadProposal(strategyPayloadDto.getStrategyProposalLink(), strategyEntity.getWriterId(), strategyEntity.getStrategyId());
                }
            } else { // 잘못된 링크를 보내주면 에러 메시지
                throw new FileMetadataNotFoundException("The provided file link is invalid or does not exist : " + strategyPayloadDto.getStrategyProposalLink());
            }

        } else { // link 로 null 값이 들어오면,
            // 기존 strategyProposal이 있으면, strategyProposal 삭제
            if(strategyProposalRepository.findByStrategy(strategyEntity).isPresent()){
                strategyProposalService.deleteProposal(strategyEntity.getStrategyId(), strategyEntity.getWriterId());
            }
            // 기존 strategyProposal도 없으면 아무일 없음
        }

        // 11. 응답
        Map<String, Long> responseMap = new HashMap<>();
        responseMap.put("Strategy_Id", strategyEntity.getStrategyId());
        return responseMap;
    }

    //6. 전략 종료
    /**
     * 6. 전략을 운용 종료하는 메서드
     *
     * @Param strategyId 해당 전략 id
     * 1) 전략 status code 가 STRATEGY_OPERATION_TERMINATED로 변경되고 운용 종료일에 종료 일시를 기록한다.
     * 2) 전략 내역에 운용 종료 로그를 기록한다.
     *
     */
    public Map<String, Long> terminateStrategy(Long strategyId, String adminId) {
        MemberEntity admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 관리자가 존재하지 않습니다."));

        //1.전략 수정
        //id 값으로 해당 전략엔티티를 가져온다.
        StrategyEntity strategyEntity = strategyRepo.findById(strategyId).orElseThrow(() ->
                new NoSuchElementException("종료하려는 전략이 존재하지 않습니다."));

        //만약 운용종료상태이면 이미 종료되었다면 예외 반환
        if(strategyEntity.getStrategyStatusCode().equals("STRATEGY_OPERATION_TERMINATED")) {
            throw new StrategyAlreadyTerminatedException("전략이 이미 운용종료된 상태입니다.");
        }

        //수정시작일시 설정
        LocalDateTime changeStartDatetime = LocalDateTime.now();

        //운용종료 상태코드, 운용종료일시 설정
        strategyEntity.setStrategyStatusCode("STRATEGY_OPERATION_TERMINATED");
        strategyEntity.setExitDate(LocalDateTime.now());
        //수정자, 수정일시 설정
        strategyEntity.setUpdaterId(admin.getMemberId());
        strategyEntity.setUpdatedAt(LocalDateTime.now());
        //저장
        strategyRepo.save(strategyEntity);

        //2. 전략 수정 이력
        StrategyHistoryEntity strategyHistoryEntity = new StrategyHistoryEntity(strategyEntity, "STRATEGY_STATUS_TERMINATED", changeStartDatetime);
        strategyHistoryRepo.save(strategyHistoryEntity);

        //3. 반환값 생성
        Map<String, Long> responseMap = new HashMap<>();
        responseMap.put("Strategy_Id", strategyEntity.getStrategyId());
        return responseMap;
    }

    //7. 전략 승인 요청
    /**
     * 7. 전략 승인을 요청하는 메서드
     *
     * @Param strategyId 해당 전략 id, applicantId 요청자의 id
     * 해당전략 id로 엔티티를 가져오고 등록일 부터 일일 데이터 갯수가 3개인지, isApproved=N인지 판별
     * 이후 전략 승인 요청 목록에 엔티티 생성해서 등록, 전략 엔티티의 is_Approved = P로 변경
     */
    @Transactional
    public Map<String, Long> approvalRequest(Long strategyId, String applicantId){
        //승인 요청자 정보 가져오기
        MemberEntity applicantEntity = memberRepository.findById(applicantId).orElseThrow(
                () -> new NoSuchElementException("해당 트레이더를 찾을 수 없습니다.")
        );

        //승인 요청할 전략 정보 가져오기
        StrategyEntity strategyEntity = strategyRepo.findById(strategyId).orElseThrow(
                () -> new NoSuchElementException("해당 전략을 찾을 수 없습니다."));

        //전략 등록일을 가져와서 이후 일일 거래 데이터 3개 이상이면 진행
        //3개 미만이면 예외를 던진다.
        LocalDateTime createDatetime = strategyEntity.getWritedAt();
        LocalDate createDate = createDatetime.toLocalDate();
        if(dailyStatisticsRepository.countByDateBetween(createDate, LocalDate.now()) < 3){
            throw new DailyDataNotEnoughException("일일 거래 데이터가 3개 이상인 경우에만 승인 요청을 보낼 수 있습니다.");
        }

        //이미 승인받은 전략은 승인요청을 보낼 수 없다.
        if(strategyEntity.getIsApproved().equals("Y")){
            throw new StrategyAlreadyApprovedException("이미 승인받은 전략입니다.");
        }

        //승인 요청중인 전략은 승인요청을 보낼 수 없다.
        if(strategyEntity.getIsApproved().equals("P")){
            throw new StrategyAlreadyApprovedException("승인 요청중인 전략입니다.");
        }

        //새 요청 엔티티 생성
        StrategyApprovalRequestsEntity approvalRequestsEntity = new StrategyApprovalRequestsEntity();

        //요청 엔티티에 요청 정보 넣기
        approvalRequestsEntity.setRequestDatetime(LocalDateTime.now());
        approvalRequestsEntity.setIsApproved("P"); //승인여부 결정안됐으니 P 설정
        approvalRequestsEntity.setStrategy(strategyEntity);
        approvalRequestsEntity.setIsPosted(strategyEntity.getIsPosted());
        approvalRequestsEntity.setApplicant(applicantEntity);

        //요청 엔티티 저장, 해당 id 값 받기
        StrategyApprovalRequestsEntity savedRequest = strategyApprovalRequestsRepository.save(approvalRequestsEntity);

        //해당 전략의 is_Approved = P로 변경
        LocalDateTime changeStartDatetime = LocalDateTime.now(); //변경시작시간
        strategyEntity.setIsApproved("P");
        strategyEntity.setUpdaterId(applicantEntity.getMemberId());
        strategyEntity.setUpdatedAt(LocalDateTime.now());
        strategyRepo.save(strategyEntity);

        //전략 이력에 P변경 내역 넣기
        StrategyHistoryEntity changedHistory = new StrategyHistoryEntity(strategyEntity, "STRATEGY_STATUS_UPDATED", changeStartDatetime);
        strategyHistoryRepo.save(changedHistory);

        //전략 Id값, 요청 Id값 반환
        Map<String, Long> responseMap = new HashMap<>();
        responseMap.put("Strategy_Id", strategyEntity.getStrategyId());
        responseMap.put("request_Id", savedRequest.getStrategyApprovalRequestsId());
        return responseMap;
    }

    /**
     * 7-1. 해당 전략의 승인 요청 데이터를 가져오는 메서드
     *
     * @Param strategyId 해당 전략 id
     * 전략 승인 요청 목록에서 해당 전략 id의 데이터를 가져와 보여줌.(is_Approved = N 인 것만)
     */
    @Transactional
    public Map<String, Object> findRequestByStrategyId(Long strategyId){
        //전략 id로 해당되는 요청 가져오기 - isApproved = N인것중 제일 최신것만 가져옴
        StrategyApprovalRequestsEntity approvalRequest = strategyApprovalRequestsRepository.findLatestRejectedRequestByStrategyId(strategyId)
                .orElseThrow(() -> new NoSuchElementException());

        RejectionRequestResponseDto rejectionDto = new RejectionRequestResponseDto();
        rejectionDto.setStrategyApprovalRequestId(approvalRequest.getStrategyApprovalRequestsId());
        rejectionDto.setRequestDatetime(approvalRequest.getRequestDatetime());
        rejectionDto.setIsApproved(approvalRequest.getIsApproved());
        rejectionDto.setStrategyId(approvalRequest.getStrategy().getStrategyId());
        rejectionDto.setIsPosted(approvalRequest.getIsPosted());
        rejectionDto.setApplicantId(approvalRequest.getApplicant().getMemberId());
        rejectionDto.setManagerNickname(approvalRequest.getAdmin().getNickname());
        rejectionDto.setProfileImg(approvalRequest.getAdmin().getFileId());
        rejectionDto.setRejectionReason(approvalRequest.getRejectionReason());
        rejectionDto.setRejectionDatetime(approvalRequest.getRejectionDatetime());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", rejectionDto);
        return responseMap;
    }

    // 8. 해당 전략의 팔로워수 증가
    /**
     * 특정 전략과 관련된 Strategy의 FollowersCount를 각각 1씩 증가시킵니다.
     *
     * @param strategyId FollowersCount를 증가시킬 전략의 ID
     */
    @Transactional
    public void increaseFollowersCount(Long strategyId) {
        // 1. StrategyEntity의 FollowersCount 증가
        StrategyEntity strategy = strategyRepo.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 전략이 존재하지 않습니다: " + strategyId));
        strategy.setFollowersCount(strategy.getFollowersCount() + 1);

        // 3. 변경 사항 저장
        strategyRepo.save(strategy); // StrategyEntity 저장
    }

    // 9. 전략 상세 차트 옵션 2개 조회
    /**
     * 특정 전략 ID에 대한 선택된 데이터 옵션을 날짜순으로 조회
     *
     * @param strategyId 전략 ID
     * @param option1 첫 번째 데이터 옵션 (예: "referencePrice", "balance" 등)
     * @param option2 두 번째 데이터 옵션 (예: "dailyProfitLoss", "cumulativeProfitLoss" 등)
     * @return DailyStatisticsChartResponseDto (전략 차트 데이터와 타임스탬프)
     */
    public DailyStatisticsChartResponseDto getStrategyChartDetails(Long strategyId, String option1, String option2) {
        // Repository에서 데이터를 조회하여 Map 형태로 반환
        Map<String, List<?>> chartData = strategyRepo.findChartDataByOptions(strategyId, option1, option2);

        // 반환된 데이터가 null 또는 비어 있을 경우 예외 처리
        if (chartData == null || chartData.isEmpty()) {
            throw new IllegalArgumentException("요청하신 데이터가 존재하지 않습니다. 전략 ID: " + strategyId);
        }

        // 현재 UTC 시간 추가
        String timestamp = Instant.now().toString();

        // DTO 생성 및 반환
        return new DailyStatisticsChartResponseDto(chartData, timestamp);
    }

    /**
     * 회원 탈퇴 시 전략과 전략에 관련된 데이터 모두 삭제하는 메소드
     * 관련 데이터 : 전략이력, 전략제안서, 실계좌인증, 전략승인요청, 관계테이블이력, 일간통계, 월간통계, 관심전략, 상담, 리뷰
     */
    @Transactional
    public void deleteStrategyForWithdrawal(StrategyEntity strategy) {
        Long strategyId = strategy.getStrategyId();

        // TODO) 전략 ID로 등록된 리뷰를 모두 삭제한다.

        strategyHistoryRepo.deleteAllByStrategyId(strategyId);  // 전략이력 삭제 [X]
        if(strategyProposalService.getProposalByStrategyId(strategy.getStrategyId()).isPresent()){  // 전략제안서 삭제
            strategyProposalService.deleteProposal(strategy.getStrategyId(), strategy.getWriterId());
        }
        if(!liveAccountDataRepository.findAllByStrategy(strategy).isEmpty()){  // 실계좌인증 삭제
            liveAccountDataService.deleteAllLiveAccountData(strategy.getStrategyId());
        }
        strategyApprovalRequestsRepository.deleteAllByStrategy(strategy);  // 전략승인요청 삭제
        monthlyStatisticsRepository.deleteByStrategyEntity(strategy);  // 월간통계 삭제 [X]
        dailyStatisticsRepository.deleteAllByStrategyEntity(strategy);  // 일간통계 삭제 [X]
        followingStrategyRepository.deleteAllByStrategy(strategy);  // 관심전략 삭제 [X]
        consultationRepository.deleteAllByStrategy(strategy);  // 상담 삭제 [X]
        strategyIACHistoryRepository.deleteAllByStrategyId(strategyId);  // 관계테이블이력 삭제 [X]
        strategyIACRepository.deleteAllByStrategyEntity(strategy);  // 관계테이블 삭제 [X]

        strategyRepo.delete(strategy);  // 전략 삭제 [X]
    }
}