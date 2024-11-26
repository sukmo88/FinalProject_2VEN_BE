package com.sysmatic2.finalbe.util;

import com.sysmatic2.finalbe.admin.dto.*;
import com.sysmatic2.finalbe.admin.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.admin.entity.StrategyApprovalRequestsEntity;
import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.dto.StrategyResponseDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DtoEntityConversionUtils
 * <p>
 * 엔티티와 DTO 간의 변환 로직을 중앙에서 관리하는 유틸리티 클래스입니다.
 * 모든 메서드는 정적 메서드로, DTO와 엔티티 간의 변환을 간단히 수행할 수 있습니다.
 */
public class DtoEntityConversionUtils {

    // 인스턴스화를 방지하기 위한 private 생성자
    private DtoEntityConversionUtils() {}

    //투자자산 분류
    /**
     * InvestmentAssetClassesEntity를 InvestmentAssetClassesDto로 변환하는 메서드.
     *
     * @param iacEntity 변환할 InvestmentAssetClassesEntity 객체
     * @return 변환된 InvestmentAssetClassesDto 객체
     */
    public static InvestmentAssetClassesDto toDto(InvestmentAssetClassesEntity iacEntity) {
        InvestmentAssetClassesDto iacDto = new InvestmentAssetClassesDto();
        iacDto.setInvestmentAssetClassesId(iacEntity.getInvestmentAssetClassesId());
        iacDto.setOrder(iacEntity.getOrder());
        iacDto.setInvestmentAssetClassesName(iacEntity.getInvestmentAssetClassesName());
        iacDto.setInvestmentAssetClassesIcon(iacEntity.getInvestmentAssetClassesIcon());
        iacDto.setIsActive(iacEntity.getIsActive());
        return iacDto;
    }

    /**
     * InvestmentAssetClassesDto를 InvestmentAssetClassesEntity로 변환하는 메서드.
     *
     * @param iacDto 변환할 InvestmentAssetClassesDto 객체
     * @return 변환된 InvestmentAssetClassesEntity 객체
     */
    public static InvestmentAssetClassesEntity toEntity(InvestmentAssetClassesDto iacDto) {
        InvestmentAssetClassesEntity iacEntity = new InvestmentAssetClassesEntity();
        iacEntity.setOrder(iacDto.getOrder());
        iacEntity.setInvestmentAssetClassesName(iacDto.getInvestmentAssetClassesName());
        iacEntity.setInvestmentAssetClassesIcon(iacDto.getInvestmentAssetClassesIcon());
        iacEntity.setIsActive(iacDto.getIsActive());
        return iacEntity;
    }

    /**
     * InvestmentAssetClassEntity 리스트를 InvestmentAssetClassesRegistrationDto 리스트로 변환하는 메서드.
     *
     * @param assetClasses 변환할 InvestmentAssetClassEntity 리스트
     * @return 변환된 InvestmentAssetClassesRegistrationDto 리스트
     */
    public static List<InvestmentAssetClassesRegistrationDto> convertToInvestmentAssetClassDtos(List<InvestmentAssetClassesEntity> assetClasses) {
        return assetClasses.stream()
                .map(assetClass -> {
                    InvestmentAssetClassesRegistrationDto dto = new InvestmentAssetClassesRegistrationDto();
                    dto.setInvestmentAssetClassesId(assetClass.getInvestmentAssetClassesId());
                    dto.setInvestmentAssetClassesName(assetClass.getInvestmentAssetClassesName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //매매유형
    /**
     * TradingTypeEntity를 TradingTypeAdminResponseDto로 변환하는 메서드.
     *
     * @param tradingTypeEntity 변환할 TradingTypeEntity 객체
     * @return 변환된 TradingTypeAdminResponseDto 객체
     */
    public static TradingTypeAdminResponseDto toDto(TradingTypeEntity tradingTypeEntity) {
        TradingTypeAdminResponseDto dto = new TradingTypeAdminResponseDto();
        dto.setTradingTypeId(tradingTypeEntity.getTradingTypeId());
        dto.setTradingTypeOrder(tradingTypeEntity.getTradingTypeOrder());
        dto.setTradingTypeName(tradingTypeEntity.getTradingTypeName());
        dto.setTradingTypeIcon(tradingTypeEntity.getTradingTypeIcon());
        dto.setIsActive(tradingTypeEntity.getIsActive());
        return dto;
    }

    /**
     * TradingTypeAdminRequestDto를 TradingTypeEntity로 변환하는 메서드.
     *
     * @param dto 변환할 TradingTypeAdminRequestDto 객체
     * @return 변환된 TradingTypeEntity 객체
     */
    public static TradingTypeEntity toEntity(TradingTypeAdminRequestDto dto) {
        TradingTypeEntity tradingTypeEntity = new TradingTypeEntity();
        tradingTypeEntity.setTradingTypeOrder(dto.getTradingTypeOrder());
        tradingTypeEntity.setTradingTypeName(dto.getTradingTypeName());
        tradingTypeEntity.setTradingTypeIcon(dto.getTradingTypeIcon());
        tradingTypeEntity.setIsActive(dto.getIsActive());
        return tradingTypeEntity;
    }

    /**
     * TradingCycleEntity를 TradingCycleAdminResponseDto로 변환하는 메서드.
     *
     * @param tradingCycleEntity 변환할 TradingCycleEntity 객체
     * @return 변환된 TradingCycleAdminResponseDto 객체
     */
    public static TradingCycleAdminResponseDto toDto(TradingCycleEntity tradingCycleEntity) {
        TradingCycleAdminResponseDto dto = new TradingCycleAdminResponseDto();
        dto.setTradingCycleId(tradingCycleEntity.getTradingCycleId());
        dto.setTradingCycleOrder(tradingCycleEntity.getTradingCycleOrder());
        dto.setTradingCycleName(tradingCycleEntity.getTradingCycleName());
        dto.setTradingCycleIcon(tradingCycleEntity.getTradingCycleIcon());
        dto.setIsActive(tradingCycleEntity.getIsActive());
        return dto;
    }

    /**
     * TradingCycleAdminRequestDto를 TradingCycleEntity로 변환하는 메서드.
     *
     * @param dto 변환할 TradingCycleAdminRequestDto 객체
     * @return 변환된 TradingCycleEntity 객체
     */
    public static TradingCycleEntity toEntity(TradingCycleAdminRequestDto dto) {
        TradingCycleEntity tradingCycleEntity = new TradingCycleEntity();
        tradingCycleEntity.setTradingCycleOrder(dto.getTradingCycleOrder());
        tradingCycleEntity.setTradingCycleName(dto.getTradingCycleName());
        tradingCycleEntity.setTradingCycleIcon(dto.getTradingCycleIcon());
        tradingCycleEntity.setIsActive(dto.getIsActive());
        return tradingCycleEntity;
    }

    /**
     * TradingTypeEntity 리스트를 TradingTypeRegistrationDto 리스트로 변환하는 메서드.
     *
     * @param tradingTypes 변환할 TradingTypeEntity 리스트
     * @return 변환된 TradingTypeRegistrationDto 리스트
     */
    public static List<TradingTypeRegistrationDto> convertToTradingTypeDtos(List<TradingTypeEntity> tradingTypes) {
        return tradingTypes.stream()
                .map(tradingType -> {
                    TradingTypeRegistrationDto dto = new TradingTypeRegistrationDto();
                    dto.setTradingTypeId(tradingType.getTradingTypeId());
                    dto.setTradingTypeName(tradingType.getTradingTypeName());
                    dto.setTradingTypeIcon(tradingType.getTradingTypeIcon());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //전략
    /**
     * StrategyEntity의 기본정보를 StrategyResponseDto에 담는 메서드.
     *
     * @param  변환할 StrategyEntity
     * @return 변환된 StrategyResponseDto
     */
    public static StrategyResponseDto convertToStrategyDto(StrategyEntity strategyEntity) {
        StrategyResponseDto responseDto = new StrategyResponseDto();

        responseDto.setStrategyId(strategyEntity.getStrategyId());
        responseDto.setStrategyTitle(strategyEntity.getStrategyTitle());
        responseDto.setStrategyStatusCode(strategyEntity.getStrategyStatusCode());
        responseDto.setMinInvestmentAmount(strategyEntity.getMinInvestmentAmount());
        responseDto.setStrategyOverview(strategyEntity.getStrategyOverview());
        responseDto.setWritedAt(strategyEntity.getWritedAt());
        responseDto.setIsPosted(strategyEntity.getIsPosted());
        responseDto.setIsApproved(strategyEntity.getIsApproved());

        return responseDto;
    }

    /**
     * TradingCycleEntity 리스트를 TradingCycleRegistrationDto 리스트로 변환하는 메서드.
     *
     * @param tradingCycles 변환할 TradingCycleEntity 리스트
     * @return 변환된 TradingCycleRegistrationDto 리스트
     */
    public static List<TradingCycleRegistrationDto> convertToTradingCycleDtos(List<TradingCycleEntity> tradingCycles) {
        return tradingCycles.stream()
                .map(tradingCycle -> {
                    TradingCycleRegistrationDto dto = new TradingCycleRegistrationDto();
                    dto.setTradingCycleId(tradingCycle.getTradingCycleId());
                    dto.setTradingCycleName(tradingCycle.getTradingCycleName());
                    dto.setTradingCycleIcon(tradingCycle.getTradingCycleIcon());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 회원
    /**
     * SignupDTO를 MemberEntity로 변환하는 메서드.
     *
     * @param  변환할 SignupDTO
     * @return 변환된 MemberEntity로
     */
    public static MemberEntity convertToMemberEntity(SignupDTO signupDTO, PasswordEncoder passwordEncoder) {
        MemberEntity member = new MemberEntity();

        member.setMemberGradeCode("MEMBER_ROLE_" + signupDTO.getMemberType());
        member.setMemberStatusCode("MEMBER_STATUS_ACTIVE");
        member.setEmail(signupDTO.getEmail());
        member.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        member.setNickname(signupDTO.getNickname());
        member.setPhoneNumber(signupDTO.getPhoneNumber());
        member.setSignupAt(LocalDateTime.now());
        member.setIsAgreedMarketingAd(signupDTO.getMarketingOptional() ? 'Y' : 'N');

        return member;
    }

    //관리자
    /**
     * StrategyApprovalRequestsEntity ApprovalRequestRepsponseDto로 변환하는 메서드.
     *
     * @param strategyApprovalRequestsEntity
     * @return ApprovalRequestRepsponseDto
     *
     */
    public static ApprovalRequestResponseDto convertToApprovalDto(StrategyApprovalRequestsEntity requestsEntity){
        ApprovalRequestResponseDto responseDto = new ApprovalRequestResponseDto();

        responseDto.setStrategyApprovalRequestId(requestsEntity.getStrategyApprovalRequestsId());
        responseDto.setRequestDatetime(requestsEntity.getRequestDatetime());
        responseDto.setIsApproved(requestsEntity.getStrategy().getIsApproved());
        responseDto.setStrategyId(requestsEntity.getStrategy().getStrategyId());
        responseDto.setStrategyTitle(requestsEntity.getStrategy().getStrategyTitle());
        responseDto.setIsPosted(requestsEntity.getStrategy().getIsPosted());
        responseDto.setStrategyStatus(requestsEntity.getStrategy().getStrategyStatusCode());
        responseDto.setTradingTypeIcon(requestsEntity.getStrategy().getTradingTypeEntity().getTradingTypeIcon());
        responseDto.setTradingCycleIcon(requestsEntity.getStrategy().getTradingCycleEntity().getTradingCycleIcon());

        return responseDto;
    }

}
