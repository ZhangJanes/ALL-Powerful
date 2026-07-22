<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  DocumentTextOutline,
  WalletOutline,
  CheckboxOutline,
  AirplaneOutline,
  BulbOutline,
  ImagesOutline,
  ReaderOutline,
  GridOutline,
} from '@vicons/ionicons5'
import { useMemoStore } from '@/stores/memo'
import { useLedgerStore } from '@/stores/ledger'
import { useHabitStore } from '@/stores/habit'
import { useMessageStore } from '@/stores/message'
import { useHomeStore } from '@/stores/home'
import FmChartBlock from '@/components/charts/FmChartBlock.vue'
import {
  aggregateExpenseByDay,
  buildBudgetRingOption,
  buildExpenseLineBarOption,
} from '@/utils/chartOptions'

const router = useRouter()
const memoStore = useMemoStore()
const ledgerStore = useLedgerStore()
const habitStore = useHabitStore()
const messageStore = useMessageStore()
const homeStore = useHomeStore()

const homeLineMixOption = computed(() => {
  const { categories, values } = aggregateExpenseByDay(ledgerStore.ledger, 7)
  return buildExpenseLineBarOption(categories, values)
})

const homeBudgetRing = computed(() => buildBudgetRingOption(ledgerStore.monthlySpent, ledgerStore.monthlyBudget))

const shortcuts = [
  { label: '备忘录', name: 'memo', icon: DocumentTextOutline, color: '#14b8a6' },
  { label: '记账', name: 'ledger', icon: WalletOutline, color: '#f472b6' },
  { label: '打卡', name: 'habits', icon: CheckboxOutline, color: '#34d399' },
  { label: '出行', name: 'travel', icon: AirplaneOutline, color: '#f97316' },
  { label: 'New Idea', name: 'ideas', icon: BulbOutline, color: '#fbbf24' },
  { label: '照片', name: 'photos', icon: ImagesOutline, color: '#0d9488' },
  { label: '办事指南', name: 'guide', icon: ReaderOutline, color: '#94a3b8' },
  { label: '更多', name: 'more', icon: GridOutline, color: '#cbd5e1' },
] as const

const weekday = new Intl.DateTimeFormat('zh-CN', { weekday: 'long' }).format(new Date())
const dateStr = new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date())
</script>

<template>
  <div class="app-shell">
    <div class="hero glass">
      <div class="hero-top">
        <div>
          <div class="date-line">{{ dateStr }} {{ weekday }}</div>
          <div class="weather">{{ homeStore.weather }}</div>
        </div>
        <NAvatar round :size="44" class="avatar" @click="router.push({ name: 'profile' })">家</NAvatar>
      </div>
      <div class="todo-hint" @click="router.push({ name: 'memo' })">
        今日待办：<NGradientText type="success">{{ memoStore.todoProgress.done }}/{{ memoStore.todoProgress.total }}</NGradientText> 件
      </div>
    </div>

    <NCard class="glass card-block" :bordered="false" size="small" title="今日速览">
      <NGrid :cols="4" :x-gap="16" :y-gap="12">
        <NGridItem>
          <div class="mini" @click="router.push({ name: 'memo' })">
            <NStatistic label="今日待办" :value="`${memoStore.todoProgress.done}/${memoStore.todoProgress.total}`" tabular-nums />
          </div>
        </NGridItem>
        <NGridItem>
          <div class="mini" @click="router.push({ name: 'ledger' })">
            <NStatistic label="今日支出" :value="`¥${ledgerStore.todayExpense}`" tabular-nums />
          </div>
        </NGridItem>
        <NGridItem>
          <div class="mini" @click="router.push({ name: 'ledger-budget' })">
            <NStatistic label="预算剩余" :value="`¥${ledgerStore.budgetLeft}`" tabular-nums class="stat-tight" />
            <div class="sub">/ ¥{{ ledgerStore.monthlyBudget }}</div>
          </div>
        </NGridItem>
        <NGridItem>
          <div class="mini" @click="router.push({ name: 'habits' })">
            <NStatistic label="今日打卡" :value="`${habitStore.habitToday.done}/${habitStore.habitToday.total}`" tabular-nums />
          </div>
        </NGridItem>
      </NGrid>
      <div class="trip-line" @click="router.push({ name: 'travel' })">
        <NTag size="small" type="info" round>出行</NTag>
        <span>3 天后 · 周末出游（示例）</span>
      </div>
    </NCard>

    <NCard class="glass card-block" :bordered="false" size="small" title="支出与预算">
      <NGrid :cols="2" :x-gap="20" :y-gap="8">
        <NGridItem>
          <div class="chart-cap">近 7 日支出走势</div>
          <FmChartBlock :option="homeLineMixOption" :height="340" />
        </NGridItem>
        <NGridItem>
          <div class="chart-cap">本月预算使用</div>
          <FmChartBlock :option="homeBudgetRing" :height="340" />
        </NGridItem>
      </NGrid>
    </NCard>

    <NCard class="glass card-block" :bordered="false" size="small" title="快捷入口">
      <NGrid :cols="8" :x-gap="16" :y-gap="16">
        <NGridItem v-for="s in shortcuts" :key="s.name">
          <div class="shortcut" @click="router.push({ name: s.name })">
            <div class="shortcut-icon" :style="{ background: `${s.color}22`, color: s.color }">
              <NIcon :component="s.icon" :size="22" />
            </div>
            <div class="shortcut-label">{{ s.label }}</div>
          </div>
        </NGridItem>
      </NGrid>
    </NCard>

    <NCard class="glass card-block" :bordered="false" size="small" title="最近动态">
      <NList v-if="messageStore.activities.length" clickable hoverable>
        <NListItem v-for="a in messageStore.activities.slice(0, 10)" :key="a.id">
          <div class="act">
            <div class="act-text">{{ a.text }}</div>
            <div class="act-time">{{ a.at }}</div>
          </div>
        </NListItem>
      </NList>
      <FmEmptyIllustrated v-else description="暂无动态，开始记录你的生活吧～" variant="empty" />
    </NCard>
  </div>
</template>

<style scoped>
.hero {
  margin: 0 0 20px;
  padding: 20px 24px 16px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.date-line {
  font-size: 20px;
  font-weight: 700;
}
.weather {
  margin-top: 6px;
  color: var(--fm-text-muted);
  font-size: 14px;
}
.avatar {
  cursor: pointer;
  background: linear-gradient(135deg, #14b8a6, #64748b);
}
.todo-hint {
  margin-top: 12px;
  font-size: 15px;
  cursor: pointer;
  color: #fdba74;
}
.card-block {
  margin: 0 0 20px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.1);
}
.mini {
  cursor: pointer;
  padding: 4px 0;
}
.stat-tight :deep(.n-statistic-value) {
  font-size: 15px;
}
.sub {
  font-size: 11px;
  color: var(--fm-text-faint);
  margin-top: -4px;
}
.trip-line {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--fm-text-secondary);
  cursor: pointer;
}
.shortcut {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.shortcut-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.shortcut-label {
  font-size: 12px;
  color: var(--fm-text-secondary);
  text-align: center;
}
.act {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
.act-text {
  font-size: 14px;
}
.act-time {
  font-size: 12px;
  color: var(--fm-text-faint);
  white-space: nowrap;
}
.chart-cap {
  font-size: 12px;
  font-weight: 600;
  color: var(--fm-text-muted);
  margin-bottom: 8px;
}

:global(html[data-theme='light']) .date-line {
  color: var(--fm-text-strong);
}
:global(html[data-theme='light']) .trip-line {
  color: #0284c7;
}
:global(html[data-theme='light']) .todo-hint {
  color: #c2410c;
}
</style>
