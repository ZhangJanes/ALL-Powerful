<script setup lang="ts">
import { ListOutline, GridOutline } from '@vicons/ionicons5'
import { useListDisplayMode } from '@/composables/useListDisplayMode'

const { displayMode, setMode } = useListDisplayMode()
</script>

<template>
  <NButtonGroup size="small" class="fm-dm">
    <NButton
      :quaternary="displayMode !== 'list'"
      :secondary="displayMode === 'list'"
      @click="setMode('list')"
    >
      <NIcon :component="ListOutline" />
    </NButton>
    <NButton
      :quaternary="displayMode !== 'cards'"
      :secondary="displayMode === 'cards'"
      @click="setMode('cards')"
    >
      <NIcon :component="GridOutline" />
    </NButton>
  </NButtonGroup>
</template>

<style scoped>
.fm-dm {
  flex-shrink: 0;
}
</style>
