/*
 * @Author: zhangjianing 1026597665@qq.com
 * @Date: 2026-04-16 10:43:51
 * @LastEditors: zhangjianing 1026597665@qq.com
 * @LastEditTime: 2026-04-16 14:25:40
 * @FilePath: /Home-App/ALL-Powerful/Frontend/src/stores/settings.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import type { ThemePresetKey } from '@/theme/presets'
import { getPreset } from '@/theme/presets'

export type ThemeMode = 'light' | 'dark'
export type FontMode = 'default' | 'alimama' | 'cute' | 'bobo' | 'maiyuan'

const LS_MODE = 'fm.theme.mode'
const LS_PRESET = 'fm.theme.preset'
const LS_FONT = 'fm.font.mode'

function normalizeMode(raw: unknown): ThemeMode {
    if (raw === 'light' || raw === 'dark') return raw
    // 兼容旧值（含已移除的 blue / purple 色调），统一回落到深色
    return 'dark'
}

function normalizeFontMode(raw: unknown): FontMode {
    if (raw === 'alimama' || raw === 'cute' || raw === 'bobo' || raw === 'maiyuan' || raw === 'default') return raw
    return 'default'
}

function applyDomTheme(theme: 'light' | 'dark', preset: ThemePresetKey, font: FontMode) {
    document.documentElement.dataset.theme = theme
    document.documentElement.dataset.preset = preset
    document.documentElement.dataset.font = font
    document.documentElement.style.colorScheme = theme
    const meta = document.querySelector('meta[name="theme-color"]') as HTMLMetaElement | null
    const p = getPreset(preset)
    if (meta) {
        meta.content = theme === 'dark' ? p.primary.dark : p.primary.light
    }
}

export const useSettingsStore = defineStore('settings', () => {
    const themeMode = ref<ThemeMode>(normalizeMode(localStorage.getItem(LS_MODE)))
    const themePreset = ref<ThemePresetKey>((localStorage.getItem(LS_PRESET) as ThemePresetKey) || 'ocean')
    const fontMode = ref<FontMode>(normalizeFontMode(localStorage.getItem(LS_FONT)))

    const resolvedTheme = computed<'light' | 'dark'>(() => {
        if (themeMode.value === 'light') return 'light'
        return 'dark'
    })

    const isDark = computed(() => resolvedTheme.value === 'dark')

    // 统一在这里把主题写入 DOM：任何 mode/preset 变化都会立即生效
    watch(
        [resolvedTheme, themePreset, fontMode],
        ([theme, preset, font]) => {
            applyDomTheme(theme, preset, font)
        },
        { immediate: true },
    )

    onMounted(() => { })
    onBeforeUnmount(() => { })

    function setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
        localStorage.setItem(LS_MODE, mode)
    }

    function setThemePreset(preset: ThemePresetKey) {
        themePreset.value = preset
        localStorage.setItem(LS_PRESET, preset)
    }

    function setFontMode(mode: FontMode) {
        fontMode.value = mode
        localStorage.setItem(LS_FONT, mode)
    }

    function syncDomTheme() {
        applyDomTheme(resolvedTheme.value, themePreset.value, fontMode.value)
    }

    return { themeMode, themePreset, fontMode, resolvedTheme, isDark, setThemeMode, setThemePreset, setFontMode, syncDomTheme }
})

