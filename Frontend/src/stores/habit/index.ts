import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  checkinHabitApi,
  createHabitTaskApi,
  fetchHabitAchievementsApi,
  fetchHabitDetailApi,
  fetchHabitTasksApi,
  makeupHabitApi,
  type HabitAchievementDto,
} from '@/api/habit'
import { useMessageStore } from '@/stores/message'

export type Habit = {
  id: string
  name: string
  icon: string
  streak: number
  total: number
  targetDays: number
  doneToday: boolean
}

export const useHabitStore = defineStore('habit', () => {
  const habits = ref<Habit[]>([])
  const achievements = ref<HabitAchievementDto[]>([])
  const loaded = ref(false)

  const habitToday = computed(() => {
    const list = habits.value
    const done = list.filter((h) => h.doneToday).length
    return { done, total: list.length }
  })

  async function syncHabits() {
    const rows = await fetchHabitTasksApi()
    habits.value = rows.map((h) => ({
      id: String(h.id),
      name: h.name,
      icon: h.icon,
      streak: Number(h.streak || 0),
      total: Number(h.total || 0),
      targetDays: Number(h.targetDays || 0),
      doneToday: Boolean(h.doneToday),
    }))
    loaded.value = true
  }

  async function syncAchievements() {
    achievements.value = await fetchHabitAchievementsApi()
  }

  async function checkIn(habitId: string) {
    const h = habits.value.find((x) => x.id === habitId)
    if (!h || h.doneToday) return
    await checkinHabitApi(habitId)
    await syncHabits()
    await syncAchievements()
    useMessageStore().pushActivity(`完成打卡：${h.name}`)
  }

  async function createHabit(name: string, targetDays: number) {
    await createHabitTaskApi({
      name,
      targetDays,
      remindEnabled: false,
      icon: 'sunny',
      description: '',
    })
    await syncHabits()
  }

  async function makeUp(habitId: string, day: string, note?: string) {
    await makeupHabitApi(habitId, day, note)
    await syncHabits()
    await syncAchievements()
  }

  async function getHabitDetail(habitId: string) {
    return fetchHabitDetailApi(habitId)
  }

  if (!loaded.value) {
    syncHabits().then(syncAchievements).catch(() => {
      habits.value = []
    })
  }

  return { habits, habitToday, achievements, checkIn, createHabit, makeUp, getHabitDetail, syncHabits, syncAchievements }
})
