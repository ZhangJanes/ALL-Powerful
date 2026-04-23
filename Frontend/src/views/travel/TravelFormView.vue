<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { useMessage } from 'naive-ui'

const router = useRouter()
const store = useAppStore()
const message = useMessage()

const title = ref('')
const category = ref('周末出游')
const place = ref('')
const start = ref('2026-04-20 09:00')
const end = ref('2026-04-20 16:00')

function save() {
  if (!title.value.trim() || !place.value.trim()) {
    message.warning('请填写标题与地点')
    return
  }
  store.addTrip({
    title: title.value,
    category: category.value,
    start: start.value,
    end: end.value,
    place: place.value,
    done: false,
    checklist: [],
  })
  message.success('已保存行程')
  router.replace({ name: 'travel' })
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">创建出行</div>
      <NButton type="primary" text @click="save">保存</NButton>
    </div>

    <NForm label-placement="top">
      <NFormItem label="标题">
        <NInput v-model:value="title" placeholder="如：周末带孩子去公园" />
      </NFormItem>
      <NFormItem label="分类">
        <NSelect
          v-model:value="category"
          :options="['日常通勤', '周末出游', '旅行', '回老家', '办事'].map((v) => ({ label: v, value: v }))"
        />
      </NFormItem>
      <NFormItem label="开始">
        <NInput v-model:value="start" />
      </NFormItem>
      <NFormItem label="结束">
        <NInput v-model:value="end" />
      </NFormItem>
      <NFormItem label="地点">
        <NInput v-model:value="place" placeholder="可关联地图（演示）" />
      </NFormItem>
    </NForm>
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
</style>
