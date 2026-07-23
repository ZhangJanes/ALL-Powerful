import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createLedgerEntryApi,
  deleteLedgerEntryApi,
  exportLedgerApi,
  fetchLedgerBudgetApi,
  fetchLedgerEntriesApi,
  fetchLedgerStatsApi,
  upsertLedgerBudgetApi,
  type LedgerStatsDto,
} from '@/api/ledger'
import { useMessageStore } from '@/stores/message'
import { todayYmd } from '@/stores/shared/utils'

export type LedgerEntry = {
  id: string
  type: 'expense' | 'income'
  amount: number
  category: string
  note: string
  at: string
}

export const useLedgerStore = defineStore('ledger', () => {
  const monthlyBudget = ref(0)
  const monthlySpent = ref(0)
  const ledger = ref<LedgerEntry[]>([])
  const alertThreshold = ref(80)
  const loaded = ref(false)
  const stats = ref<LedgerStatsDto | null>(null)

  const todayExpense = computed(() => {
    const d = todayYmd()
    return ledger.value.filter((e) => e.type === 'expense' && e.at.startsWith(d)).reduce((s, e) => s + e.amount, 0)
  })

  const budgetLeft = computed(() => Math.max(0, monthlyBudget.value - monthlySpent.value))

  async function syncLedger(range?: string) {
    const [rows, budget] = await Promise.all([fetchLedgerEntriesApi(range ? { range } : undefined), fetchLedgerBudgetApi()])
    ledger.value = rows.map((x) => ({
      id: String(x.id),
      type: x.type,
      amount: Number(x.amount),
      category: x.categoryName,
      note: x.note || '—',
      at: String(x.occurredAt).replace('T', ' '),
    }))
    monthlyBudget.value = Number(budget.totalBudget || 0)
    monthlySpent.value = Number(budget.spent || 0)
    alertThreshold.value = Number(budget.alertThreshold || 80)
    loaded.value = true
  }

  async function addLedger(e: Omit<LedgerEntry, 'id'>) {
    await createLedgerEntryApi({
      type: e.type,
      amount: e.amount,
      categoryName: e.category,
      note: e.note,
      occurredAt: e.at.replace(' ', 'T'),
    })
    await syncLedger()
    useMessageStore().pushActivity(`新增记账：${e.category} ¥${e.amount}`)
  }

  async function removeLedger(id: string) {
    await deleteLedgerEntryApi(id)
    await syncLedger()
  }

  async function refreshBudget() {
    const budget = await fetchLedgerBudgetApi()
    monthlyBudget.value = Number(budget.totalBudget || 0)
    monthlySpent.value = Number(budget.spent || 0)
    alertThreshold.value = Number(budget.alertThreshold || 80)
  }

  async function saveBudget(totalBudget: number, items: { categoryName: string; amount: number }[] = []) {
    const d = new Date()
    const month = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    await upsertLedgerBudgetApi({ month, totalBudget, alertThreshold: alertThreshold.value, items })
    await refreshBudget()
  }

  async function syncStats(range = 'month') {
    stats.value = await fetchLedgerStatsApi({ range })
  }

  async function exportLedger(range = 'month') {
    return exportLedgerApi({ range })
  }

  if (!loaded.value) {
    syncLedger().catch(() => {
      const today = todayYmd()
      ledger.value = [{ id: 'local-1', type: 'expense', amount: 0, category: '其他', note: '离线', at: `${today} 00:00` }]
    })
  }

  return {
    ledger,
    monthlyBudget,
    monthlySpent,
    alertThreshold,
    todayExpense,
    budgetLeft,
    stats,
    syncLedger,
    addLedger,
    removeLedger,
    refreshBudget,
    saveBudget,
    syncStats,
    exportLedger,
  }
})
