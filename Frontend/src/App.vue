<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { darkTheme, lightTheme, NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
import { getNaiveThemeOverrides } from '@/theme/naiveTheme'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const auth = useAuthStore()
const settings = useSettingsStore()

const naiveTheme = computed(() => (settings.isDark ? darkTheme : lightTheme))
const overrides = computed(() => getNaiveThemeOverrides(settings.resolvedTheme, settings.themePreset))

onMounted(() => {
  if (auth.isAuthed) void settings.syncFromServer()
})

watch(
  () => auth.isAuthed,
  (authed) => {
    if (authed) void settings.syncFromServer()
  },
)
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
