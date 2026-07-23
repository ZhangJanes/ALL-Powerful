package com.allpowerful.backend.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideCategoryRepository extends JpaRepository<GuideCategory, Long> {
    List<GuideCategory> findByEnabledTrueOrderBySortOrderAscIdAsc();
    Optional<GuideCategory> findByCode(String code);
}
