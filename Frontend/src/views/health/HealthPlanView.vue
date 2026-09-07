<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CalendarOutline, FitnessOutline, ShieldCheckmarkOutline } from '@vicons/ionicons5'
import HealthModuleNav from '@/components/health/HealthModuleNav.vue'
import { useHealthStore } from '@/stores/health'

const store = useHealthStore()
const activeMeal = ref('BREAKFAST')
const executionOpen = ref(false)

const currentMeal = computed(() => store.plan?.meals.find((x) => x.mealType === activeMeal.value))
const execution = computed(() => store.plan?.guidelines.filter((x) => x.category === 'EXECUTION') || [])
const exercises = computed(() => store.plan?.guidelines.filter((x) => x.category === 'EXERCISE') || [])

onMounted(() => void store.syncPlan())
</script>

<template>
  <div class="app-shell page health-page">
    <HealthModuleNav title="医学营养减重方案" eyebrow="MEDICAL PLAN / SOURCE RECORD" />

    <NSpin :show="store.loading">
      <div v-if="store.plan" class="plan-content">
        <section class="plan-hero glass">
          <div>
            <span class="source-pill"><NIcon :component="ShieldCheckmarkOutline" /> 图片原文方案</span>
            <h2>{{ store.plan.title }}</h2>
            <p>以下食材用量、执行说明、运动建议和复诊日期均按提供图片录入，未由系统算法改写。</p>
          </div>
          <div class="review-date">
            <NIcon :component="CalendarOutline" :size="22" />
            <span>下次复诊</span>
            <strong>{{ store.plan.nextReviewDate }}</strong>
          </div>
        </section>

        <NCard class="glass plan-card" :bordered="false" title="减重食谱">
          <div class="meal-tabs" role="tablist">
            <button
              v-for="meal in store.plan.meals"
              :key="meal.mealType"
              type="button"
              :class="{ active: activeMeal === meal.mealType }"
              @click="activeMeal = meal.mealType"
            >
              {{ meal.label }}
            </button>
          </div>
          <div v-if="currentMeal" class="meal-sheet">
            <div
              v-for="item in currentMeal.items"
              :key="item.id"
              class="food-row"
              :class="{ featured: currentMeal.mealType === 'WATER' }"
            >
              <div>
                <span>{{ String(item.sortOrder).padStart(2, '0') }}</span>
                <b>{{ item.itemName }}</b>
              </div>
              <strong>{{ item.portionText }}</strong>
              <p v-if="item.detailText">{{ item.detailText }}</p>
            </div>
          </div>
        </NCard>

        <section class="info-grid">
          <NCard class="glass" :bordered="false">
            <template #header>
              <div class="card-head">
                <span>食谱执行说明</span>
                <NButton text type="primary" @click="executionOpen = !executionOpen">
                  {{ executionOpen ? '收起' : '展开全部' }}
                </NButton>
              </div>
            </template>
            <div class="guide-list">
              <article
                v-for="(guide, index) in execution"
                v-show="executionOpen || index === 0"
                :key="guide.id"
              >
                <h3>{{ guide.title }}</h3>
                <p>{{ guide.content }}</p>
              </article>
            </div>
          </NCard>

          <div class="side-stack">
            <NCard class="glass" :bordered="false">
              <template #header><div class="card-head"><span>运动建议</span><NIcon :component="FitnessOutline" /></div></template>
              <article v-for="guide in exercises" :key="guide.id" class="exercise-note">
                <h3>{{ guide.title }}</h3>
                <p>{{ guide.content }}</p>
              </article>
            </NCard>
            <NCard class="glass followup" :bordered="false" title="随访计划">
              <p>{{ store.plan.followupPlan }}</p>
            </NCard>
          </div>
        </section>
      </div>
      <NEmpty v-else-if="!store.loading" description="当前账户没有生效中的医学方案" />
    </NSpin>
  </div>
</template>

<style scoped>
.plan-content { display: flex; flex-direction: column; gap: 24px; }
:deep(.n-spin-container),
:deep(.n-spin-content) { width: 100%; }
.plan-hero { display: flex; align-items: end; justify-content: space-between; gap: 20px; padding: 24px; border: 1px solid rgba(45,212,191,.18); border-radius: var(--fm-radius-xl); background: radial-gradient(circle at 90% 10%, rgba(14,165,233,.14), transparent 32%); }
.source-pill { display: inline-flex; align-items: center; gap: 6px; color: #2dd4bf; font-size: 11px; font-weight: 800; letter-spacing: .12em; }
.plan-hero h2 { margin: 12px 0 8px; color: var(--fm-text-strong); font-size: clamp(28px,4vw,46px); letter-spacing: -.04em; }
.plan-hero p,.followup p { margin: 0; max-width: 720px; color: var(--fm-text-muted); line-height: 1.7; }
.review-date { display: grid; min-width: 180px; gap: 5px; padding: 15px; border-radius: var(--fm-radius-md); background: rgba(139,92,246,.13); color: var(--fm-text-muted); }
.review-date strong { color: var(--fm-text-strong); font-size: 21px; }
.plan-card { border-radius: var(--fm-radius-lg); }
.meal-tabs { display: grid; grid-template-columns: repeat(5,1fr); gap: 7px; padding: 5px; border-radius: 14px; background: rgba(100,116,139,.08); }
.meal-tabs button { padding: 11px; border: 0; border-radius: 10px; background: transparent; color: var(--fm-text-muted); font: inherit; cursor: pointer; }
.meal-tabs button.active { color: #ecfeff; background: linear-gradient(135deg,#0f766e,#2563eb); box-shadow: 0 8px 20px rgba(37,99,235,.18); }
.meal-sheet { display: grid; gap: 1px; margin-top: 16px; overflow: hidden; border-radius: 15px; background: rgba(148,163,184,.1); }
.food-row { display: grid; grid-template-columns: 1fr auto; gap: 9px 18px; padding: 14px 17px; background: color-mix(in srgb, var(--fm-main-bg) 92%, transparent); }
.food-row > div { display: flex; gap: 14px; }
.food-row span { color: var(--fm-text-faint); font-size: 10px; letter-spacing: .12em; }
.food-row b,.food-row strong { color: var(--fm-text-strong); }
.food-row p { grid-column: 1/-1; margin: 4px 0 0 28px; max-width: 880px; color: var(--fm-text-muted); line-height: 1.65; }
.featured { padding-block: 22px; }
.info-grid { display: grid; grid-template-columns: minmax(0,1.5fr) minmax(320px,.8fr); gap: 24px; }
.card-head { display: flex; justify-content: space-between; align-items: center; font-weight: 800; }
.guide-list { display: grid; gap: 12px; }
.guide-list article,.exercise-note { padding: 18px; border-left: 2px solid rgba(45,212,191,.55); border-radius: 0 14px 14px 0; background: rgba(100,116,139,.06); }
h3 { margin: 0 0 8px; color: var(--fm-text-strong); font-size: 14px; }
.guide-list p,.exercise-note p { margin: 0; color: var(--fm-text-muted); line-height: 1.85; white-space: pre-line; }
.side-stack { display: grid; gap: 24px; align-content: start; }
.exercise-note + .exercise-note { margin-top: 10px; }
@media (max-width: 900px) { .info-grid { grid-template-columns: 1fr; } .plan-hero { align-items: stretch; flex-direction: column; } }
@media (max-width: 650px) { .meal-tabs { display: flex; overflow-x: auto; } .meal-tabs button { min-width: 88px; } .food-row { grid-template-columns: 1fr; } }
</style>
