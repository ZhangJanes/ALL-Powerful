# 登录页视觉系统 · 细则

## 波（`loginWave.ts`）

当前常量：

| 名 | 值 | 含义 |
| --- | --- | --- |
| `WAVE_C` | 15.5 | 波前速度 |
| `WAVE_K` | 0.55 | 空间波数 |
| `WAVE_LIFE` | 7.2s | 单波寿命 |
| `WAVE_LIFT` | 2.65 | 径向位移倍率 |
| `MAX_PULSES` | 10 | 同时存在的波源上限 |

点击：`pointerdown` → NDC → `spawnPulseFromNdc`。波源存在模块级 `pulses[]`。

采样要点（不要改回随机相位）：

- `front = WAVE_C * t * (1 - 0.09 * life)`，正弦锁在波前：`sin(k * behind)`
- Hann 尾 + sigmoid 前沿 + `sin + 0.5*sin` 填谷
- 出生缓入 `1 - exp(-elapsed * 2.6)`
- 位移沿径向：`(dx, dy) * inv * mag`，网格 `z` 保持 0
- `homeOf` 用规则格子 `origin + (x,y)*cell + sample`，线用正交邻接，禁止 XY 剪切

`fillDisplacementMap`：按 UI 包围盒采样，R=X、G=Y，画布 96×54。`LoginView` 里 `CLOTH_SCALE = 72`，与 `#auth-wave-cloth feDisplacementMap scale="72"` 一致。

## UI 布面（`LoginView.vue`）

```
svg#auth-wave-cloth
  feImage → feDisplacementMap (R/G, scale 72)
.ui-sheet[data-wave-cloth]  { filter: url(#auth-wave-cloth) }
```

循环：`floatOnWave` → `fillDisplacementMap` → canvas `toDataURL` 写 `feImage` 的 `href`（同时设 `xlink:href`）。

`captureWaveRests()`：读 sheet 的 `getBoundingClientRect`。时机：mount、resize、切场景或登录/注册后 320ms。

## 网格（`LoginThreeField.vue`）

- 正交相机，`halfH = 4.55`，按容器宽高比铺满
- 点 + 线；`CELL = 0.168`
- 鼠标移动：`emitSparks` 打散粒子（与波独立）
- 左键点击：只 `spawnPulseFromNdc`，不要在 mousemove 里生波

## 玻璃控件

对齐输入框：

- 底：`transparent`
- 边：`1px solid rgba(255,255,255,0.22)`
- hover 边：`rgba(255,255,255,0.38)`
- 字：`#f8fafc`

覆盖三处：

1. `formOverrides.Input` / `formOverrides.Button`
2. scoped：`.card :deep(.n-button--primary-type)`、`.mode-tabs` / `.mode-tab`
3. 非 scoped：`[data-page='auth'] .n-input`、`.n-button` 的 `--n-color*`

主按钮才加白描边。输入框 suffix 里的 `n-button` 只透明、不加边。

`.mode-tab.active`：透明 + 描边，不要半透明实底。`.text-btn` 保持无底。

## Vue SFC + Three.js 坑

```js
// 错误（.vue 编译失败）
exp(-d ** 2 * 5.2)
-(x ** 2)

// 正确
exp(-d * d * 5.2)
-(x * x)
```

```js
THREE.ColorManagement.enabled = false
renderer.outputColorSpace = THREE.LinearSRGBColorSpace
renderer.toneMapping = THREE.NoToneMapping
```

片元仍写 `gl_FragColor`。

## 场景 key

`journal` | `health` | `rhythm` | `voyage` | `vault`

三处 palette / 文案必须同 key。轮播 7200ms；`reduceMotion` 只停轮播，不停 WebGL。
