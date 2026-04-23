/** 与 Naive 主题协调的图表色板（青绿 / 暖色 / 灰，避免亮蓝与紫色） */
export const FM_CHART_COLORS_DARK = [
  '#2dd4bf',
  '#fb7185',
  '#34d399',
  '#fb923c',
  '#fbbf24',
  '#f472b6',
  '#14b8a6',
  '#94a3b8',
  '#78716c',
  '#fcd34d',
] as const

export const FM_CHART_COLORS_LIGHT = [
  '#0d9488',
  '#f97316',
  '#10b981',
  '#ea580c',
  '#e11d48',
  '#f59e0b',
  '#14b8a6',
  '#64748b',
] as const

export type FmThemeMode = 'dark' | 'light'
export type FmPresetKey =
  | 'ocean'
  | 'aurora'
  | 'sunset'
  | 'graphite'
  | 'blackgold'
  | 'chinarred'
  | 'applemono'
  | 'sketch'
  | 'antdraw'

export function getFmEchartsTokens(mode: FmThemeMode, preset?: FmPresetKey) {
  const isDark = mode === 'dark'
  const p = preset ?? getDomThemePreset()
  const colors =
    p === 'sketch'
      ? isDark
        ? ['#4ade80', '#14b8a6', '#f87171', '#fb923c', '#f59e0b', '#f472b6', '#2dd4bf', '#a8a29e']
        : ['#16a34a', '#0d9488', '#dc2626', '#ea580c', '#b45309', '#db2777', '#0d9488', '#57534e']
      : p === 'antdraw'
        ? isDark
          ? ['#d6d3d1', '#fb923c', '#84cc16', '#f59e0b', '#f87171', '#2dd4bf', '#fdba74', '#a8a29e']
          : ['#44403c', '#ea580c', '#65a30d', '#b45309', '#dc2626', '#0d9488', '#c2410c', '#78716c']
        : p === 'aurora'
          ? isDark
            ? ['#34d399', '#f59e0b', '#2dd4bf', '#fbbf24', '#f472b6', '#14b8a6', '#fb7185', '#94a3b8']
            : ['#22c55e', '#64748b', '#0d9488', '#f97316', '#e11d48', '#14b8a6', '#f59e0b', '#475569']
          : p === 'sunset'
            ? isDark
              ? ['#fb923c', '#f472b6', '#2dd4bf', '#34d399', '#f59e0b', '#fbbf24', '#14b8a6', '#94a3b8']
              : ['#f97316', '#e11d48', '#0d9488', '#10b981', '#64748b', '#f59e0b', '#14b8a6', '#64748b']
            : p === 'graphite'
              ? isDark
                ? ['#f8fafc', '#2dd4bf', '#34d399', '#fbbf24', '#f59e0b', '#f472b6', '#14b8a6', '#fb7185']
                : ['#020617', '#0d9488', '#10b981', '#f97316', '#64748b', '#e11d48', '#f59e0b', '#64748b']
              : p === 'blackgold'
                ? isDark
                  ? ['#d4af37', '#fbbf24', '#2dd4bf', '#34d399', '#f59e0b', '#f472b6', '#94a3b8', '#fb7185']
                  : ['#b88a1e', '#111827', '#f59e0b', '#0f172a', '#d97706', '#6b7280', '#ca8a04', '#334155']
                : p === 'chinarred'
                  ? isDark
                    ? ['#ff4d4f', '#fbbf24', '#2dd4bf', '#34d399', '#f59e0b', '#f472b6', '#14b8a6', '#94a3b8']
                    : ['#d92d20', '#f59e0b', '#0d9488', '#10b981', '#111827', '#64748b', '#e11d48', '#64748b']
                  : p === 'applemono'
                    ? isDark
                      ? ['#f8fafc', '#94a3b8', '#2dd4bf', '#34d399', '#fbbf24', '#f59e0b', '#f472b6', '#14b8a6']
                      : ['#111827', '#6b7280', '#334155', '#0f172a', '#94a3b8', '#1f2937', '#475569', '#64748b']
                    : isDark
                      ? [...FM_CHART_COLORS_DARK]
                      : [...FM_CHART_COLORS_LIGHT]
  return {
    colors,
    tooltip: {
      backgroundColor: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
      borderColor: isDark ? 'rgba(148, 163, 184, 0.22)' : 'rgba(15, 23, 42, 0.12)',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: {
        color: isDark ? 'rgba(248, 250, 252, 0.95)' : 'rgba(15, 23, 42, 0.92)',
        fontSize: 12,
      },
      extraCssText: 'border-radius: 6px; box-shadow: 0 8px 28px rgba(0,0,0,0.25);',
    },
    axisLine: {
      lineStyle: { color: isDark ? 'rgba(148, 163, 184, 0.22)' : 'rgba(15, 23, 42, 0.14)' },
    },
    splitLine: {
      lineStyle: { color: isDark ? 'rgba(148, 163, 184, 0.08)' : 'rgba(15, 23, 42, 0.08)', type: 'dashed' as const },
    },
    text: {
      color: isDark ? 'rgba(226, 232, 240, 0.82)' : 'rgba(15, 23, 42, 0.82)',
      fontSize: 11,
    },
    panelBorder: isDark ? 'rgba(15, 23, 42, 0.88)' : 'rgba(255, 255, 255, 0.92)',
  }
}

export function getDomThemeMode(): FmThemeMode {
  const t = document?.documentElement?.dataset?.theme
  return t === 'light' ? 'light' : 'dark'
}

export function getDomThemePreset(): FmPresetKey {
  const p = document?.documentElement?.dataset?.preset
  if (
    p === 'aurora' ||
    p === 'sunset' ||
    p === 'graphite' ||
    p === 'ocean' ||
    p === 'blackgold' ||
    p === 'chinarred' ||
    p === 'applemono' ||
    p === 'sketch' ||
    p === 'antdraw'
  )
    return p
  return 'ocean'
}
