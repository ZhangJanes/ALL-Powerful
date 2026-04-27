package com.allpowerful.backend.message;

import java.time.LocalDateTime;

public record MessageItem(
        String sourceType,
        Long sourceId,
        String title,
        String content,
        LocalDateTime plannedAt,
        String statusLabel
) {}
