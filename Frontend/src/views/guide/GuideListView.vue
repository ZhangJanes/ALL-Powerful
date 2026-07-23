<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowBackOutline, ChevronForwardOutline } from '@vicons/ionicons5'
import { useGuideStore } from '@/stores/guide'

const route = useRoute()
const router = useRouter()
const guideStore = useGuideStore()
const items = ref<Awaited<ReturnType<typeof guideStore.listArticles>>>([])

const category = computed(() => route.params.category as string)
const title = computed(() => guideStore.categoryMap[category.value]?.title ?? '办事指南')

async function load() {
  items.value = await guideStore.listArticles(category.value)
}

watch(category, () => load().catch(() => {}))

onMounted(async () => {
  await guideStore.syncCategories()
  await load()
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">{{ title }}</div>
      <div style="width: 34px" />
    </div>

    <NList v-if="items.length" bordered class="glass list">
      <NListItem
        v-for="a in items"
        :key="a.id"
        @click="router.push({ name: 'guide-detail', params: { category: category, slug: a.slug } })"
      >
        <NThing :title="a.title" :description="a.intro">
          <template #header-extra>
            <NIcon :component="ChevronForwardOutline" />
          </template>
        </NThing>
      </NListItem>
    </NList>
    <FmEmptyIllustrated v-else description="敬请期待更新" variant="building" />
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
.list {
  border-radius: 14px;
  overflow: hidden;
}
</style>
