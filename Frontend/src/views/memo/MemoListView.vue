<script setup lang="ts">
import { computed, ref } from 'vue'
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

const filtered = computed(() => {
  let list = store.memos
  if (cat.value !== '全部') list = list.filter((m) => m.category === cat.value)
  if (q.value.trim()) {
    const k = q.value.trim()
    list = list.filter((m) => m.title.includes(k) || m.content.includes(k))
  }
  return [...list].sort((a, b) => Number(b.pinned) - Number(a.pinned))
})

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

    <template v-if="filtered.length">
      <NList v-if="displayMode === 'list'" class="glass list" bordered>
        <NListItem v-for="m in filtered" :key="m.id" @click="router.push({ name: 'memo-edit', params: { id: m.id } })">
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
          </NThing>
        </NListItem>
      </NList>

      <div v-else class="card-grid">
        <NCard
          v-for="(m, idx) in filtered"
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
</style>
