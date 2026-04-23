<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, AddOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { useListDisplayMode, staggerDelay } from '@/composables/useListDisplayMode'
import FmDisplayModeToggle from '@/components/FmDisplayModeToggle.vue'

const router = useRouter()
const store = useAppStore()
const { displayMode } = useListDisplayMode()
const cat = ref('全部')
const cats = ['全部', '周末出游', '旅行', '日常通勤', '回老家', '办事']

const upcoming = computed(() => {
  let t = store.trips.filter((x) => !x.done)
  if (cat.value !== '全部') t = t.filter((x) => x.category === cat.value)
  return t
})
const history = computed(() => store.trips.filter((t) => t.done))

function delayId(id: string, i: number, base: number) {
  return staggerDelay(`${id}-${base}`, i)
}
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

    <NCard class="glass section" :bordered="false" :title="`待出行（${upcoming.length}）`">
      <FmEmptyIllustrated v-if="!upcoming.length" description="暂无出行计划" variant="empty" />
      <NList v-else-if="displayMode === 'list'" bordered>
        <NListItem
          v-for="t in upcoming"
          :key="t.id"
          @click="router.push({ name: 'travel-detail', params: { id: t.id } })"
        >
          <NThing :title="t.title" :description="`${t.start} → ${t.end} · ${t.place}`">
            <template #header-extra>
              <NTag size="tiny" type="info" round>{{ t.category }}</NTag>
            </template>
          </NThing>
        </NListItem>
      </NList>
      <div v-else class="card-grid">
        <NCard
          v-for="(t, idx) in upcoming"
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
</style>
