export type ThemePresetKey =
  | 'ocean'
  | 'aurora'
  | 'sunset'
  | 'graphite'
  | 'blackgold'
  | 'chinarred'
  | 'applemono'
  | 'sketch'
  | 'antdraw'

export type ThemePreset = {
  key: ThemePresetKey
  name: string
  /** 主色（按钮/高亮） */
  primary: { light: string; dark: string }
  /** 辅色（渐变/点缀） */
  accent: { light: string; dark: string }
  /** 背景渐变（body） */
  bg: { light: string; dark: string }
  /** 毛玻璃容器底色（侧栏/卡片叠层） */
  glass: { light: string; dark: string }
  /** 用于左侧发光把手 */
  glow: { light: string; dark: string }
  /** 图表色板（按模式） */
  chart: { light: string[]; dark: string[] }
}

export const THEME_PRESETS: ThemePreset[] = [
  {
    key: 'blackgold',
    name: 'Black Gold（黑金）',
    primary: { light: '#b88a1e', dark: '#d4af37' },
    accent: { light: '#111827', dark: '#fbbf24' },
    bg: {
      light:
        'radial-gradient(1200px 700px at 10% -10%, rgba(212,175,55,0.16), transparent), radial-gradient(900px 600px at 90% 0%, rgba(17,24,39,0.08), transparent), linear-gradient(165deg, #ffffff 0%, #f8fafc 55%, #fff7e6 100%)',
      dark:
        'radial-gradient(1200px 600px at 12% -8%, rgba(212,175,55,0.18), transparent), radial-gradient(900px 520px at 100% 0%, rgba(251,191,36,0.10), transparent), linear-gradient(165deg, #050607 0%, #0a0b10 45%, #0b1220 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.92)', dark: 'rgba(10,10,12,0.38)' },
    glow: { light: 'rgba(184,138,30,0.95)', dark: 'rgba(212,175,55,0.92)' },
    chart: {
      light: ['#b88a1e', '#111827', '#f59e0b', '#0f172a', '#d97706', '#6b7280', '#ca8a04', '#334155'],
      dark: ['#d4af37', '#fbbf24', '#2dd4bf', '#34d399', '#f59e0b', '#f472b6', '#94a3b8', '#fb7185'],
    },
  },
  {
    key: 'chinarred',
    name: 'China Red（中国红）',
    primary: { light: '#d92d20', dark: '#ff4d4f' },
    accent: { light: '#f59e0b', dark: '#fbbf24' },
    bg: {
      light:
        'radial-gradient(1200px 700px at 12% -8%, rgba(217,45,32,0.18), transparent), radial-gradient(900px 600px at 90% 0%, rgba(245,158,11,0.10), transparent), linear-gradient(165deg, #ffffff 0%, #fff1f2 52%, #f8fafc 100%)',
      dark:
        'radial-gradient(1200px 600px at 10% -10%, rgba(255,77,79,0.18), transparent), radial-gradient(900px 520px at 100% 0%, rgba(251,191,36,0.10), transparent), linear-gradient(165deg, #07060a 0%, #12060a 45%, #0b1220 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.92)', dark: 'rgba(17, 0, 6, 0.38)' },
    glow: { light: 'rgba(217,45,32,0.95)', dark: 'rgba(255,77,79,0.92)' },
    chart: {
      light: ['#d92d20', '#f59e0b', '#0d9488', '#10b981', '#111827', '#64748b', '#e11d48', '#64748b'],
      dark: ['#ff4d4f', '#fbbf24', '#2dd4bf', '#34d399', '#f59e0b', '#f472b6', '#14b8a6', '#94a3b8'],
    },
  },
  {
    key: 'applemono',
    name: 'Apple Mono（黑白极简）',
    primary: { light: '#111827', dark: '#f8fafc' },
    accent: { light: '#6b7280', dark: '#94a3b8' },
    bg: {
      light:
        'radial-gradient(1200px 700px at 12% -8%, rgba(17,24,39,0.06), transparent), radial-gradient(900px 600px at 90% 0%, rgba(148,163,184,0.08), transparent), linear-gradient(165deg, #ffffff 0%, #f8fafc 58%, #ffffff 100%)',
      dark:
        'radial-gradient(1100px 600px at 12% -8%, rgba(248,250,252,0.08), transparent), radial-gradient(900px 520px at 100% 0%, rgba(148,163,184,0.08), transparent), linear-gradient(165deg, #000000 0%, #050607 40%, #0b1220 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.94)', dark: 'rgba(0,0,0,0.40)' },
    glow: { light: 'rgba(17,24,39,0.75)', dark: 'rgba(248,250,252,0.60)' },
    chart: {
      light: ['#111827', '#6b7280', '#334155', '#0f172a', '#94a3b8', '#1f2937', '#475569', '#64748b'],
      dark: ['#f8fafc', '#94a3b8', '#2dd4bf', '#34d399', '#fbbf24', '#f59e0b', '#f472b6', '#14b8a6'],
    },
  },
  {
    key: 'ocean',
    name: 'Ocean（海青）',
    primary: { light: '#0d9488', dark: '#2dd4bf' },
    accent: { light: '#059669', dark: '#34d399' },
    bg: {
      light:
        'radial-gradient(1200px 700px at 10% -10%, rgba(13,148,136,0.12), transparent), radial-gradient(900px 600px at 100% 0%, rgba(16,185,129,0.10), transparent), linear-gradient(165deg, #f8fafc 0%, #f1f5f9 52%, #f8fafc 100%)',
      dark:
        'radial-gradient(1200px 600px at 10% -10%, rgba(45,212,191,0.14), transparent), radial-gradient(900px 500px at 100% 0%, rgba(148,163,184,0.10), transparent), linear-gradient(165deg, #070b14 0%, #0c1222 40%, #0a1620 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.55)', dark: 'rgba(15,23,42,0.28)' },
    glow: { light: 'rgba(13,148,136,0.70)', dark: 'rgba(45,212,191,0.55)' },
    chart: {
      light: ['#0d9488', '#10b981', '#f97316', '#64748b', '#e11d48', '#f59e0b', '#14b8a6', '#64748b'],
      dark: ['#2dd4bf', '#f472b6', '#34d399', '#f59e0b', '#fbbf24', '#fb7185', '#14b8a6', '#94a3b8', '#78716c'],
    },
  },
  {
    key: 'aurora',
    name: 'Aurora（极光）',
    primary: { light: '#22c55e', dark: '#34d399' },
    accent: { light: '#64748b', dark: '#f59e0b' },
    bg: {
      light:
        'radial-gradient(1100px 700px at 12% -8%, rgba(34,197,94,0.14), transparent), radial-gradient(900px 600px at 90% 0%, rgba(148,163,184,0.12), transparent), linear-gradient(165deg, #f8fafc 0%, #f1f5f9 55%, #f8fafc 100%)',
      dark:
        'radial-gradient(1200px 600px at 10% -10%, rgba(52,211,153,0.14), transparent), radial-gradient(900px 500px at 100% 0%, rgba(251,191,36,0.10), transparent), linear-gradient(165deg, #050711 0%, #0a1224 42%, #081a2c 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.56)', dark: 'rgba(12,18,34,0.30)' },
    glow: { light: 'rgba(34,197,94,0.85)', dark: 'rgba(52,211,153,0.80)' },
    chart: {
      light: ['#22c55e', '#64748b', '#0d9488', '#f97316', '#e11d48', '#14b8a6', '#f59e0b', '#475569'],
      dark: ['#34d399', '#f59e0b', '#2dd4bf', '#fbbf24', '#f472b6', '#14b8a6', '#fb7185', '#94a3b8'],
    },
  },
  {
    key: 'sunset',
    name: 'Sunset（日落）',
    primary: { light: '#f97316', dark: '#fb923c' },
    accent: { light: '#ec4899', dark: '#f472b6' },
    bg: {
      light:
        'radial-gradient(1200px 700px at 12% -8%, rgba(249,115,22,0.16), transparent), radial-gradient(900px 600px at 90% 0%, rgba(236,72,153,0.12), transparent), linear-gradient(165deg, #fff7ed 0%, #f8fafc 55%, #f1f5f9 100%)',
      dark:
        'radial-gradient(1200px 600px at 10% -10%, rgba(251,146,60,0.14), transparent), radial-gradient(900px 520px at 100% 0%, rgba(244,114,182,0.12), transparent), linear-gradient(165deg, #08060f 0%, #121026 45%, #0c1b2a 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.58)', dark: 'rgba(17,24,39,0.28)' },
    glow: { light: 'rgba(249,115,22,0.90)', dark: 'rgba(251,146,60,0.82)' },
    chart: {
      light: ['#f97316', '#e11d48', '#0d9488', '#10b981', '#64748b', '#f59e0b', '#14b8a6', '#64748b'],
      dark: ['#fb923c', '#f472b6', '#2dd4bf', '#34d399', '#f59e0b', '#fbbf24', '#14b8a6', '#94a3b8'],
    },
  },
  {
    key: 'graphite',
    name: 'Graphite（石墨）',
    /** 与正文 slate 灰拉开差距：选用接近黑/近白，避免与 textColor2 混淆 */
    primary: { light: '#020617', dark: '#f8fafc' },
    accent: { light: '#0d9488', dark: '#2dd4bf' },
    bg: {
      light:
        'radial-gradient(1200px 700px at 10% -10%, rgba(148,163,184,0.18), transparent), radial-gradient(900px 600px at 90% 0%, rgba(100,116,139,0.10), transparent), linear-gradient(165deg, #f8fafc 0%, #f1f5f9 58%, #f8fafc 100%)',
      dark:
        'radial-gradient(1100px 600px at 12% -8%, rgba(148,163,184,0.12), transparent), radial-gradient(900px 520px at 100% 0%, rgba(100,116,139,0.10), transparent), linear-gradient(165deg, #060812 0%, #0b1220 45%, #081626 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.56)', dark: 'rgba(10,16,28,0.32)' },
    glow: { light: 'rgba(51,65,85,0.70)', dark: 'rgba(148,163,184,0.55)' },
    chart: {
      light: ['#020617', '#0d9488', '#10b981', '#f97316', '#64748b', '#e11d48', '#f59e0b', '#64748b'],
      dark: ['#f8fafc', '#2dd4bf', '#34d399', '#fbbf24', '#f59e0b', '#f472b6', '#14b8a6', '#fb7185'],
    },
  },
  {
    key: 'sketch',
    name: '涂鸦插画（手帐 / 新粗野风）',
    primary: { light: '#16a34a', dark: '#4ade80' },
    accent: { light: '#b45309', dark: '#fbbf24' },
    bg: {
      light:
        'url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'180\' height=\'180\' viewBox=\'0 0 180 180\'%3E%3Cg fill=\'none\' stroke=\'%23c4a574\' stroke-width=\'1.2\' opacity=\'0.38\'%3E%3Cpath d=\'M12 24l14 18M130 20l16-10M88 140q12 14-8 18M150 96l10 12-8 10-10-12z\'/%3E%3Ccircle cx=\'48\' cy=\'52\' r=\'3\'/%3E%3Cpath d=\'M164 154l6 8-6 8-6-8z\' fill=\'%23fbbf24\' stroke=\'%23a16207\'/%3E%3C/g%3E%3C/svg%3E") repeat left top / 180px 180px, linear-gradient(168deg, #faf6ef 0%, #f3ead6 42%, #faf8f2 100%)',
      dark:
        'url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'180\' height=\'180\' viewBox=\'0 0 180 180\'%3E%3Cg fill=\'none\' stroke=\'%238b7355\' stroke-width=\'1.1\' opacity=\'0.28\'%3E%3Cpath d=\'M12 24l14 18M130 20l16-10M88 140q12 14-8 18\'/%3E%3Ccircle cx=\'48\' cy=\'52\' r=\'3\'/%3E%3C/g%3E%3C/svg%3E") repeat left top / 180px 180px, linear-gradient(168deg, #1a1714 0%, #252019 48%, #1c1916 100%)',
    },
    glass: { light: 'rgba(255,255,255,0.94)', dark: 'rgba(44,40,36,0.94)' },
    glow: { light: 'rgba(22,163,74,0.9)', dark: 'rgba(74,222,128,0.8)' },
    chart: {
      light: ['#16a34a', '#0d9488', '#dc2626', '#ea580c', '#b45309', '#db2777', '#0d9488', '#57534e'],
      dark: ['#4ade80', '#14b8a6', '#f87171', '#fb923c', '#f59e0b', '#f472b6', '#2dd4bf', '#a8a29e'],
    },
  },
  {
    key: 'antdraw',
    name: 'Ant 暖灰',
    /** 偏暖的中性色 + 琥珀强调 */
    primary: { light: '#44403c', dark: '#d6d3d1' },
    accent: { light: '#ea580c', dark: '#fb923c' },
    bg: {
      light:
        'radial-gradient(1100px 720px at 8% -6%, rgba(253,186,116,0.14), transparent), radial-gradient(900px 560px at 92% 4%, rgba(251,191,36,0.10), transparent), linear-gradient(168deg, #fffaf5 0%, #fffdfb 45%, #fef3e7 100%)',
      dark:
        'radial-gradient(1100px 620px at 10% -8%, rgba(180,83,9,0.12), transparent), radial-gradient(880px 500px at 96% 0%, rgba(245,158,11,0.08), transparent), linear-gradient(168deg, #100f0c 0%, #1a1814 42%, #121110 100%)',
    },
    glass: { light: 'rgba(255,252,250,0.78)', dark: 'rgba(28,25,23,0.5)' },
    glow: { light: 'rgba(234,88,12,0.45)', dark: 'rgba(251,146,60,0.42)' },
    chart: {
      light: ['#44403c', '#ea580c', '#65a30d', '#b45309', '#dc2626', '#0d9488', '#c2410c', '#78716c'],
      dark: ['#d6d3d1', '#fb923c', '#84cc16', '#f59e0b', '#f87171', '#2dd4bf', '#fdba74', '#a8a29e'],
    },
  },
]

export function getPreset(key: ThemePresetKey): ThemePreset {
  return THEME_PRESETS.find((p) => p.key === key) ?? THEME_PRESETS[0]
}

