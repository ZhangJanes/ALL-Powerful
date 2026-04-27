<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, AddOutline, SearchOutline, NotificationsOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { useListDisplayMode, staggerDelay } from '@/composables/useListDisplayMode'
import FmDisplayModeToggle from '@/components/FmDisplayModeToggle.vue'

const router = useRouter()
const store = useAppStore()
const { displayMode } = useListDisplayMode()
const q = ref('')
const cat = ref('全部')
const cats = ['全部', '账单', '购物', '生活', '工作', '家庭']
const dayMs = 24 * 60 * 60 * 1000

type DeadlineMeta = {
  label: string
  className: string
}

const filtered = computed(() => {
  let list = store.memos
  if (cat.value !== '全部') list = list.filter((m) => m.category === cat.value)
  if (q.value.trim()) {
    const k = q.value.trim()
    list = list.filter((m) => m.title.includes(k) || m.content.includes(k))
  }
  return [...list].sort((a, b) => Number(b.pinned) - Number(a.pinned))
})

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

const decorated = computed(() =>
  filtered.value.map((m) => ({
    ...m,
    deadline: getDeadlineMeta(m.remindAt),
  })),
)

const memoEmptyVariant = computed(() => {
  if (store.memos.length && !filtered.value.length) {
    if (q.value.trim() || cat.value !== '全部') return 'search' as const
  }
  return 'empty' as const
})

const memoEmptyDescription = computed(() => {
  if (store.memos.length && !filtered.value.length && (q.value.trim() || cat.value !== '全部')) {
    return '没有符合条件的笔记，试试调整分类或关键词'
  }
  return '暂无笔记，点击右上角 + 新增'
})

function preview(m: { content: string; todos?: { text: string }[] }) {
  if (m.todos?.length) return m.todos.map((t) => t.text).join('、').slice(0, 24)
  return (m.content || '（无正文）').slice(0, 24)
}

onMounted(async () => {
  try {
    await store.syncMemos()
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
      <div class="page-title nav-title">备忘录</div>
      <div class="nav-actions">
        <FmDisplayModeToggle />
        <NButton quaternary circle @click="router.push({ name: 'memo-new' })">
          <template #icon><NIcon :component="AddOutline" /></template>
        </NButton>
      </div>
    </div>

    <NInput v-model:value="q" round placeholder="搜索标题 / 内容" clearable>
      <template #prefix>
        <NIcon :component="SearchOutline" />
      </template>
    </NInput>

    <div class="cats">
      <NButton
        v-for="c in cats"
        :key="c"
        size="small"
        round
        :secondary="c === cat"
        :quaternary="c !== cat"
        @click="cat = c"
      >
        {{ c }}
      </NButton>
    </div>

    <template v-if="decorated.length">
      <NList v-if="displayMode === 'list'" class="glass list" bordered>
        <NListItem v-for="m in decorated" :key="m.id" @click="router.push({ name: 'memo-edit', params: { id: m.id } })">
          <NThing>
            <template #header>
              <NSpace align="center" :size="6">
                <span class="t">{{ m.title }}</span>
                <NTag v-if="m.pinned" size="tiny" type="error" round>置顶</NTag>
                <NTag size="tiny" round>{{ m.category }}</NTag>
              </NSpace>
            </template>
            <template #description>
              <div class="prev">{{ preview(m) }}</div>
              <div class="meta">{{ m.updatedAt }}</div>
            </template>
            <template #avatar>
              <NAvatar style="background: rgba(13, 148, 136, 0.18); color: #ccfbf1">记</NAvatar>
            </template>
            <template #header-extra>
              <NIcon v-if="m.remindAt" color="#fbbf24" :component="NotificationsOutline" />
            </template>
            <template #footer>
              <div v-if="m.deadline && m.remindAt" class="plan-time-row">
                <NTag size="small" round :class="m.deadline.className">{{ m.deadline.label }}</NTag>
                <span class="plan-time-text">{{ m.remindAt }}</span>
              </div>
            </template>
          </NThing>
        </NListItem>
      </NList>

      <div v-else class="card-grid">
        <NCard
          v-for="(m, idx) in decorated"
          :key="m.id"
          class="glass fm-card-tile memo-card"
          :bordered="false"
          size="small"
          :style="{ animationDelay: `${staggerDelay(m.id, idx)}ms` }"
          @click="router.push({ name: 'memo-edit', params: { id: m.id } })"
        >
          <div class="memo-card__top">
            <NAvatar style="background: rgba(13, 148, 136, 0.18); color: #ccfbf1">记</NAvatar>
            <NIcon v-if="m.remindAt" color="#fbbf24" :component="NotificationsOutline" />
          </div>
          <div class="memo-card__head">
            <span class="t">{{ m.title }}</span>
            <NTag v-if="m.pinned" size="tiny" type="error" round>置顶</NTag>
          </div>
          <NSpace :size="4" style="margin: 6px 0 8px">
            <NTag size="tiny" round>{{ m.category }}</NTag>
          </NSpace>
          <div class="prev">{{ preview(m) }}</div>
          <div class="meta">{{ m.updatedAt }}</div>
          <div v-if="m.deadline && m.remindAt" class="plan-time-badge">
            <NTag size="small" round :class="m.deadline.className">{{ m.deadline.label }}</NTag>
            <span class="plan-time-text">{{ m.remindAt }}</span>
          </div>
        </NCard>
      </div>
    </template>
    <FmEmptyIllustrated v-else :description="memoEmptyDescription" :variant="memoEmptyVariant" />

    <div class="hint subtle">长按条目可扩展：编辑 / 删除 / 置顶（演示用菜单可后续接入）</div>
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
  flex-wrap: nowrap;
  overflow-x: auto;
  padding-bottom: 2px;
}
.list {
  border-radius: 14px;
  overflow: hidden;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(268px, 1fr));
  gap: 14px;
}
.memo-card {
  border-radius: 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}
.memo-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.memo-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.t {
  font-weight: 700;
  line-height: 1.35;
}
.prev {
  color: var(--fm-text-muted);
  font-size: 13px;
  line-height: 1.4;
}
.meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--fm-text-faint);
}
.hint {
  text-align: center;
}
.plan-time-row {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.plan-time-badge {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
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
