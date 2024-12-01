package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import com.sysmatic2.finalbe.attachment.service.FileService;
import com.sysmatic2.finalbe.attachment.service.ProposalService;
import com.sysmatic2.finalbe.exception.DuplicateProposalException;
import com.sysmatic2.finalbe.exception.MetadataNotFoundException;
import com.sysmatic2.finalbe.strategy.dto.StrategyProposalDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyProposalEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyProposalRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyProposalService {
    private final StrategyProposalRepository strategyProposalRepository;
    private final StrategyRepository strategyRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final ProposalService proposalService;
    private final FileService fileService;

    /**
     * 제안서 DB 등록
     */
    @Transactional
    public StrategyProposalDto uploadProposal(String proposalLink, String uploaderId, Long strategyId) {
        try {
            // strategyId로 StrategyEntity를 조회
            StrategyEntity strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid strategy ID: " + strategyId));

            // 1. proposalLink가 strategy_proposal에 이미 등록되어 있는지 확인
            String existingProposalLink = strategyProposalRepository.findByFileLink(proposalLink)
                    .map(StrategyProposalEntity::getFileLink) // Optional<String>으로 변환
                    .orElse(null); // 값이 없으면 null 반환

            if (existingProposalLink != null) {
                // 중복 데이터가 있으면 예외 발생
                throw new DuplicateProposalException("Duplicate proposal link found for file path: " + proposalLink);
            }

            // 2. proposalLink로 메타데이터 조회
            Optional<FileMetadataDto> metadata = proposalService.getProposalUrlByFilePath(proposalLink);

            if (metadata.isEmpty()) {
                // 메타데이터가 없으면 예외 발생
                throw new MetadataNotFoundException("No metadata found for proposal link: " + proposalLink);
            }

            // 3. 메타데이터를 사용하여 StrategyProposalEntity 생성
            FileMetadataDto proposalMetadataDto = metadata.get();

            StrategyProposalEntity proposalEntity = new StrategyProposalEntity();
            proposalEntity.setStrategy(strategy);
            proposalEntity.setFileLink(proposalMetadataDto.getFilePath());
            proposalEntity.setFileTitle(proposalMetadataDto.getDisplayName());
            proposalEntity.setFileType(proposalMetadataDto.getContentType());
            proposalEntity.setWritedAt(LocalDateTime.now());
            proposalEntity.setWriterId(uploaderId);

            // 시스템 컬럼 설정
            proposalEntity.setCreatedBy(uploaderId);
            proposalEntity.setCreatedAt(LocalDateTime.now());
            proposalEntity.setModifiedBy(uploaderId);
            proposalEntity.setModifiedAt(LocalDateTime.now());

            // 4. 엔티티 저장
            StrategyProposalEntity savedEntity = strategyProposalRepository.save(proposalEntity);

            // 5. 메타데이터 업데이트 (파일 메타데이터에 전략 ID 저장)
            proposalMetadataDto.setFileCategoryItemId(strategy.getStrategyId().toString());
            fileMetadataRepository.save(FileMetadataDto.toEntity(proposalMetadataDto));

            // 6. 저장된 엔티티를 DTO로 변환하여 반환
            return StrategyProposalDto.fromEntity(savedEntity);
        } catch (Exception e) {
            // 예외를 처리하지 않고 다시 던지면 @Transactional이 롤백을 수행
            throw new RuntimeException("Failed to upload proposal: " + e.getMessage(), e);
        }
    }

    /**
     * 제안서 DB 수정
     */
    @Transactional
    public StrategyProposalDto modifyProposal(String proposalLink, String uploaderId, Long strategyId) {
        try {
            // 1. StrategyEntity 조회
            StrategyEntity strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid strategy ID: " + strategyId));

            // 2. proposalLink가 strategy_proposal에 이미 등록되어 있는지 확인(중복 방지)
            strategyProposalRepository.findByFileLink(proposalLink)
                    .ifPresent(existingProposal -> {
                        throw new IllegalArgumentException("Duplicate Link in database: " + proposalLink);
                    });

            // 3. 원래 strategyProposal 데이터 조회
            StrategyProposalEntity proposalEntity = strategyProposalRepository.findByStrategy(strategy)
                    .orElseThrow(() -> new IllegalArgumentException("No existing strategy proposal found for strategy ID: " + strategyId));

            // 4. 기존 S3 파일 및 메타데이터 삭제
            proposalService.deleteProposal(proposalEntity.getFileLink(), proposalEntity.getWriterId());

            // 5. proposalLink로 메타데이터 조회(새로 등록할 파일 메타데이터)
            FileMetadataDto proposalMetadataDto = proposalService.getProposalUrlByFilePath(proposalLink)
                    .orElseThrow(() -> new MetadataNotFoundException("No metadata found for proposal link: " + proposalLink));

            // 6. strategyProposal 수정
            proposalEntity.setFileLink(proposalMetadataDto.getFilePath());
            proposalEntity.setFileTitle(proposalMetadataDto.getDisplayName());
            proposalEntity.setFileType(proposalMetadataDto.getContentType());
            proposalEntity.setModifiedBy(uploaderId);
            proposalEntity.setModifiedAt(LocalDateTime.now());

            // 7. 엔티티 저장
            StrategyProposalEntity savedEntity = strategyProposalRepository.save(proposalEntity);

            // 8. 메타데이터 업데이트 (파일 메타데이터에 전략 ID 저장)
            proposalMetadataDto.setFileCategoryItemId(strategy.getStrategyId().toString());
            fileMetadataRepository.save(FileMetadataDto.toEntity(proposalMetadataDto));

            // 9. 저장된 엔티티를 DTO로 변환하여 반환
            return StrategyProposalDto.fromEntity(savedEntity);
        } catch (Exception e) {
            // 예외를 처리하지 않고 다시 던지면 @Transactional이 롤백을 수행
            throw new RuntimeException("Failed to modify proposal: " + e.getMessage(), e);
        }
    }


    /**
     * 제안서 DB 삭제
     */
    @Transactional
    public void deleteProposal(Long strategyId, String writerId) {
        try {
            // strategyId로 StrategyEntity를 조회
            StrategyEntity strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid strategy ID: " + strategyId));

            // 제안서 DB를 StrategyId와 writerId로 조회
            strategyProposalRepository.findByStrategyAndWriterId(strategy, writerId)
                    .ifPresent(strategyProposalEntity -> {
                        // filePath 가져오기
                        String filePath = strategyProposalEntity.getFileLink();

                        // 제안서가 존재하면 삭제
                        strategyProposalRepository.delete(strategyProposalEntity);

                        // S3와 metadata 삭제
                        proposalService.deleteProposal(filePath, writerId);
                    });
        } catch (Exception e) {
            // 예외를 처리하지 않고 다시 던지면 @Transactional이 롤백을 수행
            throw new RuntimeException("Failed to upload proposal: " + e.getMessage(), e);
        }

    }

    /**
     * 제안서 파일 조회 - strategyId
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
