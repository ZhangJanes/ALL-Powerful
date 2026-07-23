package com.allpowerful.backend.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerBudgetRepository extends JpaRepository<LedgerBudget, Long> {
    Optional<LedgerBudget> findByUserIdAndBudgetMonth(Long userId, String budgetMonth);
}
