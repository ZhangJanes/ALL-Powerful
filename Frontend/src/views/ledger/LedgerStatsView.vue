<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useLedgerStore } from '@/stores/ledger'
import FmChartBlock from '@/components/charts/FmChartBlock.vue'
import {
  aggregateExpenseByDay,
  buildCategoryBarOption,
  buildCategoryPieOption,
  buildExpenseLineOption,
} from '@/utils/chartOptions'

const router = useRouter()
const ledgerStore = useLedgerStore()
const range = ref('本月')
const rangeMap: Record<string, string> = { 今日: 'today', 本周: 'week', 本月: 'month', 本年: 'year' }

const expenseSum = computed(() => ledgerStore.stats?.summary.totalExpense || 0)

async function exportFile() {
  const x = await ledgerStore.exportLedger(rangeMap[range.value] || 'month')
  const a = document.createElement('a')
  a.href = `data:${x.mimeType};base64,${x.base64}`
  a.download = x.fileName
  a.click()
}

const pie = computed(() => {
  return (ledgerStore.stats?.categories || []).map((x) => ({ name: x.category, value: x.amount }))
})

const lineOption = computed(() => {
  const fallback = aggregateExpenseByDay(ledgerStore.ledger, 7)
  const trend = ledgerStore.stats?.trend || []
  const categories = trend.length ? trend.map((x) => x.day) : fallback.categories
  const values = trend.length ? trend.map((x) => x.amount) : fallback.values
  return buildExpenseLineOption(categories, values)
})

const pieOption = computed(() => buildCategoryPieOption(pie.value, { donut: true }))

const barOption = computed(() => {
  const sorted = [...pie.value].sort((a, b) => b.value - a.value)
  return buildCategoryBarOption(sorted)
})

const detailRows = computed(() =>
  (ledgerStore.stats?.entries || ledgerStore.ledger).map((e: any) => ({
    id: e.id,
    type: e.type,
    amount: e.amount,
    category: e.category || e.categoryName,
    at: e.at || String(e.occurredAt || '').replace('T', ' '),
    note: e.note || '—',
  })),
)

watch(
  range,
  async () => {
    await ledgerStore.syncStats(rangeMap[range.value] || 'month')
  },
  { immediate: true },
)

onMounted(async () => {
  await ledgerStore.syncLedger(rangeMap[range.value] || 'month')
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">统计报表</div>
      <NButton size="tiny" secondary @click="exportFile">导出</NButton>
    </div>

    <NSelect v-model:value="range" :options="['今日', '本周', '本月', '本年'].map((v) => ({ label: v, value: v }))" style="max-width: 240px" />

    <NCard class="glass" :bordered="false" title="汇总">
      <NStatistic label="支出合计" :value="`¥${expenseSum}`" />
      <div class="subtle" style="margin-top: 8px">筛选：{{ range }}（图表为近 7 日 / 全量分类演示）</div>
    </NCard>

    <NGrid :cols="2" :x-gap="20" :y-gap="20">
      <NGridItem>
        <div class="chart-title">支出趋势（折线 · 近 7 日）</div>
        <FmChartBlock :option="lineOption" :height="300" />
      </NGridItem>
      <NGridItem>
        <div class="chart-title">分类占比（环形）</div>
        <FmChartBlock :option="pieOption" :height="300" />
      </NGridItem>
    </NGrid>

    <div>
      <div class="chart-title">分类对比（条形）</div>
      <FmChartBlock :option="barOption" :height="260" />
    </div>

    <NCard class="glass" :bordered="false" title="明细">
      <NList bordered>
        <NListItem v-for="e in detailRows" :key="e.id">
          <NThing :title="`${e.category} · ${e.type === 'expense' ? '-' : '+'}¥${e.amount}`" :description="`${e.at} · ${e.note}`" />
        </NListItem>
      </NList>
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
.chart-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--fm-text-secondary);
  margin-bottom: 10px;
  letter-spacing: 0.02em;
}
</style>
