package com.allpowerful.backend.health;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class HealthDtos {
    private HealthDtos() {}

    public record ProfileRequest(
            @Pattern(regexp = "^(male|female)?$", message = "性别仅支持 male / female") String gender,
            @Past LocalDate birthDate,
            @DecimalMin("80") @DecimalMax("250") BigDecimal heightCm,
            @DecimalMin("20") @DecimalMax("400") BigDecimal currentWeightKg,
            @DecimalMin("20") @DecimalMax("400") BigDecimal targetWeightKg,
            @DecimalMin("1") @DecimalMax("75") BigDecimal bodyFatPercent,
            @DecimalMin("1") @DecimalMax("75") BigDecimal targetBodyFatPercent,
            @Pattern(regexp = "^(sedentary|light|moderate|very|extra)$") String activityLevel,
            @Min(800) @Max(6000) Integer targetCalories,
            @Min(500) @Max(10000) Integer waterTargetMl,
            LocalTime wakeTime,
            LocalTime sleepTime
    ) {}

    public record Calculations(
            Integer age,
            BigDecimal bmi,
            BigDecimal estimatedBodyFatPercent,
            BigDecimal effectiveBodyFatPercent,
            BigDecimal healthyBodyFatMin,
            BigDecimal healthyBodyFatMax,
            Integer restingMetabolicRate,
            Integer estimatedTdee,
            Integer calorieSuggestionMin,
            Integer calorieSuggestionMax,
            String bodyFatSource,
            String bodyFatLevel,
            String bodyFatLevelLabel,
            String bodyFatAdvice,
            String notice
    ) {}

    public record ProfileResponse(
            Long userId,
            String gender,
            LocalDate birthDate,
            BigDecimal heightCm,
            BigDecimal currentWeightKg,
            BigDecimal targetWeightKg,
            BigDecimal bodyFatPercent,
            BigDecimal targetBodyFatPercent,
            String activityLevel,
            Integer targetCalories,
            Integer waterTargetMl,
            LocalTime wakeTime,
            LocalTime sleepTime,
            Calculations calculations
    ) {}

    public record MealItemResponse(
            Long id,
            String itemName,
            String portionText,
            String detailText,
            Integer sortOrder
    ) {}

    public record MealGroupResponse(
            String mealType,
            String label,
            List<MealItemResponse> items
    ) {}

    public record GuidelineResponse(
            Long id,
            String category,
            String title,
            String content,
            Integer sortOrder
    ) {}

    public record DietPlanResponse(
            Long id,
            String title,
            String status,
            String sourceType,
            LocalDate nextReviewDate,
            String followupPlan,
            List<MealGroupResponse> meals,
            List<GuidelineResponse> guidelines
    ) {}

    public record GoalRequest(
            @NotBlank String goalType,
            @NotBlank @Size(max = 120) String title,
            @NotNull @DecimalMin("0.01") BigDecimal targetValue,
            @NotBlank @Size(max = 24) String unit,
            @NotBlank String periodType,
            @NotBlank String comparisonOperator,
            @NotNull LocalDate startDate,
            LocalDate deadline,
            String status
    ) {}

    public record GoalResponse(
            Long id,
            String goalType,
            String title,
            BigDecimal targetValue,
            String unit,
            String periodType,
            String comparisonOperator,
            LocalDate startDate,
            LocalDate deadline,
            String status
    ) {}

    public record GoalProgressResponse(
            GoalResponse goal,
            BigDecimal currentValue,
            Integer progressPercent,
            Boolean completed
    ) {}

    public record DailyRecordRequest(
            @DecimalMin("20") @DecimalMax("400") BigDecimal weightKg,
            @DecimalMin("1") @DecimalMax("75") BigDecimal bodyFatPercent,
            @Min(0) @Max(10000) Integer calorieIntake,
            LocalTime wakeTime,
            LocalTime sleepTime,
            @Min(0) @Max(1440) Integer sleepMinutes,
            @Size(max = 24) String mood,
            @Size(max = 500) String note
    ) {}

    public record DailyRecordResponse(
            Long id,
            LocalDate recordDate,
            BigDecimal weightKg,
            BigDecimal bodyFatPercent,
            Integer calorieIntake,
            LocalTime wakeTime,
            LocalTime sleepTime,
            Integer sleepMinutes,
            String mood,
            String note
    ) {}

    public record MealCheckinRequest(
            @NotBlank @Pattern(regexp = "^(completed|skipped|pending)$") String status,
            @Size(max = 255) String note
    ) {}

    public record MealCheckinResponse(
            Long id,
            LocalDate checkinDate,
            String mealType,
            String status,
            String note,
            LocalDateTime checkedAt
    ) {}

    public record WaterLogRequest(
            @NotNull @Min(1) @Max(5000) Integer amountMl,
            LocalDateTime recordedAt
    ) {}

    public record WaterLogResponse(
            Long id,
            LocalDate logDate,
            Integer amountMl,
            LocalDateTime recordedAt
    ) {}

    public record ExerciseRequest(
            @NotBlank @Pattern(regexp = "^(AEROBIC|RESISTANCE|OTHER)$") String exerciseType,
            @NotBlank @Size(max = 80) String name,
            @NotNull @Min(1) @Max(1440) Integer durationMinutes,
            @Min(30) @Max(250) Integer heartRate,
            @Size(max = 255) String note,
            LocalDateTime recordedAt
    ) {}

    public record ExerciseResponse(
            Long id,
            LocalDate logDate,
            String exerciseType,
            String name,
            Integer durationMinutes,
            Integer heartRate,
            String note,
            LocalDateTime recordedAt
    ) {}

    public record DashboardResponse(
            LocalDate date,
            ProfileResponse profile,
            DietPlanResponse activePlan,
            List<MealCheckinResponse> mealCheckins,
            Integer completedMeals,
            Integer totalMeals,
            Integer waterTotalMl,
            Integer waterTargetMl,
            List<ExerciseResponse> exercises,
            List<GoalProgressResponse> goals,
            Integer overallProgressPercent
    ) {}

    public record TrendPoint(
            LocalDate date,
            BigDecimal weightKg,
            BigDecimal bodyFatPercent,
            Integer completedMeals,
            Integer waterMl,
            Integer exerciseMinutes,
            Integer sleepMinutes
    ) {}

    public record StatsResponse(
            LocalDate from,
            LocalDate to,
            BigDecimal mealCompletionRate,
            Integer totalWaterMl,
            Integer totalExerciseMinutes,
            List<TrendPoint> trend
    ) {}
}
