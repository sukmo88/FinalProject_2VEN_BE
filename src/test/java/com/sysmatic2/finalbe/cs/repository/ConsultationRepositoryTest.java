//// ConsultationRepositoryTest.java
//
//package com.sysmatic2.finalbe.cs.repository;
//
//import com.sysmatic2.finalbe.cs.entity.ConsultationEntity;
//import com.sysmatic2.finalbe.cs.entity.ConsultationStatus;
//import com.sysmatic2.finalbe.member.entity.MemberEntity;
//import com.sysmatic2.finalbe.admin.entity.TradingCycleEntity;
//import com.sysmatic2.finalbe.admin.entity.TradingTypeEntity;
//import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import jakarta.persistence.EntityManager;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//public class ConsultationRepositoryTest {
//
//  @Autowired
//  private ConsultationRepository consultationRepository;
//
//  @Autowired
//  private EntityManager entityManager;
//
//  private MemberEntity investor;
//  private MemberEntity trader;
//  private TradingCycleEntity tradingCycle;
//  private TradingTypeEntity tradingType;
//  private StrategyEntity strategy;
//
//  @BeforeEach
//  void setUp() {
//    // 투자자 엔티티 생성 및 저장
//    investor = createMember("inv123", "투자자닉네임");
//    entityManager.persist(investor);
//
//    // 트레이더 엔티티 생성 및 저장
//    trader = createMember("trd456", "트레이더닉네임");
//    entityManager.persist(trader);
//
//    // 트레이딩 사이클 엔티티 생성 및 저장
//    tradingCycle = createTradingCycle("주간 사이클", "주간 단위의 트레이딩 사이클");
//    entityManager.persist(tradingCycle);
//
//    // 트레이딩 타입 엔티티 생성 및 저장
//    tradingType = createTradingType("스캘핑", "빠른 매매 전략");
//    entityManager.persist(tradingType);
//
//    // 전략 엔티티 생성 및 저장
//    strategy = createStrategy("성장 전략", "이 전략은 성장에 초점을 맞추고 있습니다.", 5000.0, investor, tradingCycle, tradingType);
//    entityManager.persist(strategy);
//
//    entityManager.flush(); // 강제 플러시하여 데이터베이스에 저장
//  }
//
//  private MemberEntity createMember(String memberId, String nickname) {
//    MemberEntity member = new MemberEntity();
//    member.setMemberId(memberId);
//    member.setNickname(nickname);
//    member.setEmail(memberId + "@example.com");
//    member.setPassword("password");
//    member.setMemberGradeCode("GRADE_BASIC");
//    member.setMemberStatusCode("STATUS_ACTIVE");
//    member.setPhoneNumber("010-1234-5678");
//    member.setIsLoginLocked('N');
//    member.setIsAgreedMarketingAd('Y');
//    member.setSignupAt(LocalDateTime.now());
//    member.setCreatedAt(LocalDateTime.now());
//    member.setModifiedAt(LocalDateTime.now());
//    return member;
//  }
//
//  private TradingCycleEntity createTradingCycle(String name, String description) {
//    TradingCycleEntity cycle = new TradingCycleEntity();
//    cycle.setTradingCycleName(name);
//    cycle.setTradingCycleDescription(description);
//    cycle.setTradingCycleOrder(1);
//    cycle.setTradingCycleIcon("icon.png");
//    cycle.setIsActive("Y");
//    cycle.setCreatedAt(LocalDateTime.now());
//    cycle.setModifiedAt(LocalDateTime.now());
//    return cycle;
//  }
//
//  private TradingTypeEntity createTradingType(String name, String description) {
//    TradingTypeEntity type = new TradingTypeEntity();
//    type.setTradingTypeName(name);
//    type.setTradingTypeDescription(description);
//    type.setTradingTypeOrder(1);
//    type.setTradingTypeIcon("icon.png");
//    type.setIsActive("Y");
//    type.setCreatedAt(LocalDateTime.now());
//    type.setModifiedAt(LocalDateTime.now());
//    return type;
//  }
//
//  private StrategyEntity createStrategy(String title, String overview, double minInvestmentAmount, MemberEntity createdBy,
//                                        TradingCycleEntity tradingCycle, TradingTypeEntity tradingType) {
//    StrategyEntity strategy = new StrategyEntity();
//    strategy.setStrategyTitle(title);
//    strategy.setStrategyOverview(overview);
//    strategy.setMinInvestmentAmount(minInvestmentAmount);
//    strategy.setFollowersCount(0L);
//    strategy.setIsPosted(true);
//    strategy.setIsGranted(false);
//    strategy.setCreatedAt(LocalDateTime.now());
//    strategy.setModifiedAt(LocalDateTime.now());
//    strategy.setTradingCycle(tradingCycle); // 필수 필드 설정
//    strategy.setTradingType(tradingType);   // 필수 필드 설정
//    strategy.setCreatedBy(createdBy);
//    strategy.setModifiedBy(createdBy);
//    strategy.setWriterId(createdBy.getMemberId());
//    strategy.setUpdaterId(createdBy.getMemberId());
//    return strategy;
//  }
//
//  @Test
//  void saveConsultation_성공() {
//    // 상담 엔티티 생성
//    ConsultationEntity consultation = ConsultationEntity.builder()
//            .investor(investor)
//            .trader(trader)
//            .strategy(strategy)
//            .investmentAmount(5000.0)
//            .investmentDate(LocalDateTime.now())
//            .title("투자 문의")
//            .content("성장 전략에 대해 더 알고 싶습니다.")
//            .status(ConsultationStatus.PENDING)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//    // 상담 저장
//    ConsultationEntity savedConsultation = consultationRepository.save(consultation);
//
//    // 저장된 상담 검증
//    assertThat(savedConsultation.getId()).isNotNull();
//    assertThat(savedConsultation.getInvestor()).isEqualTo(investor);
//    assertThat(savedConsultation.getTrader()).isEqualTo(trader);
//    assertThat(savedConsultation.getStrategy()).isEqualTo(strategy);
//    assertThat(savedConsultation.getInvestmentAmount()).isEqualTo(5000.0);
//    assertThat(savedConsultation.getTitle()).isEqualTo("투자 문의");
//    assertThat(savedConsultation.getContent()).isEqualTo("성장 전략에 대해 더 알고 싶습니다.");
//    assertThat(savedConsultation.getStatus()).isEqualTo(ConsultationStatus.PENDING);
//  }
//
//  @Test
//  void findAllByInvestorMemberId_성공() {
//    // 여러 상담 엔티티 생성 및 저장
//    ConsultationEntity consultation1 = ConsultationEntity.builder()
//            .investor(investor)
//            .trader(trader)
//            .strategy(strategy)
//            .investmentAmount(5000.0)
//            .investmentDate(LocalDateTime.now())
//            .title("투자 문의 1")
//            .content("상세 내용 1")
//            .status(ConsultationStatus.PENDING)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//    ConsultationEntity consultation2 = ConsultationEntity.builder()
//            .investor(investor)
//            .trader(trader)
//            .strategy(strategy)
//            .investmentAmount(10000.0)
//            .investmentDate(LocalDateTime.now())
//            .title("투자 문의 2")
//            .content("상세 내용 2")
//            .status(ConsultationStatus.COMPLETED)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//    consultationRepository.save(consultation1);
//    consultationRepository.save(consultation2);
//
//    // 투자자 ID로 상담 조회
//    List<ConsultationEntity> consultations = consultationRepository.findAllByInvestor_MemberId(investor.getMemberId());
//
//    // 조회 결과 검증
//    assertThat(consultations).hasSize(2);
//    assertThat(consultations).extracting("title").containsExactlyInAnyOrder("투자 문의 1", "투자 문의 2");
//  }
//}
