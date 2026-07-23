# CLAUDE.md

> **权威完整版**：[`zDocs/CLAUDE.md`](zDocs/CLAUDE.md)  
> Cursor 规则：[`.cursor/rules/`](.cursor/rules/)（会话自动注入）

本仓库是 **ALL-Powerful（家庭小管家）**：`Frontend/`（Vue 3 + Vite + Naive UI + Pinia）+ `Backend/`（Spring Boot 3.3 + JPA/MyBatis + JWT + Flyway + MySQL/Redis/Kafka）。

## 四条铁律

1. 先想后写，不懂就问别瞎猜  
2. 能 50 行绝不写 200 行  
3. 只动该动的地方，其他别碰  
4. 目标是过测试，不是秀操作  

编码前先确认范围与方案；改接口时同一轮同步前后端契约。

## 快速命令

```bash
# 前端
cd Frontend && pnpm install && pnpm dev

# 基础设施 + 后端
cd Backend && docker compose up -d && mvn spring-boot:run
```

- 前端：`http://localhost:5173`  
- API：`http://127.0.0.1:8080/api`（`VITE_API_BASE_URL` 可覆盖）  
- 路径别名：`@` → `Frontend/src/`  
- 统一响应：`{ success, data, message }`；Token：`fm.auth.token`

## 模块数据现状

- **已接后端**：auth、settings、memo、travel、photo、message  
- **仍偏前端 mock**：ledger、habits、ideas；指南为 `src/data/guide.ts`

细节、目录约定、安全白名单、Flyway 规则见 `zDocs/CLAUDE.md`。
