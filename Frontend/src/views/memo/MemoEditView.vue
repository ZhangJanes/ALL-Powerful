<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CheckmarkOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const store = useAppStore()
const message = useMessage()

const isNew = computed(() => route.name === 'memo-new')
const id = computed(() => (route.params.id as string) || '')

const title = ref('')
const content = ref('')
const category = ref('生活')
const todoMode = ref(false)
const todoLines = ref('')

const existing = computed(() => store.memos.find((m) => m.id === id.value))

watch(
  () => route.fullPath,
  () => {
    if (isNew.value) {
      title.value = ''
      content.value = ''
      category.value = '生活'
      todoMode.value = false
      todoLines.value = ''
      return
    }
    const m = existing.value
    if (m) {
      title.value = m.title
      content.value = m.content
      category.value = m.category
      if (m.todos?.length) {
        todoMode.value = true
        todoLines.value = m.todos.map((t) => `${t.done ? 'x ' : ''}${t.text}`).join('\n')
      } else {
        todoMode.value = false
        todoLines.value = ''
      }
    }
  },
  { immediate: true },
)

function save() {
  const todos = todoMode.value
    ? todoLines.value
        .split('\n')
        .map((l) => l.trim())
        .filter(Boolean)
        .map((line) => {
          const done = /^x\s+/i.test(line)
          const text = line.replace(/^x\s+/i, '').trim()
          return { id: Math.random().toString(36).slice(2, 8), text, done }
        })
    : undefined

  if (isNew.value) {
    store.addMemo({
      title: title.value || '未命名',
      content: todoMode.value ? '' : content.value,
      category: category.value,
      pinned: false,
      todos,
    })
    message.success('已保存')
    router.replace({ name: 'memo' })
    return
  }
  store.updateMemo(id.value, {
    title: title.value || '未命名',
    content: todoMode.value ? '' : content.value,
    category: category.value,
    todos,
  })
  message.success('已保存')
  router.back()
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary @click="router.back()">取消</NButton>
      <div class="page-title">{{ isNew ? '新建笔记' : '编辑笔记' }}</div>
      <NButton type="primary" quaternary @click="save">
        <template #icon><NIcon :component="CheckmarkOutline" /></template>
        保存
      </NButton>
    </div>

    <NInput v-model:value="title" size="large" placeholder="请输入标题" />

    <NSpace class="tools">
      <NButton size="tiny" secondary @click="todoMode = !todoMode">{{ todoMode ? '富文本' : '待办清单' }}</NButton>
      <NSelect v-model:value="category" size="small" :options="['生活', '工作', '家庭', '购物', '账单'].map((v) => ({ label: v, value: v }))" style="width: 120px" />
    </NSpace>

    <NInput
      v-if="!todoMode"
      v-model:value="content"
      type="textarea"
      placeholder="请输入内容…"
      :autosize="{ minRows: 10, maxRows: 22 }"
    />
    <NInput v-else v-model:value="todoLines" type="textarea" placeholder="每行一条待办，前缀 x 表示已完成" :autosize="{ minRows: 10, maxRows: 22 }" />

    <NAlert type="info" title="提醒" class="glass">
      完整「重复提醒 / 富文本工具栏」可在对接后端后接入；此处聚焦结构与交互流。
    </NAlert>
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
.tools {
  justify-content: space-between;
  width: 100%;
}
</style>
