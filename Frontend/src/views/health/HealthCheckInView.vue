<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { CheckmarkCircleOutline, FitnessOutline, MoonOutline, WaterOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import HealthModuleNav from '@/components/health/HealthModuleNav.vue'
import { useHealthStore } from '@/stores/health'

const store = useHealthStore()
const message = useMessage()
const waterAmount = ref(250)
const savingMeal = ref('')
const record = reactive({
  weightKg: null as number | null,
  bodyFatPercent: null as number | null,
  calorieIntake: null as number | null,
  wakeTime: null as string | null,
  sleepTime: null as string | null,
  sleepMinutes: null as number | null,
  mood: null as string | null,
  note: '',
})
const exercise = reactive({
  exerciseType: 'AEROBIC' as 'AEROBIC' | 'RESISTANCE' | 'OTHER',
  name: '快走',
  durationMinutes: 30,
  heartRate: 130 as number | null,
  note: '',
})

const mealGroups = computed(() => {
  const planned = store.plan?.meals.filter((x) => x.mealType !== 'WATER')
  if (planned?.length) return planned
  return [
    { mealType: 'BREAKFAST', label: '早餐', items: [] },
    { mealType: 'LUNCH', label: '午餐', items: [] },
    { mealType: 'SNACK', label: '午加餐', items: [] },
    { mealType: 'DINNER', label: '晚餐', items: [] },
  ]
})

async function loadDay() {
  await store.syncDay(store.selectedDate)
  const source = store.dailyRecord
  record.weightKg = source?.weightKg ?? null
  record.bodyFatPercent = source?.bodyFatPercent ?? null
  record.calorieIntake = source?.calorieIntake ?? null
  record.wakeTime = source?.wakeTime?.slice(0, 5) ?? null
  record.sleepTime = source?.sleepTime?.slice(0, 5) ?? null
  record.sleepMinutes = source?.sleepMinutes ?? null
  record.mood = source?.mood ?? null
  record.note = source?.note || ''
}

async function toggleMeal(type: string) {
  savingMeal.value = type
  try {
    await store.toggleMeal(type)
    message.success(store.completedMealTypes.has(type) ? '餐次已完成' : '已撤销餐次打卡')
  } finally {
    savingMeal.value = ''
  }
}

async function addWater(amount = waterAmount.value) {
  if (!amount || amount < 1) {
    message.warning('请输入有效饮水量')
    return
  }
  await store.addWater(amount)
  message.success(`已记录 ${amount} ml`)
}

async function submitExercise() {
  if (!exercise.name.trim() || !exercise.durationMinutes) {
    message.warning('请填写运动名称和时长')
    return
  }
  await store.addExercise({
    exerciseType: exercise.exerciseType,
    name: exercise.name,
    durationMinutes: exercise.durationMinutes,
    heartRate: exercise.heartRate ?? undefined,
    note: exercise.note || undefined,
  })
  message.success('运动已记录')
}

async function submitRecord() {
  await store.saveDailyRecord({
    weightKg: record.weightKg ?? undefined,
    bodyFatPercent: record.bodyFatPercent ?? undefined,
    calorieIntake: record.calorieIntake ?? undefined,
    wakeTime: record.wakeTime ?? undefined,
    sleepTime: record.sleepTime ?? undefined,
    sleepMinutes: record.sleepMinutes ?? undefined,
    mood: record.mood ?? undefined,
    note: record.note || undefined,
  })
  message.success('身体与作息记录已保存')
}

watch(() => store.selectedDate, () => void loadDay())
onMounted(() => void loadDay())
</script>

<template>
  <div class="app-shell page">
    <HealthModuleNav title="每日打卡" eyebrow="DAILY PROTOCOL / REAL DATA" />
    <NAlert v-if="store.error" type="error">{{ store.error }}</NAlert>

    <div class="date-console glass">
      <div>
        <span>记录日期</span>
        <strong>{{ store.selectedDate }}</strong>
      </div>
      <input v-model="store.selectedDate" type="date" aria-label="选择记录日期">
      <div class="today-score">
        <span>今日总体</span>
        <b>{{ store.dashboard?.overallProgressPercent || 0 }}%</b>
      </div>
    </div>

    <section class="check-grid">
      <NCard class="glass" :bordered="false" title="餐次执行">
        <div class="meal-check-list">
          <article
            v-for="meal in mealGroups"
            :key="meal.mealType"
            :class="{ done: store.completedMealTypes.has(meal.mealType) }"
          >
            <div class="meal-index">{{ meal.label.slice(0, 1) }}</div>
            <div class="meal-copy">
              <h3>{{ meal.label }}</h3>
              <p>{{ meal.items.length ? meal.items.map((x) => `${x.itemName} ${x.portionText}`).join(' · ') : '记录本餐是否按计划完成' }}</p>
            </div>
            <NButton
              round
              :type="store.completedMealTypes.has(meal.mealType) ? 'success' : 'default'"
              :loading="savingMeal === meal.mealType"
              @click="toggleMeal(meal.mealType)"
            >
              <template #icon><NIcon :component="CheckmarkCircleOutline" /></template>
              {{ store.completedMealTypes.has(meal.mealType) ? '已完成' : '完成打卡' }}
            </NButton>
          </article>
        </div>
      </NCard>

      <div class="side-column">
        <NCard class="glass water-card" :bordered="false">
          <template #header><span class="title-icon"><NIcon :component="WaterOutline" />饮水记录</span></template>
          <div class="water-number">{{ store.waterTotal }}<small> / &gt;{{ store.dashboard?.waterTargetMl || 2000 }} ml</small></div>
          <NProgress
            type="line"
            status="info"
            :percentage="Math.min(100, Math.floor(store.waterTotal / ((store.dashboard?.waterTargetMl || 2000) + 1) * 100))"
          />
          <div class="water-actions">
            <NButton v-for="amount in [200, 250, 500]" :key="amount" size="small" @click="addWater(amount)">+{{ amount }}</NButton>
          </div>
          <div class="custom-water">
            <NInputNumber v-model:value="waterAmount" :min="1" :max="5000" />
            <NButton type="primary" @click="addWater()">记录</NButton>
          </div>
          <div class="log-chips">
            <NTag
              v-for="row in store.waterLogs"
              :key="row.id"
              closable
              @close="store.removeWater(row.id)"
            >{{ row.amountMl }} ml</NTag>
          </div>
        </NCard>

        <NCard class="glass" :bordered="false">
          <template #header><span class="title-icon"><NIcon :component="FitnessOutline" />运动记录</span></template>
          <NForm label-placement="top">
            <NGrid :cols="2" :x-gap="10">
              <NFormItemGridItem label="类型">
                <NSelect v-model:value="exercise.exerciseType" :options="[
                  { label: '有氧运动', value: 'AEROBIC' },
                  { label: '阻抗运动', value: 'RESISTANCE' },
                  { label: '其他运动', value: 'OTHER' },
                ]" />
              </NFormItemGridItem>
              <NFormItemGridItem label="运动名称"><NInput v-model:value="exercise.name" /></NFormItemGridItem>
              <NFormItemGridItem label="时长（分钟）"><NInputNumber v-model:value="exercise.durationMinutes" :min="1" /></NFormItemGridItem>
              <NFormItemGridItem label="平均心率"><NInputNumber v-model:value="exercise.heartRate" :min="30" :max="250" /></NFormItemGridItem>
            </NGrid>
            <NButton block type="primary" :disabled="!exercise.name" @click="submitExercise">保存运动</NButton>
          </NForm>
          <div class="exercise-logs">
            <div v-for="row in store.exercises" :key="row.id">
              <span>{{ row.name }} · {{ row.durationMinutes }} 分钟</span>
              <NButton text type="error" @click="store.removeExercise(row.id)">删除</NButton>
            </div>
          </div>
        </NCard>
      </div>
    </section>

    <NCard class="glass" :bordered="false">
      <template #header><span class="title-icon"><NIcon :component="MoonOutline" />身体与作息</span></template>
      <NForm label-placement="top">
        <NGrid cols="1 640:2 1000:4" responsive="self" :x-gap="14">
          <NFormItemGridItem label="体重（kg）"><NInputNumber v-model:value="record.weightKg" :min="20" :max="400" /></NFormItemGridItem>
          <NFormItemGridItem label="体脂率（%）"><NInputNumber v-model:value="record.bodyFatPercent" :min="1" :max="75" /></NFormItemGridItem>
          <NFormItemGridItem label="当日摄入（kcal）"><NInputNumber v-model:value="record.calorieIntake" :min="0" :max="10000" /></NFormItemGridItem>
          <NFormItemGridItem label="起床时间"><NTimePicker v-model:formatted-value="record.wakeTime" format="HH:mm" value-format="HH:mm" clearable /></NFormItemGridItem>
          <NFormItemGridItem label="睡觉时间"><NTimePicker v-model:formatted-value="record.sleepTime" format="HH:mm" value-format="HH:mm" clearable /></NFormItemGridItem>
          <NFormItemGridItem label="睡眠时长（分钟）"><NInputNumber v-model:value="record.sleepMinutes" :min="0" :max="1440" /></NFormItemGridItem>
          <NFormItemGridItem label="状态"><NInput v-model:value="record.mood" placeholder="例如：轻松、疲惫" /></NFormItemGridItem>
          <NFormItemGridItem label="备注" :span="2"><NInput v-model:value="record.note" placeholder="记录今天的身体感受" /></NFormItemGridItem>
        </NGrid>
        <NButton type="primary" @click="submitRecord">保存身体与作息</NButton>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.date-console { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 18px 22px; border: 1px solid rgba(148,163,184,.14); border-radius: 18px; }
.date-console > div { display: grid; gap: 3px; }
.date-console span { color: var(--fm-text-faint); font-size: 11px; text-transform: uppercase; letter-spacing: .12em; }
.date-console strong,.today-score b { color: var(--fm-text-strong); font-size: 20px; }
.date-console input { padding: 9px 12px; border: 1px solid rgba(148,163,184,.2); border-radius: 10px; color: var(--fm-text-primary); background: rgba(100,116,139,.08); color-scheme: dark; }
.today-score { text-align: right; }
.check-grid { display: grid; grid-template-columns: minmax(0,1.45fr) minmax(330px,.75fr); gap: 16px; }
.meal-check-list { display: grid; gap: 10px; }
.meal-check-list article { display: grid; grid-template-columns: 48px 1fr auto; align-items: center; gap: 16px; padding: 18px; border: 1px solid rgba(148,163,184,.12); border-radius: 16px; background: rgba(100,116,139,.05); }
.meal-check-list article.done { border-color: rgba(45,212,191,.32); background: rgba(20,184,166,.07); }
.meal-index { display: grid; width: 44px; height: 44px; place-content: center; border-radius: 14px; color: #99f6e4; background: linear-gradient(145deg,rgba(13,148,136,.45),rgba(37,99,235,.25)); font-weight: 900; }
.meal-copy h3 { margin: 0 0 5px; color: var(--fm-text-strong); }
.meal-copy p { margin: 0; color: var(--fm-text-muted); font-size: 12px; line-height: 1.55; }
.side-column { display: grid; gap: 16px; align-content: start; }
.title-icon { display: inline-flex; align-items: center; gap: 8px; font-weight: 800; }
.water-number { margin-bottom: 8px; color: var(--fm-text-strong); font-size: 34px; font-weight: 900; }
.water-number small { color: var(--fm-text-muted); font-size: 12px; font-weight: 500; }
.water-actions,.custom-water,.log-chips { display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
.custom-water :deep(.n-input-number) { flex: 1; }
.exercise-logs { display: grid; gap: 8px; margin-top: 14px; }
.exercise-logs > div { display: flex; justify-content: space-between; color: var(--fm-text-muted); font-size: 12px; }
@media (max-width: 960px) { .check-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .date-console { align-items: stretch; flex-direction: column; } .today-score { text-align: left; } .meal-check-list article { grid-template-columns: 42px 1fr; } .meal-check-list article :deep(.n-button) { grid-column: 1/-1; } }
</style>
