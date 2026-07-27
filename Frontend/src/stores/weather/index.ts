import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchWeatherApi, type WeatherDto } from '@/api/weather'
import { useSettingsStore } from '@/stores/settings'

export const useWeatherStore = defineStore('weather', () => {
  const loading = ref(false)
  const error = ref('')
  const weather = ref<WeatherDto | null>(null)

  const city = computed(() => weather.value?.city || useSettingsStore().weatherCity)

  const summary = computed(() => {
    if (!weather.value) return '天气加载中…'
    return `${weather.value.city} · ${weather.value.summary}`
  })

  async function syncWeather() {
    loading.value = true
    error.value = ''
    try {
      weather.value = await fetchWeatherApi()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取天气失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    weather,
    city,
    summary,
    syncWeather,
  }
})
