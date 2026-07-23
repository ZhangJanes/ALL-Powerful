import { del, get, post, put } from '../request'

export type LedgerCategoryDto = {
  id: number
  type: 'expense' | 'income'
  name: string
  icon?: string
  color?: string
  isDefault: boolean
}

export type LedgerEntryDto = {
  id: number
  type: 'expense' | 'income'
  amount: number
  categoryId?: number
  categoryName: string
  note?: string
  photoUrl?: string
  occurredAt: string
}

export type LedgerBudgetItemDto = {
  categoryName: string
  amount: number
  spent: number
  left: number
}

export type LedgerBudgetDto = {
  month: string
  totalBudget: number
  spent: number
  left: number
  alertThreshold: number
  items: LedgerBudgetItemDto[]
}

export type LedgerStatsDto = {
  summary: {
    totalIncome: number
    totalExpense: number
    avgDailyExpense: number
    maxDailyExpense: number
  }
  trend: { day: string; amount: number }[]
  categories: { category: string; amount: number }[]
  entries: LedgerEntryDto[]
}

export type LedgerExportDto = {
  fileName: string
  mimeType: string
  base64: string
}

export function fetchLedgerCategoriesApi() {
  return get<LedgerCategoryDto[]>('/ledgers/categories')
}

export function createLedgerCategoryApi(data: Omit<LedgerCategoryDto, 'id' | 'isDefault'>) {
  return post<LedgerCategoryDto>('/ledgers/categories', data)
}

export function updateLedgerCategoryApi(id: number | string, data: Omit<LedgerCategoryDto, 'id' | 'isDefault'>) {
  return put<LedgerCategoryDto>(`/ledgers/categories/${id}`, data)
}

export function deleteLedgerCategoryApi(id: number | string) {
  return del<void>(`/ledgers/categories/${id}`)
}

export function fetchLedgerEntriesApi(params?: { range?: string; type?: string }) {
  return get<LedgerEntryDto[]>('/ledgers/entries', params)
}

export function createLedgerEntryApi(data: Omit<LedgerEntryDto, 'id'>) {
  return post<LedgerEntryDto>('/ledgers/entries', data)
}

export function updateLedgerEntryApi(id: number | string, data: Omit<LedgerEntryDto, 'id'>) {
  return put<LedgerEntryDto>(`/ledgers/entries/${id}`, data)
}

export function deleteLedgerEntryApi(id: number | string) {
  return del<void>(`/ledgers/entries/${id}`)
}

export function fetchLedgerBudgetApi(month?: string) {
  return get<LedgerBudgetDto>('/ledgers/budget', month ? { month } : undefined)
}

export function upsertLedgerBudgetApi(data: {
  month: string
  totalBudget: number
  alertThreshold: number
  items: { categoryName: string; amount: number }[]
}) {
  return put<LedgerBudgetDto>('/ledgers/budget', data)
}

export function fetchLedgerStatsApi(params?: { range?: string; type?: string }) {
  return get<LedgerStatsDto>('/ledgers/stats', params)
}

export function exportLedgerApi(params?: { range?: string; type?: string }) {
  return get<LedgerExportDto>('/ledgers/export', params)
}
