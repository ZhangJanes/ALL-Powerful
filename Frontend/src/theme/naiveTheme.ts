import type { GlobalThemeOverrides } from 'naive-ui'
import type { ThemePresetKey } from '@/theme/presets'
import { getPreset } from '@/theme/presets'
export function getNaiveThemeOverrides(
  mode: 'light' | 'dark',
  presetKey: ThemePresetKey,
): GlobalThemeOverrides {
  const p = getPreset(presetKey)
  const primaryBase = mode === 'dark' ? p.primary.dark : p.primary.light
  const accentBase = mode === 'dark' ? p.accent.dark : p.accent.light
  const isDark = mode === 'dark'

  const primary = primaryBase
  const accent = accentBase

  /** 石墨：未选 vs 已选（高对比） */
  const graphite = {
    sel: isDark ? '#f8fafc' : '#020617',
    unsel: isDark ? 'rgba(163, 163, 163, 0.88)' : 'rgba(100, 116, 139, 0.9)',
  }

  const ink = isDark ? '#ece8df' : '#4a3f36'
  const cream = isDark ? '#1f1c18' : '#faf6ef'
  const paper = isDark ? '#2c2824' : '#ffffff'
  const pop = isDark ? '#0a0908' : 'rgba(74, 63, 54, 0.88)'

  if (presetKey === 'sketch') {
    const sketchPrimary = primaryBase
    const sketchAccent = accentBase
    return {
      common: {
        primaryColor: sketchPrimary,
        primaryColorHover: sketchPrimary,
        primaryColorPressed: sketchPrimary,
        primaryColorSuppl: sketchAccent,
        borderRadius: '10px',
        fontFamily: 'var(--fm-font-sans)',
        fontSize: '14.5px',
        heightTiny: '28px',
        heightSmall: '34px',
        heightMedium: '40px',
        heightLarge: '46px',
        bodyColor: cream,
        cardColor: paper,
        modalColor: paper,
        popoverColor: paper,
        tableColor: isDark ? 'rgba(44, 40, 36, 0.65)' : 'rgba(255, 255, 255, 0.92)',
        textColorBase: isDark ? '#f5f2eb' : ink,
        textColor1: isDark ? 'rgba(245, 242, 235, 0.94)' : 'rgba(20, 20, 20, 0.92)',
        textColor2: isDark ? 'rgba(212, 206, 196, 0.88)' : 'rgba(55, 48, 40, 0.88)',
        textColor3: isDark ? 'rgba(168, 162, 158, 0.85)' : 'rgba(87, 83, 78, 0.85)',
        textColorDisabled: isDark ? 'rgba(120, 113, 108, 0.75)' : 'rgba(120, 113, 108, 0.65)',
        borderColor: ink,
      },
      Card: {
        color: paper,
        titleTextColor: isDark ? '#f5f2eb' : ink,
        textColor: isDark ? 'rgba(232, 228, 220, 0.9)' : 'rgba(30, 27, 24, 0.9)',
        borderColor: ink,
      },
      Layout: { color: 'transparent' },
      Tabs: {
        tabTextColorActiveBar: sketchPrimary,
        barColor: sketchPrimary,
      },
      Button: {
        fontWeight: '700',
        heightTiny: '28px',
        heightSmall: '34px',
        heightMedium: '40px',
        heightLarge: '46px',
        borderRadiusTiny: '8px',
        borderRadiusSmall: '10px',
        borderRadiusMedium: '12px',
        borderRadiusLarge: '14px',
        border: `2px solid ${ink}`,
        borderPrimary: `2px solid ${ink}`,
        textColorPrimary: isDark ? '#14532d' : '#ffffff',
      },
      Radio: {
        buttonBorderColor: ink,
        buttonBorderColorActive: ink,
        buttonBorderColorHover: ink,
        buttonBorderRadius: '10px',
        buttonBoxShadow: `3px 3px 0 ${pop}`,
        buttonBoxShadowHover: `4px 4px 0 ${pop}`,
        buttonBoxShadowFocus: `3px 3px 0 ${pop}`,
        buttonColor: isDark ? paper : '#ffffff',
        buttonColorActive: sketchPrimary,
        buttonTextColor: isDark ? '#f5f2eb' : ink,
        buttonTextColorActive: isDark ? '#14532d' : '#2d241c',
        buttonTextColorHover: isDark ? '#f5f2eb' : ink,
      },
      Input: {
        border: `2px solid ${ink}`,
        borderHover: `2px solid ${ink}`,
        borderFocus: `2px solid ${ink}`,
        boxShadowFocus: `3px 3px 0 ${pop}`,
      },
      Select: {
        menuBoxShadow: `6px 6px 0 ${pop}`,
        peers: {
          InternalSelection: {
            border: `2px solid ${ink}`,
            borderHover: `2px solid ${ink}`,
            borderActive: `2px solid ${ink}`,
            borderFocus: `2px solid ${ink}`,
            boxShadowFocus: `3px 3px 0 ${pop}`,
            boxShadowHover: `4px 4px 0 ${pop}`,
            boxShadowActive: `3px 3px 0 ${pop}`,
          },
        },
      },
      Checkbox: {
        border: `2px solid ${ink}`,
        borderFocus: `2px solid ${ink}`,
        borderChecked: `2px solid ${ink}`,
        borderDisabled: `2px solid ${ink}`,
        boxShadowFocus: `2px 2px 0 ${pop}`,
      },
      Tag: {
        border: `2px solid ${ink}`,
        fontWeightStrong: '700',
      },
      List: {
        borderColor: ink,
      },
      Switch: {
        buttonBoxShadow: `2px 2px 0 ${pop}`,
        boxShadowFocus: `0 0 0 2px ${paper}, 3px 3px 0 ${pop}`,
      },
      Empty: {
        textColor: isDark ? 'rgba(245, 242, 235, 0.88)' : ink,
      },
    }
  }

  return {
    common: {
      primaryColor: primary,
      primaryColorHover: primary,
      primaryColorPressed: primary,
      primaryColorSuppl: accent,
      borderRadius: '14px',
      fontFamily: 'var(--fm-font-sans)',
      fontSize: '14.5px',
      heightTiny: '28px',
      heightSmall: '34px',
      heightMedium: '40px',
      heightLarge: '46px',

      // 关键：浅色要更“干净”，深色要更“通透”
      bodyColor: isDark ? '#0b1220' : '#f8fafc',
      cardColor: isDark ? 'rgba(18, 28, 48, 0.72)' : 'rgba(255, 255, 255, 0.92)',
      modalColor: isDark ? 'rgba(17, 24, 39, 0.96)' : '#ffffff',
      popoverColor: isDark ? 'rgba(17, 24, 39, 0.96)' : '#ffffff',
      tableColor: isDark ? 'rgba(15, 23, 42, 0.60)' : 'rgba(255, 255, 255, 0.86)',
      // 浅色：正文/次要文字加深，避免发灰看不清
      ...(isDark
        ? {}
        : {
            textColorBase: '#0f172a',
            textColor1: 'rgba(15, 23, 42, 0.92)',
            textColor2: 'rgba(51, 65, 85, 0.9)',
            textColor3: 'rgba(71, 85, 105, 0.82)',
            textColorDisabled: 'rgba(148, 163, 184, 0.85)',
          }),
    },
    Card: {
      color: isDark ? 'rgba(17, 24, 39, 0.55)' : 'rgba(255, 255, 255, 0.90)',
      borderColor: isDark ? 'rgba(148, 163, 184, 0.12)' : 'rgba(15, 23, 42, 0.10)',
      titleFontWeight: '700',
    },
    Layout: { color: 'transparent' },
    Tabs:
      presetKey === 'graphite'
        ? {
            tabTextColorLine: graphite.unsel,
            tabTextColorActiveLine: graphite.sel,
            tabTextColorHoverLine: graphite.sel,
            tabTextColorBar: graphite.unsel,
            tabTextColorActiveBar: graphite.sel,
            tabTextColorHoverBar: graphite.sel,
            barColor: graphite.sel,
            tabTextColorSegment: graphite.unsel,
            tabTextColorActiveSegment: graphite.sel,
            tabTextColorHoverSegment: graphite.sel,
            tabTextColorCard: graphite.unsel,
            tabTextColorActiveCard: graphite.sel,
            tabTextColorHoverCard: graphite.sel,
          }
        : {
            tabTextColorActiveBar: primary,
            barColor: primary,
          },
    Radio:
      presetKey === 'graphite'
        ? {
            buttonTextColor: graphite.unsel,
            buttonTextColorHover: graphite.sel,
            buttonTextColorActive: isDark ? '#0c0a09' : '#ffffff',
            buttonColor: isDark ? 'rgba(38, 38, 38, 0.55)' : 'rgba(255, 255, 255, 0.96)',
            buttonColorActive: primary,
            buttonBorderColor: isDark ? 'rgba(120, 113, 108, 0.35)' : 'rgba(148, 163, 184, 0.45)',
            buttonBorderColorActive: primary,
          }
        : undefined,
    Button: {
      fontWeight: '600',
      heightTiny: '28px',
      heightSmall: '34px',
      heightMedium: '40px',
      heightLarge: '46px',
      borderRadiusTiny: '8px',
      borderRadiusSmall: '10px',
      borderRadiusMedium: '12px',
      borderRadiusLarge: '14px',
    },
  }
}
