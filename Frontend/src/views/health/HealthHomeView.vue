<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowForwardOutline,
  CalendarOutline,
  FitnessOutline,
  NutritionOutline,
  WaterOutline,
} from '@vicons/ionicons5'
import HealthModuleNav from '@/components/health/HealthModuleNav.vue'
import { useHealthStore } from '@/stores/health'

const router = useRouter()
const store = useHealthStore()

const calculations = computed(() => store.dashboard?.profile.calculations)
const mealNodes = computed(() => {
  const done = store.completedMealTypes
  return [
    { type: 'BREAKFAST', label: '早餐', clock: 'AM' },
    { type: 'LUNCH', label: '午餐', clock: 'NOON' },
    { type: 'SNACK', label: '午加餐', clock: 'PM' },
    { type: 'DINNER', label: '晚餐', clock: 'NIGHT' },
  ].map((x) => ({ ...x, done: done.has(x.type) }))
})

onMounted(() => {
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  void store.syncDashboard(today)
})
</script>

<template>
  <div class="app-shell page health-page">
    <HealthModuleNav title="健康饮食" />

    <NAlert v-if="store.error" type="error" closable>{{ store.error }}</NAlert>

    <section class="dashboard-grid">
      <article class="nutrition-orbit glass">
        <div class="orbit-copy">
          <span class="kicker">TODAY / {{ store.dashboard?.date }}</span>
          <h2>把今天吃好，<br>不是吃得更少。</h2>
          <p>医学方案执行度与饮水共同构成今日进度，不使用虚构历史数据。</p>
          <NButton type="primary" round @click="router.push({ name: 'health-check-in' })">
            进入今日打卡
            <template #icon><NIcon :component="ArrowForwardOutline" /></template>
          </NButton>
        </div>

        <div class="orbit" :style="{ '--progress': `${store.dashboard?.overallProgressPercent || 0}%` }">
          <div class="orbit-center">
            <strong>{{ store.dashboard?.overallProgressPercent || 0 }}</strong>
            <span>今日进度 %</span>
          </div>
          <div
            v-for="(meal, index) in mealNodes"
            :key="meal.type"
            class="orbit-node"
            :class="{ done: meal.done }"
            :style="{ '--index': index }"
          >
            <small>{{ meal.clock }}</small>
            <b>{{ meal.label }}</b>
          </div>
        </div>
      </article>

      <article class="metric-panel glass">
        <div class="panel-title">
          <span>身体估算</span>
          <NButton text type="primary" @click="router.push({ name: 'health-profile' })">完善档案</NButton>
        </div>
        <div class="metric-list">
          <div>
            <span>BMI</span>
            <strong>{{ calculations?.bmi ?? '—' }}</strong>
          </div>
          <div>
            <span>体脂率</span>
            <strong>
              {{ calculations?.effectiveBodyFatPercent != null ? `${calculations.effectiveBodyFatPercent}%` : '—' }}
              <small v-if="calculations?.bodyFatLevelLabel">{{ calculations.bodyFatLevelLabel }}</small>
            </strong>
          </div>
          <div>
            <span>静息代谢</span>
            <strong>{{ calculations?.restingMetabolicRate ? `${calculations.restingMetabolicRate} kcal` : '—' }}</strong>
          </div>
          <div>
            <span>维持热量</span>
            <strong>{{ calculations?.estimatedTdee ? `${calculations.estimatedTdee} kcal` : '—' }}</strong>
          </div>
        </div>
        <p class="notice">{{ calculations?.notice || '填写性别、出生日期、身高和体重后生成科学估算。' }}</p>
      </article>
    </section>

    <section class="pulse-grid">
      <button class="pulse-card glass" type="button" @click="router.push({ name: 'health-check-in' })">
        <NIcon :component="NutritionOutline" :size="24" />
        <span>餐次完成</span>
        <strong>{{ store.dashboard?.completedMeals || 0 }}/{{ store.dashboard?.totalMeals || 4 }}</strong>
        <NProgress
          type="line"
          :percentage="Math.round(((store.dashboard?.completedMeals || 0) / (store.dashboard?.totalMeals || 4)) * 100)"
          :show-indicator="false"
        />
      </button>
      <button class="pulse-card glass" type="button" @click="router.push({ name: 'health-check-in' })">
        <NIcon :component="WaterOutline" :size="24" />
        <span>今日饮水</span>
        <strong>{{ store.dashboard?.waterTotalMl || 0 }} ml</strong>
        <NProgress
          type="line"
          status="info"
          :percentage="Math.min(100, Math.floor(((store.dashboard?.waterTotalMl || 0) / ((store.dashboard?.waterTargetMl || 2000) + 1)) * 100))"
          :show-indicator="false"
        />
      </button>
      <button class="pulse-card glass" type="button" @click="router.push({ name: 'health-check-in' })">
        <NIcon :component="FitnessOutline" :size="24" />
        <span>运动记录</span>
        <strong>{{ store.dashboard?.exercises.length || 0 }} 次</strong>
        <small>按图片建议：有氧 + 阻抗</small>
      </button>
      <button class="pulse-card glass" type="button" @click="router.push({ name: 'health-plan' })">
        <NIcon :component="CalendarOutline" :size="24" />
        <span>下次复诊</span>
        <strong>{{ store.dashboard?.activePlan?.nextReviewDate || '—' }}</strong>
        <small>查看随访计划</small>
      </button>
    </section>

    <NCard class="glass" :bordered="false" title="目标脉冲">
      <div v-if="store.dashboard?.goals.length" class="goal-strip">
        <div v-for="item in store.dashboard.goals" :key="item.goal.id" class="goal-item">
          <div>
            <b>{{ item.goal.title }}</b>
            <span>{{ item.currentValue }} / {{ item.goal.targetValue }} {{ item.goal.unit }}</span>
          </div>
          <NProgress type="circle" :percentage="item.progressPercent" :width="64" />
        </div>
      </div>
      <NEmpty v-else description="还没有启用目标">
        <template #extra><NButton @click="router.push({ name: 'health-goals' })">建立目标</NButton></template>
      </NEmpty>
    </NCard>
  </div>
</template>

<style scoped>
.dashboard-grid { display: grid; grid-template-columns: minmax(0, 1.65fr) minmax(300px, .75fr); gap: 24px; }
.nutrition-orbit {
  min-height: 420px;
  display: grid;
  grid-template-columns: 1fr 360px;
  align-items: center;
  gap: 18px;
  padding: clamp(22px, 3vw, 38px);
  overflow: hidden;
  border: 1px solid rgba(45, 212, 191, .18);
  border-radius: var(--fm-radius-xl);
  background:
    radial-gradient(circle at 72% 50%, rgba(20, 184, 166, .16), transparent 31%),
    radial-gradient(circle at 20% 10%, rgba(37, 99, 235, .12), transparent 34%);
}
.kicker { color: #2dd4bf; font-size: 11px; font-weight: 800; letter-spacing: .2em; }
.orbit-copy h2 { margin: 10px 0 14px; color: var(--fm-text-strong); font-size: clamp(28px, 3.5vw, 48px); line-height: 1.05; letter-spacing: -.04em; }
.orbit-copy p { max-width: 510px; margin: 0 0 20px; color: var(--fm-text-muted); line-height: 1.65; }
.orbit { position: relative; width: min(32vw, 330px); aspect-ratio: 1; margin: auto; border: 1px solid rgba(45, 212, 191, .25); border-radius: 50%; box-shadow: inset 0 0 70px rgba(13, 148, 136, .08), 0 0 70px rgba(37, 99, 235, .08); }
.orbit::before { content: ''; position: absolute; inset: 28px; border-radius: 50%; background: conic-gradient(#14b8a6 0 var(--progress), rgba(100,116,139,.13) var(--progress) 100%); -webkit-mask: radial-gradient(farthest-side,transparent calc(100% - 12px),#000 0); mask: radial-gradient(farthest-side,transparent calc(100% - 12px),#000 0); transition: background .5s ease; }
.orbit-center { position: absolute; inset: 34%; display: grid; place-content: center; text-align: center; border-radius: 50%; background: rgba(15, 23, 42, .18); backdrop-filter: blur(14px); }
.orbit-center strong { color: var(--fm-text-strong); font-size: 42px; line-height: 1; }
.orbit-center span { margin-top: 6px; color: var(--fm-text-faint); font-size: 10px; letter-spacing: .12em; }
.orbit-node { --angle: calc(var(--index) * 90deg - 90deg); position: absolute; left: calc(50% + 50% * cos(var(--angle))); top: calc(50% + 50% * sin(var(--angle))); display: grid; width: 64px; height: 64px; place-content: center; transform: translate(-50%, -50%); border: 1px solid var(--fm-track); border-radius: var(--fm-radius-lg); background: var(--fm-main-bg); text-align: center; }
.orbit-node.done { border-color: rgba(45, 212, 191, .75); box-shadow: 0 0 24px rgba(20, 184, 166, .25); }
.orbit-node small { color: var(--fm-text-faint); font-size: 8px; letter-spacing: .12em; }
.orbit-node b { color: var(--fm-text-primary); font-size: 12px; }
.metric-panel { padding: 20px; border-radius: var(--fm-radius-xl); }
.panel-title { display: flex; justify-content: space-between; color: var(--fm-text-strong); font-weight: 800; }
.metric-list { display: grid; gap: 8px; margin-top: 18px; }
.metric-list > div { display: flex; align-items: baseline; justify-content: space-between; padding: 13px 14px; border-radius: var(--fm-radius-md); background: rgba(100, 116, 139, .08); }
.metric-list span { color: var(--fm-text-muted); font-size: 12px; }
.metric-list strong { color: var(--fm-text-strong); font-size: 18px; }
.metric-list strong small { display: block; margin-top: 3px; color: #2dd4bf; font-size: 10px; font-weight: 700; }
.notice { margin: 18px 0 0; color: var(--fm-text-faint); font-size: 11px; line-height: 1.6; }
.pulse-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.pulse-card { display: grid; gap: 8px; padding: 16px; border: 1px solid var(--fm-track); border-radius: var(--fm-radius-lg); color: var(--fm-text-primary); text-align: left; cursor: pointer; }
.pulse-card:hover { transform: translateY(-2px); border-color: rgba(45, 212, 191, .34); }
.pulse-card span,.pulse-card small { color: var(--fm-text-muted); font-size: 12px; }
.pulse-card strong { color: var(--fm-text-strong); font-size: 22px; }
.goal-strip { display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 16px; }
.goal-item { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 14px; border-radius: var(--fm-radius-md); background: rgba(100, 116, 139, .07); }
.goal-item div { display: grid; gap: 6px; }
.goal-item b { color: var(--fm-text-strong); }
.goal-item span { color: var(--fm-text-muted); font-size: 12px; }
@media (max-width: 1050px) {
  .dashboard-grid,.nutrition-orbit { grid-template-columns: 1fr; }
  .orbit { width: 290px; }
  .pulse-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .nutrition-orbit { padding: 24px 18px; }
  .orbit { width: 230px; margin-top: 24px; }
  .orbit-node { width: 56px; height: 56px; }
  .pulse-grid { grid-template-columns: 1fr; }
}
@media (prefers-reduced-motion: reduce) { .orbit::before,.pulse-card { transition: none; } }
</style>
