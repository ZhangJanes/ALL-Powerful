ALTER TABLE user_settings
    ADD COLUMN weather_city VARCHAR(16) NOT NULL DEFAULT '北京' AFTER font_mode;
