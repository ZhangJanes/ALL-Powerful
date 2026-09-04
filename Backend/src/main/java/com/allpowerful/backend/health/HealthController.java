package com.allpowerful.backend.health;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {
    private final HealthService healthService;

    @GetMapping("/dashboard")
    public ApiResponse<HealthDtos.DashboardResponse> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(healthService.dashboard(CurrentUser.id(), date));
    }

    @GetMapping("/profile")
    public ApiResponse<HealthDtos.ProfileResponse> profile() {
        return ApiResponse.ok(healthService.getProfile(CurrentUser.id()));
    }

    @PutMapping("/profile")
    public ApiResponse<HealthDtos.ProfileResponse> saveProfile(
            @Valid @RequestBody HealthDtos.ProfileRequest req
    ) {
        return ApiResponse.ok(healthService.saveProfile(CurrentUser.id(), req));
    }

    @GetMapping("/diet-plan")
    public ApiResponse<HealthDtos.DietPlanResponse> dietPlan() {
        return ApiResponse.ok(healthService.getActivePlan(CurrentUser.id()));
    }

    @GetMapping("/goals")
    public ApiResponse<List<HealthDtos.GoalResponse>> goals() {
        return ApiResponse.ok(healthService.listGoals(CurrentUser.id()));
    }

    @PostMapping("/goals")
    public ApiResponse<HealthDtos.GoalResponse> createGoal(
            @Valid @RequestBody HealthDtos.GoalRequest req
    ) {
        return ApiResponse.ok(healthService.createGoal(CurrentUser.id(), req));
    }

    @PutMapping("/goals/{id}")
    public ApiResponse<HealthDtos.GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody HealthDtos.GoalRequest req
    ) {
        return ApiResponse.ok(healthService.updateGoal(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/goals/{id}")
    public ApiResponse<Void> deleteGoal(@PathVariable Long id) {
        healthService.deleteGoal(CurrentUser.id(), id);
        return ApiResponse.ok(null, "目标已删除");
    }

    @GetMapping("/daily-records/{date}")
    public ApiResponse<HealthDtos.DailyRecordResponse> dailyRecord(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(healthService.getDailyRecord(CurrentUser.id(), date));
    }

    @PutMapping("/daily-records/{date}")
    public ApiResponse<HealthDtos.DailyRecordResponse> saveDailyRecord(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody HealthDtos.DailyRecordRequest req
    ) {
        return ApiResponse.ok(healthService.saveDailyRecord(CurrentUser.id(), date, req));
    }

    @GetMapping("/meal-checkins/{date}")
    public ApiResponse<List<HealthDtos.MealCheckinResponse>> mealCheckins(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(healthService.listMealCheckins(CurrentUser.id(), date));
    }

    @PutMapping("/meal-checkins/{date}/{mealType}")
    public ApiResponse<HealthDtos.MealCheckinResponse> saveMealCheckin(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String mealType,
            @Valid @RequestBody HealthDtos.MealCheckinRequest req
    ) {
        return ApiResponse.ok(healthService.saveMealCheckin(CurrentUser.id(), date, mealType, req));
    }

    @DeleteMapping("/meal-checkins/{date}/{mealType}")
    public ApiResponse<Void> deleteMealCheckin(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String mealType
    ) {
        healthService.deleteMealCheckin(CurrentUser.id(), date, mealType);
        return ApiResponse.ok(null, "餐次打卡已撤销");
    }

    @GetMapping("/water-logs/{date}")
    public ApiResponse<List<HealthDtos.WaterLogResponse>> waterLogs(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(healthService.listWaterLogs(CurrentUser.id(), date));
    }

    @PostMapping("/water-logs/{date}")
    public ApiResponse<HealthDtos.WaterLogResponse> addWater(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody HealthDtos.WaterLogRequest req
    ) {
        return ApiResponse.ok(healthService.addWater(CurrentUser.id(), date, req));
    }

    @DeleteMapping("/water-logs/{id}")
    public ApiResponse<Void> deleteWater(@PathVariable Long id) {
        healthService.deleteWater(CurrentUser.id(), id);
        return ApiResponse.ok(null, "饮水记录已删除");
    }

    @GetMapping("/exercise-logs/{date}")
    public ApiResponse<List<HealthDtos.ExerciseResponse>> exerciseLogs(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(healthService.listExercises(CurrentUser.id(), date));
    }

    @PostMapping("/exercise-logs/{date}")
    public ApiResponse<HealthDtos.ExerciseResponse> addExercise(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody HealthDtos.ExerciseRequest req
    ) {
        return ApiResponse.ok(healthService.addExercise(CurrentUser.id(), date, req));
    }

    @PutMapping("/exercise-logs/{id}")
    public ApiResponse<HealthDtos.ExerciseResponse> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody HealthDtos.ExerciseRequest req
    ) {
        return ApiResponse.ok(healthService.updateExercise(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/exercise-logs/{id}")
    public ApiResponse<Void> deleteExercise(@PathVariable Long id) {
        healthService.deleteExercise(CurrentUser.id(), id);
        return ApiResponse.ok(null, "运动记录已删除");
    }

    @GetMapping("/stats")
    public ApiResponse<HealthDtos.StatsResponse> stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(healthService.stats(CurrentUser.id(), from, to));
    }
}
