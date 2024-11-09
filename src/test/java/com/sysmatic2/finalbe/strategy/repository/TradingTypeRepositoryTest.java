package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.TradingType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TradingTypeRepositoryTest {

    @Autowired
    TradingTypeRepository tradingTypeRepository;

    public void createTradingTypeList() {
        for (int i = 1; i <= 10; i++) {
            TradingType tradingType = new TradingType();
            tradingType.setTradingTypeOrder(i);
            tradingType.setTradingTypeName("test Trading" + i);
            tradingType.setTradingTypeIcon("test" + i + ".png");

            // i가 홀수일 때 "Y", 짝수일 때 "N"을 설정
            tradingType.setIsActive(i % 2 == 0 ? "N" : "Y");

            tradingTypeRepository.save(tradingType);
        }
    }

    @BeforeEach
    public void resetDatabase() {
        tradingTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("매매유형 등록 테스트")
    public void registerTradingTypeTest() {
        // 새로운 tradingType 엔티티 생성
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(1);
        tradingType.setTradingTypeName("Day Trading");
        tradingType.setTradingTypeIcon("icon_day.png");
        tradingType.setTradingTypeDescription("Short-term trades within the day.");
        tradingType.setIsActive("Y");

        // 엔티티 저장
        TradingType savedTradingType = tradingTypeRepository.save(tradingType);
        System.out.println(savedTradingType.toString());

        // 검증
        /*
            등록 전 엔티티와 등록 후 엔티티 비교
         */
        Assertions.assertEquals(tradingType.getTradingTypeId(), savedTradingType.getTradingTypeId());
        Assertions.assertEquals(tradingType.getTradingTypeOrder(), savedTradingType.getTradingTypeOrder());
        Assertions.assertEquals(tradingType.getTradingTypeName(), savedTradingType.getTradingTypeName());
        Assertions.assertEquals(tradingType.getTradingTypeIcon(), savedTradingType.getTradingTypeIcon());
        Assertions.assertEquals(tradingType.getTradingTypeDescription(), savedTradingType.getTradingTypeDescription());
        Assertions.assertEquals(tradingType.getIsActive(), savedTradingType.getIsActive());
    }

    @Test
    @DisplayName("unique 중복 검증")
    public void uniqueDuplicateValidation() {
        // 새로운 tradingType 엔티티 생성
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(1);
        tradingType.setTradingTypeName("Day Trading");
        tradingType.setTradingTypeIcon("icon_day.png");
        tradingType.setTradingTypeDescription("Short-term trades within the day.");
        tradingType.setIsActive("Y");

        // 첫 번째 엔티티 저장
        TradingType savedTradingType = tradingTypeRepository.save(tradingType);

        // 동일한 order를 가진 두 번째 엔티티 생성
        TradingType duplicateTradingType = new TradingType();
        duplicateTradingType.setTradingTypeOrder(1); // 중복 값 설정
        duplicateTradingType.setTradingTypeName("Swing Trading");
        duplicateTradingType.setTradingTypeIcon("icon_swing.png");
        duplicateTradingType.setTradingTypeDescription("Medium-term trades over several days.");
        duplicateTradingType.setIsActive("N");

        // 중복 저장 시 예외 발생 여부 검증
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingTypeRepository.save(duplicateTradingType);
        });
    }

    @Test
    @DisplayName("매매유형 조회 테스트")
    public void retrieveTradingTypeTest() {
        // 새로운 tradingType 엔티티 생성
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(1);
        tradingType.setTradingTypeName("Day Trading");
        tradingType.setTradingTypeIcon("icon_day.png");
        tradingType.setTradingTypeDescription("Short-term trades within the day.");
        tradingType.setIsActive("Y");

        // 엔티티 저장
        TradingType savedTradingType = tradingTypeRepository.save(tradingType);
        // 저장된 매매유형명으로 검색
        TradingType retrievedTradingType = tradingTypeRepository.findByTradingTypeName(savedTradingType.getTradingTypeName());

        // 조회된 매매유형이 NULL이 아님
        assertNotNull(retrievedTradingType);
        // 저장한 매매유형명과 조회된 매매유형명이 같아야함
        assertEquals(savedTradingType.getTradingTypeName(), retrievedTradingType.getTradingTypeName());
    }

    @Test
    @DisplayName("매매유형 수정 테스트")
    public void updateTradingTypeTest() {
        // 새로운 tradingType 엔티티 생성
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(1);
        tradingType.setTradingTypeName("Day Trading");
        tradingType.setTradingTypeIcon("icon_day.png");
        tradingType.setTradingTypeDescription("Short-term trades within the day.");
        tradingType.setIsActive("Y");

        // 엔티티 저장
        TradingType savedTradingType = tradingTypeRepository.save(tradingType);
        // 저장된 엔티티 수정
        savedTradingType.setTradingTypeName("Modified Scalping");
        // 수정된 엔티티 저장
        TradingType updatedTradingType = tradingTypeRepository.save(savedTradingType);

        // 수정된 엔티티와 저장된 엔티티 비교
        assertEquals(savedTradingType.getTradingTypeName(), updatedTradingType.getTradingTypeName());
    }

    @Test
    @DisplayName("매매유형 삭제 테스트")
    public void deleteTradingTypeTest() {
        // 새로운 tradingType 엔티티 생성
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(1);
        tradingType.setTradingTypeName("Day Trading");
        tradingType.setTradingTypeIcon("icon_day.png");
        tradingType.setTradingTypeDescription("Short-term trades within the day.");
        tradingType.setIsActive("Y");

        // 엔티티 저장
        TradingType savedTradingType = tradingTypeRepository.save(tradingType);
        // 엔티티 삭제
        tradingTypeRepository.delete(savedTradingType);

        // 잘 삭제됐는지 조회
        Optional<TradingType> deletedTradingType = tradingTypeRepository.findById(savedTradingType.getTradingTypeId());
        // 비어있으면 삭제 성공
        assertTrue(deletedTradingType.isEmpty());
    }

    @Test
    @DisplayName("isActive 필수값 검증 테스트")
    public void isActiveNotNullValidation() {
        // 새로운 tradingType 엔티티 생성
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(2);
        tradingType.setTradingTypeName("Swing Trading");
        tradingType.setTradingTypeIcon("icon_swing.png");
        tradingType.setTradingTypeDescription("Medium-term trades over several days.");

        // isActive 값이 설정되지 않음
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingTypeRepository.save(tradingType);
        });
    }

    @Test
    @DisplayName("tradingTypeIcon 필수값 검증 테스트")
    public void tradingTypeIconNotNullValidation() {
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(3);
        tradingType.setTradingTypeName("Position Trading");
        tradingType.setIsActive("Y");

        // tradingTypeIcon 값이 설정되지 않음
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingTypeRepository.save(tradingType);
        });
    }

    @Test
    @DisplayName("tradingTypeName 고유성 검증 테스트")
    public void duplicateTradingTypeNameTest() {
        // 첫 번째 엔티티 저장
        TradingType tradingType1 = new TradingType();
        tradingType1.setTradingTypeOrder(4);
        tradingType1.setTradingTypeName("Day Trading");
        tradingType1.setTradingTypeIcon("icon_day.png");
        tradingType1.setIsActive("Y");
        tradingTypeRepository.save(tradingType1);

        // 두 번째 엔티티 저장 - tradingTypeName 중복값 설정
        TradingType tradingType2 = new TradingType();
        tradingType2.setTradingTypeOrder(5);
        tradingType2.setTradingTypeName("Day Trading"); // 중복된 이름
        tradingType2.setTradingTypeIcon("icon_day_alt.png");
        tradingType2.setIsActive("N");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingTypeRepository.save(tradingType2);
        });
    }

    @Test
    @DisplayName("tradingTypeOrder 고유성 검증 테스트")
    public void uniqueTradingTypeOrderTest() {
        // 첫 번째 엔티티 저장
        TradingType tradingType1 = new TradingType();
        tradingType1.setTradingTypeOrder(10);
        tradingType1.setTradingTypeName("Swing Trading");
        tradingType1.setTradingTypeIcon("icon_swing.png");
        tradingType1.setIsActive("Y");
        tradingTypeRepository.save(tradingType1);

        // 두 번째 엔티티 저장 - tradingTypeOrder 중복값 설정
        TradingType tradingType2 = new TradingType();
        tradingType2.setTradingTypeOrder(10); // 중복된 order
        tradingType2.setTradingTypeName("Position Trading");
        tradingType2.setTradingTypeIcon("icon_position.png");
        tradingType2.setIsActive("N");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            tradingTypeRepository.save(tradingType2);
        });
    }

    @Test
    @DisplayName("tradingTypeDescription null 등록 테스트")
    public void optionalTradingTypeDescriptionTest() {
        // tradingTypeDescription에 null주고 엔티티 등록
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(6);
        tradingType.setTradingTypeName("Scalping");
        tradingType.setTradingTypeIcon("icon_scalping.png");
        tradingType.setIsActive("Y");

        TradingType savedTradingType = tradingTypeRepository.save(tradingType);
        assertNull(savedTradingType.getTradingTypeDescription());
    }

    @Test
    @DisplayName("매매유형 여러개 등록 후 조회 개수 확인 테스트")
    public void multipleEntitiesCountTest() {
        // 다중 엔티티 등록
        createTradingTypeList();

        // 모든 엔티티 조회 후 개수 확인
        List<TradingType> tradingTypes = tradingTypeRepository.findAll();
        assertEquals(10, tradingTypes.size());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시도 테스트")
    public void findByIdNonExistentTest() {
        Optional<TradingType> tradingType = tradingTypeRepository.findById(-1);
        assertTrue(tradingType.isEmpty());
    }

    @Test
    @DisplayName("isActive 상태별 페이지네이션 조회 테스트")
    public void findByIsActiveWithPaginationTest() {
        // 여러 개의 엔티티 추가
        createTradingTypeList();

        // isActive가 "Y"인 엔티티를 페이지네이션으로 조회 (2개씩)
        Pageable pageable = PageRequest.of(0, 2); // 첫 번째 페이지, 페이지 크기 2
        Page<TradingType> activeTypesPage = tradingTypeRepository.findByIsActive("Y", pageable);

        // 조회한 페이지의 개수와 각 요소 확인
        assertEquals(2, activeTypesPage.getSize()); // 페이지 크기가 2인지 확인
        assertEquals(3, activeTypesPage.getTotalPages()); // 총 페이지가 5개인지 확인 (Y가 5개이므로 페이지 크기 2일 때 3페이지)
        assertEquals(5, activeTypesPage.getTotalElements()); // "Y" 상태의 엔티티가 총 5개인지 확인
        assertEquals("test Trading1", activeTypesPage.getContent().get(0).getTradingTypeName());
        assertEquals("test Trading3", activeTypesPage.getContent().get(1).getTradingTypeName());

        // 첫 페이지 여부 및 다음 페이지 존재 여부 확인
        assertTrue(activeTypesPage.isFirst()); // 첫 번째 페이지인지 확인
        assertFalse(activeTypesPage.isLast()); // 마지막 페이지가 아님을 확인
        assertTrue(activeTypesPage.hasNext()); // 다음 페이지가 존재하는지 확인

        // 두 번째 페이지 조회
        Pageable secondPage = PageRequest.of(1, 2);
        Page<TradingType> secondActiveTypesPage = tradingTypeRepository.findByIsActive("Y", secondPage);

        assertEquals(2, secondActiveTypesPage.getSize()); // 페이지 크기가 2인지 확인
        assertEquals("test Trading5", secondActiveTypesPage.getContent().get(0).getTradingTypeName());
        assertEquals("test Trading7", secondActiveTypesPage.getContent().get(1).getTradingTypeName());

        // isActive가 "N"인 엔티티를 페이지네이션으로 조회 (2개씩)
        Page<TradingType> inactiveTypesPage = tradingTypeRepository.findByIsActive("N", pageable);

        // 조회한 페이지의 개수와 각 요소 확인
        assertEquals(2, inactiveTypesPage.getSize()); // 페이지 크기가 2인지 확인
        assertEquals(3, inactiveTypesPage.getTotalPages()); // 총 페이지가 5개인지 확인 (N이 5개이므로 페이지 크기 2일 때 3페이지)
        assertEquals(5, inactiveTypesPage.getTotalElements()); // "N" 상태의 엔티티가 총 5개인지 확인
        assertEquals("test Trading2", inactiveTypesPage.getContent().get(0).getTradingTypeName());
        assertEquals("test Trading4", inactiveTypesPage.getContent().get(1).getTradingTypeName());

        // 두 번째 페이지의 첫 페이지 및 마지막 페이지 여부 확인
        assertFalse(secondActiveTypesPage.isFirst()); // 첫 번째 페이지가 아님을 확인
        assertFalse(secondActiveTypesPage.isLast()); // 마지막 페이지가 아님을 확인
        assertTrue(secondActiveTypesPage.hasNext()); // 다음 페이지가 존재하는지 확인

        // 세 번째 페이지 조회 (마지막 페이지, 한 개의 엔티티만 포함)
        Pageable thirdPage = PageRequest.of(2, 2);
        Page<TradingType> thirdActiveTypesPage = tradingTypeRepository.findByIsActive("Y", thirdPage);

        assertEquals(1, thirdActiveTypesPage.getContent().size()); // 마지막 페이지는 1개의 엔티티만 포함
        assertEquals("test Trading9", thirdActiveTypesPage.getContent().get(0).getTradingTypeName());

        // 마지막 페이지 여부 확인
        assertFalse(thirdActiveTypesPage.isFirst()); // 첫 번째 페이지가 아님을 확인
        assertTrue(thirdActiveTypesPage.isLast()); // 마지막 페이지임을 확인
        assertFalse(thirdActiveTypesPage.hasNext()); // 다음 페이지가 없음을 확인
    }

    @Test
    @DisplayName("시스템 컬럼 자동 업데이트 테스트")
    public void auditingFieldsAutoUpdateTest() {
        TradingType tradingType = new TradingType();
        tradingType.setTradingTypeOrder(9);
        tradingType.setTradingTypeName("Long-term Trading");
        tradingType.setTradingTypeIcon("icon_longterm.png");
        tradingType.setIsActive("Y");

        // 엔티티 저장
        TradingType savedTradingType = tradingTypeRepository.save(tradingType);

        // createdBy와 createdAt 필드가 자동으로 생성되었는지 검증
        assertNotNull(savedTradingType.getCreatedBy());
        assertNotNull(savedTradingType.getCreatedAt());

        // 엔티티 업데이트
        savedTradingType.setTradingTypeName("Updated Long-term Trading");
        TradingType updatedTradingType = tradingTypeRepository.save(savedTradingType);

        // modifiedBy와 updatedAt 필드가 자동으로 업데이트되었는지 검증
        assertNotNull(updatedTradingType.getModifiedBy());
        assertNotNull(updatedTradingType.getUpdatedAt());
    }
}