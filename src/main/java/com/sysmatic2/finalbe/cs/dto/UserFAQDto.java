package com.sysmatic2.finalbe.cs.dto;

import com.sysmatic2.finalbe.cs.entity.FAQCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserFAQDto implements FAQResponse {
    private Long id;

    private String question;

    private String answer;

    private LocalDateTime postedAt;

    private FAQCategory faqCategory;

    public UserFAQDto(Long id, String question, String answer, FAQCategory faqCategory) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.faqCategory = faqCategory;
    }

}
