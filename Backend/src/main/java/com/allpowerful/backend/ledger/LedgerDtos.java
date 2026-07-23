package com.allpowerful.backend.ledger;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class LedgerDtos {
    public record CategoryRequest(
            @NotBlank String type,
            @NotBlank String name,
            String icon,
            String color
    ) {}

    public record CategoryResponse(
            Long id,
            String type,
            String name,
            String icon,
            String color,
            Boolean isDefault
    ) {}

    public record EntryRequest(
            @NotBlank String type,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            Long categoryId,
            @NotBlank String categoryName,
            String note,
            String photoUrl,
            @NotNull LocalDateTime occurredAt
    ) {}

    public record EntryResponse(
            Long id,
            String type,
            BigDecimal amount,
            Long categoryId,
            String categoryName,
            String note,
            String photoUrl,
            LocalDateTime occurredAt
    ) {}

    public record BudgetItemRequest(
            @NotBlank String categoryName,
            @NotNull @DecimalMin(value = "0") BigDecimal amount
    ) {}

    public record BudgetRequest(
            @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$") String month,
            @NotNull @DecimalMin(value = "0") BigDecimal totalBudget,
            @Min(1) @Max(100) Integer alertThreshold,
            List<BudgetItemRequest> items
    ) {}

    public record BudgetItemResponse(
            String categoryName,
            BigDecimal amount,
            BigDecimal spent,
            BigDecimal left
    ) {}

    public record BudgetResponse(
            String month,
            BigDecimal totalBudget,
            BigDecimal spent,
            BigDecimal left,
            Integer alertThreshold,
            List<BudgetItemResponse> items
    ) {}

    public record SummaryResponse(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal avgDailyExpense,
            BigDecimal maxDailyExpense
    ) {}

    public record TrendPoint(
            String day,
            BigDecimal amount
    ) {}

    public record CategoryStat(
            String category,
            BigDecimal amount
    ) {}

    public record StatsResponse(
            SummaryResponse summary,
            List<TrendPoint> trend,
            List<CategoryStat> categories,
            List<EntryResponse> entries
    ) {}

    public record ExportResponse(
            String fileName,
            String mimeType,
            String base64
    ) {}
}
