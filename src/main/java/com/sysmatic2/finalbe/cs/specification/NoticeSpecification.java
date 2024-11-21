package com.sysmatic2.finalbe.cs.specification;

import com.sysmatic2.finalbe.cs.entity.NoticeEntity;
import com.sysmatic2.finalbe.cs.entity.NoticeStatus;
import org.springframework.data.jpa.domain.Specification;

public class NoticeSpecification {

  /**
   * 제목에 키워드가 포함되는지 여부
   *
   * @param titleKeyword 제목 키워드
   * @return Specification
   */
  public static Specification<NoticeEntity> titleContains(String titleKeyword) {
    return (root, query, criteriaBuilder) -> {
      if (titleKeyword == null || titleKeyword.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + titleKeyword.toLowerCase() + "%");
    };
  }

  /**
   * 내용에 키워드가 포함되는지 여부
   *
   * @param contentKeyword 내용 키워드
   * @return Specification
   */
  public static Specification<NoticeEntity> contentContains(String contentKeyword) {
    return (root, query, criteriaBuilder) -> {
      if (contentKeyword == null || contentKeyword.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), "%" + contentKeyword.toLowerCase() + "%");
    };
  }

  /**
   * 특정 공지 상태를 가지는지 여부
   *
   * @param status 공지 상태
   * @return Specification
   */
  public static Specification<NoticeEntity> hasStatus(NoticeStatus status) {
    return (root, query, criteriaBuilder) -> {
      if (status == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("noticeStatus"), status);
    };
  }
}
