CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE memos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    content TEXT,
    category VARCHAR(32) NOT NULL,
    pinned TINYINT(1) NOT NULL DEFAULT 0,
    remind_at DATETIME NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_memos_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_memos_user_updated ON memos(user_id, updated_at DESC);
CREATE INDEX idx_memos_user_remind ON memos(user_id, remind_at);

CREATE TABLE memo_todos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    memo_id BIGINT NOT NULL,
    text VARCHAR(255) NOT NULL,
    done TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_memo_todos_memo FOREIGN KEY (memo_id) REFERENCES memos(id) ON DELETE CASCADE
);
CREATE INDEX idx_memo_todos_memo ON memo_todos(memo_id, sort_order);

CREATE TABLE trips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(32) NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    place VARCHAR(255) NOT NULL,
    done TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_trips_user_start ON trips(user_id, start_at);

CREATE TABLE trip_checklists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    text VARCHAR(255) NOT NULL,
    done TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_trip_checklists_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE TABLE photo_libraries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    visibility VARCHAR(16) NOT NULL,
    password_hash VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_photo_libraries_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE UNIQUE INDEX uk_photo_libraries_user_name ON photo_libraries(user_id, name);
CREATE INDEX idx_photo_libraries_user_visibility ON photo_libraries(user_id, visibility);

CREATE TABLE photos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    library_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(32) NOT NULL,
    locked TINYINT(1) NOT NULL DEFAULT 0,
    object_key VARCHAR(255) NULL,
    at_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_photos_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_photos_library FOREIGN KEY (library_id) REFERENCES photo_libraries(id) ON DELETE CASCADE
);
CREATE INDEX idx_photos_library ON photos(library_id, created_at DESC);
CREATE INDEX idx_photos_user_category ON photos(user_id, category);

CREATE TABLE outbox_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME NULL
);
CREATE INDEX idx_outbox_status_created ON outbox_events(status, created_at);
