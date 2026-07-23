<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useIdeaStore } from '@/stores/idea'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const ideaStore = useIdeaStore()
const message = useMessage()

const idea = computed(() => ideaStore.ideas.find((i) => i.id === route.params.id))

async function remove() {
  if (!idea.value) return
  await ideaStore.removeIdea(idea.value.id)
  message.success('已移至回收站')
  router.replace({ name: 'ideas' })
}

async function toggleStar() {
  if (!idea.value) return
  await ideaStore.toggleStar(idea.value.id)
}
</script>

<template>
  <div v-if="idea" class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">详情</div>
      <NSpace>
        <NButton size="tiny" secondary @click="toggleStar">{{ idea.starred ? '取消收藏' : '收藏' }}</NButton>
        <NButton size="tiny" secondary @click="router.push({ name: 'ideas-edit', params: { id: idea.id } })">编辑</NButton>
        <NButton size="tiny" tertiary @click="remove">删除</NButton>
      </NSpace>
    </div>

    <NCard class="glass" :bordered="false" :title="idea.title">
      <NSpace>
        <NTag round>{{ idea.category }}</NTag>
        <NTag v-for="t in idea.tags" :key="t" secondary round>{{ t }}</NTag>
      </NSpace>
      <NDivider style="margin: 12px 0" />
      <div class="body">{{ idea.body }}</div>
    </NCard>
  </div>
  <div v-else class="app-shell page">
    <NResult status="404" title="未找到" />
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
.body {
  white-space: pre-wrap;
  line-height: 1.6;
  color: var(--fm-text-primary);
}
</style>
