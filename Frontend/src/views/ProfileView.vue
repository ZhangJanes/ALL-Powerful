<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ShieldCheckmarkOutline,
  CloudUploadOutline,
  PeopleOutline,
  ColorPaletteOutline,
  LogOutOutline,
} from '@vicons/ionicons5'
import { useHomeStore } from '@/stores/home'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore, type FontMode, type ThemeMode } from '@/stores/settings'
import { THEME_PRESETS, type ThemePresetKey } from '@/theme/presets'

const router = useRouter()
const homeStore = useHomeStore()
const auth = useAuthStore()
const settings = useSettingsStore()

onMounted(async () => {
  if (auth.isAuthed) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      router.replace({ name: 'login' })
    }
  }
})

function tip(msg: string) {
  alert(msg)
}

function logout() {
  auth.logout()
  router.replace({ name: 'login' })
}

const rows = [
  { label: '家庭共享', icon: PeopleOutline, desc: '邀请家人、权限与动态', action: () => tip('演示：家庭共享') },
  { label: '云端备份', icon: CloudUploadOutline, desc: '自动/手动备份与恢复', action: () => tip('演示：备份') },
  { label: '账号与安全', icon: ShieldCheckmarkOutline, desc: '登录方式、隐私与加密', action: () => tip('演示：安全') },
  { label: '个性化', icon: ColorPaletteOutline, desc: '主题、字体、首页排序', action: () => tip('演示：设置') },
]

const themeOptions: { label: string; value: ThemeMode }[] = [
  { label: '浅色', value: 'light' },
  { label: '深色', value: 'dark' },
]

const presetOptions = THEME_PRESETS.map((p) => ({ label: p.name, value: p.key }))

const fontOptions: { label: string; value: FontMode }[] = [
  { label: '默认字体', value: 'default' },
  { label: '方圆体', value: 'alimama' },
  { label: '可爱体', value: 'cute' },
  { label: '波波体', value: 'bobo' },
  { label: '麦圆体', value: 'maiyuan' },
]

function getFontFamilyByMode(mode: FontMode) {
  if (mode === 'alimama') return '阿里妈妈方圆体'
  if (mode === 'cute') return '可爱字体'
  if (mode === 'bobo') return '波波黑'
  if (mode === 'maiyuan') return '麦圆体'
  return 'var(--fm-font-sans)'
}

function applyFont(v: FontMode) {
  settings.setFontMode(v)
  if (v === 'default') return
  const family = getFontFamilyByMode(v)
  // 如果项目里没有真正打包字体文件（或系统未安装），这里会回退到系统字体，看起来就像“没变”
  const ok = typeof document !== 'undefined' && 'fonts' in document && document.fonts.check(`14px "${family}"`)
  if (!ok) alert(`未检测到「${family}」字体文件/安装，请确认已在 src/font 通过 @font-face 引入。`)
}
</script>

<template>
  <div class="app-shell page">
    <NCard class="glass profile" :bordered="false">
      <div class="head">
        <NAvatar round :size="56" class="big">{{ (auth.user?.displayName || auth.user?.username || '家').slice(0, 1) }}</NAvatar>
        <div>
          <div class="name">{{ auth.user?.displayName || '未登录' }}</div>
          <div class="subtle">@{{ auth.user?.username || 'guest' }}</div>
        </div>
      </div>
      <NTag type="success" size="small" round style="margin-top: 12px">已登录</NTag>
    </NCard>

    <NCard class="glass block" :bordered="false" title="我的成就">
      <NSpace>
        <NTag v-for="a in homeStore.achievements" :key="a" type="warning" round>{{ a }}</NTag>
      </NSpace>
    </NCard>

    <NCard class="glass block" :bordered="false" title="主题">
      <div v-if="settings.themePreset === 'sketch'" class="sketch-theme-hero">
        <FmAntdIllustration variant="empty" :width="112" />
        <div class="sketch-theme-hero__text">
          <div class="sketch-theme-hero__title">涂鸦插画主题</div>
          <div class="subtle sketch-theme-hero__desc">
            粗描边、硬阴影与手帐底纹；与全站空状态插画一致。可用下方开关切换浅色 / 深色。
          </div>
        </div>
      </div>
      <div class="subtle">在浅色与深色之间切换</div>
      <div style="height: 10px" />
      <NRadioGroup
        :value="settings.themeMode"
        @update:value="(v: ThemeMode) => settings.setThemeMode(v)"
      >
        <NRadioButton v-for="o in themeOptions" :key="o.value" :value="o.value">
          {{ o.label }}
        </NRadioButton>
      </NRadioGroup>
      <div class="subtle" style="margin-top: 10px">
        当前：
        {{ settings.themeMode === 'light' ? '浅色' : '深色' }}
      </div>
      <div style="height: 14px" />
      <div class="subtle">主题套装（配色）</div>
      <div style="height: 10px" />
      <NSelect
        style="max-width: 260px"
        :value="settings.themePreset"
        :options="presetOptions"
        @update:value="(v: ThemePresetKey) => settings.setThemePreset(v)"
      />
      <div class="preset-grid">
        <div
          v-for="p in THEME_PRESETS"
          :key="p.key"
          class="preset-card glass"
          :class="{ active: settings.themePreset === p.key }"
          @click="settings.setThemePreset(p.key)"
        >
          <div class="preset-name">{{ p.name }}</div>
          <div class="preset-swatches">
            <span class="sw" :style="{ background: settings.resolvedTheme === 'dark' ? p.primary.dark : p.primary.light }" />
            <span class="sw" :style="{ background: settings.resolvedTheme === 'dark' ? p.accent.dark : p.accent.light }" />
            <span class="sw dim" />
          </div>
        </div>
      </div>
      <div style="height: 18px" />
      <div class="subtle">字体</div>
      <div style="height: 10px" />
      <NRadioGroup :value="settings.fontMode" @update:value="applyFont">
        <NRadioButton
          v-for="o in fontOptions"
          :key="o.value"
          :value="o.value"
          :style="{ fontFamily: getFontFamilyByMode(o.value) }"
        >
          {{ o.label }}
        </NRadioButton>
      </NRadioGroup>
    </NCard>

    <NCard class="glass block" :bordered="false" title="快捷管理">
      <NList clickable hoverable>
        <NListItem v-for="r in rows" :key="r.label" @click="r.action">
          <template #prefix>
            <NIcon :component="r.icon" :size="20" />
          </template>
          <NThing :title="r.label" :description="r.desc" />
        </NListItem>
      </NList>
    </NCard>

    <NButton class="logout" tertiary block @click="logout">
      <NIcon :component="LogOutOutline" style="margin-right: 8px" />
      退出登录
    </NButton>
  </div>
</template>

<style scoped>
.profile {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.head {
  display: flex;
  gap: 14px;
  align-items: center;
}
.big {
  background: linear-gradient(135deg, #14b8a6, #64748b);
  font-size: 22px;
}
.name {
  font-size: 18px;
  font-weight: 700;
}
.block {
  margin-top: 12px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.1);
}
.logout {
  margin-top: 16px;
}

.preset-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.preset-card {
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  padding: 12px 12px 10px;
  cursor: pointer;
  transition: transform 120ms ease, border-color 120ms ease, box-shadow 120ms ease;
}

.preset-card:hover {
  transform: translateY(-1px);
  border-color: rgba(13, 148, 136, 0.3);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.18);
}

.preset-card.active {
  border-color: rgba(13, 148, 136, 0.5);
  box-shadow: 0 0 0 1px rgba(13, 148, 136, 0.2), 0 12px 34px rgba(13, 148, 136, 0.12);
}

.preset-name {
  font-weight: 700;
  font-size: 13px;
}

.preset-swatches {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.sw {
  width: 18px;
  height: 18px;
  border-radius: 6px;
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.sw.dim {
  background: rgba(148, 163, 184, 0.16);
}

.sketch-theme-hero {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 2px solid var(--sk-ui-ink, rgba(74, 63, 54, 0.85));
  box-shadow: var(--sk-shadow-md, 4px 4px 0) var(--sk-ui-pop, rgba(74, 63, 54, 0.88));
  background: rgba(255, 252, 248, 0.96);
}

html[data-theme='dark'] .sketch-theme-hero {
  background: rgba(52, 46, 42, 0.92);
}

.sketch-theme-hero__title {
  font-weight: 800;
  font-size: 15px;
  letter-spacing: 0.02em;
  color: var(--fm-text-strong);
}

.sketch-theme-hero__desc {
  margin-top: 6px;
  line-height: 1.45;
}

</style>
