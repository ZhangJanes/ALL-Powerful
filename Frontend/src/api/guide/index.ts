import { del, get, post, put } from '../request'

export type GuideCategoryDto = {
  id: number
  code: string
  title: string
  description?: string
  sortOrder: number
  enabled: boolean
  count: number
}

export type GuideArticleDto = {
  id: number
  categoryId: number
  categoryCode: string
  slug: string
  title: string
  intro?: string
  conditionsText?: string
  materialsText?: string
  processText?: string
  locationText?: string
  timeText?: string
  periodText?: string
  feeText?: string
  tipsText?: string
  consultUrl?: string
  favorite: boolean
  updatedAt: string
}

export type GuideFavoriteDto = {
  articleId: number
  title: string
  categoryCode: string
}

export function fetchGuideCategoriesApi() {
  return get<GuideCategoryDto[]>('/guides/categories')
}

export function fetchGuideArticlesApi(params: { category?: string; keyword?: string }) {
  return get<GuideArticleDto[]>('/guides/articles', params)
}

export function fetchGuideArticleApi(id: number | string) {
  return get<GuideArticleDto>(`/guides/articles/${id}`)
}

export function fetchGuideArticleBySlugApi(category: string, slug: string) {
  return get<GuideArticleDto>(`/guides/categories/${category}/articles/${slug}`)
}

export function fetchGuideFavoritesApi() {
  return get<GuideFavoriteDto[]>('/guides/favorites')
}

export function favoriteGuideApi(id: number | string) {
  return post<void>(`/guides/articles/${id}/favorite`)
}

export function unfavoriteGuideApi(id: number | string) {
  return del<void>(`/guides/articles/${id}/favorite`)
}

export function linkGuideMemoApi(id: number | string, data?: { extraNote?: string; remindAt?: string }) {
  return post<{ memoId: number }>(`/guides/articles/${id}/link-memo`, data)
}

export function createGuideCategoryApi(data: {
  code: string
  title: string
  description?: string
  sortOrder: number
  enabled: boolean
}) {
  return post<GuideCategoryDto>('/guides/categories', data)
}

export function createGuideArticleApi(data: {
  categoryId: number
  slug: string
  title: string
  intro?: string
  conditionsText?: string
  materialsText?: string
  processText?: string
  locationText?: string
  timeText?: string
  periodText?: string
  feeText?: string
  tipsText?: string
  consultUrl?: string
}) {
  return post<GuideArticleDto>('/guides/articles', data)
}

export function updateGuideArticleApi(
  id: number | string,
  data: {
    categoryId: number
    slug: string
    title: string
    intro?: string
    conditionsText?: string
    materialsText?: string
    processText?: string
    locationText?: string
    timeText?: string
    periodText?: string
    feeText?: string
    tipsText?: string
    consultUrl?: string
  },
) {
  return put<GuideArticleDto>(`/guides/articles/${id}`, data)
}
