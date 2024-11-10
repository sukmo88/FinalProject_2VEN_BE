package com.sysmatic2.finalbe.cs.service;

import com.sysmatic2.finalbe.cs.dto.AdminFAQDto;
import com.sysmatic2.finalbe.cs.dto.FAQResponse;
import com.sysmatic2.finalbe.cs.dto.UserFAQDto;
import com.sysmatic2.finalbe.cs.entity.FAQ;
import com.sysmatic2.finalbe.cs.repository.FAQCategoryRepository;
import com.sysmatic2.finalbe.cs.repository.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FAQService {

    private final FAQRepository faqRepository;
    private final FAQCategoryRepository faqCategoryRepository;

    @Autowired
    public FAQService(FAQRepository faqRepository, FAQCategoryRepository faqCategoryRepository) {
        this.faqRepository = faqRepository;
        this.faqCategoryRepository = faqCategoryRepository;
    }

    @Transactional
    public AdminFAQDto createFAQ(FAQ faq, String role){
        validateAdminRole(role);
        FAQ savedFaq = faqRepository.save(faq);
        return new AdminFAQDto(
                savedFaq.getId(),
                savedFaq.getQuestion(),
                savedFaq.getAnswer(),
                savedFaq.getWriterId(),
                savedFaq.getPostedAt(),
                savedFaq.getUpdatedAt(),
                savedFaq.getIsActive(),
                savedFaq.getFaqCategory()
        );
    }


    public List<FAQResponse> getAllFAQs(String role){
        List<FAQ> faqs = faqRepository.findAll();

        if("ADMIN".equalsIgnoreCase(role)){
            return faqs.stream()
                    .map(faq -> new AdminFAQDto(
                            faq.getId(),
                            faq.getQuestion(),
                            faq.getAnswer(),
                            faq.getWriterId(),
                            faq.getPostedAt(),
                            faq.getUpdatedAt(),
                            faq.getIsActive(),
                            faq.getFaqCategory()))
                    .collect(Collectors.toList());
        } else {
            return faqs.stream()
                    .map(faq -> new UserFAQDto(
                            faq.getId(),
                            faq.getQuestion(),
                            faq.getAnswer(),
                            faq.getFaqCategory()))
                    .collect(Collectors.toList());
        }
    }

    public FAQResponse getFAQById(Long id, String role) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ with id " + id + " not found"));

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new AdminFAQDto(
                    faq.getId(),
                    faq.getQuestion(),
                    faq.getAnswer(),
                    faq.getWriterId(),
                    faq.getPostedAt(),
                    faq.getUpdatedAt(),
                    faq.getIsActive(),
                    faq.getFaqCategory()
            );
        } else {
            return new UserFAQDto(
                    faq.getId(),
                    faq.getQuestion(),
                    faq.getAnswer(),
                    faq.getFaqCategory()
            );
        }
    }

    @Transactional
    public AdminFAQDto updateFAQ(Long id, FAQ faq, String role){
        validateAdminRole(role);

        // 기존 FAQ 조회. 존재하지 않으면 예외 발생
        FAQ existingFAQ = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ with id " + id + " not found"));

        // 필드 업데이트
        existingFAQ.setQuestion(faq.getQuestion());
        existingFAQ.setAnswer(faq.getAnswer());
        existingFAQ.setPostedAt(faq.getPostedAt());
        existingFAQ.setFaqCategory(faq.getFaqCategory());
        existingFAQ.setQuestion(faq.getQuestion());
        existingFAQ.setAnswer(faq.getAnswer());
        existingFAQ.setPostedAt(faq.getPostedAt());
        existingFAQ.setFaqCategory(faq.getFaqCategory());

        FAQ savedFaq = faqRepository.save(existingFAQ);
        return new AdminFAQDto(
                savedFaq.getId(),
                savedFaq.getQuestion(),
                savedFaq.getAnswer(),
                savedFaq.getWriterId(),
                savedFaq.getPostedAt(),
                savedFaq.getUpdatedAt(),
                savedFaq.getIsActive(),
                savedFaq.getFaqCategory()
        );
    }

    @Transactional
    public void deleteFAQ(Long id, String role){
        validateAdminRole(role);

        // 기존 FAQ 조회. 존재하지 않으면 예외 발생
        FAQ existingFAQ = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ with id " + id + " not found"));

        faqRepository.delete(existingFAQ);
    }


    public Page<UserFAQDto> searchFAQs(String keyword, Pageable pageable) {
        Page<FAQ> faqPage = faqRepository.findByQuestionContainingOrAnswerContaining(keyword, keyword, pageable);

        // FAQ 엔티티를 FAQSearchResultDto로 변환하여 페이지 반환
        return new PageImpl<>(
                faqPage.getContent().stream()
                        .map(faq -> new UserFAQDto(
                                faq.getId(),
                                faq.getQuestion(),
                                faq.getAnswer(),
                                faq.getFaqCategory() != null ? faq.getFaqCategory() : null))
                        .collect(Collectors.toList()),
                pageable,
                faqPage.getTotalElements()
        );
    }

    // ADMIN 권한 검증 메서드
    private void validateAdminRole(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Only admins can perform this operation.");
        }
    }
}
