package com.allpowerful.backend.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SettingsDtos {
    private SettingsDtos() {}

    public record SettingsResponse(
            String themeMode,
            String themePreset,
            String fontMode
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
            String fontMode
    ) {}
}
