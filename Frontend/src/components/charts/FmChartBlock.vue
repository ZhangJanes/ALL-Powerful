<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import VChart from 'vue-echarts'

const props = withDefaults(
  defineProps<{
    option: EChartsOption
    /** 图表区域高度（px） */
    height?: number
  }>(),
  { height: 280 },
)

const wrapStyle = computed(() => ({
  height: `${props.height}px`,
}))
</script>

<template>
  <div class="fm-chart-block" :style="wrapStyle">
    <VChart class="fm-chart-inner" :option="option" autoresize />
  </div>
</template>

<style scoped>
.fm-chart-block {
  border-radius: var(--fm-radius-md);
  overflow: hidden;
  box-sizing: border-box;
  padding: 6px;
  background: var(--fm-chart-bg);
  border: 1px solid var(--fm-chart-border);
  box-shadow: var(--fm-chart-shadow);
}

.fm-chart-inner {
  width: 100%;
  height: 100%;
  min-height: 0;
}
</style>
