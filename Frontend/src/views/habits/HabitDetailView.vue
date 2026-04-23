<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const store = useAppStore()

const habit = computed(() => store.habits.find((h) => h.id === route.params.id))
const rate = computed(() => (habit.value ? Math.round((habit.value.total / habit.value.targetDays) * 100) : 0))
</script>

<template>
  <div v-if="habit" class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">{{ habit.name }}</div>
      <div style="width: 34px" />
    </div>

    <NCard class="glass" :bordered="false" title="概览">
      <NDescriptions bordered size="small" :column="1">
        <NDescriptionsItem label="目标">{{ habit.targetDays }} 天</NDescriptionsItem>
        <NDescriptionsItem label="总打卡">{{ habit.total }} 天</NDescriptionsItem>
        <NDescriptionsItem label="连续">{{ habit.streak }} 天</NDescriptionsItem>
        <NDescriptionsItem label="完成率">{{ rate }}%</NDescriptionsItem>
      </NDescriptions>
    </NCard>

    <NCard class="glass" :bordered="false" title="打卡日历（示意）">
      <NAlert type="default" title="可视化日历">
        接入数据后可按 PRD 显示绿 / 灰 / 红状态；此处保留布局占位。
      </NAlert>
    </NCard>

    <NSpace>
      <NButton secondary block>补卡</NButton>
      <NButton tertiary block>暂停任务</NButton>
    </NSpace>
  </div>
  <div v-else class="app-shell page">
    <NResult status="404" title="未找到任务" description="请返回列表重试" />
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
