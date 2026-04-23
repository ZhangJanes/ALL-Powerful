<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'

const router = useRouter()

function tip(msg: string) {
  alert(msg)
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">更多</div>
      <div style="width: 34px" />
    </div>
    <NCard class="glass" :bordered="false" title="辅助功能">
      <NSpace vertical>
        <NButton block secondary @click="router.push({ name: 'profile' })">家庭共享</NButton>
        <NButton block secondary @click="tip('数据备份演示')">数据备份与恢复</NButton>
        <NButton block secondary @click="tip('主题 / 提醒 / 语言')">个性化设置</NButton>
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 6px 12px;
  margin-bottom: 10px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
</style>
