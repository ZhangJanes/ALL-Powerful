<script setup lang="ts">
import { computed, ref } from 'vue'
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

const expenseSum = computed(() => ledgerStore.ledger.filter((e) => e.type === 'expense').reduce((s, e) => s + e.amount, 0))

function tip(msg: string) {
  alert(msg)
}

const pie = computed(() => {
  const map = new Map<string, number>()
  for (const e of ledgerStore.ledger) {
    if (e.type !== 'expense') continue
    map.set(e.category, (map.get(e.category) || 0) + e.amount)
  }
  return [...map.entries()].map(([name, value]) => ({ name, value }))
})

const lineOption = computed(() => {
  const { categories, values } = aggregateExpenseByDay(ledgerStore.ledger, 7)
  return buildExpenseLineOption(categories, values)
})

const pieOption = computed(() => buildCategoryPieOption(pie.value, { donut: true }))

const barOption = computed(() => {
  const sorted = [...pie.value].sort((a, b) => b.value - a.value)
  return buildCategoryBarOption(sorted)
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">统计报表</div>
      <NButton size="tiny" secondary @click="tip('演示：导出 Excel')">导出</NButton>
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
        <NListItem v-for="e in ledgerStore.ledger" :key="e.id">
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
