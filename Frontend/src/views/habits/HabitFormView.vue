<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import { useHabitStore } from '@/stores/habit'

const router = useRouter()
const message = useMessage()
const habitStore = useHabitStore()
const name = ref('')
const days = ref(30)

async function save() {
  if (!name.value.trim()) {
    message.warning('请输入任务名称')
    return
  }
  await habitStore.createHabit(name.value.trim(), Number(days.value || 30))
  message.success('任务已创建')
  router.back()
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">创建打卡</div>
      <div style="width: 34px" />
    </div>
    <NForm label-placement="top">
      <NFormItem label="任务名称">
        <NInput v-model:value="name" placeholder="如：早起、阅读、喝水" />
      </NFormItem>
      <NFormItem label="目标周期（天）">
        <NInputNumber v-model:value="days" :min="1" :max="365" style="width: 100%" />
      </NFormItem>
      <NFormItem label="提醒">
        <NSwitch /> 每日提醒（示意）
      </NFormItem>
    </NForm>
    <NButton type="primary" block @click="save">保存</NButton>
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
