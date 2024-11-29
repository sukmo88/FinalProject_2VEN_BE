package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyProposalEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StrategyProposalDto {

    private Long strategyProposalId; // 전략제안서 ID
    private Long strategyId; // 전략 ID
    private String fileTitle = "전략제안서"; // 전략제안서 제목
    private String fileLink; // 전략제안서 링크
    private Integer fileSize; // 전략제안서 사이즈
    private String fileIntroduce; // 제안서 설명
    private String fileType; // 제안서 파일 확장자
    private String writerId; // 작성자 ID
    private String updaterId; // 수정자 ID
    private LocalDateTime writedAt; // 작성일시
    private LocalDateTime updatedAt; // 수정일시

    public static StrategyProposalDto fromEntity(StrategyProposalEntity entity) {
        StrategyProposalDto dto = new StrategyProposalDto();
        dto.setStrategyProposalId(entity.getStrategyProposalId());
        dto.setStrategyId(entity.getStrategy() != null ? entity.getStrategy().getStrategyId() : null);
        dto.setFileTitle(entity.getFileTitle());
        dto.setFileLink(entity.getFileLink());
        dto.setFileSize(entity.getFileSize());
        dto.setFileIntroduce(entity.getFileIntroduce());
        dto.setFileType(entity.getFileType());
        dto.setWriterId(entity.getWriterId());
        dto.setUpdaterId(entity.getUpdaterId());
        dto.setWritedAt(entity.getWritedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public static StrategyProposalEntity toEntity(StrategyProposalDto dto, StrategyEntity strategy) {
        StrategyProposalEntity entity = new StrategyProposalEntity();
        entity.setStrategyProposalId(dto.getStrategyProposalId());
        entity.setStrategy(strategy); // 연관된 StrategyEntity 설정
        entity.setFileTitle(dto.getFileTitle());
        entity.setFileLink(dto.getFileLink());
        entity.setFileSize(dto.getFileSize());
        entity.setFileIntroduce(dto.getFileIntroduce());
        entity.setFileType(dto.getFileType());
        entity.setWriterId(dto.getWriterId());
        entity.setUpdaterId(dto.getUpdaterId());
        entity.setWritedAt(dto.getWritedAt() != null ? dto.getWritedAt() : LocalDateTime.now());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }
}
