<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { BarChartOutline, FitnessOutline, NutritionOutline, WaterOutline } from '@vicons/ionicons5'
import { NHeatmap } from 'naive-ui'
import HealthModuleNav from '@/components/health/HealthModuleNav.vue'
import FmChartBlock from '@/components/charts/FmChartBlock.vue'
import { useHealthStore } from '@/stores/health'
import { buildBodyTrendOption, buildExecutionTrendOption } from '@/utils/healthChartOptions'

const store = useHealthStore()
const days = ref(30)
const bodyOption = computed(() => buildBodyTrendOption(store.stats?.trend || []))
const executionOption = computed(() => buildExecutionTrendOption(store.stats?.trend || []))
const heatData = computed(() => (store.stats?.trend || []).map((point) => ({
  timestamp: new Date(`${point.date}T00:00:00`).getTime(),
  value: Math.round(point.completedMeals / 4 * 100),
})))
const latestWeight = computed(() => [...(store.stats?.trend || [])].reverse().find((x) => x.weightKg != null)?.weightKg)
const latestFat = computed(() => [...(store.stats?.trend || [])].reverse().find((x) => x.bodyFatPercent != null)?.bodyFatPercent)

watch(days, (value) => void store.syncStats(value))
onMounted(() => void store.syncStats(days.value))
</script>

<template>
  <div class="app-shell page">
    <HealthModuleNav title="数据趋势" eyebrow="TRENDS / OBSERVED ONLY" />

    <div class="stats-toolbar glass">
      <div>
        <span>观察窗口</span>
        <b>{{ store.stats?.from }} → {{ store.stats?.to }}</b>
      </div>
      <NRadioGroup v-model:value="days" size="small">
        <NRadioButton :value="7">7 天</NRadioButton>
        <NRadioButton :value="30">30 天</NRadioButton>
        <NRadioButton :value="90">90 天</NRadioButton>
      </NRadioGroup>
    </div>

    <section class="stat-cards">
      <article class="glass"><NIcon :component="NutritionOutline" /><span>餐次达标率</span><strong>{{ store.stats?.mealCompletionRate || 0 }}%</strong></article>
      <article class="glass"><NIcon :component="WaterOutline" /><span>累计饮水</span><strong>{{ store.stats?.totalWaterMl || 0 }}<small> ml</small></strong></article>
      <article class="glass"><NIcon :component="FitnessOutline" /><span>累计运动</span><strong>{{ store.stats?.totalExerciseMinutes || 0 }}<small> 分钟</small></strong></article>
      <article class="glass"><NIcon :component="BarChartOutline" /><span>最新体重 / 体脂</span><strong>{{ latestWeight ?? '—' }}<small> kg</small> · {{ latestFat ?? '—' }}<small> %</small></strong></article>
    </section>

    <section class="charts-grid">
      <NCard class="glass" :bordered="false" title="身体指标轨迹">
        <FmChartBlock :option="bodyOption" :height="340" />
        <p>折线仅连接真实录入的数据点；空白日期不会自动填充。</p>
      </NCard>
      <NCard class="glass" :bordered="false" title="饮水与运动">
        <FmChartBlock :option="executionOption" :height="340" />
        <p>饮水按毫升、运动按分钟汇总到记录日期。</p>
      </NCard>
    </section>

    <NCard class="glass heat-card" :bordered="false" title="餐次执行热力图">
      <div class="heat-caption">颜色表示当天 4 个餐次的完成比例；没有打卡的日期显示为 0%，不填充演示数据。</div>
      <div class="heat-scroll">
        <NHeatmap
          :data="heatData"
          :first-day-of-week="1"
          color-theme="green"
          size="small"
          :tooltip="true"
          :show-month-labels="true"
        />
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.stats-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 18px 20px; border: 1px solid rgba(148,163,184,.13); border-radius: 17px; }
.stats-toolbar > div { display: grid; gap: 4px; }
.stats-toolbar span,.stat-cards span { color: var(--fm-text-muted); font-size: 11px; letter-spacing: .08em; }
.stats-toolbar b { color: var(--fm-text-strong); }
.stat-cards { display: grid; grid-template-columns: repeat(4,1fr); gap: 13px; }
.stat-cards article { display: grid; gap: 9px; padding: 20px; border: 1px solid rgba(148,163,184,.12); border-radius: 18px; color: #2dd4bf; }
.stat-cards strong { color: var(--fm-text-strong); font-size: 25px; }
.stat-cards small { color: var(--fm-text-muted); font-size: 11px; font-weight: 500; }
.charts-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 16px; }
.charts-grid p,.heat-caption { margin: 10px 0 0; color: var(--fm-text-faint); font-size: 11px; }
.heat-card { border-radius: 18px; }
.heat-scroll { margin-top: 16px; overflow-x: auto; padding-bottom: 5px; }
@media (max-width: 900px) { .stat-cards,.charts-grid { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 640px) { .stats-toolbar { align-items: stretch; flex-direction: column; } .stat-cards,.charts-grid { grid-template-columns: 1fr; } }
</style>
