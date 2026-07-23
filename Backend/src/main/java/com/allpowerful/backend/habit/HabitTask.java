package com.allpowerful.backend.habit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "habit_tasks")
public class HabitTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 32)
    private String icon;

    @Column(length = 255)
    private String description;

    @Column(name = "target_days", nullable = false)
    private Integer targetDays;

    @Column(name = "remind_enabled", nullable = false)
    private Boolean remindEnabled = false;

    @Column(name = "remind_time")
    private LocalTime remindTime;

    @Column(nullable = false)
    private Boolean paused = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
