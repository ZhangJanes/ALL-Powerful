package com.allpowerful.backend.habit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitTaskRepository extends JpaRepository<HabitTask, Long> {
    List<HabitTask> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
    Optional<HabitTask> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
