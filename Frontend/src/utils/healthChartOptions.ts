import type { EChartsOption } from 'echarts'
import { getDomThemeMode, getDomThemePreset, getFmEchartsTokens } from '@/theme/echartsFm'
import type { HealthTrendPointDto } from '@/api/health'

export function buildBodyTrendOption(points: HealthTrendPointDto[]): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    tooltip: { trigger: 'axis', ...t.tooltip },
    legend: { top: 4, right: 8, textStyle: t.text },
    grid: { left: '2%', right: '3%', top: 44, bottom: '4%', containLabel: true },
    xAxis: {
      type: 'category',
      data: points.map((x) => x.date.slice(5)),
      axisLine: t.axisLine,
      axisLabel: { ...t.text, fontSize: 10 },
    },
    yAxis: [
      { type: 'value', name: 'kg', axisLabel: t.text, splitLine: t.splitLine },
      { type: 'value', name: '%', axisLabel: t.text, splitLine: { show: false } },
    ],
    series: [
      {
        name: '体重',
        type: 'line',
        smooth: true,
        connectNulls: true,
        symbolSize: 7,
        data: points.map((x) => x.weightKg ?? null),
        lineStyle: { width: 3 },
        areaStyle: { opacity: 0.08 },
      },
      {
        name: '体脂率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        connectNulls: true,
        symbolSize: 7,
        data: points.map((x) => x.bodyFatPercent ?? null),
        lineStyle: { width: 2 },
      },
    ],
  }
}

export function buildExecutionTrendOption(points: HealthTrendPointDto[]): EChartsOption {
  const t = getFmEchartsTokens(getDomThemeMode(), getDomThemePreset())
  return {
    backgroundColor: 'transparent',
    color: t.colors,
    tooltip: { trigger: 'axis', ...t.tooltip },
    legend: { top: 4, right: 8, textStyle: t.text },
    grid: { left: '2%', right: '3%', top: 44, bottom: '4%', containLabel: true },
    xAxis: {
      type: 'category',
      data: points.map((x) => x.date.slice(5)),
      axisLine: t.axisLine,
      axisLabel: { ...t.text, fontSize: 10 },
    },
    yAxis: { type: 'value', axisLabel: t.text, splitLine: t.splitLine },
    series: [
      {
        name: '饮水 ml',
        type: 'bar',
        data: points.map((x) => x.waterMl),
        barMaxWidth: 20,
        itemStyle: { borderRadius: [5, 5, 0, 0], opacity: 0.72 },
      },
      {
        name: '运动分钟',
        type: 'line',
        smooth: true,
        data: points.map((x) => x.exerciseMinutes),
        symbolSize: 6,
        lineStyle: { width: 2.5 },
      },
    ],
  }
}
