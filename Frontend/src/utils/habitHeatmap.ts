/** 为 NHeatmap 生成「周 / 月 / 季」目标完成度数据（当前周、当月、当季） */

export type HabitHeatmapPeriod = 'week' | 'month' | 'quarter'

export type HeatmapDatum = { timestamp: number; value?: number | null }

export function startOfDay(d: Date): Date {
  const x = new Date(d)
  x.setHours(0, 0, 0, 0)
  return x
}

export function addDays(d: Date, n: number): Date {
  const x = new Date(d.getTime())
  x.setDate(x.getDate() + n)
  return startOfDay(x)
}

/** 以周一为一周起始（与 NHeatmap firstDayOfWeek=1 一致） */
export function startOfWeekMonday(d: Date): Date {
  const x = startOfDay(d)
  const day = x.getDay()
  const offset = day === 0 ? -6 : 1 - day
  return addDays(x, offset)
}

export function endOfWeekMonday(d: Date): Date {
  return addDays(startOfWeekMonday(d), 6)
}

export function startOfMonth(d: Date): Date {
  return startOfDay(new Date(d.getFullYear(), d.getMonth(), 1))
}

export function endOfMonth(d: Date): Date {
  return startOfDay(new Date(d.getFullYear(), d.getMonth() + 1, 0))
}

export function startOfQuarter(d: Date): Date {
  const q = Math.floor(d.getMonth() / 3)
  return startOfDay(new Date(d.getFullYear(), q * 3, 1))
}

export function endOfQuarter(d: Date): Date {
  const q = Math.floor(d.getMonth() / 3)
  return startOfDay(new Date(d.getFullYear(), q * 3 + 3, 0))
}

function demoCompletion(day: Date, habitCount: number): number {
  if (habitCount <= 0) return 0
  const n = Math.floor(day.getTime() / 86400000)
  return Math.min(100, 28 + (n * 17 + habitCount * 31) % 68)
}

function formatZhRange(a: Date, b: Date) {
  const f = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric' })
  return `${f.format(a)} – ${f.format(b)}`
}

export function buildHabitGoalHeatmap(
  period: HabitHeatmapPeriod,
  habits: { doneToday: boolean }[],
  now = new Date(),
): { data: HeatmapDatum[]; caption: string; periodTitle: string } {
  const today = startOfDay(now)
  const habitCount = habits.length
  const doneToday = habits.filter((h) => h.doneToday).length
  const todayPct = habitCount > 0 ? Math.round((doneToday / habitCount) * 100) : null

  let periodStart: Date
  let periodEnd: Date
  let periodTitle: string

  if (period === 'week') {
    periodStart = startOfWeekMonday(today)
    periodEnd = endOfWeekMonday(today)
    periodTitle = '周目标（本周）'
  } else if (period === 'month') {
    periodStart = startOfMonth(today)
    periodEnd = endOfMonth(today)
    periodTitle = '月目标（本月）'
  } else {
    periodStart = startOfQuarter(today)
    periodEnd = endOfQuarter(today)
    periodTitle = '季目标（本季度）'
  }

  const gridStart = startOfWeekMonday(periodStart)
  const gridEnd = endOfWeekMonday(periodEnd)

  const data: HeatmapDatum[] = []
  for (let t = gridStart.getTime(); t <= gridEnd.getTime(); t += 86400000) {
    const day = startOfDay(new Date(t))
    if (day.getTime() < periodStart.getTime() || day.getTime() > periodEnd.getTime()) {
      data.push({ timestamp: day.getTime(), value: null })
      continue
    }
    if (day.getTime() > today.getTime()) {
      data.push({ timestamp: day.getTime(), value: null })
      continue
    }
    if (day.getTime() === today.getTime()) {
      data.push({ timestamp: day.getTime(), value: todayPct })
      continue
    }
    data.push({ timestamp: day.getTime(), value: demoCompletion(day, habitCount) })
  }

  const y = today.getFullYear()
  const caption =
    period === 'week'
      ? `${y} 年 · 本周：${formatZhRange(periodStart, periodEnd)}`
      : period === 'month'
        ? `${y} 年 ${today.getMonth() + 1} 月`
        : `${y} 年第 ${Math.floor(today.getMonth() / 3) + 1} 季度 · ${formatZhRange(periodStart, periodEnd)}`

  return { data, caption, periodTitle }
}
