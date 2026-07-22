import { del, get, post, put } from '../request'

export type MemoTodoDto = {
  id?: number
  text: string
  done: boolean
}

export type MemoPayload = {
  title: string
  content?: string
  category: string
  pinned: boolean
  remindAt?: string | null
  todos?: MemoTodoDto[]
}

export type MemoDto = {
  id: number
  title: string
  content: string
  category: string
  pinned: boolean
  remindAt?: string
  updatedAt: string
  todos?: { id: number; text: string; done: boolean }[]
}

/** 备忘录列表 */
export function fetchMemoListApi() {
  return get<MemoDto[]>('/memos')
}

/** 创建备忘录 */
export function createMemoApi(data: MemoPayload) {
  return post<MemoDto>('/memos', data)
}

/** 更新备忘录 */
export function updateMemoApi(id: number | string, data: MemoPayload) {
  return put<MemoDto>(`/memos/${id}`, data)
}

/** 删除备忘录 */
export function deleteMemoApi(id: number | string) {
  return del<void>(`/memos/${id}`)
}
