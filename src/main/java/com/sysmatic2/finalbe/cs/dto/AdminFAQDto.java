package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.FAQ;
import com.sysmatic2.finalbe.cs.entity.FAQCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminFAQDto implements FAQResponse{

    private Long id;

    private Long writerId;

    private String question;

    private String answer;

    private LocalDateTime postedAt;

    private LocalDateTime updatedAt;

    private Boolean isActive;

    private FAQCategory faqCategory;

    public AdminFAQDto(Long id, String question, String answer, Long writerId, LocalDateTime postedAt,
                       LocalDateTime updatedAt, Boolean isActive, FAQCategory faqCategory) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.writerId = writerId;
        this.postedAt = postedAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.faqCategory = faqCategory;
    }
}
