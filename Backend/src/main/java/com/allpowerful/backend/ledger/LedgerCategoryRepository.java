package com.allpowerful.backend.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerCategoryRepository extends JpaRepository<LedgerCategory, Long> {
    List<LedgerCategory> findByUserIdAndDeletedAtIsNullOrderByTypeAscNameAsc(Long userId);
    Optional<LedgerCategory> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
