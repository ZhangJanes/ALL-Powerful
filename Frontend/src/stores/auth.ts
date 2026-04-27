import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { apiRequest } from '@/api/client'

const LS_TOKEN = 'fm.auth.token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(LS_TOKEN) || '')

  const isAuthed = computed(() => Boolean(token.value))

  async function login(account: string, password: string) {
    const data = await apiRequest<{ token: string }>('/auth/login', 'POST', { username: account, password })
    token.value = data.token
    localStorage.setItem(LS_TOKEN, token.value)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem(LS_TOKEN)
  }

  return { token, isAuthed, login, logout }
})

