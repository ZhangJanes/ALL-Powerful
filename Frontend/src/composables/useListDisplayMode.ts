import { ref, watch } from 'vue'

const LS_KEY = 'fm.ui.displayMode'

export type DisplayMode = 'list' | 'cards'

const displayMode = ref<DisplayMode>('list')

if (typeof localStorage !== 'undefined') {
  const raw = localStorage.getItem(LS_KEY)
  if (raw === 'list' || raw === 'cards') displayMode.value = raw
}

watch(displayMode, (v) => {
  if (typeof localStorage !== 'undefined') localStorage.setItem(LS_KEY, v)
})

export function useListDisplayMode() {
  function setMode(m: DisplayMode) {
    displayMode.value = m
  }
  return { displayMode, setMode }
}

/** 为卡片入场动画生成稳定延迟（ms） */
export function staggerDelay(id: string, i: number, step = 55) {
  let h = 0
  for (let c = 0; c < id.length; c++) h = (h + id.charCodeAt(c) * 13) % 120
  return i * step + (h % 40)
}
