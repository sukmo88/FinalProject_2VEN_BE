package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.FAQCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FAQCategoryRepository extends JpaRepository<FAQCategory, Long> {

}
