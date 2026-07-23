CREATE TABLE user_settings (
    user_id BIGINT PRIMARY KEY,
    theme_mode VARCHAR(16) NOT NULL DEFAULT 'dark',
    theme_preset VARCHAR(32) NOT NULL DEFAULT 'ocean',
    font_mode VARCHAR(32) NOT NULL DEFAULT 'default',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
