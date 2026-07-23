import { del, get, post, put } from '../request'

export type IdeaDto = {
  id: number
  title: string
  body: string
  category: string
  tags: string[]
  imageUrls: string[]
  starred: boolean
  updatedAt: string
}

export function fetchIdeaListApi() {
  return get<IdeaDto[]>('/ideas')
}

export function fetchIdeaTrashApi() {
  return get<IdeaDto[]>('/ideas/trash')
}

export function createIdeaApi(data: {
  title: string
  body?: string
  category: string
  tags: string[]
  imageUrls?: string[]
  starred: boolean
}) {
  return post<IdeaDto>('/ideas', data)
}

export function updateIdeaApi(
  id: number | string,
  data: { title: string; body?: string; category: string; tags: string[]; imageUrls?: string[]; starred: boolean },
) {
  return put<IdeaDto>(`/ideas/${id}`, data)
}

export function deleteIdeaApi(id: number | string) {
  return del<void>(`/ideas/${id}`)
}

export function restoreIdeaApi(id: number | string) {
  return put<void>(`/ideas/${id}/restore`)
}

export function deleteIdeaPermanentApi(id: number | string) {
  return del<void>(`/ideas/${id}/permanent`)
}

export function toggleIdeaStarApi(id: number | string) {
  return put<IdeaDto>(`/ideas/${id}/star`)
}
