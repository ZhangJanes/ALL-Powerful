<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, AddOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { NHeatmap, useDialog, useMessage } from 'naive-ui'
import type { HabitHeatmapPeriod } from '@/utils/habitHeatmap'
import { buildHabitGoalHeatmap } from '@/utils/habitHeatmap'

const router = useRouter()
const store = useAppStore()

const heatTab = ref<HabitHeatmapPeriod>('week')
const habitHeat = computed(() => buildHabitGoalHeatmap(heatTab.value, store.habits))

const dialog = useDialog()
const message = useMessage()

function onCheck(h: { id: string; name: string; doneToday: boolean }) {
  if (h.doneToday) return
  dialog.warning({
    title: '确认打卡',
    content: `确认完成「${h.name}」今日打卡？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => {
      store.checkIn(h.id)
      message.success('打卡成功')
    },
  })
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">打卡 & 目标</div>
      <NButton quaternary circle @click="router.push({ name: 'habits-new' })">
        <template #icon><NIcon :component="AddOutline" /></template>
      </NButton>
    </div>

    <NCard class="glass heat-card" :bordered="false" size="small" title="今日完成度">
      <div class="heat-head">
        <div class="heat-sub">{{ habitHeat.periodTitle }}</div>
        <div class="heat-caption">{{ habitHeat.caption }}</div>
      </div>
      <NTabs v-model:value="heatTab" class="heat-tabs" type="segment" size="small">
        <NTabPane name="week" tab="周目标" />
        <NTabPane name="month" tab="月目标" />
        <NTabPane name="quarter" tab="季目标" />
      </NTabs>
      <div class="heat-hint">格子颜色表示当日打卡完成度（0–100%）；今日为真实数据，历史为演示填充。</div>
      <div class="hm-scroll">
        <NHeatmap
          :key="heatTab"
          :data="habitHeat.data"
          :first-day-of-week="1"
          color-theme="green"
          size="small"
          :tooltip="true"
          :show-month-labels="heatTab !== 'week'"
        />
      </div>
    </NCard>

    <NCard class="glass" :bordered="false" :title="`今日打卡（${store.habits.length}）`">
      <NSpace vertical>
        <NCard v-for="h in store.habits" :key="h.id" size="small" embedded class="habit" @click="router.push({ name: 'habits-detail', params: { id: h.id } })">
          <div class="row">
            <div>
              <div class="name">{{ h.name }}</div>
              <div class="subtle">{{ h.targetDays }} 天目标 · 已打卡 {{ h.total }} 天 · 连续 {{ h.streak }}</div>
            </div>
            <NButton size="small" :disabled="h.doneToday" :type="h.doneToday ? 'default' : 'success'" @click.stop="onCheck(h)">
              {{ h.doneToday ? '已完成' : '打卡' }}
            </NButton>
          </div>
        </NCard>
      </NSpace>
    </NCard>

    <NCard class="glass" :bordered="false" title="我的成就">
      <NSpace>
        <NTag v-for="a in store.achievements" :key="a" type="warning" round>{{ a }}</NTag>
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 6px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.habit {
  cursor: pointer;
  border-radius: 12px;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.name {
  font-weight: 700;
}
.chart-cap {
  font-size: 13px;
  font-weight: 600;
  color: var(--fm-text-secondary);
  margin-bottom: 10px;
}

.heat-card {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.1);
}
.heat-head {
  margin-bottom: 10px;
}
.heat-sub {
  font-size: 13px;
  font-weight: 700;
  color: var(--fm-text-primary);
}
.heat-caption {
  margin-top: 4px;
  font-size: 12px;
  color: var(--fm-text-muted);
}
.heat-tabs {
  margin-bottom: 10px;
}
.heat-hint {
  font-size: 11px;
  color: var(--fm-text-faint);
  margin-bottom: 10px;
  line-height: 1.45;
}
.hm-scroll {
  overflow-x: auto;
  padding-bottom: 4px;
}

</style>
