import type { EChartsOption } from 'echarts'
import { getDomThemeMode, getDomThemePreset, getFmEchartsTokens } from '@/theme/echartsFm'

function getFmSansFontFamily() {
  const v = getComputedStyle(document.documentElement).getPropertyValue('--fm-font-sans').trim()
  return v || 'Lato, system-ui, sans-serif'
}

export type LedgerLike = { at: string; type: string; amount: number }

/** 近 N 天每日支出聚合（用于折线图） */
export function aggregateExpenseByDay(ledger: LedgerLike[], days = 7) {
  const categories: string[] = []
  const values: number[] = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const ymd = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    categories.push(`${d.getMonth() + 1}/${d.getDate()}`)
    const sum = ledger
      .filter((e) => e.type === 'expense' && e.at.slice(0, 10) === ymd)
      .reduce((s, e) => s + e.amount, 0)
    values.push(sum)
  }
  return { categories, values }
}

/** 折线/面积：近 N 日支出等 */
export function buildExpenseLineOption(categories: string[], values: number[], title?: string): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    title: title
      ? {
          text: title,
          left: 8,
          top: 4,
          textStyle: { ...t.text, fontSize: 12, fontWeight: 600 },
        }
      : undefined,
    tooltip: {
      trigger: 'axis',
      ...t.tooltip,
      axisPointer: { type: 'cross', crossStyle: { color: 'rgba(148,163,184,0.35)' } },
    },
    grid: { left: '2%', right: '3%', bottom: '4%', top: title ? 36 : 12, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      axisLine: t.axisLine,
      axisLabel: { ...t.text, fontSize: 10 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { ...t.text, formatter: (v: number) => (v >= 1000 ? `${(v / 1000).toFixed(1)}k` : `${v}`) },
      splitLine: t.splitLine,
    },
    series: [
      {
        name: '金额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: true,
        lineStyle: { width: 2.5, shadowBlur: 12, shadowColor: 'rgba(13, 148, 136, 0.22)' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(13, 148, 136, 0.32)' },
              { offset: 1, color: 'rgba(13, 148, 136, 0.02)' },
            ],
          },
        },
        itemStyle: {
          color: t.colors[0] ?? '#0d9488',
          borderColor: t.panelBorder,
          borderWidth: 2,
          shadowBlur: 8,
          shadowColor: 'rgba(13, 148, 136, 0.35)',
        },
        data: values,
      },
    ],
  }
}

function movingAverage(values: number[], window = 3) {
  const w = Math.max(1, Math.floor(window))
  return values.map((_, i) => {
    const start = Math.max(0, i - w + 1)
    const slice = values.slice(start, i + 1)
    const sum = slice.reduce((s, v) => s + v, 0)
    return Number((sum / slice.length).toFixed(2))
  })
}

/** 组合图：柱状（日支出）+ 折线（移动平均趋势） */
export function buildExpenseLineBarOption(categories: string[], values: number[], title?: string): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  const ma = movingAverage(values, 3)
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    title: title
      ? {
          text: title,
          left: 8,
          top: 4,
          textStyle: { ...t.text, fontSize: 12, fontWeight: 600 },
        }
      : undefined,
    tooltip: {
      trigger: 'axis',
      ...t.tooltip,
      axisPointer: { type: 'cross', crossStyle: { color: 'rgba(148,163,184,0.35)' } },
    },
    legend: {
      top: title ? 26 : 6,
      right: 10,
      textStyle: { ...t.text, fontSize: 10 },
      itemWidth: 10,
      itemHeight: 10,
    },
    grid: { left: '2%', right: '3%', bottom: '6%', top: title ? 46 : 22, containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: t.axisLine,
      axisLabel: { ...t.text, fontSize: 10 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { ...t.text, formatter: (v: number) => (v >= 1000 ? `${(v / 1000).toFixed(1)}k` : `${v}`) },
      splitLine: t.splitLine,
    },
    series: [
      {
        name: '日支出',
        type: 'bar',
        data: values,
        barWidth: '52%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          opacity: 0.75,
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.18)',
        },
        emphasis: { focus: 'series' },
      },
      {
        name: '趋势（3日均值）',
        type: 'line',
        data: ma,
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: true,
        lineStyle: { width: 2.5, shadowBlur: 12, shadowColor: 'rgba(13, 148, 136, 0.22)' },
        itemStyle: {
          color: t.colors[0] ?? '#0d9488',
          borderColor: t.panelBorder,
          borderWidth: 2,
          shadowBlur: 8,
          shadowColor: 'rgba(13, 148, 136, 0.35)',
        },
      },
    ],
  }
}

/** 饼图 / 环形图：分类占比 */
export function buildCategoryPieOption(
  data: { name: string; value: number }[],
  opts?: { donut?: boolean; title?: string },
): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  const donut = opts?.donut ?? false
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    title: opts?.title
      ? {
          text: opts.title,
          left: 8,
          top: 4,
          textStyle: { ...t.text, fontSize: 12, fontWeight: 600 },
        }
      : undefined,
    tooltip: {
      trigger: 'item',
      ...t.tooltip,
      formatter: '{b}<br/>¥{c}（{d}%）',
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      bottom: 0,
      textStyle: { ...t.text, fontSize: 10 },
      pageIconColor: '#94a3b8',
      pageTextStyle: { color: t.text.color },
    },
    series: [
      {
        name: '分类',
        type: 'pie',
        radius: donut ? ['44%', '68%'] : ['0%', '68%'],
        center: ['50%', donut ? '46%' : '48%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: t.panelBorder,
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.35)',
        },
        label: {
          color: t.text.color,
          fontSize: 10,
          formatter: '{b}\n{d}%',
        },
        labelLine: { lineStyle: { color: 'rgba(148,163,184,0.35)' } },
        data,
      },
    ],
  }
}

/** 环形：预算已用 / 剩余（超支时显示「预算内 + 超出」） */
export function buildBudgetRingOption(spent: number, budget: number): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  if (budget <= 0) {
    return buildCategoryPieOption([{ name: '未设预算', value: 1 }], { donut: true })
  }

  const rest = Math.max(0, budget - spent)
  const data =
    spent <= budget
      ? [
          { name: '已支出', value: spent },
          { name: '剩余', value: rest },
        ]
      : [
          { name: '预算内', value: budget, itemStyle: { color: '#0d9488' } },
          { name: '超出', value: spent - budget, itemStyle: { color: '#fb7185' } },
        ]

  const pct = Math.min(100, Math.round((spent / budget) * 100))

  return {
    backgroundColor: 'transparent',
    color: ['#0d9488', '#34d399', '#fb7185'],
    title: {
      text: spent <= budget ? `${pct}%` : '超支',
      subtext: spent <= budget ? `已用 ¥${spent}\n预算 ¥${budget}` : `已用 ¥${spent}\n预算 ¥${budget}`,
      left: 'center',
      top: 'middle',
      textAlign: 'center',
      textVerticalAlign: 'middle',
      itemGap: 6,
      textStyle: {
        fontSize: 26,
        fontWeight: 800,
        color: t.text.color,
        fontFamily: getFmSansFontFamily(),
      },
      subtextStyle: {
        fontSize: 11,
        color: t.text.color,
        lineHeight: 16,
      },
    },
    tooltip: { trigger: 'item', ...t.tooltip, formatter: '{b}<br/>¥{c}' },
    series: [
      {
        type: 'pie',
        radius: ['52%', '76%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: t.panelBorder,
          borderWidth: 2,
          shadowBlur: 12,
          shadowColor: 'rgba(0, 0, 0, 0.4)',
        },
        label: { show: false },
        labelLine: { show: false },
        data,
      },
    ],
  }
}

/** 柱状：近 N 日每日支出（补充指标） */
export function buildExpenseBarOption(categories: string[], values: number[], title?: string): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  const max = Math.max(0, ...values)
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    title: title
      ? {
          text: title,
          left: 8,
          top: 4,
          textStyle: { ...t.text, fontSize: 12, fontWeight: 600 },
        }
      : undefined,
    tooltip: { trigger: 'axis', ...t.tooltip, axisPointer: { type: 'shadow' } },
    grid: { left: '2%', right: '3%', bottom: '6%', top: title ? 36 : 12, containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: t.axisLine,
      axisLabel: { ...t.text, fontSize: 10 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { ...t.text, formatter: (v: number) => (v >= 1000 ? `${(v / 1000).toFixed(1)}k` : `${v}`) },
      splitLine: t.splitLine,
    },
    series: [
      {
        name: '支出',
        type: 'bar',
        data: values,
        barWidth: '52%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.22)',
        },
        emphasis: { focus: 'series' },
        markLine:
          max > 0
            ? {
                symbol: ['none', 'none'],
                label: { show: false },
                lineStyle: { color: 'rgba(148,163,184,0.35)', type: 'dashed' },
                data: [{ yAxis: Math.round(max * 0.7) }],
              }
            : undefined,
      },
    ],
  }
}

/** 今日打卡完成比例（环形） */
export function buildHabitDonutOption(done: number, total: number): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  const pct = total > 0 ? Math.round((done / total) * 100) : 0

  let data: { name: string; value: number; itemStyle?: { color: string } }[]
  if (total <= 0) {
    data = [{ name: '暂无任务', value: 1 }]
  } else if (done >= total) {
    data = [{ name: '已完成', value: done }]
  } else if (done <= 0) {
    data = [{ name: '待完成', value: total, itemStyle: { color: 'rgba(100, 116, 139, 0.55)' } }]
  } else {
    data = [
      { name: '已完成', value: done },
      { name: '待完成', value: total - done, itemStyle: { color: 'rgba(100, 116, 139, 0.45)' } },
    ]
  }

  return {
    backgroundColor: 'transparent',
    color: ['#34d399', 'rgba(100, 116, 139, 0.45)'],
    title: {
      text: total > 0 ? `${done}/${total}` : '—',
      subtext: total > 0 ? `完成度 ${pct}%\n今日打卡` : '暂无打卡任务',
      left: 'center',
      top: '40%',
      textAlign: 'center',
      itemGap: 6,
      textStyle: {
        fontSize: 24,
        fontWeight: 800,
        color: t.text.color,
        fontFamily: getFmSansFontFamily(),
      },
      subtextStyle: {
        fontSize: 11,
        color: t.text.color,
        lineHeight: 16,
      },
    },
    tooltip: { trigger: 'item', ...t.tooltip },
    series: [
      {
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['50%', '50%'],
        itemStyle: {
          borderRadius: 4,
          borderColor: t.panelBorder,
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.35)',
        },
        label: { show: false },
        labelLine: { show: false },
        data,
      },
    ],
  }
}

/** 横向条形：分类对比（可选） */
export function buildCategoryBarOption(items: { name: string; value: number }[]): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  const names = items.map((i) => i.name)
  const vals = items.map((i) => i.value)
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    tooltip: { trigger: 'axis', ...t.tooltip, axisPointer: { type: 'shadow' } },
    grid: { left: '4%', right: '6%', bottom: '4%', top: '4%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLine: t.axisLine,
      axisLabel: t.text,
      splitLine: t.splitLine,
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: t.axisLine,
      axisLabel: { ...t.text, fontSize: 10 },
      axisTick: { show: false },
    },
    series: [
      {
        type: 'bar',
        data: vals,
        barWidth: '55%',
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          shadowBlur: 8,
          shadowColor: 'rgba(13, 148, 136, 0.22)',
        },
      },
    ],
  }
}
