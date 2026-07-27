import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { fetchUserSettingsApi, updateUserSettingsApi } from '@/api/settings'
import { WEATHER_CITIES, type WeatherCity } from '@/constants/weatherCities'
import { useAuthStore } from '@/stores/auth'
import type { ThemePresetKey } from '@/theme/presets'
import { getPreset, THEME_PRESETS } from '@/theme/presets'

export type ThemeMode = 'light' | 'dark'
export type FontMode = 'default' | 'alimama' | 'cute' | 'bobo' | 'maiyuan'
export type { WeatherCity }
export { WEATHER_CITIES }

const LS_MODE = 'fm.theme.mode'
const LS_PRESET = 'fm.theme.preset'
const LS_FONT = 'fm.font.mode'
const LS_WEATHER_CITY = 'fm.weather.city'

const PRESET_KEYS = new Set(THEME_PRESETS.map((p) => p.key))
const FONT_MODES = new Set<FontMode>(['default', 'alimama', 'cute', 'bobo', 'maiyuan'])
const WEATHER_CITY_SET = new Set<string>(WEATHER_CITIES)

function normalizeMode(raw: unknown): ThemeMode {
  if (raw === 'light' || raw === 'dark') return raw
  return 'dark'
}

function normalizePreset(raw: unknown): ThemePresetKey {
  if (typeof raw === 'string' && PRESET_KEYS.has(raw as ThemePresetKey)) return raw as ThemePresetKey
  return 'ocean'
}

function normalizeFontMode(raw: unknown): FontMode {
  if (typeof raw === 'string' && FONT_MODES.has(raw as FontMode)) return raw as FontMode
  return 'default'
}

function normalizeWeatherCity(raw: unknown): WeatherCity {
  if (typeof raw === 'string' && WEATHER_CITY_SET.has(raw)) return raw as WeatherCity
  return '北京'
}

function applyDomTheme(theme: 'light' | 'dark', preset: ThemePresetKey, font: FontMode) {
  document.documentElement.dataset.theme = theme
  document.documentElement.dataset.preset = preset
  document.documentElement.dataset.font = font
  document.documentElement.style.colorScheme = theme
  const meta = document.querySelector('meta[name="theme-color"]') as HTMLMetaElement | null
  const p = getPreset(preset)
  if (meta) {
    meta.content = theme === 'dark' ? p.primary.dark : p.primary.light
  }
}

export const useSettingsStore = defineStore('settings', () => {
  const themeMode = ref<ThemeMode>(normalizeMode(localStorage.getItem(LS_MODE)))
  const themePreset = ref<ThemePresetKey>(normalizePreset(localStorage.getItem(LS_PRESET)))
  const fontMode = ref<FontMode>(normalizeFontMode(localStorage.getItem(LS_FONT)))
  const weatherCity = ref<WeatherCity>(normalizeWeatherCity(localStorage.getItem(LS_WEATHER_CITY)))
  const syncing = ref(false)
  const synced = ref(false)

  let saveTimer: ReturnType<typeof setTimeout> | null = null

  const resolvedTheme = computed<'light' | 'dark'>(() => {
    if (themeMode.value === 'light') return 'light'
    return 'dark'
  })

  const isDark = computed(() => resolvedTheme.value === 'dark')

  watch(
    [resolvedTheme, themePreset, fontMode],
    ([theme, preset, font]) => {
      applyDomTheme(theme, preset, font)
    },
    { immediate: true },
  )

  function persistLocal() {
    localStorage.setItem(LS_MODE, themeMode.value)
    localStorage.setItem(LS_PRESET, themePreset.value)
    localStorage.setItem(LS_FONT, fontMode.value)
    localStorage.setItem(LS_WEATHER_CITY, weatherCity.value)
  }

  function applySettings(payload: {
    themeMode: string
    themePreset: string
    fontMode: string
    weatherCity?: string
  }) {
    themeMode.value = normalizeMode(payload.themeMode)
    themePreset.value = normalizePreset(payload.themePreset)
    fontMode.value = normalizeFontMode(payload.fontMode)
    weatherCity.value = normalizeWeatherCity(payload.weatherCity)
    persistLocal()
  }

  function currentPayload() {
    return {
      themeMode: themeMode.value,
      themePreset: themePreset.value,
      fontMode: fontMode.value,
      weatherCity: weatherCity.value,
    }
  }

  function scheduleSaveToServer() {
    const auth = useAuthStore()
    if (!auth.isAuthed) return

    if (saveTimer) clearTimeout(saveTimer)
    saveTimer = setTimeout(() => {
      saveTimer = null
      void saveToServer()
    }, 400)
  }

  async function saveToServer() {
    const auth = useAuthStore()
    if (!auth.isAuthed) return

    try {
      const data = await updateUserSettingsApi(currentPayload())
      applySettings(data)
      synced.value = true
    } catch (error) {
      console.warn('[settings] 保存到服务器失败', error)
    }
  }

  /** 登录后从服务器拉取设置；首次登录会把当前本地设置上传 */
  async function syncFromServer() {
    const auth = useAuthStore()
    if (!auth.isAuthed || syncing.value) return

    syncing.value = true
    try {
      const localBeforeSync = currentPayload()
      const remote = await fetchUserSettingsApi()
      const isDefaultRemote =
        remote.themeMode === 'dark' &&
        remote.themePreset === 'ocean' &&
        remote.fontMode === 'default' &&
        normalizeWeatherCity(remote.weatherCity) === '北京'
      const localCustomized =
        localBeforeSync.themeMode !== 'dark' ||
        localBeforeSync.themePreset !== 'ocean' ||
        localBeforeSync.fontMode !== 'default' ||
        localBeforeSync.weatherCity !== '北京'

      if (isDefaultRemote && localCustomized) {
        const saved = await updateUserSettingsApi(localBeforeSync)
        applySettings(saved)
      } else {
        applySettings(remote)
      }
      synced.value = true
    } catch (error) {
      console.warn('[settings] 从服务器同步失败，继续使用本地设置', error)
    } finally {
      syncing.value = false
    }
  }

  function setThemeMode(mode: ThemeMode) {
    themeMode.value = mode
    persistLocal()
    scheduleSaveToServer()
  }

  function setThemePreset(preset: ThemePresetKey) {
    themePreset.value = preset
    persistLocal()
    scheduleSaveToServer()
  }

  function setFontMode(mode: FontMode) {
    fontMode.value = mode
    persistLocal()
    scheduleSaveToServer()
  }

  function setWeatherCity(city: WeatherCity) {
    weatherCity.value = normalizeWeatherCity(city)
    persistLocal()
    // 天气接口读服务端设置，城市变更立即落库，避免首页仍用旧城市
    if (saveTimer) {
      clearTimeout(saveTimer)
      saveTimer = null
    }
    void saveToServer()
  }

  function syncDomTheme() {
    applyDomTheme(resolvedTheme.value, themePreset.value, fontMode.value)
  }

  return {
    themeMode,
    themePreset,
    fontMode,
    weatherCity,
    resolvedTheme,
    isDark,
    syncing,
    synced,
    setThemeMode,
    setThemePreset,
    setFontMode,
    setWeatherCity,
    syncDomTheme,
    syncFromServer,
    saveToServer,
  }
})
