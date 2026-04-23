<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowBackOutline, ShieldCheckmarkOutline, HeartOutline, IdCardOutline, LayersOutline, DocumentTextOutline } from '@vicons/ionicons5'
import { guideCategories } from '@/data/guide'
import type { Component } from 'vue'

const router = useRouter()

const icons: Record<string, Component> = {
  insurance: ShieldCheckmarkOutline,
  hospital: HeartOutline,
  idcard: IdCardOutline,
  other: LayersOutline,
}

function go(cat: string) {
  router.push({ name: 'guide-list', params: { category: cat } })
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">生活办事指南</div>
      <NButton size="tiny" quaternary>
        <template #icon><NIcon :component="DocumentTextOutline" /></template>
      </NButton>
    </div>

    <NAlert type="info" title="更新提示" class="glass">有 3 项指南已更新（演示文案）</NAlert>

    <div class="grid">
      <NCard
        v-for="c in guideCategories"
        :key="c.key"
        class="cat glass"
        :bordered="false"
        hoverable
        @click="go(c.key)"
      >
        <div class="icon-wrap">
          <NIcon :component="icons[c.key] || DocumentTextOutline" :size="26" />
        </div>
        <div class="t">{{ c.title }}</div>
        <div class="d">{{ c.desc }}</div>
        <NTag size="tiny" round type="info">{{ c.count }} 项</NTag>
      </NCard>
    </div>

    <NCard class="glass" :bordered="false" title="我的收藏（示意）">
      <NSpace>
        <NTag round>医保报销</NTag>
        <NTag round>身份证补办</NTag>
      </NSpace>
    </NCard>
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
.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.cat {
  border-radius: 16px;
  cursor: pointer;
  min-height: 120px;
}
.icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(13, 148, 136, 0.1);
  color: #7dd3fc;
  margin-bottom: 10px;
}
.t {
  font-weight: 800;
  margin-bottom: 6px;
}
.d {
  font-size: 12px;
  color: var(--fm-text-muted);
  margin-bottom: 8px;
}
</style>
