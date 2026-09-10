---
name: login-visual-system
description: >-
  Preserves and iterates the ALL-Powerful login/register landing visual system:
  Three.js click-ripple grid, SVG cloth displacement on UI, glass-transparent
  form controls, scene palettes. Use when editing LoginView, LoginThreeField,
  LoginFormAura, loginWave, /login, /register, auth page effects, 登录页, 波纹, 人浪,
  水浪, 布面, 透明按钮, or Three.js on the auth landing.
---

# 登录页视觉系统

这是 **登录/注册落地页**（`/login`、`/register`，`LoginView.vue`），不是登录后的仪表盘首页。改视觉前先读本 skill，再动代码。

细则与常量：[reference.md](reference.md) · 提示词：[examples.md](examples.md)

## 文件

| 职责 | 路径 |
| --- | --- |
| 页面、场景文案、玻璃 UI、布面滤镜 | `Frontend/src/views/LoginView.vue` |
| 背景网格 + 点击波 + 划过火花 | `Frontend/src/components/auth/LoginThreeField.vue` |
| 登录卡片后的 shader 光晕 | `Frontend/src/components/auth/LoginFormAura.vue` |
| 共享波源 / 采样 / 位移图 | `Frontend/src/components/auth/loginWave.ts` |

波的数学只放 `loginWave.ts`。网格顶点与 UI 布面都必须调用同一套 `samplePulses`，禁止各写一套相位。

## 三层结构（不要合并、不要用 CSS 代替）

1. **舞台**：全屏场景色 + `LoginThreeField`（正交相机点/线网格）。
2. **卡片光**：`LoginFormAura` 叠在表单卡后面。
3. **UI 布**：顶栏 + `main` 包在 `.ui-sheet[data-wave-cloth]` 里，用 SVG `feDisplacementMap` 跟波弯曲。

禁止用整层 `translate` / `rotateX/Y` 冒充布面：那是刚体抖动或倾斜，没有像素形变。

## 硬约束

- **Three.js 必须始终挂载**。不要用 `v-if="!reduceMotion"` 卸掉 `LoginThreeField` / `LoginFormAura`。Windows 关掉动画时 `prefers-reduced-motion: reduce` 为 true，组件会被拆掉导致「特效没了」。`reduceMotion` 只暂停场景轮播定时器。
- **不要做文字特效**。禁止恢复 `LoginHeroType.vue`、canvas 纹理字、彩虹/色散。标题普通 HTML，颜色统一 `var(--copy-main)`。
- **波是点击处向外扩的正弦水浪**，不是随机人浪、不是整场呼吸缩放、不要 XY 剪切。网格线保持正交，不要随机相位把线拧斜。
- **波从大到小、略慢**；前两圈用波前锁定相位衔接，不要生硬接上。
- **表单控件玻璃化**：输入框、主按钮、登录/注册 tab 都是透明底 + `1px solid rgba(255,255,255,0.22)`，浅色字。禁止主按钮实心白渐变。密码框内眼睛/清除按钮透明、不要额外描边。
- **Naive 内联色**必须用非 scoped 的 `[data-page='auth'] .n-input` / `.n-button` 去压 `--n-color`。
- Vue SFC 里的 shader 字符串：**禁止 `-(x ** 2)`**（编译报 `Illegal expression`）。写成 `-(x * x)` 或再加括号。
- Three.js：`ColorManagement.enabled = false`，`outputColorSpace = LinearSRGBColorSpace`，`NoToneMapping`，shader 继续用 `gl_FragColor`。
- 空 `feImage` 会把 UI 扭没：mount 后先 `pushClothMap` 再开 rAF。resize / 切场景 / 切登录注册后 320ms 再 `captureWaveRests`。
- 点击命中按未变形布局（浏览器常态）。不要为了「点哪跟哪」去改 hit-test。

## 改动时同步

- 改波速度/波长/寿命 → 只改 `loginWave.ts` 常量，网格和布面会一起变。
- 改网格覆盖范围 → `layoutGrid`/`resize` 里继续 `syncWaveSpace`。
- 改 UI 弯曲强度 → `CLOTH_SCALE`（现 72）与 `feDisplacementMap scale` 保持一致。
- 改按钮/输入外观 → 同步：`formOverrides`、scoped `:deep`、非 scoped `[data-page='auth']`。
- 新场景色 → `LoginView` 的 `scenes` + `LoginThreeField`/`LoginFormAura` 的 `PALETTES` 三处同 key。

## 明确拒绝的旧方案

- 用 `prefers-reduced-motion` 卸载 WebGL
- 标题 shader / 逐字异色
- 随机相位 + 剪切造成斜线
- UI 整块 translate 抖动、rotateX/Y 刚体倾斜
- 主按钮实心浅色填充

## 验收

- Windows「关闭动画」时网格仍在，点击仍出波，UI 仍弯曲。
- 点击一点，波从该点一圈圈扩开，由大转小，线不斜。
- 标题每个字同色；登录按钮与输入框一样透明描边。
- 划过网格仍可打散粒子；划过不是波源，**点击才是波源**。
