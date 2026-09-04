# ALL-Powerful Backend 接入说明

本文件用于指导你在本地启动并接入 `Backend` 后端服务，包括：

- Java 后端运行环境准备
- MySQL / Redis / Kafka 基础设施启动
- 数据库迁移（Flyway）
- Spring Boot 服务启动
- 前端联调配置
- 常见问题排查

---

## 1. 当前后端技术栈

后端位于：`Backend`

核心技术：

- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- MyBatis（消息聚合等复杂查询）
- MySQL
- Redis
- Kafka
- Flyway（数据库版本迁移）

---

## 2. 环境要求

请先确保以下工具可用：

- JDK 21
- Maven 3.9+
- Docker + Docker Compose
- Node.js + pnpm（仅前端联调时需要）

可用下面命令自检：

```bash
java -version
mvn -version
docker -v
docker compose version
```

---

## 3. 启动基础设施（MySQL/Redis/Kafka）

在项目根目录执行：

```bash
cd Backend
docker compose up -d
```

默认端口：

- MySQL: `3306`
- Redis: `6379`
- Kafka: `9092`
- Zookeeper: `2181`

查看运行状态：

```bash
docker compose ps
```

停止：

```bash
docker compose down
```

---

## 4. 配置后端环境变量

后端提供模板文件：`Backend/.env.example`

你可以复制一份并按需修改（本地默认一般可直接用）：

```bash
cd Backend
cp .env.example .env
```

关键变量说明：

- `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD`：MySQL 连接信息
- `REDIS_HOST` / `REDIS_PORT`：Redis 连接
- `KAFKA_BOOTSTRAP_SERVERS`：Kafka 连接地址
- `APP_JWT_SECRET`：JWT 密钥（生产环境必须改成强密钥）

---

## 5. 数据库迁移（Flyway）

后端启动时会自动执行 `Backend/src/main/resources/db/migration` 下的 SQL 迁移脚本。

当前首版迁移文件：

- `V1__init_schema.sql`

包含核心表：

- `users`
- `memos` / `memo_todos`
- `trips` / `trip_checklists`
- `photo_libraries` / `photos`
- `outbox_events`

你通常不需要手动执行 SQL，只需保证数据库可连通并启动应用。

---

## 6. 启动后端服务

```bash
cd Backend
mvn spring-boot:run
```

启动成功后：

- API 基础地址：`http://127.0.0.1:8080/api`
- 健康检查：`http://127.0.0.1:8080/actuator/health`

---

## 7. 前端联调配置

前端 API 客户端文件：

- `Frontend/src/api/client.ts`

默认会请求：

- `http://127.0.0.1:8080/api`

你也可以用环境变量覆盖，创建 `Frontend/.env.local`：

```bash
VITE_API_BASE_URL=http://127.0.0.1:8080/api
```

然后启动前端：

```bash
cd Frontend
pnpm install
pnpm dev
```

---

## 8. 已提供的核心接口（一期）

### 8.1 鉴权

- `POST /api/auth/login`
- `GET /api/auth/me`

说明：

- 登录成功返回 token
- 受保护接口需携带 `Authorization: Bearer <token>`

### 8.2 备忘录

- `GET /api/memos`
- `POST /api/memos`
- `PUT /api/memos/{id}`
- `DELETE /api/memos/{id}`

### 8.3 出行计划

- `GET /api/trips`
- `POST /api/trips`
- `PUT /api/trips/{id}`
- `DELETE /api/trips/{id}`

### 8.4 照片库

- `GET /api/photos/libraries`
- `POST /api/photos/libraries`
- `POST /api/photos/libraries/{libraryId}/verify`
- `GET /api/photos/libraries/{libraryId}`
- `GET /api/photos/public`
- `POST /api/photos/upload`

### 8.5 消息聚合

- `GET /api/messages`

---

## 9. 常见问题与排查

### 9.1 `mvn: command not found`

原因：本机未安装 Maven。  
处理：安装 Maven 后重新执行 `mvn -version` 验证。

### 9.2 后端启动失败，提示数据库连接错误

排查步骤：

1. `docker compose ps` 确认 mysql 已运行
2. 检查 `.env` 中 DB 参数是否正确
3. 确认 `3306` 端口未被其他进程占用

### 9.3 Kafka 连接失败

排查步骤：

1. `docker compose ps` 确认 zookeeper + kafka 都已运行
2. 检查 `KAFKA_BOOTSTRAP_SERVERS` 是否为 `127.0.0.1:9092`

### 9.4 前端接口 401 未认证

原因：请求未携带 token 或 token 过期。  
处理：重新登录，确认本地 `fm.auth.token` 存在并随请求发送。

### 9.5 Flyway 迁移报错

排查步骤：

1. 查看控制台第一条 SQL 报错位置
2. 确认数据库是空库或与脚本版本一致
3. 若本地重置环境，可删除 mysql volume 后重启：

```bash
docker compose down -v
docker compose up -d
```

---

## 10. 推荐开发顺序（后续）

如果你继续扩展后端，建议按以下顺序推进：

1. 完善 DTO 校验与统一错误码
2. 私有库密码改为更严格策略（哈希与更新策略）
3. 文件上传落对象存储（MinIO / OSS / S3），`objectKey` 存库
4. Redis 缓存与失效策略细化
5. Kafka 消费逻辑从日志升级为真实异步任务
6. 增加集成测试与接口文档（OpenAPI/Swagger）

