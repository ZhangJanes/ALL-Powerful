import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  favoriteGuideApi,
  fetchGuideArticleApi,
  fetchGuideArticleBySlugApi,
  fetchGuideArticlesApi,
  fetchGuideCategoriesApi,
  fetchGuideFavoritesApi,
  linkGuideMemoApi,
  unfavoriteGuideApi,
  type GuideArticleDto,
  type GuideCategoryDto,
} from '@/api/guide'

export const useGuideStore = defineStore('guide', () => {
  const categories = ref<GuideCategoryDto[]>([])
  const favorites = ref<{ articleId: number; title: string; categoryCode: string }[]>([])
  const loaded = ref(false)

  async function syncCategories() {
    categories.value = await fetchGuideCategoriesApi()
    loaded.value = true
  }

  async function syncFavorites() {
    favorites.value = await fetchGuideFavoritesApi()
  }

  async function listArticles(category?: string, keyword?: string) {
    return fetchGuideArticlesApi({ category, keyword })
  }

  async function getArticle(id: string | number) {
    return fetchGuideArticleApi(id)
  }

  async function getArticleBySlug(category: string, slug: string) {
    return fetchGuideArticleBySlugApi(category, slug)
  }

  async function toggleFavorite(article: GuideArticleDto) {
    if (article.favorite) await unfavoriteGuideApi(article.id)
    else await favoriteGuideApi(article.id)
    await syncFavorites()
  }

  async function linkMemo(articleId: string | number) {
    return linkGuideMemoApi(articleId)
  }

  const categoryMap = computed(() =>
    categories.value.reduce<Record<string, GuideCategoryDto>>((acc, cur) => {
      acc[cur.code] = cur
      return acc
    }, {}),
  )

  if (!loaded.value) {
    syncCategories().then(syncFavorites).catch(() => {})
  }

  return {
    categories,
    favorites,
    categoryMap,
    syncCategories,
    syncFavorites,
    listArticles,
    getArticle,
    getArticleBySlug,
    toggleFavorite,
    linkMemo,
  }
})
