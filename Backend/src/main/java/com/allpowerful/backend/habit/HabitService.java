package com.allpowerful.backend.habit;

import com.allpowerful.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final HabitTaskRepository taskRepository;
    private final HabitLogRepository logRepository;
    private final HabitMakeupQuotaRepository makeupQuotaRepository;

    @Transactional(readOnly = true)
    public List<HabitDtos.TaskResponse> list(Long userId) {
        return taskRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId).stream().map(t -> toTaskResponse(t, userId)).toList();
    }

    @Transactional
    public HabitDtos.TaskResponse create(Long userId, HabitDtos.TaskRequest req) {
        HabitTask task = new HabitTask();
        patch(task, userId, req);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return toTaskResponse(task, userId);
    }

    @Transactional
    public HabitDtos.TaskResponse update(Long userId, Long id, HabitDtos.TaskRequest req) {
        HabitTask task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("任务不存在"));
        patch(task, userId, req);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return toTaskResponse(task, userId);
    }

    @Transactional
    public HabitDtos.TaskResponse togglePause(Long userId, Long id) {
        HabitTask task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("任务不存在"));
        task.setPaused(!task.getPaused());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return toTaskResponse(task, userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        HabitTask task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("任务不存在"));
        task.setDeletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    public HabitDtos.TaskResponse checkin(Long userId, Long id, HabitDtos.CheckinRequest req) {
        HabitTask task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("任务不存在"));
        if (Boolean.TRUE.equals(task.getPaused())) throw new AppException("任务已暂停");
        LocalDate today = LocalDate.now();
        if (logRepository.findByTaskIdAndCheckinDate(task.getId(), today).isPresent()) {
            throw new AppException("今日已打卡");
        }
        HabitLog log = new HabitLog();
        log.setTaskId(task.getId());
        log.setUserId(userId);
        log.setCheckinDate(today);
        log.setCheckinTime(LocalDateTime.now());
        log.setIsMakeup(false);
        log.setNote(req == null ? null : req.note());
        log.setCreatedAt(LocalDateTime.now());
        logRepository.save(log);
        return toTaskResponse(task, userId);
    }

    @Transactional
    public HabitDtos.TaskResponse makeup(Long userId, Long id, HabitDtos.MakeupRequest req) {
        HabitTask task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("任务不存在"));
        LocalDate day = req.checkinDate();
        if (day.isAfter(LocalDate.now()) || day.isEqual(LocalDate.now())) throw new AppException("补卡日期必须是过去日期");
        if (logRepository.findByTaskIdAndCheckinDate(task.getId(), day).isPresent()) throw new AppException("该日期已打卡");

        HabitMakeupQuota quota = loadQuota(userId, YearMonth.now());
        if (quota.getUsedCount() >= 3) throw new AppException("本月补卡次数已用完");
        quota.setUsedCount(quota.getUsedCount() + 1);
        quota.setUpdatedAt(LocalDateTime.now());
        makeupQuotaRepository.save(quota);

        HabitLog log = new HabitLog();
        log.setTaskId(task.getId());
        log.setUserId(userId);
        log.setCheckinDate(day);
        log.setCheckinTime(LocalDateTime.now());
        log.setIsMakeup(true);
        log.setNote(req.note());
        log.setCreatedAt(LocalDateTime.now());
        logRepository.save(log);
        return toTaskResponse(task, userId);
    }

    @Transactional(readOnly = true)
    public HabitDtos.DetailResponse detail(Long userId, Long id) {
        HabitTask task = taskRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("任务不存在"));
        List<HabitLog> logs = logRepository.findByTaskIdAndUserIdOrderByCheckinDateDesc(task.getId(), userId);
        HabitDtos.TaskResponse taskResponse = toTaskResponse(task, userId);
        List<HabitDtos.LogResponse> logResponses = logs.stream().map(this::toLogResponse).toList();

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(59);
        List<HabitDtos.CalendarPoint> calendar = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            LocalDate currentDay = d;
            boolean checked = logs.stream().anyMatch(x -> x.getCheckinDate().equals(currentDay));
            String status = checked ? "checked" : (d.isBefore(today) ? "missed" : "pending");
            calendar.add(new HabitDtos.CalendarPoint(d, status));
        }
        HabitMakeupQuota quota = loadQuota(userId, YearMonth.now());
        return new HabitDtos.DetailResponse(taskResponse, logResponses, calendar, quota.getUsedCount(), 3 - quota.getUsedCount());
    }

    @Transactional(readOnly = true)
    public List<HabitDtos.AchievementResponse> achievements(Long userId) {
        List<HabitTask> tasks = taskRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        long maxStreak = tasks.stream().mapToLong(t -> computeStreak(t.getId(), userId)).max().orElse(0);
        long totalCheckin = logRepository.findByUserIdOrderByCheckinDateDesc(userId).size();
        List<HabitDtos.AchievementResponse> rows = new ArrayList<>();
        rows.add(new HabitDtos.AchievementResponse("streak7", "连续打卡 7 天", maxStreak >= 7, (int) Math.min(maxStreak, 7), 7));
        rows.add(new HabitDtos.AchievementResponse("streak30", "连续打卡 30 天", maxStreak >= 30, (int) Math.min(maxStreak, 30), 30));
        rows.add(new HabitDtos.AchievementResponse("total30", "累计打卡 30 天", totalCheckin >= 30, (int) Math.min(totalCheckin, 30), 30));
        return rows;
    }

    private HabitTask patch(HabitTask task, Long userId, HabitDtos.TaskRequest req) {
        task.setUserId(userId);
        task.setName(req.name().trim());
        task.setIcon((req.icon() == null || req.icon().isBlank()) ? "sunny" : req.icon());
        task.setDescription(req.description());
        task.setTargetDays(req.targetDays());
        task.setRemindEnabled(req.remindEnabled());
        task.setRemindTime(req.remindEnabled() ? req.remindTime() : null);
        return task;
    }

    private HabitDtos.TaskResponse toTaskResponse(HabitTask task, Long userId) {
        long total = logRepository.countByTaskIdAndUserId(task.getId(), userId);
        long streak = computeStreak(task.getId(), userId);
        boolean doneToday = logRepository.findByTaskIdAndCheckinDate(task.getId(), LocalDate.now()).isPresent();
        int rate = task.getTargetDays() <= 0 ? 0 : (int) Math.min(100, Math.round((total * 100.0) / task.getTargetDays()));
        return new HabitDtos.TaskResponse(
                task.getId(),
                task.getName(),
                task.getIcon(),
                task.getDescription(),
                task.getTargetDays(),
                task.getRemindEnabled(),
                task.getRemindTime(),
                task.getPaused(),
                (int) streak,
                total,
                doneToday,
                rate
        );
    }

    private long computeStreak(Long taskId, Long userId) {
        List<HabitLog> logs = logRepository.findByTaskIdAndUserIdOrderByCheckinDateDesc(taskId, userId);
        if (logs.isEmpty()) return 0;
        LocalDate cursor = LocalDate.now();
        long streak = 0;
        while (true) {
            LocalDate finalCursor = cursor;
            boolean checked = logs.stream().anyMatch(l -> l.getCheckinDate().equals(finalCursor));
            if (!checked) {
                if (streak == 0 && cursor.equals(LocalDate.now())) {
                    cursor = cursor.minusDays(1);
                    continue;
                }
                break;
            }
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private HabitDtos.LogResponse toLogResponse(HabitLog x) {
        return new HabitDtos.LogResponse(x.getId(), x.getCheckinDate(), x.getCheckinTime(), x.getIsMakeup(), x.getNote());
    }

    private HabitMakeupQuota loadQuota(Long userId, YearMonth ym) {
        return makeupQuotaRepository.findByUserIdAndQuotaMonth(userId, ym.format(MONTH_FMT)).orElseGet(() -> {
            HabitMakeupQuota q = new HabitMakeupQuota();
            q.setUserId(userId);
            q.setQuotaMonth(ym.format(MONTH_FMT));
            q.setUsedCount(0);
            q.setCreatedAt(LocalDateTime.now());
            q.setUpdatedAt(LocalDateTime.now());
            return q;
        });
    }
}
