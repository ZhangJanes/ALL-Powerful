<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, AddOutline, Star, StarOutline } from '@vicons/ionicons5'
import { useIdeaStore } from '@/stores/idea'
import { useListDisplayMode, staggerDelay } from '@/composables/useListDisplayMode'
import FmDisplayModeToggle from '@/components/FmDisplayModeToggle.vue'

const router = useRouter()
const ideaStore = useIdeaStore()
const { displayMode } = useListDisplayMode()
const q = ref('')

const list = computed(() => {
  let xs = ideaStore.ideas
  const k = q.value.trim()
  if (k) xs = xs.filter((i) => i.title.includes(k) || i.body.includes(k) || i.tags.some((t) => t.includes(k)))
  return xs
})

onMounted(async () => {
  try {
    await ideaStore.syncIdeas()
  } catch {
    // noop
  }
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title nav-title">New Idea</div>
      <div class="nav-actions">
        <FmDisplayModeToggle />
        <NButton quaternary circle @click="router.push({ name: 'ideas-new' })">
          <template #icon><NIcon :component="AddOutline" /></template>
        </NButton>
      </div>
    </div>

    <NInput v-model:value="q" round placeholder="搜索灵感、标签" clearable />

    <template v-if="list.length">
      <NList v-if="displayMode === 'list'" bordered class="glass list">
        <NListItem v-for="i in list" :key="i.id" @click="router.push({ name: 'ideas-detail', params: { id: i.id } })">
          <NThing :title="i.title">
            <template #avatar>
              <NIcon :component="i.starred ? Star : StarOutline" :color="i.starred ? '#f472b6' : undefined" :size="22" />
            </template>
            <template #header-extra>
              <NTag size="tiny" round>{{ i.category }}</NTag>
            </template>
            <template #description>
              <div>{{ (i.body || '').slice(0, 48) }}</div>
              <NSpace size="small" style="margin-top: 6px">
                <NTag v-for="t in i.tags" :key="t" size="tiny" secondary round>{{ t }}</NTag>
              </NSpace>
              <div class="subtle" style="margin-top: 6px">{{ i.at }}</div>
            </template>
          </NThing>
        </NListItem>
      </NList>

      <div v-else class="card-grid">
        <NCard
          v-for="(i, idx) in list"
          :key="i.id"
          class="glass fm-card-tile idea-card"
          :bordered="false"
          size="small"
          :style="{ animationDelay: `${staggerDelay(i.id, idx)}ms` }"
          @click="router.push({ name: 'ideas-detail', params: { id: i.id } })"
        >
          <div class="idea-card__row">
            <NIcon :component="i.starred ? Star : StarOutline" :color="i.starred ? '#f472b6' : undefined" :size="22" />
            <NTag size="tiny" round>{{ i.category }}</NTag>
          </div>
          <div class="idea-card__title">{{ i.title }}</div>
          <p class="idea-card__body">{{ (i.body || '').slice(0, 80) }}{{ (i.body || '').length > 80 ? '…' : '' }}</p>
          <NSpace size="small" class="tags">
            <NTag v-for="t in i.tags" :key="t" size="tiny" secondary round>{{ t }}</NTag>
          </NSpace>
          <div class="subtle idea-card__at">{{ i.at }}</div>
        </NCard>
      </div>
    </template>
    <FmEmptyIllustrated v-else description="暂无灵感" variant="empty" />
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
.list {
  border-radius: 14px;
  overflow: hidden;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}
.idea-card {
  border-radius: 16px;
  cursor: pointer;
}
.idea-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.idea-card__title {
  font-weight: 800;
  font-size: 16px;
  line-height: 1.35;
  color: var(--fm-text-strong);
  margin-bottom: 8px;
}
.idea-card__body {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--fm-text-secondary);
}
.tags {
  margin-bottom: 8px;
}
.idea-card__at {
  font-size: 12px;
}
</style>
