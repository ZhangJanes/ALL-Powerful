import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchMeApi, loginApi, registerApi, type LoginResult } from '@/api/auth'

const LS_TOKEN = 'fm.auth.token'
const LS_USER = 'fm.auth.user'

export type AuthUser = {
  userId: number
  username: string
  displayName: string
}

function readStorage(key: string) {
  return localStorage.getItem(key) || sessionStorage.getItem(key) || ''
}

function clearAuthStorage() {
  localStorage.removeItem(LS_TOKEN)
  localStorage.removeItem(LS_USER)
  sessionStorage.removeItem(LS_TOKEN)
  sessionStorage.removeItem(LS_USER)
}

function loadUser(): AuthUser | null {
  const raw = readStorage(LS_USER)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

function persistAuth(data: LoginResult, remember: boolean) {
  clearAuthStorage()
  const storage = remember ? localStorage : sessionStorage
  storage.setItem(LS_TOKEN, data.token)
  storage.setItem(
    LS_USER,
    JSON.stringify({
      userId: data.userId,
      username: data.username,
      displayName: data.displayName,
    } satisfies AuthUser),
  )
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(readStorage(LS_TOKEN))
  const user = ref<AuthUser | null>(loadUser())

  const isAuthed = computed(() => Boolean(token.value))

  function applyLogin(data: LoginResult, remember = true) {
    token.value = data.token
    user.value = {
      userId: data.userId,
      username: data.username,
      displayName: data.displayName,
    }
    persistAuth(data, remember)
  }

  async function login(account: string, password: string, remember = true) {
    const data = await loginApi({ username: account.trim(), password })
    applyLogin(data, remember)
    return data
  }

  async function register(username: string, password: string, displayName?: string, remember = true) {
    const data = await registerApi({
      username: username.trim(),
      password,
      displayName: displayName?.trim() || undefined,
    })
    applyLogin(data, remember)
    return data
  }

  async function fetchMe() {
    const data = await fetchMeApi()
    user.value = {
      userId: data.id,
      username: data.username,
      displayName: data.displayName,
    }
    const storage = localStorage.getItem(LS_TOKEN) ? localStorage : sessionStorage
    storage.setItem(LS_USER, JSON.stringify(user.value))
    return user.value
  }

  function logout() {
    token.value = ''
    user.value = null
    clearAuthStorage()
  }

  return { token, user, isAuthed, login, register, fetchMe, logout }
})
