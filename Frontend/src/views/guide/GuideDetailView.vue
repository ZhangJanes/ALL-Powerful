<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useGuideStore } from '@/stores/guide'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const guideStore = useGuideStore()
const article = ref<Awaited<ReturnType<typeof guideStore.getArticleBySlug>> | null>(null)

const sections = computed(() => {
  if (!article.value) return []
  return [
    { title: '一、办理条件', body: article.value.conditionsText || '-' },
    { title: '二、所需材料', body: article.value.materialsText || '-', highlight: true },
    { title: '三、办理流程', body: article.value.processText || '-' },
    { title: '四、办理地点', body: article.value.locationText || '-' },
    { title: '五、办理时间', body: article.value.timeText || '-' },
    { title: '六、办理周期', body: article.value.periodText || '-' },
    { title: '七、收费标准', body: article.value.feeText || '-' },
    { title: '八、避坑技巧', body: article.value.tipsText || '-' },
  ]
})

async function linkMemo() {
  if (!article.value) return
  await guideStore.linkMemo(article.value.id)
  message.success('已生成关联备忘录')
  router.push({ name: 'memo' })
}

async function toggleFavorite() {
  if (!article.value) return
  await guideStore.toggleFavorite(article.value)
  article.value.favorite = !article.value.favorite
}

onMounted(async () => {
  try {
    article.value = await guideStore.getArticleBySlug(String(route.params.category), String(route.params.slug))
  } catch {
    article.value = null
  }
})
</script>

<template>
  <div v-if="article" class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">指南</div>
      <NButton size="tiny" secondary @click="linkMemo">备忘</NButton>
    </div>

    <NCard class="glass" :bordered="false" :title="article.title">
      <div class="intro">{{ article.intro }}</div>
      <NDivider />
      <div v-for="(s, idx) in sections" :key="idx" class="sec">
        <div class="sec-title">{{ s.title }}</div>
        <div :class="['sec-body', s.highlight && 'hi']">{{ s.body }}</div>
      </div>
    </NCard>

    <NSpace vertical style="width: 100%">
      <NButton block secondary @click="toggleFavorite">{{ article.favorite ? '取消收藏' : '收藏' }}</NButton>
      <NButton block tertiary>{{ article.consultUrl ? '一键咨询（链接已配置）' : '一键咨询（待配置）' }}</NButton>
    </NSpace>
  </div>
  <div v-else class="app-shell page">
    <NResult status="404" title="未找到指南" />
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
.intro {
  color: var(--fm-text-secondary);
  line-height: 1.6;
}
.sec + .sec {
  margin-top: 14px;
}
.sec-title {
  font-weight: 800;
  margin-bottom: 6px;
}
.sec-body {
  color: var(--fm-text-secondary);
  line-height: 1.65;
  white-space: pre-wrap;
}
.hi {
  color: #fdba74;
}
</style>
