package com.allpowerful.backend.idea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class IdeaDtos {
    public record IdeaRequest(
            @NotBlank String title,
            String body,
            @NotBlank String category,
            List<String> tags,
            List<String> imageUrls,
            @NotNull Boolean starred
    ) {}

    public record IdeaResponse(
            Long id,
            String title,
            String body,
            String category,
            List<String> tags,
            List<String> imageUrls,
            Boolean starred,
            LocalDateTime updatedAt
    ) {}
}
