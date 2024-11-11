package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.AdminFAQDto;
import com.sysmatic2.finalbe.cs.dto.FAQResponse;
import com.sysmatic2.finalbe.cs.dto.UserFAQDto;
import com.sysmatic2.finalbe.cs.entity.FAQ;
import com.sysmatic2.finalbe.cs.entity.FAQCategory;
import com.sysmatic2.finalbe.cs.repository.FAQCategoryRepository;
import com.sysmatic2.finalbe.cs.repository.FAQRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class FAQServiceTest {

    @Autowired
    private FAQService faqService;

    @Autowired
    private FAQCategoryRepository faqCategoryRepository;

    @Autowired
    private FAQRepository faqRepository;

    private FAQCategory category;

    @BeforeEach
    void setUp() {
        // 테스트용 FAQCatgory를 저장
        category = new FAQCategory();
        category.setName("General");
        category.setDescription("General FAQs");
        faqCategoryRepository.save(category);
    }

    @Test
    public void whenCreateFAQ_thenSuccessForAdmin() {
        // given
        FAQ faq = new FAQ();
        faq.setQuestion("What is FAQ?");
        faq.setAnswer("This is FAQ");
        faq.setWriterId(1L);
        faq.setPostedAt(LocalDateTime.now());
        faq.setFaqCategory(category);

        // when
        AdminFAQDto createdFAQ = faqService.createFAQ(faq, "ADMIN");

        // then
        assertNotNull(createdFAQ);
        assertEquals(createdFAQ.getQuestion(), faq.getQuestion());
        assertEquals(createdFAQ.getAnswer(), faq.getAnswer());
    }

    @Test
    public void whenCreateFAQ_thenFindAllForAdmin() {
        // given
        for (int i = 1; i <= 10; i++) {
            FAQ faq = new FAQ();
            faq.setQuestion("What is FAQ?" + i);
            faq.setAnswer("This is FAQ" + i);
            faq.setWriterId(1L);
            faq.setPostedAt(LocalDateTime.now());
            faq.setFaqCategory(category);
            faqService.createFAQ(faq, "ADMIN");
        }

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<FAQResponse> faqList = faqService.getAllFAQs("ADMIN", pageable);

        // then
        assertEquals(10, faqList.getTotalElements());
        assertThat(faqList.getContent().get(0)).isInstanceOf(AdminFAQDto.class);

        AdminFAQDto firstFaq = (AdminFAQDto) faqList.getContent().get(0);
        assertEquals("What is FAQ?1", firstFaq.getQuestion());
        assertEquals("This is FAQ1", firstFaq.getAnswer());
    }

    @Test
    public void whenCreateFAQ_thenFindAllForUser() {
        // given
        for (int i = 1; i <= 10; i++) {
            FAQ faq = new FAQ();
            faq.setQuestion("What is FAQ?" + i);
            faq.setAnswer("This is FAQ" + i);
            faq.setWriterId(1L);
            faq.setPostedAt(LocalDateTime.now());
            faq.setFaqCategory(category);
            faqService.createFAQ(faq, "ADMIN");
        }

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<FAQResponse> faqList = faqService.getAllFAQs("USER", pageable);

        // then
        assertEquals(10, faqList.getTotalElements());
        assertThat(faqList.getContent().get(0)).isInstanceOf(UserFAQDto.class);

        UserFAQDto firstFaq = (UserFAQDto) faqList.getContent().get(0);
        assertEquals("What is FAQ?1", firstFaq.getQuestion());
        assertEquals("This is FAQ1", firstFaq.getAnswer());
    }

    @Test
    public void whenCreateFAQ_thenFindByIdForAdmin(){
        // given
        FAQ faq = new FAQ();
        faq.setQuestion("What is FAQ?");
        faq.setAnswer("This is FAQ");
        faq.setWriterId(1L);
        faq.setPostedAt(LocalDateTime.now());
        faq.setFaqCategory(category);

        // when
        AdminFAQDto createdFAQ = faqService.createFAQ(faq, "ADMIN");
        FAQ findFAQ = faqRepository.findById(createdFAQ.getId()).get();

        // then
        assertNotNull(findFAQ);
        assertEquals(createdFAQ.getQuestion(), findFAQ.getQuestion());
        assertEquals(createdFAQ.getAnswer(), findFAQ.getAnswer());
    }

    @Test
    public void whenUpdateById_thenSuccessForAdmin() {
        // given
        FAQ faq = new FAQ();
        faq.setQuestion("What is FAQ?");
        faq.setAnswer("This is FAQ");
        faq.setWriterId(1L);
        faq.setPostedAt(LocalDateTime.now());
        faq.setFaqCategory(category);
        AdminFAQDto createdFAQ = faqService.createFAQ(faq, "ADMIN");

        // when
        createdFAQ.setQuestion("updated FAQ?");
        createdFAQ.setAnswer("updated FAQ");

        // 업데이트를 위해 AdminFAQDto를 FAQ 엔티티로 변환
        FAQ updatedFAQEntity = new FAQ();
        updatedFAQEntity.setId(createdFAQ.getId());  // ID 설정이 필요
        updatedFAQEntity.setQuestion(createdFAQ.getQuestion());
        updatedFAQEntity.setAnswer(createdFAQ.getAnswer());
        updatedFAQEntity.setWriterId(createdFAQ.getWriterId());
        updatedFAQEntity.setPostedAt(createdFAQ.getPostedAt());
        updatedFAQEntity.setFaqCategory(createdFAQ.getFaqCategory());
        updatedFAQEntity.setIsActive(createdFAQ.getIsActive());

        AdminFAQDto updatedFAQ = faqService.updateFAQ(updatedFAQEntity.getId(), updatedFAQEntity, "ADMIN");

        // then
        assertNotNull(updatedFAQ);
        assertEquals(createdFAQ.getId(), updatedFAQ.getId());
        assertEquals("updated FAQ?", updatedFAQ.getQuestion());
        assertEquals("updated FAQ", updatedFAQ.getAnswer());
    }

    @Test
    public void whenDeleteById_thenSuccessForAdmin() {
        // given
        FAQ faq = new FAQ();
        faq.setQuestion("What is FAQ?");
        faq.setAnswer("This is FAQ");
        faq.setWriterId(1L);
        faq.setPostedAt(LocalDateTime.now());
        faq.setFaqCategory(category);
        AdminFAQDto createdFAQ = faqService.createFAQ(faq, "ADMIN");

        // when
        faqService.deleteFAQ(createdFAQ.getId(), "ADMIN");

        //then
        assertFalse(faqRepository.findById(createdFAQ.getId()).isPresent());
    }

    @Test
    public void whenSearchFaqsIgnore_thenSuccessForAdmin() {
        // given
        FAQ faq = new FAQ();
        faq.setQuestion("What is faq?");
        faq.setAnswer("This is faq");
        faq.setWriterId(1L);
        faq.setPostedAt(LocalDateTime.now());
        faq.setFaqCategory(category);

        FAQ faq2 = new FAQ();
        faq2.setQuestion("What is this?");
        faq2.setAnswer("This is answer");
        faq2.setWriterId(1L);
        faq2.setPostedAt(LocalDateTime.now());
        faq2.setFaqCategory(category);

        faqRepository.save(faq);
        faqRepository.save(faq2);

        // when
        String keyword = "faq";
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserFAQDto> result = faqService.searchFAQs(keyword, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("What is faq?", result.getContent().get(0).getQuestion());

    }
}