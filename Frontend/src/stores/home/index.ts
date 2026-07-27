import { ref } from 'vue'
import { defineStore } from 'pinia'

/** 首页展示用的全局 UI 状态（非业务模块数据） */
export const useHomeStore = defineStore('home', () => {
  const achievements = ref(['连续7天', '记账达人'])

  return { achievements }
})
