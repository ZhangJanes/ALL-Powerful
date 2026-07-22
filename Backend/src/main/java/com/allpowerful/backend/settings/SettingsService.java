package com.allpowerful.backend.settings;

import com.allpowerful.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private static final Set<String> THEME_PRESETS = Set.of(
            "ocean", "aurora", "sunset", "graphite", "blackgold", "chinarred", "applemono", "sketch", "antdraw"
    );
    private static final Set<String> FONT_MODES = Set.of("default", "alimama", "cute", "bobo", "maiyuan");

    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public SettingsDtos.SettingsResponse get(Long userId) {
        return toResponse(getOrCreate(userId));
    }

    @Transactional
    public SettingsDtos.SettingsResponse update(Long userId, SettingsDtos.SettingsUpdateRequest req) {
        validatePreset(req.themePreset());
        validateFont(req.fontMode());
        UserSettings settings = getOrCreate(userId);
        settings.setThemeMode(req.themeMode());
        settings.setThemePreset(req.themePreset());
        settings.setFontMode(req.fontMode());
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsRepository.save(settings);
        return toResponse(settings);
    }

    private UserSettings getOrCreate(Long userId) {
        return userSettingsRepository.findById(userId).orElseGet(() -> {
            UserSettings settings = new UserSettings();
            settings.setUserId(userId);
            settings.setThemeMode("dark");
            settings.setThemePreset("ocean");
            settings.setFontMode("default");
            settings.setUpdatedAt(LocalDateTime.now());
            return userSettingsRepository.save(settings);
        });
    }

    private void validatePreset(String themePreset) {
        if (!THEME_PRESETS.contains(themePreset)) {
            throw new AppException("不支持的主题预设: " + themePreset);
        }
    }

    private void validateFont(String fontMode) {
        if (!FONT_MODES.contains(fontMode)) {
            throw new AppException("不支持的字体模式: " + fontMode);
        }
    }

    private SettingsDtos.SettingsResponse toResponse(UserSettings settings) {
        return new SettingsDtos.SettingsResponse(
                settings.getThemeMode(),
                settings.getThemePreset(),
                settings.getFontMode()
        );
    }
}
