import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useMessageStore } from '@/stores/message'
import { todayYmd, uid } from '@/stores/shared/utils'

export type LedgerEntry = {
  id: string
  type: 'expense' | 'income'
  amount: number
  category: string
  note: string
  at: string
}

export const useLedgerStore = defineStore('ledger', () => {
  const today = todayYmd()
  const monthlyBudget = ref(2000)
  const monthlySpent = ref(770)

  const ledger = ref<LedgerEntry[]>([
    { id: 'l1', type: 'expense', amount: 15, category: '餐饮', note: '早餐', at: `${today} 08:12` },
    { id: 'l2', type: 'expense', amount: 30, category: '餐饮', note: '午餐', at: `${today} 12:40` },
    { id: 'l3', type: 'expense', amount: 44, category: '交通', note: '地铁', at: `${today} 18:05` },
  ])

  const todayExpense = computed(() => {
    const d = todayYmd()
    return ledger.value.filter((e) => e.type === 'expense' && e.at.startsWith(d)).reduce((s, e) => s + e.amount, 0)
  })

  const budgetLeft = computed(() => Math.max(0, monthlyBudget.value - monthlySpent.value))

  function addLedger(e: Omit<LedgerEntry, 'id'>) {
    ledger.value.unshift({ ...e, id: uid() })
    if (e.type === 'expense') monthlySpent.value += e.amount
    useMessageStore().pushActivity(`新增记账：${e.category} ¥${e.amount}`)
  }

  return { ledger, monthlyBudget, monthlySpent, todayExpense, budgetLeft, addLedger }
})
