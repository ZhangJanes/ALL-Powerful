package com.allpowerful.backend.memo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class MemoDtos {
    public record TodoItem(Long id, @NotBlank String text, @NotNull Boolean done) {}
    public record MemoRequest(
            @NotBlank String title,
            String content,
            @NotBlank String category,
            @NotNull Boolean pinned,
            LocalDateTime remindAt,
            String remindRepeat,
            List<TodoItem> todos
    ) {}
    public record MemoResponse(
            Long id,
            String title,
            String content,
            String category,
            Boolean pinned,
            LocalDateTime remindAt,
            String remindRepeat,
            LocalDateTime updatedAt,
            List<TodoItem> todos
    ) {}
}
