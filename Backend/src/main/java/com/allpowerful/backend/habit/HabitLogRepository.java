package com.allpowerful.backend.habit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByUserIdOrderByCheckinDateDesc(Long userId);
    List<HabitLog> findByTaskIdAndUserIdOrderByCheckinDateDesc(Long taskId, Long userId);
    Optional<HabitLog> findByTaskIdAndCheckinDate(Long taskId, LocalDate checkinDate);
    long countByTaskIdAndUserId(Long taskId, Long userId);
}
