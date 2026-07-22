package com.allpowerful.backend.settings;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @GetMapping
    public ApiResponse<SettingsDtos.SettingsResponse> get() {
        return ApiResponse.ok(settingsService.get(CurrentUser.id()));
    }

    @PutMapping
    public ApiResponse<SettingsDtos.SettingsResponse> update(@Valid @RequestBody SettingsDtos.SettingsUpdateRequest req) {
        return ApiResponse.ok(settingsService.update(CurrentUser.id(), req));
    }
}
