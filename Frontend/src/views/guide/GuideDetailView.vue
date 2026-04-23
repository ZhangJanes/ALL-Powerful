<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { getArticle } from '@/data/guide'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const article = computed(() => getArticle(route.params.category as string, route.params.slug as string))

function linkMemo() {
  message.success('已生成备忘录草稿（演示）')
  router.push({ name: 'memo-new' })
}
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
      <div v-for="(s, idx) in article.sections" :key="idx" class="sec">
        <div class="sec-title">{{ s.title }}</div>
        <div :class="['sec-body', s.highlight && 'hi']">{{ s.body }}</div>
      </div>
    </NCard>

    <NSpace vertical style="width: 100%">
      <NButton block secondary>收藏</NButton>
      <NButton block tertiary>一键咨询（演示）</NButton>
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
