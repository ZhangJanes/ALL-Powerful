# CLAUDE.md

本文件为 AI / 开发者在本仓库改代码时的项目级指引。权威副本在 `zDocs/CLAUDE.md`；仓库根目录 `CLAUDE.md` 与 `.cursor/rules/` 会同步引用。

## 项目一句话

**ALL-Powerful（家庭小管家）**：家庭场景管理应用。前端 `Frontend/`（Vue 3 SPA），后端 `Backend/`（Spring Boot 单体），基础设施 MySQL / Redis / Kafka（Docker Compose）。

## 工作纪律 · 四条铁律（任何改动都遵守）

1. **先想后写，不懂就问别瞎猜**：范围、方案、表结构 / API 契约不清楚，先澄清确认再动手。
2. **能 50 行绝不写 200 行**：用最小必要实现，不堆冗余代码、不加用不到的抽象。
3. **只动该动的地方，其他别碰**：严格限定改动面，不顺手重构 / 格式化无关代码。
4. **目标是过测试，不是秀操作**：以通过测试、达成需求为准，不炫技、不过度设计。

## 工作方式（本仓库所有改动遵守）

1. **不要过度设计**：按当前需求做最小必要实现；不预留用不到的扩展点、不为假想场景加抽象层。
2. **编码前先确认范围与方案**：动手改代码前，先澄清改造范围、给出改造方案，与用户确认后再编码。
3. **前后端契约一致**：改接口时同一轮任务内同步 DTO、Flyway（如需）、前端 `api/` 与 `stores/`；暂不实现的部分在交接中说明。
4. **模块按域落地**：后端按业务包、前端按 `api/` + `stores/` + `views/` 同名域扩展，不要平铺到杂项目录。

---

## 技术栈与环境

### 前端（`Frontend/`）

| 项 | 说明 |
| --- | --- |
| 框架 | Vue 3.5 + TypeScript ~5.6 + Vite 5 |
| UI | Naive UI + `@vicons/ionicons5` + `vfonts` |
| 状态 / 路由 | Pinia + Vue Router 4（**history 模式**） |
| HTTP | Axios（封装在 `src/api/request.ts`） |
| 图表 | ECharts 6 + vue-echarts |
| 工程化 | `unplugin-auto-import`、`unplugin-vue-components`（NaiveUiResolver） |
| 包管理 | **pnpm@9.15.9**（见 `packageManager`） |
| Node | 建议 ≥ 18 |

### 后端（`Backend/`）

| 项 | 说明 |
| --- | --- |
| 运行时 | Java 17 + Spring Boot **3.3.5** |
| Web / 安全 | spring-boot-starter-web + Security + **JWT（jjwt 0.12）** |
| 持久化 | Spring Data JPA + **MyBatis**（复杂查询，如消息聚合）+ **Flyway** |
| 中间件 | MySQL 8.4、Redis 7.4、Kafka（本地默认 **关闭**，`app.kafka.enabled=false`） |
| 其他 | Validation、Actuator、Lombok、BCrypt |

### 基础设施端口

| 服务 | 端口 |
| --- | --- |
| 前端 Vite | `5173`（preview `4173`） |
| 后端 API | `8080`，前缀 `/api` |
| MySQL | `3306`，库名 `all_powerful` |
| Redis | `6379` |
| Kafka / ZK | `9092` / `2181` |

---

## 常用脚本

### 前端

```bash
cd Frontend
pnpm install
pnpm dev          # http://localhost:5173 ，host: true 可局域网访问
pnpm build        # vue-tsc --noEmit && vite build → dist/
pnpm preview
```

### 后端

```bash
cd Backend
docker compose up -d          # MySQL / Redis /（可选）Kafka
cp .env.example .env          # 首次；本地默认一般可直接用
mvn spring-boot:run           # http://127.0.0.1:8080/api
# 健康检查：http://127.0.0.1:8080/actuator/health
```

前端 API 基址：`VITE_API_BASE_URL`（默认 `http://127.0.0.1:8080/api`），可写在 `Frontend/.env.local`。

---

## 仓库结构（改代码前先认路）

```text
ALL-Powerful/
├── Frontend/
│   ├── src/
│   │   ├── api/           # 按业务域：auth / memo / travel / photo / message / settings
│   │   ├── api/request.ts # 统一 Axios + ApiResult 解包 + JWT
│   │   ├── stores/        # 按业务域 Pinia（auth / memo / travel / …）
│   │   ├── views/         # 页面（memo / ledger / habits / travel / …）
│   │   ├── layouts/       # MainLayout（侧栏 + 内容区）
│   │   ├── components/    # 复用组件（图表、空状态插画等）
│   │   ├── composables/
│   │   ├── theme/         # Naive / ECharts 主题与预设
│   │   ├── data/          # 纯前端静态数据（如 guide）
│   │   ├── router/        # 路由 + beforeEach 鉴权
│   │   └── utils/
│   └── vite.config.ts     # 别名 @ → src/
├── Backend/
│   ├── src/main/java/com/allpowerful/backend/
│   │   ├── auth/          # 登录注册 / JWT / User
│   │   ├── memo/ travel/ photo/ message/ settings/
│   │   ├── common/        # ApiResponse / AppException / CurrentUser / …
│   │   └── config/        # Security / CORS / Kafka
│   ├── src/main/resources/
│   │   ├── application.yml / application-dev.yml
│   │   ├── db/migration/  # Flyway：V1__…、V2__…
│   │   └── mapper/        # MyBatis XML
│   ├── docker-compose.yml
│   └── pom.xml
└── zDocs/                 # 文档（本文件、前后端 README、全栈手册）
```

---

## 前端约定（改 UI / 联调必读）

### 路径与自动导入

- 路径别名：**`@` → `src/`**（见 `vite.config.ts`）。import 写 `@/...`。
- `vue` / `vue-router` / `pinia` API 可由 auto-import 注入（类型在 `src/auto-imports.d.ts`）。
- Naive UI 组件按需自动注册（`src/components.d.ts`）。

### 请求层（`src/api/request.ts`）

- 统一响应：`{ success, data, message }`；`success !== true` 时 reject。
- 默认带 `Authorization: Bearer <token>`；登录等接口设 `withAuth: false`。
- Token 键：`fm.auth.token`（localStorage / sessionStorage）。
- 业务 API 放在 `src/api/<域>/index.ts`，页面 / store **不要**直接 new Axios。

### 路由与鉴权

- 入口：`src/router/index.ts`，`createWebHistory`。
- `meta.public: true` 仅登录 / 注册；其余需 `useAuthStore().isAuthed`。
- 未登录跳转登录并带 `redirect`；已登录访问公开页回首页。
- 新增业务页：同步改 **路由**、`MainLayout` 侧栏（如需要）、对应 `views/`。

### Store 与数据现状

| 模块 | 数据来源（当前） |
| --- | --- |
| 鉴权 / 用户设置 | 后端 API |
| 备忘录 / 出行 / 照片 / 消息 | 后端 API |
| 记账 / 习惯 / 灵感 | 前端 store 本地 / mock（尚未全部接后端） |
| 办事指南 | `src/data/guide.ts` 静态数据 |

新增已接后端的模块：优先 `api/` → `stores/<域>/` → `views/`，保持与后端路径一致。

---

## 后端约定（改接口 / 表结构必读）

### 包与分层

每个业务域一个包（例：`memo`），通常包含：

- `*Controller`：`@RequestMapping("/api/...")`，返回 `ApiResponse<T>`
- `*Service`：业务逻辑
- Entity / Repository（JPA）；复杂查询用 MyBatis Mapper + `resources/mapper/**/*.xml`
- `*Dtos`：请求 / 响应记录类型

公共能力在 `common/`：`ApiResponse`、`AppException`、`GlobalExceptionHandler`、`CurrentUser`、`DomainEventPublisher`。

### 安全

- 白名单：`/api/auth/login`、`/api/auth/register`、`/actuator/health`。
- 其余需 JWT；过滤器解析 `Authorization: Bearer …`。
- 密码 BCrypt；JWT 密钥 `APP_JWT_SECRET`（生产必须更换）。

### 数据库

- **Flyway 管 schema**；`jpa.hibernate.ddl-auto=validate`，禁止靠 Hibernate 自动改表。
- 表结构变更：新增 `V{n}__xxx.sql`，不要改已发布的旧脚本。
- 已有迁移：`V1__init_schema.sql`、`V2__user_settings.sql`。

### 统一响应与错误

```java
ApiResponse.ok(data);
ApiResponse.fail(message);
```

业务异常走 `AppException` + 全局处理器，保持与前端 `ApiResult` 字段一致。

### Kafka

- 本地默认关闭（`application.yml` + `application-dev.yml` 排除 Kafka 自动配置）。
- 需要异步事件时：起 Docker 中的 Kafka，并设 `app.kafka.enabled=true`，沿用 `DomainEventPublisher` / `KafkaTopics`，不要另起一套消息通道。

---

## 已提供的核心 API（一期）

| 域 | 路径前缀 | 要点 |
| --- | --- | --- |
| 鉴权 | `/api/auth` | `login` / `register` / `me` |
| 备忘录 | `/api/memos` | CRUD |
| 出行 | `/api/trips` | CRUD |
| 照片 | `/api/photos` | libraries / verify / public / upload |
| 消息 | `/api/messages` | 聚合列表 |
| 设置 | `/api/settings` | GET / PUT |

详细字段与联调步骤见 `zDocs/README-Backend.md`、`zDocs/全栈开发与部署手册.md`。

---

## 开发边界

- 只改当前任务需要的前后端代码；避免无关重构、全文件格式化、扩大依赖面。
- 记账 / 习惯 / 灵感若未接后端，不要假装已有 API；接后端时按现有 `memo` 域模式复制最小闭环。
- 不要提交密钥、真实生产 `.env`、或把 `Backend/target/` 当源码改。
- UI 改动优先贴合现有 Naive UI + 主题体系（`theme/`、`styles/`），不要另起一套组件库。

---

## 重要参考

| 文档 | 用途 |
| --- | --- |
| `zDocs/README-Frontend.md` | 前端模块与启动 |
| `zDocs/README-Backend.md` | 后端启动、接口、排障 |
| `zDocs/全栈开发与部署手册.md` | 从零环境、新建接口示例、部署 |
| `Backend/.env.example` | 环境变量模板 |
| `Backend/docker-compose.yml` | 本地中间件 |
