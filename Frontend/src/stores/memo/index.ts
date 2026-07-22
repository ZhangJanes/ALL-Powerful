import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { createMemoApi, deleteMemoApi, fetchMemoListApi, updateMemoApi } from '@/api/memo'
import type { MemoDto } from '@/api/memo'

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

function mapMemoDto(x: MemoDto): Memo {
  return {
    id: String(x.id),
    title: x.title,
    content: x.content || '',
    category: x.category,
    pinned: Boolean(x.pinned),
    remindAt: x.remindAt ? String(x.remindAt).replace('T', ' ') : undefined,
    updatedAt: String(x.updatedAt).replace('T', ' '),
    todos: (x.todos || []).map((t) => ({ id: String(t.id), text: t.text, done: Boolean(t.done) })),
  }
}

export const useMemoStore = defineStore('memo', () => {
  const memos = ref<Memo[]>([])
  const loading = ref(false)
  const error = ref('')
  const loaded = ref(false)

  const memoCategory = ref('全部')

  const todoProgress = computed(() => {
    let total = 0
    let done = 0
    for (const m of memos.value) {
      if (m.todos?.length) {
        total += m.todos.length
        done += m.todos.filter((t) => t.done).length
      }
    }
    if (!total) return { done: 0, total: 0 }
    return { done, total }
  })

  async function syncMemos() {
    loading.value = true
    error.value = ''
    try {
      const rows = await fetchMemoListApi()
      memos.value = rows.map(mapMemoDto)
      loaded.value = true
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载备忘录失败'
      throw e
    } finally {
      loading.value = false
    }
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
    if (id) await updateMemoApi(id, payload)
    else await createMemoApi(payload)
    await syncMemos()
  }

  async function deleteMemoFromServer(id: string) {
    await deleteMemoApi(id)
    await syncMemos()
  }

  return {
    memos,
    loading,
    error,
    loaded,
    memoCategory,
    todoProgress,
    syncMemos,
    saveMemoToServer,
    deleteMemoFromServer,
  }
})
