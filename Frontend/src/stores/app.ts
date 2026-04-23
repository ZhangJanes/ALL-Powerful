import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

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
  locked: boolean
  at: string
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
    { id: 'p1', name: '2026体检报告', category: '病历', locked: true, at: '2026-03-10' },
    { id: 'p2', name: '春节全家福', category: '家庭照片', locked: false, at: '2026-02-01' },
    { id: 'p3', name: '身份证正反面', category: '证件照', locked: true, at: '2025-12-20' },
    { id: 'p4', name: '装修报价单', category: '发票', locked: false, at: '2026-01-15' },
    { id: 'p5', name: '孩子获奖证书', category: '家庭照片', locked: false, at: '2025-11-02' },
    { id: 'p6', name: '合同扫描件', category: '工作文件', locked: true, at: '2026-03-01' },
    { id: 'p7', name: '旅行机票截图', category: '工作文件', locked: false, at: '2026-04-01' },
    { id: 'p8', name: '老照片翻拍', category: '家庭照片', locked: false, at: '2024-08-10' },
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
    activities,
    memoCategory,
    achievements,
    todayExpense,
    todoProgress,
    habitToday,
    budgetLeft,
    pushActivity,
    addMemo,
    updateMemo,
    addLedger,
    checkIn,
    addTrip,
    addIdea,
  }
})
