import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

const LS_TOKEN = 'fm.auth.token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(LS_TOKEN) || '')

  const isAuthed = computed(() => Boolean(token.value))

  function login(_account: string, _password: string) {
    // 演示项目：不接后端，直接写入一个本地 token
    token.value = `demo_${Date.now()}`
    localStorage.setItem(LS_TOKEN, token.value)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem(LS_TOKEN)
  }

  return { token, isAuthed, login, logout }
})

