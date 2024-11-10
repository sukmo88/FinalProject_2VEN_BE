package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.FAQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

    // FAQ 검색기능 (question 또는 answer 필드에 특정 키워드가 포함된 항목을 검색
    Page<FAQ> findByQuestionContainingOrAnswerContaining(String questionKeyword, String answerKeyword, Pageable pageable);

}
