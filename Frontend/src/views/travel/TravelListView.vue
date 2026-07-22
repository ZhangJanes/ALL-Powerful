<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, AddOutline } from '@vicons/ionicons5'
import { useTravelStore } from '@/stores/travel'
import { useListDisplayMode, staggerDelay } from '@/composables/useListDisplayMode'
import FmDisplayModeToggle from '@/components/FmDisplayModeToggle.vue'

const router = useRouter()
const travelStore = useTravelStore()
const { displayMode } = useListDisplayMode()
const cat = ref('全部')
const cats = ['全部', '周末出游', '旅行', '日常通勤', '回老家', '办事']
const dayMs = 24 * 60 * 60 * 1000

type DeadlineMeta = {
  label: string
  className: string
}

const upcoming = computed(() => {
  let t = travelStore.trips.filter((x) => !x.done)
  if (cat.value !== '全部') t = t.filter((x) => x.category === cat.value)
  return t
})
const history = computed(() => travelStore.trips.filter((t) => t.done))

function parseTime(s?: string) {
  if (!s) return Number.NaN
  return new Date(s.replace(' ', 'T')).getTime()
}

function getDeadlineMeta(plannedAt?: string): DeadlineMeta | null {
  if (!plannedAt) return null
  const target = parseTime(plannedAt)
  if (Number.isNaN(target)) return null
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const targetDate = new Date(target)
  const startOfTarget = new Date(targetDate.getFullYear(), targetDate.getMonth(), targetDate.getDate()).getTime()
  const dayDiff = Math.floor((startOfTarget - startOfToday) / dayMs)
  if (dayDiff < 0) return { label: '过期', className: 'level-expired' }
  if (dayDiff === 0) return { label: '今天', className: 'level-today' }
  if (dayDiff === 1) return { label: '明天', className: 'level-d1' }
  if (dayDiff === 2) return { label: '后天', className: 'level-d2' }
  return { label: '大后天', className: 'level-d3' }
}

const decoratedUpcoming = computed(() =>
  upcoming.value.map((t) => ({
    ...t,
    deadline: getDeadlineMeta(t.start),
  })),
)

function delayId(id: string, i: number, base: number) {
  return staggerDelay(`${id}-${base}`, i)
}

onMounted(async () => {
  try {
    await travelStore.syncTrips()
  } catch {
    // fallback to local mock
  }
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title nav-title">出行计划</div>
      <div class="nav-actions">
        <FmDisplayModeToggle />
        <NButton quaternary circle @click="router.push({ name: 'travel-new' })">
          <template #icon><NIcon :component="AddOutline" /></template>
        </NButton>
      </div>
    </div>

    <div class="cats">
      <NButton v-for="c in cats" :key="c" size="small" round :secondary="c === cat" :quaternary="c !== cat" @click="cat = c">
        {{ c }}
      </NButton>
    </div>

    <NCard class="glass section" :bordered="false" :title="`待出行（${decoratedUpcoming.length}）`">
      <FmEmptyIllustrated v-if="!decoratedUpcoming.length" description="暂无出行计划" variant="empty" />
      <NList v-else-if="displayMode === 'list'" bordered>
        <NListItem
          v-for="t in decoratedUpcoming"
          :key="t.id"
          @click="router.push({ name: 'travel-detail', params: { id: t.id } })"
        >
          <NThing :title="t.title" :description="`${t.start} → ${t.end} · ${t.place}`">
            <template #header-extra>
              <NTag size="tiny" type="info" round>{{ t.category }}</NTag>
            </template>
            <template #footer>
              <div v-if="t.deadline" class="plan-time-row">
                <NTag size="small" round :class="t.deadline.className">{{ t.deadline.label }}</NTag>
                <span class="plan-time-text">{{ t.start }}</span>
              </div>
            </template>
          </NThing>
        </NListItem>
      </NList>
      <div v-else class="card-grid">
        <NCard
          v-for="(t, idx) in decoratedUpcoming"
          :key="t.id"
          class="glass fm-card-tile travel-card"
          :bordered="false"
          size="small"
          :style="{ animationDelay: `${delayId(t.id, idx, 0)}ms` }"
          @click="router.push({ name: 'travel-detail', params: { id: t.id } })"
        >
          <NTag class="travel-card__cat" size="small" type="info" round>{{ t.category }}</NTag>
          <div class="travel-card__title">{{ t.title }}</div>
          <div class="travel-card__line">{{ t.start }} → {{ t.end }}</div>
          <div class="subtle travel-card__place">{{ t.place }}</div>
          <div v-if="t.deadline" class="travel-card__deadline">
            <NTag size="small" round :class="t.deadline.className">{{ t.deadline.label }}</NTag>
            <span class="plan-time-text">{{ t.start }}</span>
          </div>
        </NCard>
      </div>
    </NCard>

    <NCard class="glass section" :bordered="false" title="历史行程">
      <FmEmptyIllustrated v-if="!history.length" description="暂无归档" variant="empty" />
      <NList v-else-if="displayMode === 'list'" bordered>
        <NListItem v-for="t in history" :key="t.id">
          <NThing :title="t.title" :description="t.place" />
        </NListItem>
      </NList>
      <div v-else class="card-grid">
        <NCard
          v-for="(t, idx) in history"
          :key="t.id"
          class="glass fm-card-tile travel-card travel-card--past"
          :bordered="false"
          size="small"
          :style="{ animationDelay: `${delayId(t.id, idx, 100)}ms` }"
        >
          <div class="travel-card__title">{{ t.title }}</div>
          <div class="subtle travel-card__place">{{ t.place }}</div>
        </NCard>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 6px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.nav-title {
  flex: 1;
  min-width: 0;
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cats {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}
.section {
  border-radius: 16px;
  margin-top: 4px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(256px, 1fr));
  gap: 14px;
  margin-top: 4px;
}
.travel-card {
  position: relative;
  border-radius: 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}
.travel-card--past {
  cursor: default;
  opacity: 0.95;
}
.travel-card__cat {
  margin-bottom: 8px;
}
.travel-card__title {
  font-weight: 800;
  font-size: 16px;
  line-height: 1.35;
  color: var(--fm-text-strong);
  margin-bottom: 6px;
}
.travel-card__line {
  font-size: 12px;
  color: var(--fm-text-secondary);
  margin-bottom: 4px;
}
.travel-card__place {
  font-size: 13px;
}
.travel-card__deadline {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.plan-time-row {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.plan-time-text {
  font-size: 12px;
  color: var(--fm-text-muted);
}
.level-d3 {
  color: #10b981;
}
.level-d2 {
  color: #f59e0b;
}
.level-d1 {
  color: #fb923c;
}
.level-today {
  color: #ef4444;
}
.level-expired {
  color: #991b1b;
}
html[data-theme='dark'] .level-d3 {
  color: #6ee7b7;
}
html[data-theme='dark'] .level-d2 {
  color: #fcd34d;
}
html[data-theme='dark'] .level-d1 {
  color: #fb923c;
}
html[data-theme='dark'] .level-today {
  color: #f87171;
}
html[data-theme='dark'] .level-expired {
  color: #fca5a5;
}
</style>
