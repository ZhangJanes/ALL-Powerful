package com.allpowerful.backend.habit;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {
    private final HabitService habitService;

    @GetMapping("/tasks")
    public ApiResponse<List<HabitDtos.TaskResponse>> list() {
        return ApiResponse.ok(habitService.list(CurrentUser.id()));
    }

    @PostMapping("/tasks")
    public ApiResponse<HabitDtos.TaskResponse> create(@Valid @RequestBody HabitDtos.TaskRequest req) {
        return ApiResponse.ok(habitService.create(CurrentUser.id(), req));
    }

    @PutMapping("/tasks/{id}")
    public ApiResponse<HabitDtos.TaskResponse> update(@PathVariable Long id, @Valid @RequestBody HabitDtos.TaskRequest req) {
        return ApiResponse.ok(habitService.update(CurrentUser.id(), id, req));
    }

    @PutMapping("/tasks/{id}/pause")
    public ApiResponse<HabitDtos.TaskResponse> togglePause(@PathVariable Long id) {
        return ApiResponse.ok(habitService.togglePause(CurrentUser.id(), id));
    }

    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        habitService.delete(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }

    @PostMapping("/tasks/{id}/check-in")
    public ApiResponse<HabitDtos.TaskResponse> checkin(@PathVariable Long id, @RequestBody(required = false) HabitDtos.CheckinRequest req) {
        return ApiResponse.ok(habitService.checkin(CurrentUser.id(), id, req));
    }

    @PostMapping("/tasks/{id}/make-up")
    public ApiResponse<HabitDtos.TaskResponse> makeup(@PathVariable Long id, @Valid @RequestBody HabitDtos.MakeupRequest req) {
        return ApiResponse.ok(habitService.makeup(CurrentUser.id(), id, req));
    }

    @GetMapping("/tasks/{id}")
    public ApiResponse<HabitDtos.DetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(habitService.detail(CurrentUser.id(), id));
    }

    @GetMapping("/achievements")
    public ApiResponse<List<HabitDtos.AchievementResponse>> achievements() {
        return ApiResponse.ok(habitService.achievements(CurrentUser.id()));
    }
}
