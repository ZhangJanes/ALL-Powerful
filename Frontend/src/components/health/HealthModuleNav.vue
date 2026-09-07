<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'

defineProps<{ title: string; eyebrow?: string }>()

const route = useRoute()
const router = useRouter()
const tabs = [
  { name: 'health', label: '总览' },
  { name: 'health-plan', label: '医学方案' },
  { name: 'health-check-in', label: '每日打卡' },
  { name: 'health-profile', label: '基础档案' },
  { name: 'health-goals', label: '目标计划' },
  { name: 'health-stats', label: '数据趋势' },
]
</script>

<template>
  <header class="health-header glass">
    <div class="heading">
      <NButton quaternary circle aria-label="返回首页" @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div>
        <div class="eyebrow">{{ eyebrow || 'HEALTH / NUTRITION' }}</div>
        <h1>{{ title }}</h1>
      </div>
    </div>
    <nav class="module-tabs" aria-label="健康饮食模块导航">
      <button
        v-for="tab in tabs"
        :key="tab.name"
        type="button"
        :class="{ active: route.name === tab.name }"
        @click="router.push({ name: tab.name })"
      >
        {{ tab.label }}
      </button>
    </nav>
  </header>
</template>

<style scoped>
.health-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 11px 15px;
  border: 1px solid var(--fm-track);
  border-radius: var(--fm-radius-lg);
}
.heading {
  display: flex;
  align-items: center;
  gap: 12px;
}
.eyebrow {
  color: var(--fm-text-faint);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.12em;
}
h1 {
  margin: 2px 0 0;
  color: var(--fm-text-strong);
  font-size: var(--fm-font-title);
  line-height: 28px;
  letter-spacing: -0.018em;
}
.module-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  overflow-x: auto;
  border: 1px solid var(--fm-track);
  border-radius: var(--fm-radius-sm);
  background: rgba(15, 23, 42, 0.08);
}
.module-tabs button {
  min-width: max-content;
  min-height: 32px;
  padding: 6px 11px;
  border: 0;
  border-radius: 7px;
  color: var(--fm-text-muted);
  background: transparent;
  cursor: pointer;
  font: inherit;
  font-size: var(--fm-font-label);
  font-weight: 700;
}
.module-tabs button:hover,
.module-tabs button:focus-visible {
  color: var(--fm-text-strong);
  outline: none;
}
.module-tabs button.active {
  color: #eafff7;
  background: linear-gradient(135deg, rgba(13, 148, 136, 0.92), rgba(37, 99, 235, 0.84));
  box-shadow: 0 8px 22px rgba(13, 148, 136, 0.2);
}
@media (max-width: 900px) {
  .health-header { position: static; align-items: stretch; flex-direction: column; }
  .module-tabs { width: 100%; }
}
</style>
