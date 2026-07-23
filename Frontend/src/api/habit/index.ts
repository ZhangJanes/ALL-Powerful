import { del, get, post, put } from '../request'

export type HabitTaskDto = {
  id: number
  name: string
  icon: string
  description?: string
  targetDays: number
  remindEnabled: boolean
  remindTime?: string
  paused: boolean
  streak: number
  total: number
  doneToday: boolean
  completionRate: number
}

export type HabitDetailDto = {
  task: HabitTaskDto
  logs: { id: number; checkinDate: string; checkinTime: string; isMakeup: boolean; note?: string }[]
  calendar: { date: string; status: 'checked' | 'missed' | 'pending' }[]
  makeupUsed: number
  makeupLeft: number
}

export type HabitAchievementDto = {
  code: string
  name: string
  unlocked: boolean
  progress: number
  target: number
}

export function fetchHabitTasksApi() {
  return get<HabitTaskDto[]>('/habits/tasks')
}

export function createHabitTaskApi(data: {
  name: string
  icon?: string
  description?: string
  targetDays: number
  remindEnabled: boolean
  remindTime?: string
}) {
  return post<HabitTaskDto>('/habits/tasks', data)
}

export function updateHabitTaskApi(id: number | string, data: {
  name: string
  icon?: string
  description?: string
  targetDays: number
  remindEnabled: boolean
  remindTime?: string
}) {
  return put<HabitTaskDto>(`/habits/tasks/${id}`, data)
}

export function toggleHabitPauseApi(id: number | string) {
  return put<HabitTaskDto>(`/habits/tasks/${id}/pause`)
}

export function deleteHabitTaskApi(id: number | string) {
  return del<void>(`/habits/tasks/${id}`)
}

export function checkinHabitApi(id: number | string, note?: string) {
  return post<HabitTaskDto>(`/habits/tasks/${id}/check-in`, { note })
}

export function makeupHabitApi(id: number | string, checkinDate: string, note?: string) {
  return post<HabitTaskDto>(`/habits/tasks/${id}/make-up`, { checkinDate, note })
}

export function fetchHabitDetailApi(id: number | string) {
  return get<HabitDetailDto>(`/habits/tasks/${id}`)
}

export function fetchHabitAchievementsApi() {
  return get<HabitAchievementDto[]>('/habits/achievements')
}
