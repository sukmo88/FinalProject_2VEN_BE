package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.strategy.dto.StrategyProposalDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyProposalEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyProposalRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyProposalService {
    private final StrategyProposalRepository strategyProposalRepository;
    private final StrategyRepository strategyRepository;

    /**
     * 제안서 DB 등록
     */
    public StrategyProposalDto uploadProposal(StrategyProposalDto strategyProposalDto, String uploaderId, Long StrategyId) {

        // strategyId로 StrategyEntity를 조회
        StrategyEntity strategy = strategyRepository.findById(StrategyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid strategy ID: " + StrategyId));

        // strategy 객체 조회 및 Dto를 entity로 변환
        StrategyProposalEntity strategyconvertToEntity = StrategyProposalDto.toEntity(strategyProposalDto, strategy);

        //최초 작성자
        // , 최초 작성일시, 최종 수정자, 최종 수정일시 넣기 - 시스템컬럼
        strategyconvertToEntity.setCreatedBy(uploaderId);
        strategyconvertToEntity.setCreatedAt(LocalDateTime.now());
        strategyconvertToEntity.setModifiedBy(uploaderId);
        strategyconvertToEntity.setModifiedAt(LocalDateTime.now());

        // entity 객체 저장
        StrategyProposalEntity savedEntity = strategyProposalRepository.save(strategyconvertToEntity);

        // dto로 변환해서 반환
        return StrategyProposalDto.fromEntity(savedEntity);
    }


    /**
     * 제안서 DB 삭제
     */
    public void deleteProposal(Long StrategyId, String writerId) {

        // strategyId로 StrategyEntity를 조회
        StrategyEntity strategy = strategyRepository.findById(StrategyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid strategy ID: " + StrategyId));

        // 제안서 DB를 StrategyId와 writerId로 조회
        strategyProposalRepository.findByStrategyAndWriterId(strategy, writerId)
                .ifPresent(strategyProposalEntity -> {
                    // 제안서가 존재하면 삭제
                    strategyProposalRepository.delete(strategyProposalEntity);
                });
    }

    /**
     * 제안서 파일 조회
     */
    public Optional<StrategyProposalDto> getProposalByStrategyId(Long strategyId) {
        // 전략 조회
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid strategy ID: " + strategyId));

        // 제안서 조회 및 dto로 변환, Optional로 반환
        return strategyProposalRepository.findByStrategy(strategyEntity)
                .map(StrategyProposalDto::fromEntity);
    }


}
