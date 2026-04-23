<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { useMessage } from 'naive-ui'
import FmChartBlock from '@/components/charts/FmChartBlock.vue'
import { buildBudgetRingOption } from '@/utils/chartOptions'

const router = useRouter()
const store = useAppStore()
const message = useMessage()

const total = ref(store.monthlyBudget)

const ringOption = computed(() => buildBudgetRingOption(store.monthlySpent, total.value))

function save() {
  store.monthlyBudget = total.value
  message.success('预算已更新（演示）')
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">预算设置</div>
      <div style="width: 34px" />
    </div>

    <NCard class="glass" :bordered="false" title="月度总预算">
      <div class="chart-caption">预算使用（环形）</div>
      <FmChartBlock :option="ringOption" :height="260" />
      <NInputNumber v-model:value="total" :min="0" :step="100" style="width: 100%; margin-top: 16px">
        <template #prefix>¥</template>
      </NInputNumber>
      <NDescriptions bordered size="small" class="mt" :column="1">
        <NDescriptionsItem label="本月已支出">¥{{ store.monthlySpent }}</NDescriptionsItem>
        <NDescriptionsItem label="剩余">¥{{ Math.max(0, total - store.monthlySpent) }}</NDescriptionsItem>
      </NDescriptions>
    </NCard>

    <NCard class="glass" :bordered="false" title="超支提醒">
      <NSpace vertical>
        <NSwitch :default-value="true" /> 开启超支提醒（80% / 100%）
        <NAlert v-if="store.monthlySpent / total >= 0.8" type="warning" title="接近预算上限" />
      </NSpace>
    </NCard>

    <NButton type="primary" block @click="save">保存修改</NButton>
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
.chart-caption {
  font-size: 13px;
  font-weight: 600;
  color: var(--fm-text-secondary);
  margin-bottom: 10px;
}
</style>
