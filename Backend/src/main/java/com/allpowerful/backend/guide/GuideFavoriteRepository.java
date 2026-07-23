package com.allpowerful.backend.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideFavoriteRepository extends JpaRepository<GuideFavorite, Long> {
    Optional<GuideFavorite> findByUserIdAndArticleId(Long userId, Long articleId);
    List<GuideFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
