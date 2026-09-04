package com.allpowerful.backend.health;

import com.allpowerful.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HealthService {
    private static final List<String> MEAL_TYPES = List.of("BREAKFAST", "LUNCH", "SNACK", "DINNER");
    private static final Set<String> GOAL_TYPES = Set.of(
            "MEAL_ADHERENCE", "WATER", "AEROBIC", "RESISTANCE", "WEIGHT", "BODY_FAT", "CALORIES", "SLEEP"
    );
    private static final Set<String> PERIOD_TYPES = Set.of("daily", "weekly", "monthly", "once");
    private static final Set<String> OPERATORS = Set.of("GTE", "GT", "LTE", "LT", "EQ");
    private static final Set<String> STATUSES = Set.of("active", "completed", "paused");

    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public HealthDtos.ProfileResponse getProfile(Long userId) {
        List<HealthDtos.ProfileResponse> rows = jdbc.query(
                "SELECT * FROM health_profiles WHERE user_id = ?",
                (rs, rowNum) -> profileFrom(rs, userId),
                userId
        );
        return rows.isEmpty() ? emptyProfile(userId) : rows.get(0);
    }

    @Transactional
    public HealthDtos.ProfileResponse saveProfile(Long userId, HealthDtos.ProfileRequest req) {
        String activity = req.activityLevel() == null ? "sedentary" : req.activityLevel();
        jdbc.update("""
                INSERT INTO health_profiles(
                    user_id, gender, birth_date, height_cm, current_weight_kg, target_weight_kg,
                    body_fat_percent, target_body_fat_percent, activity_level, target_calories,
                    water_target_ml, wake_time, sleep_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    gender = VALUES(gender), birth_date = VALUES(birth_date), height_cm = VALUES(height_cm),
                    current_weight_kg = VALUES(current_weight_kg), target_weight_kg = VALUES(target_weight_kg),
                    body_fat_percent = VALUES(body_fat_percent),
                    target_body_fat_percent = VALUES(target_body_fat_percent),
                    activity_level = VALUES(activity_level), target_calories = VALUES(target_calories),
                    water_target_ml = VALUES(water_target_ml), wake_time = VALUES(wake_time),
                    sleep_time = VALUES(sleep_time), updated_at = CURRENT_TIMESTAMP
                """,
                userId, req.gender(), req.birthDate(), req.heightCm(), req.currentWeightKg(),
                req.targetWeightKg(), req.bodyFatPercent(), req.targetBodyFatPercent(), activity,
                req.targetCalories(), req.waterTargetMl() == null ? 2000 : req.waterTargetMl(),
                req.wakeTime(), req.sleepTime()
        );
        return getProfile(userId);
    }

    @Transactional(readOnly = true)
    public HealthDtos.DietPlanResponse getActivePlan(Long userId) {
        List<Map<String, Object>> plans = jdbc.queryForList("""
                SELECT id, title, status, source_type, next_review_date, followup_plan
                FROM health_diet_plans
                WHERE user_id = ? AND status = 'active'
                ORDER BY id DESC LIMIT 1
                """, userId);
        if (plans.isEmpty()) return null;
        Map<String, Object> plan = plans.get(0);
        Long planId = ((Number) plan.get("id")).longValue();

        Map<String, List<HealthDtos.MealItemResponse>> grouped = new LinkedHashMap<>();
        for (String type : List.of("BREAKFAST", "LUNCH", "SNACK", "DINNER", "WATER")) {
            grouped.put(type, new ArrayList<>());
        }
        jdbc.query("""
                SELECT meal_type, id, item_name, portion_text, detail_text, sort_order
                FROM health_plan_meal_items WHERE plan_id = ?
                ORDER BY FIELD(meal_type, 'BREAKFAST', 'LUNCH', 'SNACK', 'DINNER', 'WATER'), sort_order
                """, rs -> {
            grouped.computeIfAbsent(rs.getString("meal_type"), ignored -> new ArrayList<>()).add(
                    new HealthDtos.MealItemResponse(
                            rs.getLong("id"), rs.getString("item_name"), rs.getString("portion_text"),
                            rs.getString("detail_text"), rs.getInt("sort_order")
                    )
            );
        }, planId);
        List<HealthDtos.MealGroupResponse> meals = grouped.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> new HealthDtos.MealGroupResponse(e.getKey(), mealLabel(e.getKey()), e.getValue()))
                .toList();
        List<HealthDtos.GuidelineResponse> guidelines = jdbc.query("""
                SELECT id, category, title, content, sort_order
                FROM health_plan_guidelines WHERE plan_id = ?
                ORDER BY FIELD(category, 'EXECUTION', 'EXERCISE'), sort_order
                """, (rs, n) -> new HealthDtos.GuidelineResponse(
                rs.getLong("id"), rs.getString("category"), rs.getString("title"),
                rs.getString("content"), rs.getInt("sort_order")
        ), planId);

        return new HealthDtos.DietPlanResponse(
                planId,
                (String) plan.get("title"),
                (String) plan.get("status"),
                (String) plan.get("source_type"),
                toLocalDate(plan.get("next_review_date")),
                (String) plan.get("followup_plan"),
                meals,
                guidelines
        );
    }

    @Transactional(readOnly = true)
    public List<HealthDtos.GoalResponse> listGoals(Long userId) {
        return jdbc.query("""
                SELECT * FROM health_goals WHERE user_id = ?
                ORDER BY CASE status WHEN 'active' THEN 0 ELSE 1 END, created_at DESC
                """, (rs, n) -> goalFrom(rs), userId);
    }

    @Transactional
    public HealthDtos.GoalResponse createGoal(Long userId, HealthDtos.GoalRequest req) {
        validateGoal(req);
        long id = insertKey("""
                INSERT INTO health_goals(
                    user_id, goal_type, title, target_value, unit, period_type,
                    comparison_operator, start_date, deadline, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, userId, req.goalType().toUpperCase(Locale.ROOT), req.title().trim(), req.targetValue(),
                req.unit().trim(), req.periodType(), req.comparisonOperator().toUpperCase(Locale.ROOT),
                req.startDate(), req.deadline(), normalizeStatus(req.status()));
        return findGoal(userId, id);
    }

    @Transactional
    public HealthDtos.GoalResponse updateGoal(Long userId, Long id, HealthDtos.GoalRequest req) {
        validateGoal(req);
        int changed = jdbc.update("""
                UPDATE health_goals SET goal_type = ?, title = ?, target_value = ?, unit = ?,
                    period_type = ?, comparison_operator = ?, start_date = ?, deadline = ?,
                    status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND user_id = ?
                """, req.goalType().toUpperCase(Locale.ROOT), req.title().trim(), req.targetValue(),
                req.unit().trim(), req.periodType(), req.comparisonOperator().toUpperCase(Locale.ROOT),
                req.startDate(), req.deadline(), normalizeStatus(req.status()), id, userId);
        if (changed == 0) throw new AppException("目标不存在");
        return findGoal(userId, id);
    }

    @Transactional
    public void deleteGoal(Long userId, Long id) {
        if (jdbc.update("DELETE FROM health_goals WHERE id = ? AND user_id = ?", id, userId) == 0) {
            throw new AppException("目标不存在");
        }
    }

    @Transactional(readOnly = true)
    public HealthDtos.DailyRecordResponse getDailyRecord(Long userId, LocalDate date) {
        List<HealthDtos.DailyRecordResponse> rows = jdbc.query("""
                SELECT * FROM health_daily_records WHERE user_id = ? AND record_date = ?
                """, (rs, n) -> dailyRecordFrom(rs), userId, date);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Transactional
    public HealthDtos.DailyRecordResponse saveDailyRecord(
            Long userId, LocalDate date, HealthDtos.DailyRecordRequest req
    ) {
        jdbc.update("""
                INSERT INTO health_daily_records(
                    user_id, record_date, weight_kg, body_fat_percent, calorie_intake, wake_time,
                    sleep_time, sleep_minutes, mood, note
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE weight_kg = VALUES(weight_kg),
                    body_fat_percent = VALUES(body_fat_percent), calorie_intake = VALUES(calorie_intake),
                    wake_time = VALUES(wake_time),
                    sleep_time = VALUES(sleep_time), sleep_minutes = VALUES(sleep_minutes),
                    mood = VALUES(mood), note = VALUES(note), updated_at = CURRENT_TIMESTAMP
                """, userId, date, req.weightKg(), req.bodyFatPercent(), req.calorieIntake(), req.wakeTime(),
                req.sleepTime(), req.sleepMinutes(), blankToNull(req.mood()), blankToNull(req.note()));
        return getDailyRecord(userId, date);
    }

    @Transactional(readOnly = true)
    public List<HealthDtos.MealCheckinResponse> listMealCheckins(Long userId, LocalDate date) {
        return jdbc.query("""
                SELECT * FROM health_meal_checkins
                WHERE user_id = ? AND checkin_date = ?
                ORDER BY FIELD(meal_type, 'BREAKFAST', 'LUNCH', 'SNACK', 'DINNER')
                """, (rs, n) -> mealCheckinFrom(rs), userId, date);
    }

    @Transactional
    public HealthDtos.MealCheckinResponse saveMealCheckin(
            Long userId, LocalDate date, String mealType, HealthDtos.MealCheckinRequest req
    ) {
        String type = normalizeMealType(mealType);
        jdbc.update("""
                INSERT INTO health_meal_checkins(
                    user_id, checkin_date, meal_type, status, note, checked_at
                ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE status = VALUES(status), note = VALUES(note),
                    checked_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                """, userId, date, type, req.status(), blankToNull(req.note()));
        return jdbc.queryForObject("""
                SELECT * FROM health_meal_checkins
                WHERE user_id = ? AND checkin_date = ? AND meal_type = ?
                """, (rs, n) -> mealCheckinFrom(rs), userId, date, type);
    }

    @Transactional
    public void deleteMealCheckin(Long userId, LocalDate date, String mealType) {
        jdbc.update("""
                DELETE FROM health_meal_checkins
                WHERE user_id = ? AND checkin_date = ? AND meal_type = ?
                """, userId, date, normalizeMealType(mealType));
    }

    @Transactional(readOnly = true)
    public List<HealthDtos.WaterLogResponse> listWaterLogs(Long userId, LocalDate date) {
        return jdbc.query("""
                SELECT * FROM health_water_logs WHERE user_id = ? AND log_date = ?
                ORDER BY recorded_at DESC
                """, (rs, n) -> waterFrom(rs), userId, date);
    }

    @Transactional
    public HealthDtos.WaterLogResponse addWater(Long userId, LocalDate date, HealthDtos.WaterLogRequest req) {
        LocalDateTime recordedAt = req.recordedAt() == null ? LocalDateTime.now() : req.recordedAt();
        long id = insertKey("""
                INSERT INTO health_water_logs(user_id, log_date, amount_ml, recorded_at)
                VALUES (?, ?, ?, ?)
                """, userId, date, req.amountMl(), recordedAt);
        return jdbc.queryForObject("SELECT * FROM health_water_logs WHERE id = ? AND user_id = ?",
                (rs, n) -> waterFrom(rs), id, userId);
    }

    @Transactional
    public void deleteWater(Long userId, Long id) {
        if (jdbc.update("DELETE FROM health_water_logs WHERE id = ? AND user_id = ?", id, userId) == 0) {
            throw new AppException("饮水记录不存在");
        }
    }

    @Transactional(readOnly = true)
    public List<HealthDtos.ExerciseResponse> listExercises(Long userId, LocalDate date) {
        return jdbc.query("""
                SELECT * FROM health_exercise_logs WHERE user_id = ? AND log_date = ?
                ORDER BY recorded_at DESC
                """, (rs, n) -> exerciseFrom(rs), userId, date);
    }

    @Transactional
    public HealthDtos.ExerciseResponse addExercise(
            Long userId, LocalDate date, HealthDtos.ExerciseRequest req
    ) {
        LocalDateTime recordedAt = req.recordedAt() == null ? LocalDateTime.now() : req.recordedAt();
        long id = insertKey("""
                INSERT INTO health_exercise_logs(
                    user_id, log_date, exercise_type, name, duration_minutes,
                    heart_rate, note, recorded_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, userId, date, req.exerciseType(), req.name().trim(), req.durationMinutes(),
                req.heartRate(), blankToNull(req.note()), recordedAt);
        return findExercise(userId, id);
    }

    @Transactional
    public HealthDtos.ExerciseResponse updateExercise(
            Long userId, Long id, HealthDtos.ExerciseRequest req
    ) {
        HealthDtos.ExerciseResponse existing = findExercise(userId, id);
        LocalDateTime recordedAt = req.recordedAt() == null ? existing.recordedAt() : req.recordedAt();
        int changed = jdbc.update("""
                UPDATE health_exercise_logs SET log_date = ?, exercise_type = ?, name = ?,
                    duration_minutes = ?, heart_rate = ?, note = ?, recorded_at = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND user_id = ?
                """, recordedAt.toLocalDate(), req.exerciseType(), req.name().trim(),
                req.durationMinutes(), req.heartRate(), blankToNull(req.note()), recordedAt, id, userId);
        if (changed == 0) throw new AppException("运动记录不存在");
        return findExercise(userId, id);
    }

    @Transactional
    public void deleteExercise(Long userId, Long id) {
        if (jdbc.update("DELETE FROM health_exercise_logs WHERE id = ? AND user_id = ?", id, userId) == 0) {
            throw new AppException("运动记录不存在");
        }
    }

    @Transactional(readOnly = true)
    public HealthDtos.DashboardResponse dashboard(Long userId, LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        HealthDtos.ProfileResponse profile = getProfile(userId);
        HealthDtos.DietPlanResponse plan = getActivePlan(userId);
        List<HealthDtos.MealCheckinResponse> meals = listMealCheckins(userId, target);
        int completedMeals = (int) meals.stream().filter(x -> "completed".equals(x.status())).count();
        int waterTotal = waterTotal(userId, target, target);
        List<HealthDtos.ExerciseResponse> exercises = listExercises(userId, target);
        List<HealthDtos.GoalProgressResponse> goals = listGoals(userId).stream()
                .filter(x -> "active".equals(x.status()))
                .map(x -> goalProgress(userId, target, x))
                .toList();
        int mealProgress = Math.min(100, completedMeals * 100 / MEAL_TYPES.size());
        int waterProgress = Math.min(100, waterTotal * 100 / Math.max(1, profile.waterTargetMl() + 1));
        int overall = (mealProgress + waterProgress) / 2;
        return new HealthDtos.DashboardResponse(
                target, profile, plan, meals, completedMeals, MEAL_TYPES.size(),
                waterTotal, profile.waterTargetMl(), exercises, goals, overall
        );
    }

    @Transactional(readOnly = true)
    public HealthDtos.StatsResponse stats(Long userId, LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (start.isAfter(end)) throw new AppException("开始日期不能晚于结束日期");
        if (ChronoUnit.DAYS.between(start, end) > 365) throw new AppException("统计范围不能超过366天");

        Map<LocalDate, HealthDtos.DailyRecordResponse> daily = new HashMap<>();
        jdbc.query("""
                SELECT * FROM health_daily_records
                WHERE user_id = ? AND record_date BETWEEN ? AND ?
                """, rs -> {
            HealthDtos.DailyRecordResponse row = dailyRecordFrom(rs);
            daily.put(row.recordDate(), row);
        }, userId, start, end);

        Map<LocalDate, Integer> meals = countByDate("""
                SELECT checkin_date d, COUNT(*) value FROM health_meal_checkins
                WHERE user_id = ? AND checkin_date BETWEEN ? AND ? AND status = 'completed'
                GROUP BY checkin_date
                """, userId, start, end);
        Map<LocalDate, Integer> water = countByDate("""
                SELECT log_date d, COALESCE(SUM(amount_ml), 0) value FROM health_water_logs
                WHERE user_id = ? AND log_date BETWEEN ? AND ? GROUP BY log_date
                """, userId, start, end);
        Map<LocalDate, Integer> exercise = countByDate("""
                SELECT log_date d, COALESCE(SUM(duration_minutes), 0) value FROM health_exercise_logs
                WHERE user_id = ? AND log_date BETWEEN ? AND ? GROUP BY log_date
                """, userId, start, end);

        List<HealthDtos.TrendPoint> trend = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            HealthDtos.DailyRecordResponse record = daily.get(d);
            trend.add(new HealthDtos.TrendPoint(
                    d,
                    record == null ? null : record.weightKg(),
                    record == null ? null : record.bodyFatPercent(),
                    meals.getOrDefault(d, 0),
                    water.getOrDefault(d, 0),
                    exercise.getOrDefault(d, 0),
                    record == null ? null : record.sleepMinutes()
            ));
        }
        int days = trend.size();
        int completedMealCount = meals.values().stream().mapToInt(Integer::intValue).sum();
        BigDecimal completionRate = BigDecimal.valueOf(completedMealCount * 100.0 / (days * MEAL_TYPES.size()))
                .setScale(1, RoundingMode.HALF_UP);
        return new HealthDtos.StatsResponse(
                start, end, completionRate,
                water.values().stream().mapToInt(Integer::intValue).sum(),
                exercise.values().stream().mapToInt(Integer::intValue).sum(),
                trend
        );
    }

    HealthDtos.Calculations calculate(HealthDtos.ProfileResponse profile) {
        Integer age = profile.birthDate() == null ? null
                : Period.between(profile.birthDate(), LocalDate.now()).getYears();
        BigDecimal bmi = null;
        if (profile.heightCm() != null && profile.currentWeightKg() != null) {
            BigDecimal heightM = profile.heightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            bmi = profile.currentWeightKg().divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);
        }

        BigDecimal estimatedFat = null;
        if (bmi != null && age != null && age >= 18 && age <= 79
                && ("male".equals(profile.gender()) || "female".equals(profile.gender()))) {
            int sex = "male".equals(profile.gender()) ? 1 : 0;
            estimatedFat = bmi.multiply(BigDecimal.valueOf(1.2))
                    .add(BigDecimal.valueOf(age).multiply(BigDecimal.valueOf(0.23)))
                    .subtract(BigDecimal.valueOf(10.8 * sex))
                    .subtract(BigDecimal.valueOf(5.4))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal minFat = null;
        BigDecimal maxFat = null;
        if (age != null && age >= 20 && age <= 79 && profile.gender() != null) {
            if ("male".equals(profile.gender())) {
                if (age < 40) { minFat = bd(8); maxFat = bd(20.9); }
                else if (age < 60) { minFat = bd(11); maxFat = bd(22.9); }
                else { minFat = bd(13); maxFat = bd(24.9); }
            } else if ("female".equals(profile.gender())) {
                if (age < 40) { minFat = bd(21); maxFat = bd(32.9); }
                else if (age < 60) { minFat = bd(23); maxFat = bd(34.9); }
                else { minFat = bd(25); maxFat = bd(37.9); }
            }
        }

        Integer rmr = null;
        Integer tdee = null;
        Integer calorieMin = null;
        Integer calorieMax = null;
        if (age != null && age >= 19 && age <= 78 && profile.heightCm() != null
                && profile.currentWeightKg() != null && profile.gender() != null) {
            double sexTerm = "male".equals(profile.gender()) ? 5 : -161;
            rmr = (int) Math.round(10 * profile.currentWeightKg().doubleValue()
                    + 6.25 * profile.heightCm().doubleValue() - 5 * age + sexTerm);
            double factor = switch (profile.activityLevel()) {
                case "light" -> 1.375;
                case "moderate" -> 1.55;
                case "very" -> 1.725;
                case "extra" -> 1.9;
                default -> 1.2;
            };
            tdee = (int) Math.round(rmr * factor);
            calorieMin = Math.max(rmr, tdee - 500);
            calorieMax = Math.max(rmr, tdee - 300);
        }
        BigDecimal effectiveFat = profile.bodyFatPercent() != null ? profile.bodyFatPercent() : estimatedFat;
        String fatLevel = null;
        String fatLevelLabel = null;
        String fatAdvice = null;
        if (effectiveFat != null && minFat != null && maxFat != null) {
            BigDecimal overweightMax;
            if ("male".equals(profile.gender())) {
                overweightMax = age < 40 ? bd(25.9) : age < 60 ? bd(28.9) : bd(30.9);
            } else {
                overweightMax = age < 40 ? bd(38.9) : age < 60 ? bd(40.9) : bd(42.9);
            }
            if (effectiveFat.compareTo(minFat) < 0) {
                fatLevel = "LOW";
                fatLevelLabel = "体脂偏低";
                fatAdvice = "不建议继续减脂；优先保证能量和蛋白质摄入，必要时咨询医生或营养师。";
            } else if (effectiveFat.compareTo(maxFat) <= 0) {
                fatLevel = "HEALTHY";
                fatLevelLabel = "健康范围";
                fatAdvice = "建议保持规律饮食、充足睡眠和稳定运动，持续观察体重与体脂趋势。";
            } else if (effectiveFat.compareTo(overweightMax) <= 0) {
                fatLevel = "HIGH";
                fatLevelLabel = "体脂偏高";
                fatAdvice = "建议在医学营养方案下逐步减脂，重点改善饮食执行、饮水和运动完成度。";
            } else {
                fatLevel = "OBESE";
                fatLevelLabel = "肥胖范围";
                fatAdvice = "建议继续执行医学减重方案，并结合医生或营养师随访，不宜采取极端节食。";
            }
        }
        return new HealthDtos.Calculations(
                age, bmi, estimatedFat, effectiveFat, minFat, maxFat, rmr, tdee,
                calorieMin, calorieMax,
                profile.bodyFatPercent() != null ? "measured" : estimatedFat == null ? null : "estimated",
                fatLevel, fatLevelLabel, fatAdvice,
                "计算结果仅为成人健康管理估算，不替代体成分检测、医生诊断或图片中的医学营养方案。"
        );
    }

    private HealthDtos.ProfileResponse profileFrom(ResultSet rs, Long userId) throws SQLException {
        HealthDtos.ProfileResponse raw = new HealthDtos.ProfileResponse(
                userId, rs.getString("gender"), localDate(rs, "birth_date"),
                rs.getBigDecimal("height_cm"), rs.getBigDecimal("current_weight_kg"),
                rs.getBigDecimal("target_weight_kg"), rs.getBigDecimal("body_fat_percent"),
                rs.getBigDecimal("target_body_fat_percent"), rs.getString("activity_level"),
                nullableInt(rs, "target_calories"), nullableInt(rs, "water_target_ml"),
                localTime(rs, "wake_time"), localTime(rs, "sleep_time"), null
        );
        return withCalculations(raw);
    }

    private HealthDtos.ProfileResponse emptyProfile(Long userId) {
        return withCalculations(new HealthDtos.ProfileResponse(
                userId, null, null, null, null, null, null, null,
                "sedentary", null, 2000, null, null, null
        ));
    }

    private HealthDtos.ProfileResponse withCalculations(HealthDtos.ProfileResponse raw) {
        return new HealthDtos.ProfileResponse(
                raw.userId(), raw.gender(), raw.birthDate(), raw.heightCm(), raw.currentWeightKg(),
                raw.targetWeightKg(), raw.bodyFatPercent(), raw.targetBodyFatPercent(),
                raw.activityLevel(), raw.targetCalories(), raw.waterTargetMl(),
                raw.wakeTime(), raw.sleepTime(), calculate(raw)
        );
    }

    private HealthDtos.GoalResponse findGoal(Long userId, Long id) {
        List<HealthDtos.GoalResponse> rows = jdbc.query(
                "SELECT * FROM health_goals WHERE id = ? AND user_id = ?",
                (rs, n) -> goalFrom(rs), id, userId);
        if (rows.isEmpty()) throw new AppException("目标不存在");
        return rows.get(0);
    }

    private HealthDtos.GoalResponse goalFrom(ResultSet rs) throws SQLException {
        return new HealthDtos.GoalResponse(
                rs.getLong("id"), rs.getString("goal_type"), rs.getString("title"),
                rs.getBigDecimal("target_value"), rs.getString("unit"), rs.getString("period_type"),
                rs.getString("comparison_operator"), localDate(rs, "start_date"),
                localDate(rs, "deadline"), rs.getString("status")
        );
    }

    private HealthDtos.DailyRecordResponse dailyRecordFrom(ResultSet rs) throws SQLException {
        return new HealthDtos.DailyRecordResponse(
                rs.getLong("id"), localDate(rs, "record_date"), rs.getBigDecimal("weight_kg"),
                rs.getBigDecimal("body_fat_percent"), nullableInt(rs, "calorie_intake"),
                localTime(rs, "wake_time"),
                localTime(rs, "sleep_time"), nullableInt(rs, "sleep_minutes"),
                rs.getString("mood"), rs.getString("note")
        );
    }

    private HealthDtos.MealCheckinResponse mealCheckinFrom(ResultSet rs) throws SQLException {
        return new HealthDtos.MealCheckinResponse(
                rs.getLong("id"), localDate(rs, "checkin_date"), rs.getString("meal_type"),
                rs.getString("status"), rs.getString("note"),
                rs.getTimestamp("checked_at").toLocalDateTime()
        );
    }

    private HealthDtos.WaterLogResponse waterFrom(ResultSet rs) throws SQLException {
        return new HealthDtos.WaterLogResponse(
                rs.getLong("id"), localDate(rs, "log_date"), rs.getInt("amount_ml"),
                rs.getTimestamp("recorded_at").toLocalDateTime()
        );
    }

    private HealthDtos.ExerciseResponse exerciseFrom(ResultSet rs) throws SQLException {
        return new HealthDtos.ExerciseResponse(
                rs.getLong("id"), localDate(rs, "log_date"), rs.getString("exercise_type"),
                rs.getString("name"), rs.getInt("duration_minutes"), nullableInt(rs, "heart_rate"),
                rs.getString("note"), rs.getTimestamp("recorded_at").toLocalDateTime()
        );
    }

    private HealthDtos.ExerciseResponse findExercise(Long userId, Long id) {
        List<HealthDtos.ExerciseResponse> rows = jdbc.query(
                "SELECT * FROM health_exercise_logs WHERE id = ? AND user_id = ?",
                (rs, n) -> exerciseFrom(rs), id, userId);
        if (rows.isEmpty()) throw new AppException("运动记录不存在");
        return rows.get(0);
    }

    private HealthDtos.GoalProgressResponse goalProgress(
            Long userId, LocalDate date, HealthDtos.GoalResponse goal
    ) {
        boolean withinDates = !date.isBefore(goal.startDate())
                && (goal.deadline() == null || !date.isAfter(goal.deadline()));
        if (!withinDates) {
            return new HealthDtos.GoalProgressResponse(goal, BigDecimal.ZERO, 0, false);
        }
        BigDecimal current = switch (goal.goalType()) {
            case "MEAL_ADHERENCE" -> bd(completedMealCount(
                    userId, periodStart(date, goal), periodEnd(date, goal)
            ));
            case "WATER" -> bd(waterTotal(userId, periodStart(date, goal), periodEnd(date, goal)));
            case "AEROBIC", "RESISTANCE" -> bd(exerciseCount(
                    userId, goal.goalType(), periodStart(date, goal), periodEnd(date, goal)
            ));
            case "WEIGHT" -> latestMetric(userId, "weight_kg", date);
            case "BODY_FAT" -> latestMetric(userId, "body_fat_percent", date);
            case "SLEEP" -> latestMetric(userId, "sleep_minutes", date);
            case "CALORIES" -> latestMetric(userId, "calorie_intake", date);
            default -> null;
        };
        boolean hasValue = current != null;
        if (current == null) current = BigDecimal.ZERO;
        boolean completed = hasValue && compare(current, goal.targetValue(), goal.comparisonOperator());
        int percent;
        if ("LTE".equals(goal.comparisonOperator()) || "LT".equals(goal.comparisonOperator())) {
            percent = completed ? 100 : 0;
        } else {
            percent = Math.min(100, current.multiply(BigDecimal.valueOf(100))
                    .divide(goal.targetValue(), 0, RoundingMode.HALF_UP).intValue());
        }
        return new HealthDtos.GoalProgressResponse(goal, current, percent, completed);
    }

    private int completedMealCount(Long userId, LocalDate from, LocalDate to) {
        Integer value = jdbc.queryForObject("""
                SELECT COUNT(*) FROM health_meal_checkins
                WHERE user_id = ? AND checkin_date BETWEEN ? AND ? AND status = 'completed'
                """, Integer.class, userId, from, to);
        return value == null ? 0 : value;
    }

    private LocalDate periodStart(LocalDate date, HealthDtos.GoalResponse goal) {
        LocalDate start = switch (goal.periodType()) {
            case "weekly" -> date.with(DayOfWeek.MONDAY);
            case "monthly" -> date.withDayOfMonth(1);
            case "once" -> goal.startDate();
            default -> date;
        };
        return start.isBefore(goal.startDate()) ? goal.startDate() : start;
    }

    private LocalDate periodEnd(LocalDate date, HealthDtos.GoalResponse goal) {
        LocalDate end = switch (goal.periodType()) {
            case "weekly" -> date.with(DayOfWeek.SUNDAY);
            case "monthly" -> date.withDayOfMonth(date.lengthOfMonth());
            case "once" -> goal.deadline() == null ? date : goal.deadline();
            default -> date;
        };
        return goal.deadline() != null && end.isAfter(goal.deadline()) ? goal.deadline() : end;
    }

    private int waterTotal(Long userId, LocalDate from, LocalDate to) {
        Integer value = jdbc.queryForObject("""
                SELECT COALESCE(SUM(amount_ml), 0) FROM health_water_logs
                WHERE user_id = ? AND log_date BETWEEN ? AND ?
                """, Integer.class, userId, from, to);
        return value == null ? 0 : value;
    }

    private int exerciseCount(Long userId, String type, LocalDate from, LocalDate to) {
        Integer value = jdbc.queryForObject("""
                SELECT COUNT(*) FROM health_exercise_logs
                WHERE user_id = ? AND exercise_type = ? AND log_date BETWEEN ? AND ?
                """, Integer.class, userId, type, from, to);
        return value == null ? 0 : value;
    }

    private BigDecimal latestMetric(Long userId, String column, LocalDate date) {
        if (!Set.of("weight_kg", "body_fat_percent", "sleep_minutes", "calorie_intake").contains(column)) {
            throw new AppException("不支持的指标");
        }
        return jdbc.query("""
                SELECT %s FROM health_daily_records
                WHERE user_id = ? AND record_date <= ? AND %s IS NOT NULL
                ORDER BY record_date DESC LIMIT 1
                """.formatted(column, column), rs -> rs.next() ? rs.getBigDecimal(1) : null, userId, date);
    }

    private Map<LocalDate, Integer> countByDate(String sql, Object... args) {
        Map<LocalDate, Integer> result = new HashMap<>();
        jdbc.query(sql, rs -> {
            result.put(localDate(rs, "d"), rs.getInt("value"));
        }, args);
        return result;
    }

    private long insertKey(String sql, Object... args) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, holder);
        Number key = holder.getKey();
        if (key == null) throw new AppException("保存失败");
        return key.longValue();
    }

    private void validateGoal(HealthDtos.GoalRequest req) {
        String type = req.goalType().toUpperCase(Locale.ROOT);
        String operator = req.comparisonOperator().toUpperCase(Locale.ROOT);
        if (!GOAL_TYPES.contains(type)) throw new AppException("不支持的目标类型");
        if (!PERIOD_TYPES.contains(req.periodType())) throw new AppException("不支持的目标周期");
        if (!OPERATORS.contains(operator)) throw new AppException("不支持的比较方式");
        if (req.deadline() != null && req.deadline().isBefore(req.startDate())) {
            throw new AppException("截止日期不能早于开始日期");
        }
        normalizeStatus(req.status());
    }

    private String normalizeStatus(String status) {
        String value = status == null || status.isBlank() ? "active" : status.toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(value)) throw new AppException("不支持的目标状态");
        return value;
    }

    private String normalizeMealType(String mealType) {
        String value = mealType == null ? "" : mealType.toUpperCase(Locale.ROOT);
        if (!MEAL_TYPES.contains(value)) throw new AppException("不支持的餐次");
        return value;
    }

    private boolean compare(BigDecimal value, BigDecimal target, String operator) {
        int result = value.compareTo(target);
        return switch (operator) {
            case "GT" -> result > 0;
            case "LTE" -> result <= 0;
            case "LT" -> result < 0;
            case "EQ" -> result == 0;
            default -> result >= 0;
        };
    }

    private String mealLabel(String type) {
        return switch (type) {
            case "BREAKFAST" -> "早餐";
            case "LUNCH" -> "午餐";
            case "SNACK" -> "午加餐";
            case "DINNER" -> "晚餐";
            case "WATER" -> "全天饮水";
            default -> type;
        };
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        java.sql.Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private static LocalTime localTime(ResultSet rs, String column) throws SQLException {
        Time value = rs.getTime(column);
        return value == null ? null : value.toLocalTime();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate d) return d;
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        return LocalDate.parse(value.toString());
    }

    private static BigDecimal bd(Number value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue()).stripTrailingZeros();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
