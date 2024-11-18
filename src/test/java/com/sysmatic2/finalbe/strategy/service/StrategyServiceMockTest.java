package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.repository.TradingCycleRepository;
import com.sysmatic2.finalbe.strategy.dto.StrategyListDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyRegistrationDto;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.admin.repository.InvestmentAssetClassesRepository;
import com.sysmatic2.finalbe.admin.repository.TradingTypeRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class StrategyServiceMockTest {

    @Mock
    private StrategyRepository strategyRepo;

    @Mock
    private TradingTypeRepository ttRepo;

    @Mock
    private InvestmentAssetClassesRepository iacRepo;

    @Mock
    private TradingCycleRepository tcRepo;

    @InjectMocks
    private StrategyService strategyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Strategy Registration Form 반환 테스트 - 모든 데이터가 포함되는 경우")
    void getStrategyRegistrationForm_ShouldReturnCompleteDto() {
        // Given: Mock 데이터 준비
        TradingTypeEntity tradingType = new TradingTypeEntity(1, 1, "Type A", "icon_A.png", "Description A", "Y");
        InvestmentAssetClassesEntity assetClass = new InvestmentAssetClassesEntity(1, 1, "Class A", "icon_B.png", "Description B", "Y");
        TradingCycleEntity tradingCycle = new TradingCycleEntity(1, 1, "Cycle A", "icon_C.png", "Description C", "Y");

        when(ttRepo.findByIsActiveOrderByTradingTypeOrderAsc("Y")).thenReturn(List.of(tradingType));
        when(iacRepo.findByIsActiveOrderByOrderAsc("Y")).thenReturn(List.of(assetClass));
        when(tcRepo.findByIsActiveOrderByTradingCycleOrderAsc("Y")).thenReturn(List.of(tradingCycle));

        // When: 메서드 호출
        StrategyRegistrationDto result = strategyService.getStrategyRegistrationForm();

        // Then: 반환된 DTO 검증
        assertThat(result.getTradingTypeRegistrationDtoList()).hasSize(1);
        assertThat(result.getInvestmentAssetClassesRegistrationDtoList()).hasSize(1);
        assertThat(result.getTradingCycleRegistrationDtoList()).hasSize(1);

        assertThat(result.getTradingTypeRegistrationDtoList().get(0).getTradingTypeName()).isEqualTo("Type A");
        assertThat(result.getInvestmentAssetClassesRegistrationDtoList().get(0).getInvestmentAssetClassesName()).isEqualTo("Class A");
        assertThat(result.getTradingCycleRegistrationDtoList().get(0).getTradingCycleName()).isEqualTo("Cycle A");
    }

    @Test
    @DisplayName("Strategy Registration Form 반환 테스트 - 비어있는 경우")
    void getStrategyRegistrationForm_ShouldReturnEmptyDto() {
        // Given: Mock 데이터가 비어있는 상태
        when(ttRepo.findByIsActiveOrderByTradingTypeOrderAsc("Y")).thenReturn(List.of());
        when(iacRepo.findByIsActiveOrderByOrderAsc("Y")).thenReturn(List.of());
        when(tcRepo.findByIsActiveOrderByTradingCycleOrderAsc("Y")).thenReturn(List.of());

        // When: 메서드 호출
        StrategyRegistrationDto result = strategyService.getStrategyRegistrationForm();

        // Then: 반환된 DTO가 비어있는지 확인
        assertThat(result.getTradingTypeRegistrationDtoList()).isEmpty();
        assertThat(result.getInvestmentAssetClassesRegistrationDtoList()).isEmpty();
        assertThat(result.getTradingCycleRegistrationDtoList()).isEmpty();
    }

    @Test
    @DisplayName("필터 조건에 따라 전략 목록 반환 테스트 - 정상적인 조건")
    void getStrategies_ShouldReturnPagedResult() {
        // Given: Mock 데이터와 페이징 설정
        int tradingCycleId = 1;
        int investmentAssetClassesId = 2;
        int page = 0;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(page, pageSize);
        List<StrategyListDto> mockData = List.of(
                new StrategyListDto("icon_type_1.png", "icon_cycle_1.png",
                        List.of("icon_asset_1.png", "icon_asset_2.png"),
                        "Strategy 1", 100L),
                new StrategyListDto("icon_type_2.png", "icon_cycle_2.png",
                        List.of("icon_asset_3.png", "icon_asset_4.png"),
                        "Strategy 2", 200L)
        );
        Page<StrategyListDto> mockPage = new PageImpl<>(mockData, pageable, 2);

        when(strategyRepo.findStrategiesByFilters(tradingCycleId, investmentAssetClassesId, pageable))
                .thenReturn(mockPage);

        // When: 메서드 호출
        Map<String, Object> result = strategyService.getStrategies(tradingCycleId, investmentAssetClassesId, page, pageSize);

        // Then: 반환된 결과 검증
        assertThat(result.get("data")).isNotNull();
        assertThat(((List<?>) result.get("data"))).hasSize(2);
        assertThat(result.get("pageSize")).isEqualTo(10);
        assertThat(result.get("totalPages")).isEqualTo(1);
        assertThat(result.get("totalElements")).isEqualTo(2L);
    }

    @Test
    @DisplayName("필터 조건에 따라 전략 목록 반환 테스트 - 비어있는 경우")
    void getStrategies_ShouldReturnEmptyResult() {
        // Given: Mock 데이터가 없는 상태
        int tradingCycleId = 1;
        int investmentAssetClassesId = 2;
        int page = 0;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<StrategyListDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(strategyRepo.findStrategiesByFilters(tradingCycleId, investmentAssetClassesId, pageable))
                .thenReturn(emptyPage);

        // When: 메서드 호출
        Map<String, Object> result = strategyService.getStrategies(tradingCycleId, investmentAssetClassesId, page, pageSize);

        // Then: 반환된 결과 검증
        assertThat(result.get("data")).isNotNull();
        assertThat(((List<?>) result.get("data"))).isEmpty();
        assertThat(result.get("pageSize")).isEqualTo(10);
        assertThat(result.get("totalPages")).isEqualTo(0);
        assertThat(result.get("totalElements")).isEqualTo(0L);
    }


}