<script setup lang="ts">
import { computed } from 'vue'
import { darkTheme, lightTheme, NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
import { getNaiveThemeOverrides } from '@/theme/naiveTheme'
import { useSettingsStore } from '@/stores/settings'

const settings = useSettingsStore()

const naiveTheme = computed(() => (settings.isDark ? darkTheme : lightTheme))
const overrides = computed(() => getNaiveThemeOverrides(settings.resolvedTheme, settings.themePreset))
</script>

<template>
  <NConfigProvider :theme="naiveTheme" :theme-overrides="overrides">
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <router-view />
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
