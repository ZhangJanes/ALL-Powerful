package com.allpowerful.backend.habit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HabitMakeupQuotaRepository extends JpaRepository<HabitMakeupQuota, Long> {
    Optional<HabitMakeupQuota> findByUserIdAndQuotaMonth(Long userId, String quotaMonth);
}
