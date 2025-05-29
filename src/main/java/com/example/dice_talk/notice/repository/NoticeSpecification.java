package com.example.dice_talk.notice.repository;

import com.example.dice_talk.notice.entity.Notice;
import org.springframework.data.jpa.domain.Specification;

public class NoticeSpecification {

    public static Specification<Notice> hasType(Notice.NoticeType type){
        return ((root, query, criteriaBuilder) ->
              type == null ? null : criteriaBuilder.equal(root.get("noticeType"), type));
    }

    public static Specification<Notice> hasStatus(Notice.NoticeStatus status){
        return ((root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("noticeStatus"), status));
    }

    public static Specification<Notice> containsKeyword(String keyword){
        return (root, query, criteriaBuilder) -> {
            if(keyword == null || keyword.isBlank()) return null;
            String k = "%" + keyword.trim() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), k),
                    criteriaBuilder.like(root.get("content"), k)
            );
        };
    }
}
