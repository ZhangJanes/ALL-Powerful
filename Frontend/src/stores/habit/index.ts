import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
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
  const habits = ref<Habit[]>([
    { id: 'h1', name: '早起', icon: 'sunny', streak: 5, total: 12, targetDays: 30, doneToday: false },
    { id: 'h2', name: '阅读30分钟', icon: 'book', streak: 2, total: 8, targetDays: 30, doneToday: true },
    { id: 'h3', name: '喝水8杯', icon: 'water', streak: 0, total: 3, targetDays: 7, doneToday: false },
  ])

  const habitToday = computed(() => {
    const list = habits.value
    const done = list.filter((h) => h.doneToday).length
    return { done, total: list.length }
  })

  function checkIn(habitId: string) {
    const h = habits.value.find((x) => x.id === habitId)
    if (!h || h.doneToday) return
    h.doneToday = true
    h.streak += 1
    h.total += 1
    useMessageStore().pushActivity(`完成打卡：${h.name}`)
  }

  return { habits, habitToday, checkIn }
})
