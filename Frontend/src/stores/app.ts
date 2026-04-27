import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { apiRequest } from '@/api/client'

export type Memo = {
  id: string
  title: string
  content: string
  category: string
  pinned: boolean
  remindAt?: string
  updatedAt: string
  todos?: { id: string; text: string; done: boolean }[]
}

export type LedgerEntry = {
  id: string
  type: 'expense' | 'income'
  amount: number
  category: string
  note: string
  at: string
}

export type Habit = {
  id: string
  name: string
  icon: string
  streak: number
  total: number
  targetDays: number
  doneToday: boolean
}

export type Trip = {
  id: string
  title: string
  category: string
  start: string
  end: string
  place: string
  done: boolean
  checklist: { id: string; text: string; done: boolean }[]
}

export type Idea = {
  id: string
  title: string
  body: string
  category: string
  tags: string[]
  starred: boolean
  at: string
}

export type PhotoItem = {
  id: string
  name: string
  category: string
  libraryId: string
  locked: boolean
  at: string
  previewUrl?: string
}

export type PhotoLibrary = {
  id: string
  name: string
  visibility: 'public' | 'private'
  password?: string
  createdAt: string
}

export type Activity = { id: string; text: string; at: string; route?: { name: string; params?: Record<string, string> } }

function uid() {
  return Math.random().toString(36).slice(2, 10)
}

function todayYmd() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export const useAppStore = defineStore('app', () => {
  const today = todayYmd()
  const publicLibraryId = 'lib_public'
  const weather = ref('晴 22℃')
  const monthlyBudget = ref(2000)
  const monthlySpent = ref(770)

  const memos = ref<Memo[]>([
    {
      id: 'm1',
      title: '缴纳水电费',
      content: '3月20日前缴纳，电费¥150，水费¥50',
      category: '账单',
      pinned: true,
      remindAt: '2026-03-19 18:00',
      updatedAt: `${today} 10:20`,
    },
    {
      id: 'm2',
      title: '周末采购',
      content: '',
      category: '购物',
      pinned: false,
      updatedAt: `${today} 09:00`,
      todos: [
        { id: 't1', text: '牛奶', done: false },
        { id: 't2', text: '鸡蛋', done: true },
        { id: 't3', text: '蔬菜', done: false },
      ],
    },
  ])

  const ledger = ref<LedgerEntry[]>([
    { id: 'l1', type: 'expense', amount: 15, category: '餐饮', note: '早餐', at: `${today} 08:12` },
    { id: 'l2', type: 'expense', amount: 30, category: '餐饮', note: '午餐', at: `${today} 12:40` },
    { id: 'l3', type: 'expense', amount: 44, category: '交通', note: '地铁', at: `${today} 18:05` },
  ])

  const habits = ref<Habit[]>([
    { id: 'h1', name: '早起', icon: 'sunny', streak: 5, total: 12, targetDays: 30, doneToday: false },
    { id: 'h2', name: '阅读30分钟', icon: 'book', streak: 2, total: 8, targetDays: 30, doneToday: true },
    { id: 'h3', name: '喝水8杯', icon: 'water', streak: 0, total: 3, targetDays: 7, doneToday: false },
  ])

  const trips = ref<Trip[]>([
    {
      id: 'tr1',
      title: '周末带孩子去公园',
      category: '周末出游',
      start: '2026-03-22 09:00',
      end: '2026-03-22 16:00',
      place: '城市中央公园',
      done: false,
      checklist: [
        { id: 'c1', text: '零食', done: false },
        { id: 'c2', text: '雨伞', done: false },
      ],
    },
    {
      id: 'tr0',
      title: '春季踏青（已结束）',
      category: '周末出游',
      start: '2026-02-10 10:00',
      end: '2026-02-10 18:00',
      place: '近郊生态农庄',
      done: true,
      checklist: [],
    },
  ])

  const ideas = ref<Idea[]>([
    {
      id: 'i1',
      title: '副业思路：手工饰品',
      body: '简约风 + 抖音/小红书直播，低成本起步。',
      category: '赚钱思路',
      tags: ['副业', '手工'],
      starred: true,
      at: '2026-03-17',
    },
  ])

  const photos = ref<PhotoItem[]>([
    { id: 'p1', name: '2026体检报告', category: '病历', libraryId: publicLibraryId, locked: true, at: '2026-03-10' },
    { id: 'p2', name: '春节全家福', category: '家庭照片', libraryId: publicLibraryId, locked: false, at: '2026-02-01' },
    { id: 'p3', name: '身份证正反面', category: '证件照', libraryId: publicLibraryId, locked: true, at: '2025-12-20' },
    { id: 'p4', name: '装修报价单', category: '发票', libraryId: publicLibraryId, locked: false, at: '2026-01-15' },
    { id: 'p5', name: '孩子获奖证书', category: '家庭照片', libraryId: publicLibraryId, locked: false, at: '2025-11-02' },
    { id: 'p6', name: '合同扫描件', category: '工作文件', libraryId: publicLibraryId, locked: true, at: '2026-03-01' },
    { id: 'p7', name: '旅行机票截图', category: '工作文件', libraryId: publicLibraryId, locked: false, at: '2026-04-01' },
    { id: 'p8', name: '老照片翻拍', category: '家庭照片', libraryId: publicLibraryId, locked: false, at: '2024-08-10' },
  ])
  const photoLibraries = ref<PhotoLibrary[]>([
    {
      id: publicLibraryId,
      name: '公共库',
      visibility: 'public',
      createdAt: `${today} 00:00:00`,
    },
  ])

  const activities = ref<Activity[]>([
    { id: 'a1', text: '新增记账：餐饮 ¥30', at: '10 分钟前' },
    { id: 'a2', text: '完成打卡：早起', at: '1 小时前' },
    { id: 'a3', text: '上传照片：病历', at: '昨天' },
  ])

  const memoCategory = ref('全部')
  const achievements = ref(['连续7天', '记账达人'])

  const todayExpense = computed(() => {
    const d = todayYmd()
    return ledger.value.filter((e) => e.type === 'expense' && e.at.startsWith(d)).reduce((s, e) => s + e.amount, 0)
  })

  const todoProgress = computed(() => {
    let total = 0
    let done = 0
    for (const m of memos.value) {
      if (m.todos?.length) {
        total += m.todos.length
        done += m.todos.filter((t) => t.done).length
      }
    }
    if (!total) return { done: 0, total: 5 }
    return { done, total }
  })

  const habitToday = computed(() => {
    const list = habits.value
    const done = list.filter((h) => h.doneToday).length
    return { done, total: list.length }
  })

  const budgetLeft = computed(() => Math.max(0, monthlyBudget.value - monthlySpent.value))
  const publicPhotos = computed(() => {
    const allowed = new Set(photoLibraries.value.filter((x) => x.visibility === 'public').map((x) => x.id))
    return photos.value.filter((p) => allowed.has(p.libraryId))
  })

  function pushActivity(text: string) {
    activities.value.unshift({ id: uid(), text, at: '刚刚' })
    if (activities.value.length > 20) activities.value.pop()
  }

  function addMemo(payload: Omit<Memo, 'id' | 'updatedAt'>) {
    const id = uid()
    memos.value.unshift({ ...payload, id, updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' ') })
    pushActivity(`新增备忘录：${payload.title}`)
  }

  function updateMemo(id: string, patch: Partial<Memo>) {
    const i = memos.value.findIndex((m) => m.id === id)
    if (i === -1) return
    memos.value[i] = { ...memos.value[i], ...patch, updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' ') }
  }

  function addLedger(e: Omit<LedgerEntry, 'id'>) {
    ledger.value.unshift({ ...e, id: uid() })
    if (e.type === 'expense') monthlySpent.value += e.amount
    pushActivity(`新增记账：${e.category} ¥${e.amount}`)
  }

  function checkIn(habitId: string) {
    const h = habits.value.find((x) => x.id === habitId)
    if (!h || h.doneToday) return
    h.doneToday = true
    h.streak += 1
    h.total += 1
    pushActivity(`完成打卡：${h.name}`)
  }

  function addTrip(t: Omit<Trip, 'id'>) {
    trips.value.unshift({ ...t, id: uid() })
    pushActivity(`新建行程：${t.title}`)
  }

  function addIdea(i: Omit<Idea, 'id' | 'at'>) {
    ideas.value.unshift({ ...i, id: uid(), at: new Date().toISOString().slice(0, 10) })
    pushActivity(`记录灵感：${i.title}`)
  }

  function createPhotoLibrary(payload: { name: string; visibility: 'public' | 'private'; password?: string }) {
    const name = payload.name.trim()
    if (!name) return null
    if (photoLibraries.value.some((x) => x.name === name)) return null
    const lib: PhotoLibrary = {
      id: `lib_${uid()}`,
      name,
      visibility: payload.visibility,
      password: payload.visibility === 'private' ? payload.password?.trim() || '' : undefined,
      createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
    }
    photoLibraries.value.unshift(lib)
    return lib
  }

  function verifyLibraryPassword(libraryId: string, password: string) {
    const lib = photoLibraries.value.find((x) => x.id === libraryId)
    if (!lib) return false
    if (lib.visibility === 'public') return true
    return lib.password === password
  }

  async function syncMemos() {
    const rows = await apiRequest<any[]>('/memos')
    memos.value = rows.map((x) => ({
      id: String(x.id),
      title: x.title,
      content: x.content || '',
      category: x.category,
      pinned: Boolean(x.pinned),
      remindAt: x.remindAt ? String(x.remindAt).replace('T', ' ') : undefined,
      updatedAt: String(x.updatedAt).replace('T', ' '),
      todos: (x.todos || []).map((t: any) => ({ id: String(t.id), text: t.text, done: Boolean(t.done) })),
    }))
  }

  async function saveMemoToServer(input: Omit<Memo, 'id' | 'updatedAt'>, id?: string) {
    const payload = {
      title: input.title,
      content: input.content,
      category: input.category,
      pinned: input.pinned,
      remindAt: input.remindAt ? input.remindAt.replace(' ', 'T') : null,
      todos: (input.todos || []).map((t) => ({ text: t.text, done: t.done })),
    }
    if (id) await apiRequest(`/memos/${id}`, 'PUT', payload)
    else await apiRequest('/memos', 'POST', payload)
    await syncMemos()
  }

  async function syncTrips() {
    const rows = await apiRequest<any[]>('/trips')
    trips.value = rows.map((x) => ({
      id: String(x.id),
      title: x.title,
      category: x.category,
      start: String(x.startAt).replace('T', ' '),
      end: String(x.endAt).replace('T', ' '),
      place: x.place,
      done: Boolean(x.done),
      checklist: (x.checklist || []).map((c: any) => ({ id: String(c.id), text: c.text, done: Boolean(c.done) })),
    }))
  }

  async function saveTripToServer(input: Omit<Trip, 'id'>, id?: string) {
    const payload = {
      title: input.title,
      category: input.category,
      startAt: input.start.replace(' ', 'T'),
      endAt: input.end.replace(' ', 'T'),
      place: input.place,
      done: input.done,
      checklist: (input.checklist || []).map((c) => ({ text: c.text, done: c.done })),
    }
    if (id) await apiRequest(`/trips/${id}`, 'PUT', payload)
    else await apiRequest('/trips', 'POST', payload)
    await syncTrips()
  }

  async function syncPhotoLibrariesAndPhotos() {
    const libs = await apiRequest<any[]>('/photos/libraries')
    photoLibraries.value = libs.map((x) => ({
      id: String(x.id),
      name: x.name,
      visibility: x.visibility,
      createdAt: String(x.createdAt).replace('T', ' '),
      password: undefined,
    }))
    const all = await apiRequest<any[]>('/photos/public')
    photos.value = all.map((p) => ({
      id: String(p.id),
      name: p.name,
      category: p.category,
      libraryId: String(p.libraryId),
      locked: Boolean(p.locked),
      at: p.atDate,
      previewUrl: p.objectKey || undefined,
    }))
  }

  async function syncMessages() {
    const rows = await apiRequest<any[]>('/messages')
    activities.value = rows.slice(0, 20).map((r, idx) => ({
      id: `msg_${idx}`,
      text: r.title,
      at: r.statusLabel,
    }))
  }

  return {
    weather,
    monthlyBudget,
    monthlySpent,
    memos,
    ledger,
    habits,
    trips,
    ideas,
    photos,
    photoLibraries,
    activities,
    memoCategory,
    achievements,
    todayExpense,
    todoProgress,
    habitToday,
    budgetLeft,
    publicPhotos,
    pushActivity,
    addMemo,
    updateMemo,
    addLedger,
    checkIn,
    addTrip,
    addIdea,
    createPhotoLibrary,
    verifyLibraryPassword,
    syncMemos,
    saveMemoToServer,
    syncTrips,
    saveTripToServer,
    syncPhotoLibrariesAndPhotos,
    syncMessages,
    publicLibraryId,
  }
})
