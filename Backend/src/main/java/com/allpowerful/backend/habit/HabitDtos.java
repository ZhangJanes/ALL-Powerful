package com.allpowerful.backend.habit;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class HabitDtos {
    public record TaskRequest(
            @NotBlank String name,
            String icon,
            String description,
            @NotNull @Min(1) @Max(3650) Integer targetDays,
            @NotNull Boolean remindEnabled,
            LocalTime remindTime
    ) {}

    public record TaskResponse(
            Long id,
            String name,
            String icon,
            String description,
            Integer targetDays,
            Boolean remindEnabled,
            LocalTime remindTime,
            Boolean paused,
            Integer streak,
            Long total,
            Boolean doneToday,
            Integer completionRate
    ) {}

    public record LogResponse(
            Long id,
            LocalDate checkinDate,
            LocalDateTime checkinTime,
            Boolean isMakeup,
            String note
    ) {}

    public record CheckinRequest(
            String note
    ) {}

    public record MakeupRequest(
            @NotNull LocalDate checkinDate,
            String note
    ) {}

    public record DetailResponse(
            TaskResponse task,
            List<LogResponse> logs,
            List<CalendarPoint> calendar,
            Integer makeupUsed,
            Integer makeupLeft
    ) {}

    public record CalendarPoint(
            LocalDate date,
            String status
    ) {}

    public record AchievementResponse(
            String code,
            String name,
            Boolean unlocked,
            Integer progress,
            Integer target
    ) {}
}
