<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useTravelStore } from '@/stores/travel'

const route = useRoute()
const router = useRouter()
const travelStore = useTravelStore()

const trip = computed(() => travelStore.trips.find((t) => t.id === route.params.id))

function openMap() {
  alert('打开地图（演示）')
}
</script>

<template>
  <div v-if="trip" class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">行程详情</div>
      <div style="width: 34px" />
    </div>

    <NCard class="glass" :bordered="false" :title="trip.title">
      <NDescriptions bordered size="small" :column="1">
        <NDescriptionsItem label="分类">{{ trip.category }}</NDescriptionsItem>
        <NDescriptionsItem label="时间">{{ trip.start }} → {{ trip.end }}</NDescriptionsItem>
        <NDescriptionsItem label="地点">
          <NSpace>
            {{ trip.place }}
            <NButton size="tiny" secondary @click="openMap">地图</NButton>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="同行">{{ trip.companions || '—' }}</NDescriptionsItem>
        <NDescriptionsItem label="交通方式">{{ trip.transport || '—' }}</NDescriptionsItem>
        <NDescriptionsItem label="备注">{{ trip.remark || '—' }}</NDescriptionsItem>
        <NDescriptionsItem label="提醒">
          {{ trip.remindEnabled ? `出发前 ${trip.remindMinutesBefore || 60} 分钟` : '未开启' }}
        </NDescriptionsItem>
      </NDescriptions>
    </NCard>

    <NCard class="glass" :bordered="false" title="行程清单">
      <FmEmptyIllustrated v-if="!trip.checklist.length" description="暂无清单" variant="empty" />
      <NList v-else bordered>
        <NListItem v-for="c in trip.checklist" :key="c.id">
          <NThing :title="c.text" :description="c.done ? '已完成' : '待完成'" />
        </NListItem>
      </NList>
      <NButton class="mt" dashed block>+ 添加清单项</NButton>
    </NCard>
  </div>
  <div v-else class="app-shell page">
    <NResult status="404" title="未找到行程" />
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
.mt {
  margin-top: 12px;
}
</style>
