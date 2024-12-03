package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.dto.TraderSearchResultDto;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {
    // 트레이더 닉네임과 정렬조건으로 조회
    Page<TraderSearchResultDto> searchByKeywordWithSorting(String keyword, String sortOption, Pageable pageable);
}
