package com.allpowerful.backend.guide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class GuideDtos {
    public record CategoryRequest(
            @NotBlank String code,
            @NotBlank String title,
            String description,
            @NotNull Integer sortOrder,
            @NotNull Boolean enabled
    ) {}

    public record CategoryResponse(
            Long id,
            String code,
            String title,
            String description,
            Integer sortOrder,
            Boolean enabled,
            Integer count
    ) {}

    public record ArticleRequest(
            @NotNull Long categoryId,
            @NotBlank String slug,
            @NotBlank String title,
            String intro,
            String conditionsText,
            String materialsText,
            String processText,
            String locationText,
            String timeText,
            String periodText,
            String feeText,
            String tipsText,
            String consultUrl
    ) {}

    public record ArticleResponse(
            Long id,
            Long categoryId,
            String categoryCode,
            String slug,
            String title,
            String intro,
            String conditionsText,
            String materialsText,
            String processText,
            String locationText,
            String timeText,
            String periodText,
            String feeText,
            String tipsText,
            String consultUrl,
            Boolean favorite,
            LocalDateTime updatedAt
    ) {}

    public record FavoriteResponse(
            Long articleId,
            String title,
            String categoryCode
    ) {}

    public record LinkMemoRequest(
            String extraNote,
            String remindAt
    ) {}

    public record LinkMemoResponse(
            Long memoId
    ) {}
}
