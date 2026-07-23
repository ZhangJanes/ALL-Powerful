package com.allpowerful.backend.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByUserIdAndDeletedAtIsNullAndOccurredAtBetweenOrderByOccurredAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<LedgerEntry> findByUserIdAndDeletedAtIsNullOrderByOccurredAtDesc(Long userId);

    Optional<LedgerEntry> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
