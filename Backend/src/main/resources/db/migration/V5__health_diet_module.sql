INSERT INTO users(username, password_hash, display_name)
SELECT 'zhangjianing',
       '$2b$10$xeZfR7pZw8FD6uxxKkA25ekcubDPrSfJ1ccTayv6G3CY8pSOQifF6',
       '张佳宁'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'zhangjianing');

CREATE TABLE health_profiles (
    user_id BIGINT PRIMARY KEY,
    gender VARCHAR(16) NULL,
    birth_date DATE NULL,
    height_cm DECIMAL(5,2) NULL,
    current_weight_kg DECIMAL(5,2) NULL,
    target_weight_kg DECIMAL(5,2) NULL,
    body_fat_percent DECIMAL(5,2) NULL,
    target_body_fat_percent DECIMAL(5,2) NULL,
    activity_level VARCHAR(24) NOT NULL DEFAULT 'sedentary',
    target_calories INT NULL,
    water_target_ml INT NOT NULL DEFAULT 2000,
    wake_time TIME NULL,
    sleep_time TIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE health_diet_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    source_type VARCHAR(24) NOT NULL DEFAULT 'medical_image',
    next_review_date DATE NULL,
    followup_plan TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_diet_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_health_diet_plans_user_title ON health_diet_plans(user_id, title);

CREATE TABLE health_plan_meal_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    meal_type VARCHAR(24) NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    portion_text VARCHAR(160) NOT NULL,
    detail_text TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_health_plan_meal_items_plan FOREIGN KEY (plan_id) REFERENCES health_diet_plans(id) ON DELETE CASCADE
);
CREATE INDEX idx_health_plan_meal_items_plan_type ON health_plan_meal_items(plan_id, meal_type, sort_order);

CREATE TABLE health_plan_guidelines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    category VARCHAR(24) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_health_plan_guidelines_plan FOREIGN KEY (plan_id) REFERENCES health_diet_plans(id) ON DELETE CASCADE
);
CREATE INDEX idx_health_plan_guidelines_plan_category ON health_plan_guidelines(plan_id, category, sort_order);

CREATE TABLE health_goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goal_type VARCHAR(32) NOT NULL,
    title VARCHAR(120) NOT NULL,
    target_value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(24) NOT NULL,
    period_type VARCHAR(16) NOT NULL DEFAULT 'daily',
    comparison_operator VARCHAR(8) NOT NULL DEFAULT 'GTE',
    start_date DATE NOT NULL,
    deadline DATE NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_health_goals_user_status ON health_goals(user_id, status, deadline);

CREATE TABLE health_daily_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    weight_kg DECIMAL(5,2) NULL,
    body_fat_percent DECIMAL(5,2) NULL,
    calorie_intake INT NULL,
    wake_time TIME NULL,
    sleep_time TIME NULL,
    sleep_minutes INT NULL,
    mood VARCHAR(24) NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_daily_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_health_daily_records_user_date ON health_daily_records(user_id, record_date);

CREATE TABLE health_meal_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    meal_type VARCHAR(24) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'completed',
    note VARCHAR(255) NULL,
    checked_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_meal_checkins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_health_meal_checkins_user_date_type ON health_meal_checkins(user_id, checkin_date, meal_type);

CREATE TABLE health_water_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    amount_ml INT NOT NULL,
    recorded_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_water_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_health_water_logs_user_date ON health_water_logs(user_id, log_date, recorded_at);

CREATE TABLE health_exercise_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    exercise_type VARCHAR(24) NOT NULL,
    name VARCHAR(80) NOT NULL,
    duration_minutes INT NOT NULL,
    heart_rate INT NULL,
    note VARCHAR(255) NULL,
    recorded_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_exercise_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_health_exercise_logs_user_date ON health_exercise_logs(user_id, log_date, recorded_at);

INSERT INTO health_profiles(user_id, water_target_ml)
SELECT id, 2000 FROM users WHERE username = 'zhangjianing';

INSERT INTO health_diet_plans(user_id, title, status, source_type, next_review_date, followup_plan)
SELECT id,
       '医学营养减重方案',
       'active',
       'medical_image',
       '2026-09-30',
       '请配合完成每周随访登记，以便让我们及时了解并帮助您更好地执行医学减重方案。'
FROM users WHERE username = 'zhangjianing';

INSERT INTO health_plan_meal_items(plan_id, meal_type, item_name, portion_text, detail_text, sort_order)
SELECT p.id, x.meal_type, x.item_name, x.portion_text, x.detail_text, x.sort_order
FROM health_diet_plans p
JOIN users u ON u.id = p.user_id AND u.username = 'zhangjianing'
JOIN (
    SELECT 'BREAKFAST' meal_type, '主食（粗细搭配）' item_name, '25 克' portion_text, NULL detail_text, 1 sort_order
    UNION ALL SELECT 'BREAKFAST', '鸡蛋', '1 个', NULL, 2
    UNION ALL SELECT 'BREAKFAST', '低脂牛奶/豆浆', '乳清蛋白20g+150 ml', NULL, 3
    UNION ALL SELECT 'BREAKFAST', '多种营养素制剂', '早晚各一粒', NULL, 4
    UNION ALL SELECT 'BREAKFAST', '鱼油胶囊（1克）', '早晚各一粒', NULL, 5
    UNION ALL SELECT 'LUNCH', '主食（粗细搭配）', '75 克', NULL, 1
    UNION ALL SELECT 'LUNCH', '蛋白质类食物', '50 克', NULL, 2
    UNION ALL SELECT 'LUNCH', '蔬菜（合计）', '250 克', NULL, 3
    UNION ALL SELECT 'LUNCH', '新鲜叶类蔬菜', '200 克', NULL, 4
    UNION ALL SELECT 'LUNCH', '新鲜菇类', '50 克', NULL, 5
    UNION ALL SELECT 'SNACK', '水果', '200 克', NULL, 1
    UNION ALL SELECT 'DINNER', '主食（粗细搭配）', '50 克', NULL, 1
    UNION ALL SELECT 'DINNER', '蛋白质类食物', '25 克', NULL, 2
    UNION ALL SELECT 'DINNER', '蔬菜（合计）', '250 克', NULL, 3
    UNION ALL SELECT 'DINNER', '新鲜叶类蔬菜', '200 克', NULL, 4
    UNION ALL SELECT 'DINNER', '新鲜菇类', '50 克', NULL, 5
    UNION ALL SELECT 'WATER', '全天饮水量', '>2000 ml',
      '备乳清蛋白粉20克，蛋白粉放入牛奶或豆浆，冲开喝。分离乳清蛋白：80%以上纯度。鱼油 Omega-3纯度不低于80%，选择TGN型，EPA:DHA配比2:1。', 1
) x
WHERE p.title = '医学营养减重方案';

INSERT INTO health_plan_guidelines(plan_id, category, title, content, sort_order)
SELECT p.id, x.category, x.title, x.content, x.sort_order
FROM health_diet_plans p
JOIN users u ON u.id = p.user_id AND u.username = 'zhangjianing'
JOIN (
    SELECT 'EXECUTION' category, '1. 主食计量' title,
      '主食、蛋白质食物、蔬菜的量都是指食材生重。100克主食 = 100克大米/白面/杂面/小米 = 100克挂面 = 100克梳打饼干 = 240克米饭 = 120克馒头/面包 = 5 × 100g 土豆/红薯/山药；每天主食一半或者1/3是粗粮。粗粮主食的做法：粗粮饭（小米饭、高粱米饭、荞麦米饭等）；二米饭；杂面煎饼；杂面条；杂面馒头/发糕；窝头。' content, 1 sort_order
    UNION ALL SELECT 'EXECUTION', '2. 蛋白质食物',
      '蛋白质食物：【红肉】猪牛羊驴兔等；【白肉】鸡鸭鱼虾等，豆腐，豆干。100克蛋白质食物 = 100克纯瘦肉 = 2 × 100克豆腐 = 2个鸡蛋 = 2 × 250毫升牛奶。午餐可以吃红肉，晚餐尽量吃白肉。', 2
    UNION ALL SELECT 'EXECUTION', '3. 新鲜蔬菜',
      '新鲜蔬菜：每天总量1斤（500克以上）。叶类菜：菠菜、芹菜、韭菜、茼蒿、生菜、木耳菜、苋菜等。菇类：木耳、金针菇、香菇、杏鲍菇、海鲜菇、牛肝菌、平菇、茶树菇等。其它：黄瓜、西红柿、西兰花、花菜、青椒、洋葱、冬瓜等。吃土豆/红薯/山药需要抵扣主食，100–150g相当于25g普通主食。', 3
    UNION ALL SELECT 'EXECUTION', '4. 水果',
      '水果：优先选择苹果、梨、杏、桃、樱桃、草莓等低糖水果。芒果、榴莲、火龙果等热带水果能量比较高。西瓜适量，1斤西瓜约等于半碗饭。', 4
    UNION ALL SELECT 'EXECUTION', '5. 少吃/不吃',
      '少吃/不吃：各种加工食品，香肠、腊肉、薯片、方便面，各种含糖饮料。肉汤、肉皮、排骨、干锅沙拉酱、奶香沙拉酱、火锅麻酱料。坚果类，如花生、瓜子、开心果、腰果。', 5
    UNION ALL SELECT 'EXECUTION', '6. 烹调方式',
      '烹调方式：生吃、拌菜、蒸、煮、涮为主；煎炒点缀，避免油炸。尽量在家吃饭。生吃/果蔬汁：生菜、青椒、彩椒、洋葱、西芹菜心、苦菊、黄瓜、西红柿、紫甘蓝等焯一拌；菠菜、苦瓜、芹菜、菇类、小白菜等。白灼：白灼芥蓝、白灼菜心等。上汤：上汤鸡毛菜、上汤苋菜等。', 6
    UNION ALL SELECT 'EXERCISE', '有氧运动',
      '建议有氧运动：【快走】【健身车】【健身操】【慢走】每周运动5~7次，每次30~60分钟，心率达到130次/分钟。', 1
    UNION ALL SELECT 'EXERCISE', '阻抗运动',
      '建议阻抗运动：【自重训练（如俯卧撑、平板支撑、引体向上、卷腹、臀桥等）】【哑铃】【固定器械】【弹力带】【杠铃】【其他--】每周运动5~7次，每次20~60分钟，心率达到100次/分钟。', 2
) x
WHERE p.title = '医学营养减重方案';

INSERT INTO health_goals(user_id, goal_type, title, target_value, unit, period_type, comparison_operator, start_date)
SELECT id, 'MEAL_ADHERENCE', '每日四餐按计划完成', 4, '餐', 'daily', 'GTE', CURRENT_DATE FROM users WHERE username = 'zhangjianing'
UNION ALL
SELECT id, 'WATER', '全天饮水量', 2000, 'ml', 'daily', 'GT', CURRENT_DATE FROM users WHERE username = 'zhangjianing'
UNION ALL
SELECT id, 'AEROBIC', '每周有氧运动', 5, '次', 'weekly', 'GTE', CURRENT_DATE FROM users WHERE username = 'zhangjianing'
UNION ALL
SELECT id, 'RESISTANCE', '每周阻抗运动', 5, '次', 'weekly', 'GTE', CURRENT_DATE FROM users WHERE username = 'zhangjianing';
