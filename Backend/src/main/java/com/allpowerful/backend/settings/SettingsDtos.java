package com.allpowerful.backend.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SettingsDtos {
    private SettingsDtos() {}

    public record SettingsResponse(
            String themeMode,
            String themePreset,
            String fontMode,
            String weatherCity
    ) {}

    public record SettingsUpdateRequest(
            @NotBlank
            @Pattern(regexp = "light|dark", message = "themeMode 仅支持 light 或 dark")
            String themeMode,
            @NotBlank
            @Size(max = 32)
            String themePreset,
            @NotBlank
            @Size(max = 32)
            String fontMode,
            @NotBlank
            @Pattern(regexp = "北京|上海|重庆|天津", message = "weatherCity 仅支持 北京/上海/重庆/天津")
            String weatherCity
    ) {}
}
