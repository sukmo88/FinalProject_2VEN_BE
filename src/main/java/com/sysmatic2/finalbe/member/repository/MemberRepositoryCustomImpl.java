package com.sysmatic2.finalbe.member.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPQLQuery;
import com.sysmatic2.finalbe.member.dto.TraderSearchResultDto;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.entity.QMemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityManager;

import java.util.List;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // EntityManager를 주입받아 JPAQueryFactory 초기화
    public MemberRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<TraderSearchResultDto> searchByKeywordWithSorting(String keyword, String sortOption, Pageable pageable) {
        QMemberEntity member = QMemberEntity.memberEntity;

        if(keyword == null) keyword = "";

        // QueryDSL에서 정렬 조건 생성
        OrderSpecifier<?> sortOrder;
        if ("latestSignup".equalsIgnoreCase(sortOption)) {
            // 최신 가입일순 정렬
            sortOrder = member.signupAt.desc();
        } else {
            // 기본: 전략 수가 많은 순 정렬
            sortOrder = Expressions.numberTemplate(Integer.class,
                    "(SELECT COUNT(s.strategyId) FROM StrategyEntity s WHERE s.writerId = {0} AND s.isApproved = 'Y')",
                    member.memberId).desc();
        }

        // QueryDSL 쿼리 작성
        JPQLQuery<TraderSearchResultDto> query = queryFactory
                .select(Projections.constructor(
                        TraderSearchResultDto.class,
                        member.memberId,                          // 트레이더 ID
                        member.nickname,                          // 닉네임
                        member.introduction,                      // 자기소개
                        member.profilePath,                       // 프로필 이미지 링크
                        Expressions.numberTemplate(Integer.class, // 전략 수 서브쿼리
                                "(SELECT COUNT(s.strategyId) FROM StrategyEntity s WHERE s.writerId = {0} AND s.isApproved = 'N' AND s.isPosted = 'Y')",
                                member.memberId)
                ))
                .from(member)
                .where(
                        member.nickname.containsIgnoreCase(keyword)
                                .and(member.memberGradeCode.eq("MEMBER_ROLE_TRADER"))
                                .and(member.memberStatusCode.eq("ACTIVE"))
                )
                .orderBy(sortOrder);

        // 페이징 처리
        long total = query.fetchCount();
        List<TraderSearchResultDto> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}