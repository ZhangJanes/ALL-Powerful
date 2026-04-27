package com.allpowerful.backend.travel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class TripDtos {
    public record ChecklistItem(Long id, @NotBlank String text, @NotNull Boolean done) {}
    public record TripRequest(
            @NotBlank String title,
            @NotBlank String category,
            @NotNull LocalDateTime startAt,
            @NotNull LocalDateTime endAt,
            @NotBlank String place,
            @NotNull Boolean done,
            List<ChecklistItem> checklist
    ) {}
    public record TripResponse(
            Long id,
            String title,
            String category,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String place,
            Boolean done,
            List<ChecklistItem> checklist
    ) {}
}
