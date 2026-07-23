<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useHabitStore } from '@/stores/habit'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const habitStore = useHabitStore()
const message = useMessage()
const detail = ref<Awaited<ReturnType<typeof habitStore.getHabitDetail>> | null>(null)

const habit = computed(() => habitStore.habits.find((h) => h.id === route.params.id))
const rate = computed(() => (habit.value ? Math.round((habit.value.total / habit.value.targetDays) * 100) : 0))

async function loadDetail() {
  if (!route.params.id) return
  detail.value = await habitStore.getHabitDetail(String(route.params.id))
}

async function makeUp() {
  if (!habit.value) return
  const d = new Date()
  d.setDate(d.getDate() - 1)
  const ymd = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  await habitStore.makeUp(habit.value.id, ymd)
  await loadDetail()
  message.success('已补卡')
}

onMounted(() => {
  loadDetail().catch(() => {})
})
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

    <NCard class="glass" :bordered="false" title="打卡日历">
      <NSpace>
        <NTag v-for="p in detail?.calendar.slice(-14) || []" :key="p.date" :type="p.status === 'checked' ? 'success' : p.status === 'missed' ? 'error' : 'default'">
          {{ p.date.slice(5) }}
        </NTag>
      </NSpace>
      <div class="subtle" style="margin-top: 8px">本月补卡剩余：{{ detail?.makeupLeft ?? 3 }} 次</div>
    </NCard>

    <NSpace>
      <NButton secondary block @click="makeUp">补卡</NButton>
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
