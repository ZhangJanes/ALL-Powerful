<script setup lang="ts">
export type FmAntdIllustrationVariant =
  | 'empty'
  | 'success'
  | 'error'
  | 'network'
  | 'search'
  | 'building'

withDefaults(
  defineProps<{
    variant?: FmAntdIllustrationVariant
    /** 插画宽度（px），高度随 viewBox 比例 */
    width?: number
  }>(),
  {
    variant: 'empty',
    width: 168,
  },
)
</script>

<template>
  <div
    class="fm-antd-illustration"
    :style="{ width: `${width}px` }"
    aria-hidden="true"
  >
    <svg
      class="fm-antd-illustration__svg"
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 200 160"
    >
      <!-- 柔和装饰（非涂鸦预设） -->
      <g class="decor-ant ill-decor-ant">
        <circle class="ill-fill-only" cx="38" cy="32" r="20" fill="var(--ill-decor-a)" opacity="0.55" />
        <circle class="ill-fill-only" cx="168" cy="44" r="14" fill="var(--ill-decor-b)" opacity="0.5" />
      </g>
      <!-- 涂鸦装饰：手绘星星 / 色块 -->
      <g class="decor-sketch ill-decor-sketch">
        <path
          class="ill-shape"
          fill="var(--ill-accent-warm-soft)"
          d="M38 20 L42 30 L52 31 L44 38 L47 48 L38 42 L29 48 L32 38 L24 31 L34 30 Z"
        />
        <path
          class="ill-shape"
          fill="var(--ill-primary-soft)"
          d="M158 36 L162 46 L172 48 L164 54 L166 64 L158 58 L150 64 L152 54 L144 48 L154 46 Z"
        />
        <circle class="ill-shape" cx="52" cy="48" r="5" fill="var(--ill-success-soft)" />
      </g>

      <!-- 空状态 -->
      <g v-if="variant === 'empty'">
        <ellipse
          class="ill-shape ill-ground"
          cx="100"
          cy="136"
          rx="76"
          ry="11"
          fill="var(--ill-shadow-oval)"
        />
        <path
          class="ill-shape"
          fill="var(--ill-surface)"
          d="M52 108 L100 88 L148 108 L148 124 Q148 130 140 130 L60 130 Q52 130 52 124 Z"
        />
        <path
          class="ill-shape"
          fill="var(--ill-primary-soft)"
          d="M52 108 L100 88 L148 108 L100 128 Z"
        />
        <path
          class="ill-shape ill-stroke-only"
          fill="none"
          d="M100 88 L100 128"
        />
        <rect
          class="ill-shape"
          x="112"
          y="56"
          width="36"
          height="44"
          rx="5"
          fill="var(--ill-paper)"
          transform="rotate(12 130 78)"
        />
        <rect
          class="ill-shape"
          x="54"
          y="62"
          width="32"
          height="40"
          rx="4"
          fill="var(--ill-paper)"
          transform="rotate(-10 70 82)"
        />
        <rect
          class="ill-shape"
          x="72"
          y="48"
          width="28"
          height="34"
          rx="4"
          fill="var(--ill-accent-warm-soft)"
        />
      </g>

      <!-- 成功 -->
      <g v-else-if="variant === 'success'">
        <ellipse
          class="ill-shape ill-ground"
          cx="100"
          cy="136"
          rx="70"
          ry="10"
          fill="var(--ill-shadow-oval)"
        />
        <circle class="ill-shape" cx="100" cy="86" r="44" fill="var(--ill-success-soft)" />
        <path
          class="ill-shape ill-mark"
          fill="none"
          d="M82 88 L94 100 L122 70"
        />
        <circle class="ill-fill-only ill-spark" cx="154" cy="48" r="6" fill="var(--ill-accent-warm)" />
        <circle class="ill-fill-only ill-spark" cx="44" cy="56" r="5" fill="var(--ill-primary)" />
      </g>

      <!-- 错误 -->
      <g v-else-if="variant === 'error'">
        <ellipse
          class="ill-shape ill-ground"
          cx="100"
          cy="136"
          rx="72"
          ry="11"
          fill="var(--ill-shadow-oval)"
        />
        <rect
          class="ill-shape"
          x="56"
          y="52"
          width="88"
          height="72"
          rx="9"
          fill="var(--ill-error-soft)"
        />
        <path class="ill-shape ill-mark" fill="none" d="M78 74 L122 118 M122 74 L78 118" />
      </g>

      <!-- 网络 -->
      <g v-else-if="variant === 'network'">
        <ellipse
          class="ill-shape ill-ground"
          cx="100"
          cy="136"
          rx="74"
          ry="11"
          fill="var(--ill-shadow-oval)"
        />
        <path
          class="ill-shape ill-stroke-only"
          fill="none"
          d="M48 96 Q100 52 152 96"
        />
        <path
          class="ill-shape ill-dash"
          fill="none"
          d="M48 96 Q100 124 152 96"
        />
        <rect class="ill-shape" x="76" y="72" width="48" height="36" rx="7" fill="var(--ill-primary-soft)" />
        <path class="ill-shape ill-mark" fill="none" d="M88 88 L112 88 M100 80 L100 104" />
      </g>

      <!-- 搜索 -->
      <g v-else-if="variant === 'search'">
        <ellipse
          class="ill-shape ill-ground"
          cx="100"
          cy="136"
          rx="70"
          ry="10"
          fill="var(--ill-shadow-oval)"
        />
        <circle class="ill-shape" cx="88" cy="78" r="34" fill="var(--ill-paper)" />
        <path class="ill-shape ill-mark" fill="none" d="M112 102 L138 128" />
        <path class="ill-shape ill-stroke-muted" fill="none" d="M72 78 L104 78 M88 64 L88 92" />
      </g>

      <!-- 建设中 -->
      <g v-else-if="variant === 'building'">
        <ellipse
          class="ill-shape ill-ground"
          cx="100"
          cy="136"
          rx="76"
          ry="11"
          fill="var(--ill-shadow-oval)"
        />
        <path
          class="ill-shape"
          fill="var(--ill-surface)"
          d="M64 124 L64 72 L100 48 L136 72 L136 124 Z"
        />
        <rect class="ill-shape" x="78" y="88" width="16" height="16" rx="3" fill="var(--ill-primary-soft)" />
        <rect class="ill-shape" x="106" y="88" width="16" height="16" rx="3" fill="var(--ill-primary-soft)" />
        <rect class="ill-shape" x="92" y="108" width="16" height="16" rx="3" fill="var(--ill-accent-warm-soft)" />
        <path class="ill-shape ill-bar" fill="none" d="M48 124 L152 124" />
        <path
          class="ill-shape"
          fill="var(--ill-accent-warm)"
          d="M72 56 L84 64 L72 72 Z"
        />
      </g>
    </svg>
  </div>
</template>

<style scoped>
.fm-antd-illustration__svg {
  display: block;
  width: 100%;
  height: auto;
  vertical-align: middle;
}

.ill-decor-sketch {
  display: none;
}

.ill-shape {
  stroke: var(--ill-ink);
  stroke-width: var(--ill-stroke-w);
  stroke-linecap: round;
  stroke-linejoin: round;
}

.ill-fill-only {
  stroke: none;
}

.ill-stroke-only {
  fill: none;
}

.ill-ground {
  stroke: none;
}

.ill-mark {
  stroke-width: calc(var(--ill-stroke-w) + 1.25px);
}

.ill-dash {
  fill: none;
  stroke-dasharray: 8 7;
  stroke: var(--ill-primary);
  stroke-width: var(--ill-stroke-w);
}

.ill-stroke-muted {
  fill: none;
  stroke: var(--ill-muted);
  stroke-width: var(--ill-stroke-w);
}

.ill-bar {
  stroke: var(--ill-accent-warm);
  stroke-width: calc(var(--ill-stroke-w) + 1.5px);
}

.ill-spark {
  stroke: var(--ill-ink);
  stroke-width: calc(var(--ill-stroke-w) * 0.65);
}
</style>

<style>
html[data-preset='sketch'] .fm-antd-illustration {
  padding: 8px 10px 10px;
  border-radius: 14px;
  background: var(--ill-frame-bg);
  border: 2px solid var(--ill-ink);
  box-shadow: var(--ill-frame-shadow);
}

html[data-preset='sketch'] .ill-decor-ant {
  display: none;
}

html[data-preset='sketch'] .ill-decor-sketch {
  display: block;
}

html[data-preset='sketch'] .ill-ground {
  stroke: var(--ill-ink);
  stroke-width: calc(var(--ill-stroke-w) * 0.45);
  opacity: 0.95;
}
</style>
