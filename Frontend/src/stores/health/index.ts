import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  addExerciseLogApi,
  addWaterLogApi,
  createHealthGoalApi,
  deleteExerciseLogApi,
  deleteHealthGoalApi,
  deleteMealCheckinApi,
  deleteWaterLogApi,
  fetchExerciseLogsApi,
  fetchHealthDailyRecordApi,
  fetchHealthDashboardApi,
  fetchHealthDietPlanApi,
  fetchHealthGoalsApi,
  fetchHealthProfileApi,
  fetchHealthStatsApi,
  fetchWaterLogsApi,
  saveHealthDailyRecordApi,
  saveHealthProfileApi,
  saveMealCheckinApi,
  updateHealthGoalApi,
  type HealthDailyRecordDto,
  type HealthDailyRecordPayload,
  type HealthDashboardDto,
  type HealthDietPlanDto,
  type HealthExerciseDto,
  type HealthExercisePayload,
  type HealthGoalDto,
  type HealthGoalPayload,
  type HealthProfileDto,
  type HealthProfilePayload,
  type HealthStatsDto,
  type HealthWaterLogDto,
} from '@/api/health'
import { useMessageStore } from '@/stores/message'

function todayYmd() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

export const useHealthStore = defineStore('health', () => {
  const selectedDate = ref(todayYmd())
  const dashboard = ref<HealthDashboardDto>()
  const profile = ref<HealthProfileDto>()
  const plan = ref<HealthDietPlanDto>()
  const goals = ref<HealthGoalDto[]>([])
  const dailyRecord = ref<HealthDailyRecordDto>()
  const waterLogs = ref<HealthWaterLogDto[]>([])
  const exercises = ref<HealthExerciseDto[]>([])
  const stats = ref<HealthStatsDto>()
  const loading = ref(false)
  const error = ref('')
  let dashboardRequestId = 0
  let dayRequestId = 0

  const completedMealTypes = computed(
    () => new Set(dashboard.value?.mealCheckins.filter((x) => x.status === 'completed').map((x) => x.mealType) || []),
  )
  const waterTotal = computed(() => waterLogs.value.reduce((sum, row) => sum + row.amountMl, 0))

  async function run<T>(task: () => Promise<T>) {
    loading.value = true
    error.value = ''
    try {
      return await task()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '健康数据加载失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function syncDashboard(date = selectedDate.value) {
    const requestId = ++dashboardRequestId
    const result = await run(() => fetchHealthDashboardApi(date))
    if (requestId !== dashboardRequestId) return
    dashboard.value = result
    profile.value = dashboard.value.profile
    plan.value = dashboard.value.activePlan
  }

  async function syncProfile() {
    profile.value = await run(fetchHealthProfileApi)
  }

  async function saveProfile(payload: HealthProfilePayload) {
    profile.value = await run(() => saveHealthProfileApi(payload))
    useMessageStore().pushActivity('更新健康基础档案')
    await syncDashboard()
  }

  async function syncPlan() {
    plan.value = (await run(fetchHealthDietPlanApi)) || undefined
  }

  async function syncGoals() {
    goals.value = await run(fetchHealthGoalsApi)
  }

  async function saveGoal(payload: HealthGoalPayload, id?: number) {
    if (id) await run(() => updateHealthGoalApi(id, payload))
    else await run(() => createHealthGoalApi(payload))
    await syncGoals()
    await syncDashboard()
  }

  async function removeGoal(id: number) {
    await run(() => deleteHealthGoalApi(id))
    await syncGoals()
    await syncDashboard()
  }

  async function syncDay(date = selectedDate.value) {
    selectedDate.value = date
    const requestId = ++dayRequestId
    const [record, water, exercise] = await run(() =>
      Promise.all([
        fetchHealthDailyRecordApi(date),
        fetchWaterLogsApi(date),
        fetchExerciseLogsApi(date),
      ]),
    )
    if (requestId !== dayRequestId) return
    dailyRecord.value = record || undefined
    waterLogs.value = water
    exercises.value = exercise
    await syncDashboard(date)
  }

  async function saveDailyRecord(payload: HealthDailyRecordPayload) {
    dailyRecord.value = await run(() => saveHealthDailyRecordApi(selectedDate.value, payload))
    useMessageStore().pushActivity(`记录健康数据：${selectedDate.value}`)
    await syncDashboard()
  }

  async function toggleMeal(mealType: string) {
    if (completedMealTypes.value.has(mealType)) {
      await run(() => deleteMealCheckinApi(selectedDate.value, mealType))
    } else {
      await run(() => saveMealCheckinApi(selectedDate.value, mealType))
      useMessageStore().pushActivity(`完成饮食打卡：${mealType}`)
    }
    await syncDashboard()
  }

  async function addWater(amountMl: number) {
    await run(() => addWaterLogApi(selectedDate.value, amountMl))
    waterLogs.value = await fetchWaterLogsApi(selectedDate.value)
    await syncDashboard()
  }

  async function removeWater(id: number) {
    await run(() => deleteWaterLogApi(id))
    waterLogs.value = await fetchWaterLogsApi(selectedDate.value)
    await syncDashboard()
  }

  async function addExercise(payload: HealthExercisePayload) {
    await run(() => addExerciseLogApi(selectedDate.value, payload))
    exercises.value = await fetchExerciseLogsApi(selectedDate.value)
    useMessageStore().pushActivity(`完成运动：${payload.name}`)
    await syncDashboard()
  }

  async function removeExercise(id: number) {
    await run(() => deleteExerciseLogApi(id))
    exercises.value = await fetchExerciseLogsApi(selectedDate.value)
    await syncDashboard()
  }

  async function syncStats(days = 30) {
    const to = todayYmd()
    const start = new Date(`${to}T00:00:00`)
    start.setDate(start.getDate() - days + 1)
    const from = `${start.getFullYear()}-${String(start.getMonth() + 1).padStart(2, '0')}-${String(start.getDate()).padStart(2, '0')}`
    stats.value = await run(() => fetchHealthStatsApi(from, to))
  }

  return {
    selectedDate,
    dashboard,
    profile,
    plan,
    goals,
    dailyRecord,
    waterLogs,
    exercises,
    stats,
    loading,
    error,
    completedMealTypes,
    waterTotal,
    syncDashboard,
    syncProfile,
    saveProfile,
    syncPlan,
    syncGoals,
    saveGoal,
    removeGoal,
    syncDay,
    saveDailyRecord,
    toggleMeal,
    addWater,
    removeWater,
    addExercise,
    removeExercise,
    syncStats,
  }
})
