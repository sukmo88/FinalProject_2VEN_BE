package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.dto.StrategyProposalDto;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyProposalEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyProposalRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StrategyProposalServiceTest {

    @Mock
    private StrategyProposalRepository strategyProposalRepository;

    @Mock
    private StrategyRepository strategyRepository;

    @InjectMocks
    private StrategyProposalService strategyProposalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("제안서 등록 성공 테스트")
    void uploadProposal_Success() {
        // Given
        Long strategyId = 1L;
        String uploaderId = "test_user";

        StrategyEntity strategy = new StrategyEntity();
        strategy.setStrategyId(strategyId);
        strategy.setStrategyTitle("Test Strategy");

        StrategyProposalDto dto = new StrategyProposalDto();
        dto.setFileTitle("Test Proposal");
        dto.setFileLink("http://example.com/proposal");
        dto.setFileSize(1024);
        dto.setFileType("PDF");

        StrategyProposalEntity savedEntity = new StrategyProposalEntity();
        savedEntity.setStrategyProposalId(1L);
        savedEntity.setFileTitle(dto.getFileTitle());
        savedEntity.setStrategy(strategy);

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategy));
        when(strategyProposalRepository.save(any(StrategyProposalEntity.class))).thenReturn(savedEntity);

        // When
        StrategyProposalDto result = strategyProposalService.uploadProposal(dto, uploaderId, strategyId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileTitle()).isEqualTo("Test Proposal");
        verify(strategyRepository, times(1)).findById(strategyId);
        verify(strategyProposalRepository, times(1)).save(any(StrategyProposalEntity.class));
    }

    @Test
    @DisplayName("제안서 등록 실패 테스트 - 잘못된 Strategy ID")
    void uploadProposal_InvalidStrategyId() {
        // Given
        Long strategyId = 999L;
        String uploaderId = "test_user";
        StrategyProposalDto dto = new StrategyProposalDto();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> strategyProposalService.uploadProposal(dto, uploaderId, strategyId));

        assertThat(exception.getMessage()).isEqualTo("Invalid strategy ID: 999");
        verify(strategyRepository, times(1)).findById(strategyId);
        verify(strategyProposalRepository, never()).save(any(StrategyProposalEntity.class));
    }

    @Test
    @DisplayName("제안서 삭제 성공 테스트")
    void deleteProposal_Success() {
        // Given
        Long strategyId = 1L;
        String writerId = "writer";

        StrategyEntity strategy = new StrategyEntity();
        strategy.setStrategyId(strategyId);

        StrategyProposalEntity proposal = new StrategyProposalEntity();
        proposal.setStrategy(strategy);
        proposal.setWriterId(writerId);

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategy));
        when(strategyProposalRepository.findByStrategyAndWriterId(strategy, writerId)).thenReturn(Optional.of(proposal));

        // When
        strategyProposalService.deleteProposal(strategyId, writerId);

        // Then
        verify(strategyRepository, times(1)).findById(strategyId);
        verify(strategyProposalRepository, times(1)).findByStrategyAndWriterId(strategy, writerId);
        verify(strategyProposalRepository, times(1)).delete(proposal);
    }

    @Test
    @DisplayName("제안서 삭제 실패 테스트 - 잘못된 Strategy ID")
    void deleteProposal_InvalidStrategyId() {
        // Given
        Long strategyId = 999L;
        String writerId = "writer";

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> strategyProposalService.deleteProposal(strategyId, writerId));

        assertThat(exception.getMessage()).isEqualTo("Invalid strategy ID: 999");
        verify(strategyRepository, times(1)).findById(strategyId);
        verify(strategyProposalRepository, never()).findByStrategyAndWriterId(any(), eq(writerId));
    }
}