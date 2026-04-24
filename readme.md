# 家庭小管家（ALL-Powerful）

一个面向家庭场景的前端管理工作台示例项目，覆盖了日常记录与轻量管理的多个高频能力：备忘录、记账、习惯打卡、出行计划、灵感记录、照片资料管理、办事指南等。

当前仓库主要包含 `Frontend` 前端工程（Vue 3 + Vite + TypeScript），用于演示完整的中后台式家庭管理 UI 交互流程。

---

## 项目亮点

- 多模块一体化：首页聚合统计 + 各业务模块独立页面
- 现代前端技术栈：Vue 3 / Vite / TypeScript / Pinia / Vue Router
- 桌面端工作台体验：可折叠侧边栏、分区导航、快捷入口
- 可视化能力：ECharts 图表用于支出趋势与预算占比展示
- 主题与样式扩展：支持主题模式、配色预设、字体模式（本地存储）
- 演示友好：无需后端即可运行，方便开发和 UI 联调

---

## 功能模块

根据当前路由与页面实现，主要功能包含：

- 首页（`/`）
  - 今日待办、今日支出、预算剩余、习惯打卡进度
  - 近 7 日支出趋势图 + 本月预算环图
  - 快捷导航与最近动态
- 登录（`/login`）
  - 演示登录（任意账号密码可登录）
  - 本地 token 存储，支持退出登录
- 备忘录（`/memo`）
  - 备忘清单、详情编辑、新建
  - 支持普通文本与待办子项（todos）
- 记账（`/ledger`）
  - 收支记录、预算设置、统计视图
- 习惯打卡（`/habits`）
  - 习惯列表、创建、详情、打卡状态
- 出行计划（`/travel`）
  - 行程列表、新建、详情与清单
- 灵感记录（`/ideas`）
  - 想法列表、详情、编辑、新建
- 照片库（`/photos`）
  - 资料类照片管理（含“是否加锁”状态）
- 办事指南（`/guide`）
  - 分类列表与详情页
- 其他页面
  - 消息（`/messages`）、个人中心（`/profile`）、更多（`/more`）

---

## 技术栈

`Frontend/package.json` 中当前依赖如下（核心）：

- 框架：`vue@3`
- 工程化：`vite@5`、`typescript`
- 状态管理：`pinia`
- 路由：`vue-router@4`
- UI 组件：`naive-ui`、`@vicons/ionicons5`
- 图表：`echarts`、`vue-echarts`
- 字体：`vfonts`
- 按需与自动导入：`unplugin-auto-import`、`unplugin-vue-components`

包管理器声明为：`pnpm@9.15.9`。

---

## 快速开始

### 1）环境要求

- Node.js：建议 `>= 18`
- pnpm：建议 `>= 9`

### 2）安装依赖

```bash
cd Frontend
pnpm install
```

### 3）启动开发环境

```bash
pnpm dev
```

- 默认开发地址：`http://localhost:5173`
- `vite.config.ts` 已开启 `host: true`，可通过局域网 IP 在手机/平板访问

### 4）打包构建

```bash
pnpm build
```

输出目录为：`Frontend/dist`。

### 5）本地预览构建结果

```bash
pnpm preview
```

默认预览端口：`4173`。

---

## 项目结构（当前）

```text
ALL-Powerful/
├── Frontend/
│   ├── src/
│   │   ├── components/      # 复用组件（图表、空状态、插画等）
│   │   ├── layouts/         # 主布局（侧边栏、内容区）
│   │   ├── router/          # 路由定义
│   │   ├── stores/          # Pinia 状态（业务数据、鉴权、主题设置）
│   │   ├── theme/           # 主题配置与预设
│   │   ├── utils/           # 工具函数（图表配置、习惯热力图等）
│   │   ├── views/           # 各业务页面
│   │   └── main.ts          # 应用入口
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
└── readme.md
```

---

## 数据与状态说明

当前项目以“前端演示态”为主，业务数据主要在 `src/stores/app.ts` 内通过内置示例数据维护，包括：

- 备忘录、记账、习惯、出行、灵感、照片、活动流
- 首页统计（今日支出、待办进度、预算剩余等）通过 `computed` 计算
- 新增/更新操作会同步更新活动流，形成轻量操作反馈

鉴权逻辑在 `src/stores/auth.ts`：

- 登录时写入本地 `token`（`localStorage`）
- 演示项目默认不对接真实后端

主题设置在 `src/stores/settings.ts`：

- 主题模式（明/暗）
- 主题预设（preset）
- 字体模式（font mode）
- 设置值会写入 `localStorage` 并实时同步到 DOM dataset

---

## 开发建议

- 建议优先在 `views` 中按业务模块迭代页面，再抽离到 `components`
- 与后端联调时，可先将 `stores/app.ts` 的静态数据替换为 API 请求层
- 若新增业务模块，推荐同步更新：
  - `src/router/index.ts` 路由
  - `src/layouts/MainLayout.vue` 侧栏导航
  - `src/views/HomeView.vue` 快捷入口/统计卡片（如需要）

---

## 可选后续规划（建议）

- 接入真实后端 API（登录、CRUD、统计）
- 引入请求层与错误处理规范（如 axios 封装）
- 增加持久化与同步策略（本地缓存 + 服务端）
- 增加单元测试与 E2E 测试（Vitest / Playwright）
- 增加 CI 流程（lint、typecheck、build）
- 增加权限模型（家庭成员角色、数据可见范围）

---

## 常见问题

### 1）为什么登录页任意账号密码都能登录？

这是当前演示策略，便于快速进入系统查看页面与交互。生产环境请替换为真实鉴权接口。

### 2）为什么数据刷新后部分会恢复默认？

目前业务主数据来自前端示例数据，未完全接入后端持久化。可按“后续规划”将其改造为 API + 存储。

### 3）如何让手机访问本机开发环境？

项目已开启 `host: true`。确保手机与电脑在同一局域网，访问 `http://<你的局域网IP>:5173` 即可。

---

## License

如需开源，请在此补充具体 License（例如 MIT）。当前仓库未声明明确开源许可。
