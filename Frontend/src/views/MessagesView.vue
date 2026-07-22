<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useRouter } from 'vue-router'
import { useListDisplayMode, staggerDelay } from '@/composables/useListDisplayMode'
import FmDisplayModeToggle from '@/components/FmDisplayModeToggle.vue'
import { useMessageStore } from '@/stores/message'

const router = useRouter()
const { displayMode } = useListDisplayMode()
const messageStore = useMessageStore()

const items = computed(() =>
  messageStore.activities.map((a, idx) => ({
    id: a.id,
    title: a.text,
    time: a.at,
    type: (idx % 3 === 0 ? 'warning' : idx % 3 === 1 ? 'default' : 'info') as 'warning' | 'default' | 'info',
  })),
)

onMounted(async () => {
  try {
    await messageStore.syncMessages()
  } catch {
    // fallback to local mock
  }
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title nav-title">消息</div>
      <FmDisplayModeToggle />
    </div>

    <NList v-if="displayMode === 'list'" class="list list-surface" bordered>
      <NListItem v-for="m in items" :key="m.id">
        <NThing :title="m.title" :description="m.time">
          <template #avatar>
            <NAvatar class="msg-av">!</NAvatar>
          </template>
          <template #header-extra>
            <NTag size="small" :type="m.type" round>提醒</NTag>
          </template>
        </NThing>
      </NListItem>
    </NList>

    <div v-else class="msg-cards">
      <NCard
        v-for="(m, idx) in items"
        :key="m.id"
        class="glass fm-card-tile msg-card"
        :bordered="false"
        :style="{ animationDelay: `${staggerDelay(m.id, idx)}ms` }"
        size="small"
      >
        <div class="msg-card__head">
          <NAvatar class="msg-av" round>!</NAvatar>
          <NTag size="small" :type="m.type" round>提醒</NTag>
        </div>
        <div class="msg-card__title">{{ m.title }}</div>
        <div class="msg-card__time subtle">{{ m.time }}</div>
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 6px 6px 10px;
  margin-bottom: 10px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.nav-title {
  flex: 1;
  min-width: 0;
  text-align: center;
}
.list {
  border-radius: 14px;
  overflow: hidden;
}
/* 浅色：干净信纸感；深色：略沉的衬底 */
.list-surface {
  background: var(--msg-list-bg);
  box-shadow: var(--msg-list-shade);
}
html[data-theme='light'] .list-surface {
  --msg-list-bg: rgba(255, 255, 255, 0.97);
  --msg-list-shade: 0 1px 0 rgba(15, 23, 42, 0.06), 0 12px 32px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(15, 23, 42, 0.08) !important;
}
html[data-theme='dark'] .list-surface {
  --msg-list-bg: rgba(15, 23, 42, 0.55);
  --msg-list-shade: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}
.msg-av {
  background: rgba(13, 148, 136, 0.2) !important;
  color: #5eead4 !important;
}
html[data-theme='light'] .msg-av {
  background: rgba(13, 148, 136, 0.12) !important;
  color: #0f766e !important;
}
.msg-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.msg-card {
  border-radius: 16px;
}
.msg-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.msg-card__title {
  font-weight: 700;
  font-size: 15px;
  line-height: 1.45;
  color: var(--fm-text-strong);
}
.msg-card__time {
  margin-top: 8px;
  font-size: 12px;
}
</style>
