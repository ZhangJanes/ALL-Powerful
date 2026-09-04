import { del, get, post, put } from '@/api/request'

export type HealthCalculationsDto = {
  age?: number
  bmi?: number
  estimatedBodyFatPercent?: number
  effectiveBodyFatPercent?: number
  healthyBodyFatMin?: number
  healthyBodyFatMax?: number
  restingMetabolicRate?: number
  estimatedTdee?: number
  calorieSuggestionMin?: number
  calorieSuggestionMax?: number
  bodyFatSource?: 'measured' | 'estimated'
  notice: string
}

export type HealthProfilePayload = {
  gender?: 'male' | 'female'
  birthDate?: string
  heightCm?: number
  currentWeightKg?: number
  targetWeightKg?: number
  bodyFatPercent?: number
  targetBodyFatPercent?: number
  activityLevel: 'sedentary' | 'light' | 'moderate' | 'very' | 'extra'
  targetCalories?: number
  waterTargetMl: number
  wakeTime?: string
  sleepTime?: string
}

export type HealthProfileDto = HealthProfilePayload & {
  userId: number
  calculations: HealthCalculationsDto
}

export type HealthMealItemDto = {
  id: number
  itemName: string
  portionText: string
  detailText?: string
  sortOrder: number
}

export type HealthMealGroupDto = {
  mealType: string
  label: string
  items: HealthMealItemDto[]
}

export type HealthGuidelineDto = {
  id: number
  category: 'EXECUTION' | 'EXERCISE'
  title: string
  content: string
  sortOrder: number
}

export type HealthDietPlanDto = {
  id: number
  title: string
  status: string
  sourceType: string
  nextReviewDate?: string
  followupPlan?: string
  meals: HealthMealGroupDto[]
  guidelines: HealthGuidelineDto[]
}

export type HealthGoalPayload = {
  goalType: string
  title: string
  targetValue: number
  unit: string
  periodType: 'daily' | 'weekly' | 'monthly' | 'once'
  comparisonOperator: 'GTE' | 'GT' | 'LTE' | 'LT' | 'EQ'
  startDate: string
  deadline?: string
  status?: 'active' | 'completed' | 'paused'
}

export type HealthGoalDto = HealthGoalPayload & { id: number; status: 'active' | 'completed' | 'paused' }

export type HealthGoalProgressDto = {
  goal: HealthGoalDto
  currentValue: number
  progressPercent: number
  completed: boolean
}

export type HealthDailyRecordPayload = {
  weightKg?: number
  bodyFatPercent?: number
  calorieIntake?: number
  wakeTime?: string
  sleepTime?: string
  sleepMinutes?: number
  mood?: string
  note?: string
}

export type HealthDailyRecordDto = HealthDailyRecordPayload & { id: number; recordDate: string }

export type HealthMealCheckinDto = {
  id: number
  checkinDate: string
  mealType: string
  status: 'completed' | 'skipped' | 'pending'
  note?: string
  checkedAt: string
}

export type HealthWaterLogDto = {
  id: number
  logDate: string
  amountMl: number
  recordedAt: string
}

export type HealthExercisePayload = {
  exerciseType: 'AEROBIC' | 'RESISTANCE' | 'OTHER'
  name: string
  durationMinutes: number
  heartRate?: number
  note?: string
  recordedAt?: string
}

export type HealthExerciseDto = HealthExercisePayload & {
  id: number
  logDate: string
  recordedAt: string
}

export type HealthDashboardDto = {
  date: string
  profile: HealthProfileDto
  activePlan?: HealthDietPlanDto
  mealCheckins: HealthMealCheckinDto[]
  completedMeals: number
  totalMeals: number
  waterTotalMl: number
  waterTargetMl: number
  exercises: HealthExerciseDto[]
  goals: HealthGoalProgressDto[]
  overallProgressPercent: number
}

export type HealthTrendPointDto = {
  date: string
  weightKg?: number
  bodyFatPercent?: number
  completedMeals: number
  waterMl: number
  exerciseMinutes: number
  sleepMinutes?: number
}

export type HealthStatsDto = {
  from: string
  to: string
  mealCompletionRate: number
  totalWaterMl: number
  totalExerciseMinutes: number
  trend: HealthTrendPointDto[]
}

export const fetchHealthDashboardApi = (date: string) =>
  get<HealthDashboardDto>('/health/dashboard', { date })
export const fetchHealthProfileApi = () => get<HealthProfileDto>('/health/profile')
export const saveHealthProfileApi = (data: HealthProfilePayload) =>
  put<HealthProfileDto>('/health/profile', data)
export const fetchHealthDietPlanApi = () => get<HealthDietPlanDto | null>('/health/diet-plan')
export const fetchHealthGoalsApi = () => get<HealthGoalDto[]>('/health/goals')
export const createHealthGoalApi = (data: HealthGoalPayload) =>
  post<HealthGoalDto>('/health/goals', data)
export const updateHealthGoalApi = (id: number, data: HealthGoalPayload) =>
  put<HealthGoalDto>(`/health/goals/${id}`, data)
export const deleteHealthGoalApi = (id: number) => del<void>(`/health/goals/${id}`)

export const fetchHealthDailyRecordApi = (date: string) =>
  get<HealthDailyRecordDto | null>(`/health/daily-records/${date}`)
export const saveHealthDailyRecordApi = (date: string, data: HealthDailyRecordPayload) =>
  put<HealthDailyRecordDto>(`/health/daily-records/${date}`, data)

export const saveMealCheckinApi = (
  date: string,
  mealType: string,
  status: HealthMealCheckinDto['status'] = 'completed',
  note?: string,
) => put<HealthMealCheckinDto>(`/health/meal-checkins/${date}/${mealType}`, { status, note })
export const deleteMealCheckinApi = (date: string, mealType: string) =>
  del<void>(`/health/meal-checkins/${date}/${mealType}`)

export const fetchWaterLogsApi = (date: string) =>
  get<HealthWaterLogDto[]>(`/health/water-logs/${date}`)
export const addWaterLogApi = (date: string, amountMl: number) =>
  post<HealthWaterLogDto>(`/health/water-logs/${date}`, { amountMl })
export const deleteWaterLogApi = (id: number) => del<void>(`/health/water-logs/${id}`)

export const fetchExerciseLogsApi = (date: string) =>
  get<HealthExerciseDto[]>(`/health/exercise-logs/${date}`)
export const addExerciseLogApi = (date: string, data: HealthExercisePayload) =>
  post<HealthExerciseDto>(`/health/exercise-logs/${date}`, data)
export const updateExerciseLogApi = (id: number, data: HealthExercisePayload) =>
  put<HealthExerciseDto>(`/health/exercise-logs/${id}`, data)
export const deleteExerciseLogApi = (id: number) => del<void>(`/health/exercise-logs/${id}`)

export const fetchHealthStatsApi = (from: string, to: string) =>
  get<HealthStatsDto>('/health/stats', { from, to })
