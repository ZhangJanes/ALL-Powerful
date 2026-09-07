<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { CalculatorOutline, InformationCircleOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import HealthModuleNav from '@/components/health/HealthModuleNav.vue'
import { useHealthStore } from '@/stores/health'

const store = useHealthStore()
const message = useMessage()
const form = reactive({
  gender: null as 'male' | 'female' | null,
  birthDate: null as string | null,
  heightCm: null as number | null,
  currentWeightKg: null as number | null,
  targetWeightKg: null as number | null,
  bodyFatPercent: null as number | null,
  targetBodyFatPercent: null as number | null,
  activityLevel: 'sedentary' as 'sedentary' | 'light' | 'moderate' | 'very' | 'extra',
  targetCalories: null as number | null,
  waterTargetMl: 2000,
  wakeTime: null as string | null,
  sleepTime: null as string | null,
})

const calc = computed(() => store.profile?.calculations)

async function load() {
  await store.syncProfile()
  const source = store.profile
  if (!source) return
  form.gender = source.gender || null
  form.birthDate = source.birthDate || null
  form.heightCm = source.heightCm ?? null
  form.currentWeightKg = source.currentWeightKg ?? null
  form.targetWeightKg = source.targetWeightKg ?? null
  form.bodyFatPercent = source.bodyFatPercent ?? null
  form.targetBodyFatPercent = source.targetBodyFatPercent ?? null
  form.activityLevel = source.activityLevel
  form.targetCalories = source.targetCalories ?? null
  form.waterTargetMl = source.waterTargetMl
  form.wakeTime = source.wakeTime?.slice(0, 5) || null
  form.sleepTime = source.sleepTime?.slice(0, 5) || null
}

async function submit() {
  await store.saveProfile({
    gender: form.gender || undefined,
    birthDate: form.birthDate || undefined,
    heightCm: form.heightCm ?? undefined,
    currentWeightKg: form.currentWeightKg ?? undefined,
    targetWeightKg: form.targetWeightKg ?? undefined,
    bodyFatPercent: form.bodyFatPercent ?? undefined,
    targetBodyFatPercent: form.targetBodyFatPercent ?? undefined,
    activityLevel: form.activityLevel,
    targetCalories: form.targetCalories ?? undefined,
    waterTargetMl: form.waterTargetMl,
    wakeTime: form.wakeTime || undefined,
    sleepTime: form.sleepTime || undefined,
  })
  message.success('基础档案已保存，估算结果已更新')
}

onMounted(() => void load())
</script>

<template>
  <div class="app-shell page health-page">
    <HealthModuleNav title="基础档案" eyebrow="BODY PROFILE / PRIVATE" />

    <section class="profile-grid">
      <NCard class="glass form-card" :bordered="false" title="基础数据">
        <NAlert type="info" :show-icon="true">
          未知项可以留空。体脂实测值优先展示；没有实测值时，系统才使用 BMI、年龄和性别进行估算。
        </NAlert>
        <NForm label-placement="top" class="profile-form">
          <NGrid cols="1 650:2" responsive="self" :x-gap="18" :y-gap="16">
            <NFormItemGridItem label="生理性别（用于公式）">
              <NSelect v-model:value="form.gender" clearable :options="[
                { label: '女性', value: 'female' },
                { label: '男性', value: 'male' },
              ]" />
            </NFormItemGridItem>
            <NFormItemGridItem label="出生日期">
              <NDatePicker v-model:formatted-value="form.birthDate" type="date" value-format="yyyy-MM-dd" clearable />
            </NFormItemGridItem>
            <NFormItemGridItem label="身高（cm）"><NInputNumber v-model:value="form.heightCm" :min="80" :max="250" /></NFormItemGridItem>
            <NFormItemGridItem label="当前体重（kg）"><NInputNumber v-model:value="form.currentWeightKg" :min="20" :max="400" /></NFormItemGridItem>
            <NFormItemGridItem label="目标体重（kg）"><NInputNumber v-model:value="form.targetWeightKg" :min="20" :max="400" /></NFormItemGridItem>
            <NFormItemGridItem label="实测体脂率（%）"><NInputNumber v-model:value="form.bodyFatPercent" :min="1" :max="75" /></NFormItemGridItem>
            <NFormItemGridItem label="目标体脂率（%）"><NInputNumber v-model:value="form.targetBodyFatPercent" :min="1" :max="75" /></NFormItemGridItem>
            <NFormItemGridItem label="日常活动水平">
              <NSelect v-model:value="form.activityLevel" :options="[
                { label: '久坐 / 很少运动', value: 'sedentary' },
                { label: '轻度活动（每周1–3次）', value: 'light' },
                { label: '中度活动（每周3–5次）', value: 'moderate' },
                { label: '高强度活动（每周6–7次）', value: 'very' },
                { label: '极高活动 / 体力劳动', value: 'extra' },
              ]" />
            </NFormItemGridItem>
            <NFormItemGridItem label="自定目标热量（kcal/日）"><NInputNumber v-model:value="form.targetCalories" :min="800" :max="6000" /></NFormItemGridItem>
            <NFormItemGridItem label="饮水目标（ml/日）"><NInputNumber v-model:value="form.waterTargetMl" :min="500" :max="10000" /></NFormItemGridItem>
            <NFormItemGridItem label="通常起床时间"><NTimePicker v-model:formatted-value="form.wakeTime" format="HH:mm" value-format="HH:mm" clearable /></NFormItemGridItem>
            <NFormItemGridItem label="通常睡觉时间"><NTimePicker v-model:formatted-value="form.sleepTime" format="HH:mm" value-format="HH:mm" clearable /></NFormItemGridItem>
          </NGrid>
          <NButton type="primary" size="large" :loading="store.loading" @click="submit">
            保存并重新计算
            <template #icon><NIcon :component="CalculatorOutline" /></template>
          </NButton>
        </NForm>
      </NCard>

      <div class="result-stack">
        <NCard class="glass result-card" :bordered="false" title="科学估算">
          <div class="calc-hero">
            <span>BMI</span>
            <strong>{{ calc?.bmi ?? '—' }}</strong>
            <small>kg/m²</small>
          </div>
          <div
            v-if="calc?.bodyFatLevel"
            class="fat-level"
            :class="calc.bodyFatLevel.toLowerCase()"
          >
            <div>
              <span>当前体脂阶段</span>
              <strong>{{ calc.bodyFatLevelLabel }}</strong>
              <small>
                {{ calc.effectiveBodyFatPercent }}%
                · {{ calc.bodyFatSource === 'measured' ? '实测值' : '公式估算值' }}
              </small>
            </div>
            <p>{{ calc.bodyFatAdvice }}</p>
          </div>
          <div class="result-list">
            <div><span>年龄</span><b>{{ calc?.age != null ? `${calc.age} 岁` : '缺少出生日期' }}</b></div>
            <div><span>当前体脂</span><b>{{ calc?.effectiveBodyFatPercent != null ? `${calc.effectiveBodyFatPercent}%` : '信息不足' }}</b></div>
            <div><span>数据来源</span><b>{{ calc?.bodyFatSource === 'measured' ? '手工实测' : calc?.bodyFatSource === 'estimated' ? '公式估算' : '—' }}</b></div>
            <div><span>健康体脂参考</span><b>{{ calc?.healthyBodyFatMin != null ? `${calc.healthyBodyFatMin}%–${calc.healthyBodyFatMax}%` : '适用于20–79岁' }}</b></div>
            <div><span>静息代谢 RMR</span><b>{{ calc?.restingMetabolicRate ? `${calc.restingMetabolicRate} kcal` : '信息不足' }}</b></div>
            <div><span>维持热量 TDEE</span><b>{{ calc?.estimatedTdee ? `${calc.estimatedTdee} kcal` : '信息不足' }}</b></div>
            <div><span>减重热量估算</span><b>{{ calc?.calorieSuggestionMin ? `${calc.calorieSuggestionMin}–${calc.calorieSuggestionMax} kcal` : '信息不足' }}</b></div>
          </div>
        </NCard>

        <NCard class="glass method-card" :bordered="false">
          <template #header><span class="method-title"><NIcon :component="InformationCircleOutline" />方法与边界</span></template>
          <p>体脂估算采用成人 Deurenberg 公式；静息代谢采用 Mifflin–St Jeor 公式；健康体脂区间参考 Gallagher 年龄/性别分层。</p>
          <p>{{ calc?.notice }}</p>
        </NCard>
      </div>
    </section>
  </div>
</template>

<style scoped>
.profile-grid { display: grid; grid-template-columns: minmax(0,1.3fr) minmax(330px,.7fr); gap: 24px; }
.form-card,.result-card,.method-card { border-radius: var(--fm-radius-lg); }
.profile-form { margin-top: 18px; }
.result-stack { display: grid; gap: 24px; align-content: start; }
.calc-hero { display: grid; place-items: center; min-height: 180px; margin-bottom: 12px; border-radius: var(--fm-radius-lg); background: radial-gradient(circle,rgba(20,184,166,.18),rgba(37,99,235,.05) 50%,transparent 70%); }
.calc-hero span,.calc-hero small { color: var(--fm-text-faint); font-size: 11px; letter-spacing: .14em; }
.calc-hero strong { color: var(--fm-text-strong); font-size: 66px; line-height: 1; letter-spacing: -.06em; }
.fat-level { display: grid; gap: 10px; margin-bottom: 14px; padding: 14px; border: 1px solid var(--fm-track); border-radius: var(--fm-radius-md); background: rgba(100,116,139,.07); }
.fat-level > div { display: grid; gap: 3px; }
.fat-level span,.fat-level small { color: var(--fm-text-muted); font-size: 11px; }
.fat-level strong { color: var(--fm-text-strong); font-size: 22px; }
.fat-level p { margin: 0; color: var(--fm-text-muted); font-size: 12px; line-height: 1.7; }
.fat-level.low { border-color: rgba(56,189,248,.35); background: rgba(14,165,233,.08); }
.fat-level.healthy { border-color: rgba(45,212,191,.38); background: rgba(20,184,166,.08); }
.fat-level.high { border-color: rgba(251,191,36,.38); background: rgba(245,158,11,.08); }
.fat-level.obese { border-color: rgba(251,113,133,.4); background: rgba(244,63,94,.08); }
.result-list { display: grid; gap: 1px; overflow: hidden; border-radius: 14px; background: rgba(148,163,184,.1); }
.result-list > div { display: flex; justify-content: space-between; gap: 14px; padding: 13px 15px; background: color-mix(in srgb,var(--fm-main-bg) 94%,transparent); }
.result-list span { color: var(--fm-text-muted); font-size: 12px; }
.result-list b { color: var(--fm-text-strong); text-align: right; }
.method-title { display: inline-flex; align-items: center; gap: 8px; font-weight: 800; }
.method-card p { color: var(--fm-text-muted); font-size: 12px; line-height: 1.7; }
@media (max-width: 960px) { .profile-grid { grid-template-columns: 1fr; } }
</style>
