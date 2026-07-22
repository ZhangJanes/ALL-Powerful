package com.allpowerful.backend.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "theme_mode", nullable = false, length = 16)
    private String themeMode;

    @Column(name = "theme_preset", nullable = false, length = 32)
    private String themePreset;

    @Column(name = "font_mode", nullable = false, length = 32)
    private String fontMode;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
