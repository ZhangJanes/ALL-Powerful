<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useIdeaStore } from '@/stores/idea'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const ideaStore = useIdeaStore()
const message = useMessage()

const isNew = computed(() => route.name === 'ideas-new')
const id = computed(() => route.params.id as string)

const title = ref('')
const body = ref('')
const category = ref('生活灵感')
const tags = ref('')
const starred = ref(false)

const existing = computed(() => ideaStore.ideas.find((i) => i.id === id.value))

watch(
  () => route.fullPath,
  () => {
    if (isNew.value) {
      title.value = ''
      body.value = ''
      category.value = '生活灵感'
      tags.value = ''
      starred.value = false
      return
    }
    const i = existing.value
    if (i) {
      title.value = i.title
      body.value = i.body
      category.value = i.category
      tags.value = i.tags.join(', ')
      starred.value = i.starred
    }
  },
  { immediate: true },
)

function save() {
  const tagList = tags.value
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
  if (isNew.value) {
    ideaStore.addIdea({
      title: title.value || '未命名灵感',
      body: body.value,
      category: category.value,
      tags: tagList,
      starred: starred.value,
    })
    message.success('已保存')
    router.replace({ name: 'ideas' })
    return
  }
  const i = existing.value
  if (!i) return
  Object.assign(i, {
    title: title.value || '未命名灵感',
    body: body.value,
    category: category.value,
    tags: tagList,
    starred: starred.value,
  })
  message.success('已保存')
  router.back()
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary @click="router.back()">取消</NButton>
      <div class="page-title">{{ isNew ? '新建灵感' : '编辑灵感' }}</div>
      <NButton type="primary" text @click="save">保存</NButton>
    </div>

    <NInput v-model:value="title" size="large" placeholder="灵感标题" />
    <NInput v-model:value="body" type="textarea" placeholder="记录你的好点子…" :autosize="{ minRows: 8, maxRows: 20 }" />

    <NForm label-placement="top">
      <NFormItem label="标签（逗号分隔）">
        <NInput v-model:value="tags" placeholder="副业, 菜谱, 礼物" />
      </NFormItem>
    </NForm>

    <NSpace justify="space-between" style="width: 100%">
      <NSelect
        v-model:value="category"
        style="width: 55%"
        :options="['生活灵感', '工作想法', '赚钱思路', '学习计划', '装修改造'].map((v) => ({ label: v, value: v }))"
      />
      <NSpace align="center">
        <span class="subtle">收藏</span>
        <NSwitch v-model:value="starred" />
      </NSpace>
    </NSpace>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 4px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
</style>
