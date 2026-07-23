package com.allpowerful.backend.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideArticleRepository extends JpaRepository<GuideArticle, Long> {
    List<GuideArticle> findByCategoryIdOrderByUpdatedAtDesc(Long categoryId);
    Optional<GuideArticle> findBySlug(String slug);
    List<GuideArticle> findByTitleContainingIgnoreCaseOrderByUpdatedAtDesc(String keyword);
}
