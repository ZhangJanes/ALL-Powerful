ALTER TABLE memos
    ADD COLUMN remind_repeat VARCHAR(16) NULL,
    ADD COLUMN deleted_at DATETIME NULL;
CREATE INDEX idx_memos_user_deleted ON memos(user_id, deleted_at, updated_at DESC);

ALTER TABLE trips
    ADD COLUMN companions VARCHAR(255) NULL,
    ADD COLUMN transport VARCHAR(32) NULL,
    ADD COLUMN remark VARCHAR(255) NULL,
    ADD COLUMN remind_enabled TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN remind_minutes_before INT NULL;

CREATE TABLE ledger_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(16) NOT NULL,
    name VARCHAR(64) NOT NULL,
    icon VARCHAR(64) NULL,
    color VARCHAR(32) NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_ledger_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_ledger_categories_user_type ON ledger_categories(user_id, type, deleted_at);

CREATE TABLE ledger_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(16) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    category_id BIGINT NULL,
    category_name VARCHAR(64) NOT NULL,
    note VARCHAR(255) NULL,
    photo_url VARCHAR(255) NULL,
    occurred_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_ledger_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ledger_entries_category FOREIGN KEY (category_id) REFERENCES ledger_categories(id) ON DELETE SET NULL
);
CREATE INDEX idx_ledger_entries_user_time ON ledger_entries(user_id, occurred_at DESC, deleted_at);

CREATE TABLE ledger_budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    budget_month VARCHAR(7) NOT NULL,
    total_budget DECIMAL(12,2) NOT NULL DEFAULT 0,
    alert_threshold INT NOT NULL DEFAULT 80,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_ledger_budgets_user_month ON ledger_budgets(user_id, budget_month);

CREATE TABLE ledger_budget_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_id BIGINT NOT NULL,
    category_name VARCHAR(64) NOT NULL,
    amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_budget_items_budget FOREIGN KEY (budget_id) REFERENCES ledger_budgets(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_ledger_budget_items_budget_category ON ledger_budget_items(budget_id, category_name);

CREATE TABLE habit_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    icon VARCHAR(32) NOT NULL DEFAULT 'sunny',
    description VARCHAR(255) NULL,
    target_days INT NOT NULL,
    remind_enabled TINYINT(1) NOT NULL DEFAULT 0,
    remind_time TIME NULL,
    paused TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_habit_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_habit_tasks_user_deleted ON habit_tasks(user_id, deleted_at, created_at DESC);

CREATE TABLE habit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    checkin_time DATETIME NOT NULL,
    is_makeup TINYINT(1) NOT NULL DEFAULT 0,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_habit_logs_task FOREIGN KEY (task_id) REFERENCES habit_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_habit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_habit_logs_task_date ON habit_logs(task_id, checkin_date);
CREATE INDEX idx_habit_logs_user_date ON habit_logs(user_id, checkin_date DESC);

CREATE TABLE habit_makeup_quotas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    quota_month VARCHAR(7) NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_habit_makeup_quotas_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_habit_makeup_quotas_user_month ON habit_makeup_quotas(user_id, quota_month);

CREATE TABLE ideas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    body TEXT NULL,
    category VARCHAR(64) NOT NULL,
    tags_json JSON NULL,
    image_urls_json JSON NULL,
    starred TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ideas_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_ideas_user_deleted_updated ON ideas(user_id, deleted_at, updated_at DESC);

CREATE TABLE guide_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    title VARCHAR(64) NOT NULL,
    description VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_guide_categories_code ON guide_categories(code);

CREATE TABLE guide_articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    slug VARCHAR(64) NOT NULL,
    title VARCHAR(128) NOT NULL,
    intro VARCHAR(255) NULL,
    conditions_text TEXT NULL,
    materials_text TEXT NULL,
    process_text TEXT NULL,
    location_text TEXT NULL,
    time_text TEXT NULL,
    period_text TEXT NULL,
    fee_text TEXT NULL,
    tips_text TEXT NULL,
    consult_url VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_guide_articles_category FOREIGN KEY (category_id) REFERENCES guide_categories(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_guide_articles_slug ON guide_articles(slug);
CREATE INDEX idx_guide_articles_category_updated ON guide_articles(category_id, updated_at DESC);

CREATE TABLE guide_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_guide_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_guide_favorites_article FOREIGN KEY (article_id) REFERENCES guide_articles(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_guide_favorites_user_article ON guide_favorites(user_id, article_id);

INSERT INTO guide_categories(code, title, description, sort_order, enabled)
VALUES ('insurance', '医保社保', '报销、转移、查询等', 1, 1),
       ('hospital', '看病就医', '异地就医、挂号、病历', 2, 1),
       ('idcard', '证件办理', '身份证、护照、户口本', 3, 1),
       ('other', '其他办事', '入学、公积金、违章等', 4, 1);
