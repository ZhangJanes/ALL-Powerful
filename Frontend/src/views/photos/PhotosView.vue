<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, LockClosedOutline, ImageOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { staggerDelay } from '@/composables/useListDisplayMode'

const router = useRouter()
const store = useAppStore()
const cat = ref('全部')
const cats = ['全部', '证件照', '病历', '发票', '家庭照片', '工作文件']

const list = computed(() => {
  if (cat.value === '全部') return store.photos
  return store.photos.filter((p) => p.category === cat.value)
})

/** 瀑布流：稳定随机高度，避免整齐网格 */
function tileStyle(id: string, i: number) {
  let h = 0
  for (let c = 0; c < id.length; c++) h = (h + id.charCodeAt(c) * 17) % 200
  const minH = 96
  const height = minH + (h % 100)
  return {
    minHeight: `${height}px`,
    animationDelay: `${staggerDelay(id, i, 40)}ms`,
  }
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">照片库</div>
      <NButton size="tiny" secondary>上传</NButton>
    </div>

    <div class="cats">
      <NButton v-for="c in cats" :key="c" size="small" round :secondary="c === cat" :quaternary="c !== cat" @click="cat = c">
        {{ c }}
      </NButton>
    </div>

    <div v-if="list.length" class="masonry">
      <div
        v-for="(p, idx) in list"
        :key="p.id"
        class="masonry__item"
      >
        <NCard
          class="tile glass fm-card-tile"
          :bordered="false"
          size="small"
          :style="tileStyle(p.id, idx)"
        >
          <div class="thumb">
            <NIcon :component="ImageOutline" :size="32" />
            <NIcon v-if="p.locked" class="lock" :component="LockClosedOutline" />
          </div>
          <div class="name">{{ p.name }}</div>
          <div class="subtle meta">{{ p.at }}</div>
        </NCard>
      </div>
    </div>
    <FmEmptyIllustrated v-else description="暂无文件" variant="empty" />
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 6px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.cats {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin-bottom: 4px;
}
.masonry {
  column-count: 3;
  column-gap: 14px;
}
@media (max-width: 1100px) {
  .masonry {
    column-count: 2;
  }
}
@media (max-width: 640px) {
  .masonry {
    column-count: 1;
  }
}
.masonry__item {
  break-inside: avoid;
  margin-bottom: 14px;
}
.tile {
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  box-sizing: border-box;
}
.thumb {
  flex: 1;
  min-height: 64px;
  border-radius: 12px;
  background: linear-gradient(145deg, rgba(13, 148, 136, 0.15), rgba(100, 116, 139, 0.12));
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.lock {
  position: absolute;
  top: 8px;
  right: 8px;
  color: #fbbf24;
}
.name {
  margin-top: 10px;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.35;
  color: var(--fm-text-strong);
}
.meta {
  font-size: 11px;
  margin-top: 4px;
}
</style>
