package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.ConsultationThreadEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ConsultationThreadRepositoryUnitTest {

  @Mock
  private ConsultationThreadRepository consultationThreadRepository;

  @InjectMocks
  private ConsultationThreadRepositoryUnitTest testSubject;

  private MemberEntity investor;
  private MemberEntity trader;
  private StrategyEntity strategy;
  private ConsultationThreadEntity consultationThread;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    // Create mock entities
    investor = new MemberEntity();
    investor.setMemberId("investor-001");

    trader = new MemberEntity();
    trader.setMemberId("trader-001");

    strategy = new StrategyEntity();
    strategy.setStrategyId(1L);

    consultationThread = new ConsultationThreadEntity();
    consultationThread.setInvestor(investor);
    consultationThread.setTrader(trader);
    consultationThread.setStrategy(strategy);
  }

  @Test
  public void testFindByInvestorAndTraderAndStrategy() {
    // Mock repository response
    when(consultationThreadRepository.findByInvestor_MemberIdAndTrader_MemberIdAndStrategy_StrategyId(
            anyString(), anyString(), anyLong()))
            .thenReturn(Optional.of(consultationThread));

    // Call repository method
    Optional<ConsultationThreadEntity> result = consultationThreadRepository
            .findByInvestor_MemberIdAndTrader_MemberIdAndStrategy_StrategyId(
                    "investor-001", "trader-001", 1L);

    // Verify results
    assertEquals("investor-001", result.get().getInvestor().getMemberId());
    assertEquals("trader-001", result.get().getTrader().getMemberId());
    assertEquals(1L, result.get().getStrategy().getStrategyId());
  }

  @Test
  public void testFindByInvestorOrTrader() {
    // Mock repository response
    when(consultationThreadRepository.findByInvestor_MemberIdOrTrader_MemberId(anyString()))
            .thenReturn(List.of(consultationThread));

    // Call repository method
    List<ConsultationThreadEntity> result = consultationThreadRepository
            .findByInvestor_MemberIdOrTrader_MemberId("investor-001");

    // Verify results
    assertEquals(1, result.size());
    assertEquals("investor-001", result.get(0).getInvestor().getMemberId());
  }
}
