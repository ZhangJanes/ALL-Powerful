<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSettingsStore } from '@/stores/settings'
import {
  HomeOutline,
  DocumentTextOutline,
  WalletOutline,
  CheckboxOutline,
  NutritionOutline,
  AirplaneOutline,
  BulbOutline,
  ImagesOutline,
  ReaderOutline,
  GridOutline,
  NotificationsOutline,
  PersonCircleOutline,
} from '@vicons/ionicons5'
const route = useRoute()
const router = useRouter()
const settings = useSettingsStore()
const isSketch = computed(() => settings.themePreset === 'sketch')

function sketchNavClass(key: NavKey) {
  return isSketch.value ? `sk-nav sk-nav--${key}` : ''
}

type NavKey =
  | 'home'
  | 'memo'
  | 'ledger'
  | 'habits'
  | 'health'
  | 'travel'
  | 'ideas'
  | 'photos'
  | 'guide'
  | 'more'
  | 'messages'
  | 'profile'

const navSections: { title: string; items: { key: NavKey; label: string; route: string; icon: typeof HomeOutline }[] }[] = [
  {
    title: '功能',
    items: [
      { key: 'home', label: '首页', route: 'home', icon: HomeOutline },
      { key: 'memo', label: '备忘录', route: 'memo', icon: DocumentTextOutline },
      { key: 'ledger', label: '记账', route: 'ledger', icon: WalletOutline },
      { key: 'habits', label: '打卡', route: 'habits', icon: CheckboxOutline },
      { key: 'health', label: '健康饮食', route: 'health', icon: NutritionOutline },
      { key: 'travel', label: '出行计划', route: 'travel', icon: AirplaneOutline },
      { key: 'ideas', label: 'New Idea', route: 'ideas', icon: BulbOutline },
      { key: 'photos', label: '照片库', route: 'photos', icon: ImagesOutline },
      { key: 'guide', label: '办事指南', route: 'guide', icon: ReaderOutline },
      { key: 'more', label: '更多', route: 'more', icon: GridOutline },
    ],
  },
  {
    title: '账户',
    items: [
      { key: 'messages', label: '消息', route: 'messages', icon: NotificationsOutline },
      { key: 'profile', label: '我的', route: 'profile', icon: PersonCircleOutline },
    ],
  },
]

function isActive(key: NavKey): boolean {
  const n = String(route.name ?? '')
  if (key === 'home') return n === 'home'
  if (key === 'photos' || key === 'more' || key === 'messages' || key === 'profile') return n === key
  return n === key || n.startsWith(`${key}-`)
}

function go(name: string) {
  router.push({ name })
}

const LS_KEY = 'fm.nav.collapsed'
const collapsed = ref<boolean>(localStorage.getItem(LS_KEY) === '1')

watch(
  collapsed,
  (v) => {
    localStorage.setItem(LS_KEY, v ? '1' : '0')
  },
  { immediate: true },
)

// 折叠宽度
const railWidth = computed(() => (collapsed.value ? 56 : 208))
// 折叠隐藏后不额外占位：仅使用 shell 的 padding 作为左右留白
const mainOffset = computed(() => (collapsed.value ? 0 : railWidth.value + 34))
</script>

<template>
  <div class="shell" :style="{ '--fm-rail-w': `${railWidth}px` }">
    <!-- 折叠态：默认隐藏，hover 左侧热区渐显 -->
    <div class="rail-wrap" :class="{ collapsed }" aria-label="主导航">
      <!-- 折叠后：屏幕最左侧的发光把手（hover/点击） -->
      <button
        v-if="collapsed"
        class="rail-peek"
        type="button"
        aria-label="展开侧栏"
        @click="collapsed = false"
      />

      <aside class="side-rail glass" :class="{ collapsed }">
        <div class="brand-wrap">
          <div class="brand-row">
            <div v-if="!collapsed" class="brand">家庭小管家</div>
          </div>
          <div v-if="!collapsed" class="brand-sub">PC 工作台</div>
        </div>

        <!-- 折叠把手：位于菜单框右侧垂直居中 -->
        <button
          v-if="!collapsed"
          class="rail-handle rail-handle-abs"
          type="button"
          :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'"
          @click="collapsed = !collapsed"
        />

        <NScrollbar class="rail-scroll" trigger="none">
          <div v-for="sec in navSections" :key="sec.title" class="sec">
            <div v-if="!collapsed" class="sec-title">{{ sec.title }}</div>
            <template v-for="it in sec.items" :key="it.key">
              <NTooltip v-if="collapsed" placement="right" :delay="120">
                <template #trigger>
                  <NButton
                    block
                    quaternary
                    class="side-item"
                    :class="sketchNavClass(it.key)"
                    :type="isActive(it.key) ? 'primary' : 'default'"
                    @click="go(it.route)"
                  >
                    <NBadge v-if="it.key === 'messages'" :value="2" :max="99">
                      <NIcon :component="it.icon" :size="20" />
                    </NBadge>
                    <NIcon v-else :component="it.icon" :size="20" />
                  </NButton>
                </template>
                {{ it.label }}
              </NTooltip>

              <NButton
                v-else
                block
                quaternary
                class="side-item"
                :class="sketchNavClass(it.key)"
                :type="isActive(it.key) ? 'primary' : 'default'"
                @click="go(it.route)"
              >
                <NBadge v-if="it.key === 'messages'" :value="2" :max="99">
                  <NIcon :component="it.icon" :size="20" />
                </NBadge>
                <NIcon v-else :component="it.icon" :size="20" />
                <span class="side-label">{{ it.label }}</span>
              </NButton>
            </template>
          </div>
        </NScrollbar>
      </aside>
    </div>

    <main class="main-flow" :style="{ '--fm-main-offset': `${mainOffset}px` }">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  align-items: stretch;
  box-sizing: border-box;
  padding: 16px 18px;
  gap: 16px;
  /* 锁定为视口高度，滚动只发生在 .main-flow，避免整页滚动与 fixed 侧栏错位 */
  height: 100vh;
  max-height: 100vh;
  overflow: hidden;
}

.rail-wrap {
  position: fixed;
  left: 16px;
  top: 16px;
  bottom: 16px; /* 与顶部间距一致 */
  height: auto;
  z-index: 60;
}

.rail-peek {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 10px; /* 点击热区 */
  height: 72px; /* 折叠按钮增高 */
  border: 0;
  padding: 0;
  cursor: pointer;

  /* 视觉：仅 2px 竖条 + 发光阴影 */
  background: transparent;
  opacity: 0.85;
  transition: opacity 180ms ease, width 180ms ease;
  z-index: 80;
}

.rail-peek::before {
  content: '';
  position: absolute;
  right: 2px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 72px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--fm-glow, rgba(148, 163, 184, 0.72)) 90%, transparent);
  box-shadow:
    0 0 0 1px rgba(15, 23, 42, 0.18),
    0 0 22px color-mix(in srgb, var(--fm-glow, rgba(148, 163, 184, 0.72)) 55%, transparent),
    0 0 48px color-mix(in srgb, var(--fm-glow, rgba(148, 163, 184, 0.72)) 25%, transparent);
}

.rail-peek:hover {
  opacity: 1;
  width: 14px;
}

.side-rail {
  width: var(--fm-rail-w, 256px);
  display: flex;
  flex-direction: column;
  padding: 18px 10px 14px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: transparent;
  overflow: hidden;
  box-shadow:
    0 18px 54px rgba(0, 0, 0, 0.42),
    inset 0 1px 0 rgba(255, 255, 255, 0.04);
  height: calc(100vh - 80px);

  /* 折叠动画 */
  transition:
    width 220ms cubic-bezier(0.2, 0.9, 0.2, 1),
    background-color 220ms ease,
    box-shadow 220ms ease,
    border-color 220ms ease;
}

.rail-wrap.collapsed .side-rail {
  /* 默认隐藏，hover 才渐显 */
  opacity: 0;
  transform: translateX(-10px);
  pointer-events: none;
  transition:
    opacity 200ms ease,
    transform 200ms ease,
    width 220ms cubic-bezier(0.2, 0.9, 0.2, 1);
}

.rail-wrap.collapsed:hover .side-rail {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
}

.rail-wrap.collapsed {
  /* 折叠隐藏时：不占用左侧空间，只保留最左热区 */
  left: 0;
  width: 0;
}

.rail-wrap.collapsed .side-rail {
  position: absolute;
  left: 0;
  top: 0;
}

.side-rail::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--fm-rail-glass, rgba(15, 23, 42, 0.28));
}

.side-rail > * {
  position: relative;
  z-index: 1;
}

:global(html[data-theme='dark']) .side-rail::before {
  /* 深色：保留通透毛玻璃 */
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
}

:global(html[data-theme='light']) .side-rail::before {
  /* 浅色：去掉毛玻璃（不要 blur），更干净 */
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
}

.side-rail.collapsed {
  padding: 16px 8px 14px;
}

.brand-wrap {
  padding: 4px 10px 16px;
  flex-shrink: 0;
}

.brand-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.brand {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.03em;
  color: var(--fm-text-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity 160ms ease, transform 220ms ease;
}

.brand-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--fm-text-faint);
  transition: opacity 160ms ease, transform 220ms ease;
}

/* 折叠把手：只有 2px 竖条（hover 才更亮），但点击热区更大 */
.rail-handle {
  position: relative;
  width: 12px;
  height: 72px; /* 与折叠态左侧把手一致 */
  padding: 0;
  border: 0;
  outline: none;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
  opacity: 0.75;
  transition: opacity 180ms ease, width 180ms ease;
}

.rail-handle::before {
  content: '';
  position: absolute;
  right: 2px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 72px;
  border-radius: 999px;
  background: var(--fm-glow, rgba(148, 163, 184, 0.72));
  box-shadow:
    0 0 0 1px rgba(15, 23, 42, 0.18),
    0 0 22px color-mix(in srgb, var(--fm-glow, rgba(148, 163, 184, 0.72)) 65%, transparent),
    0 0 48px color-mix(in srgb, var(--fm-glow, rgba(148, 163, 184, 0.72)) 28%, transparent);
}

.side-rail:hover .rail-handle {
  opacity: 1;
  width: 14px;
}

.rail-handle-abs {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

.side-rail.collapsed .rail-handle-abs {
  right: 4px;
}

.rail-scroll {
  flex: 1;
  min-height: 0;
  max-height: calc(100vh - 32px - 108px);
}

.sec + .sec {
  margin-top: 11px;
  padding-top: 10px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}

.sec-title {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--fm-text-label);
  padding: 0 10px 8px;
}

.side-item {
  justify-content: flex-start;
  margin-bottom: 2px;
  height: auto;
  min-height: 44px;
  padding: 7px 9px;
  border-radius: 11px;
  font-weight: 600;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.side-item:hover {
  background: rgba(100, 116, 139, 0.1) !important;
}

/* 图标与文字间距：在原有基础上 +5px（此处 15px） */
.side-item :deep(.n-button__content) {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
  width: 100%;
  height: 100%;
}

.side-item :deep(.n-icon) {
  flex-shrink: 0;
}

.side-rail.collapsed .side-item {
  padding: 0;
  min-height: 48px;
}

.side-rail.collapsed .side-item :deep(.n-button__content) {
  /* 折叠态：图标在按钮内水平/垂直居中 */
  justify-content: center;
  gap: 0;
  padding-right: 0;
}

.side-rail.collapsed .side-item :deep(.n-icon),
.side-rail.collapsed .side-item :deep(.n-badge) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.side-rail.collapsed .side-item :deep(.n-badge-sup) {
  /* 避免角标把图标整体挤歪（仍保留显示） */
  transform: translate(30%, -30%);
}

.side-label {
  flex: 1;
  min-width: 0;
  text-align: left;
  font-size: 14px;
  letter-spacing: 0.02em;
  line-height: 1.35;
  transition: opacity 160ms ease, transform 220ms ease;
}

.main-flow {
  flex: 1;
  min-width: 0;
  min-height: 0;
  border-radius: var(--fm-radius-lg);
  border: 1px solid var(--fm-main-border, rgba(148, 163, 184, 0.1));
  background: var(--fm-main-bg, rgba(10, 16, 28, 0.35));
  overflow: auto;
  overscroll-behavior: contain;
  box-sizing: border-box;

  /*
   * 使用 padding 而非 margin：margin 区域不绘制背景，滚动时会露出底层渐变，
   * 与 fixed 侧栏边缘产生色差/分割线。
   */
  margin-left: 0;
  padding-left: var(--fm-main-offset, 276px);
  transition: padding-left 220ms cubic-bezier(0.2, 0.9, 0.2, 1);
}
</style>
