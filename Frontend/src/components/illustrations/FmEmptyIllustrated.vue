<script setup lang="ts">
import FmAntdIllustration from './FmAntdIllustration.vue'

type IllustrationVariant = 'empty' | 'success' | 'error' | 'network' | 'search' | 'building'

withDefaults(
  defineProps<{
    description: string
    variant?: IllustrationVariant
    illustrationWidth?: number
  }>(),
  {
    variant: 'empty',
    illustrationWidth: 168,
  },
)
</script>

<template>
  <NEmpty :description="description">
    <FmAntdIllustration :variant="variant" :width="illustrationWidth" />
  </NEmpty>
</template>
