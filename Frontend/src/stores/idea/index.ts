import { ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createIdeaApi,
  deleteIdeaApi,
  fetchIdeaListApi,
  fetchIdeaTrashApi,
  restoreIdeaApi,
  toggleIdeaStarApi,
  updateIdeaApi,
} from '@/api/idea'
import { useMessageStore } from '@/stores/message'

export type Idea = {
  id: string
  title: string
  body: string
  category: string
  tags: string[]
  starred: boolean
  at: string
}

export const useIdeaStore = defineStore('idea', () => {
  const ideas = ref<Idea[]>([])
  const trash = ref<Idea[]>([])

  async function syncIdeas() {
    const rows = await fetchIdeaListApi()
    ideas.value = rows.map((i) => ({
      id: String(i.id),
      title: i.title,
      body: i.body || '',
      category: i.category,
      tags: i.tags || [],
      starred: Boolean(i.starred),
      at: String(i.updatedAt).slice(0, 10),
    }))
  }

  async function syncTrash() {
    const rows = await fetchIdeaTrashApi()
    trash.value = rows.map((i) => ({
      id: String(i.id),
      title: i.title,
      body: i.body || '',
      category: i.category,
      tags: i.tags || [],
      starred: Boolean(i.starred),
      at: String(i.updatedAt).slice(0, 10),
    }))
  }

  async function addIdea(i: Omit<Idea, 'id' | 'at'>) {
    await createIdeaApi({
      title: i.title,
      body: i.body,
      category: i.category,
      tags: i.tags,
      starred: i.starred,
      imageUrls: [],
    })
    await syncIdeas()
    useMessageStore().pushActivity(`记录灵感：${i.title}`)
  }

  async function updateIdea(id: string, payload: Omit<Idea, 'id' | 'at'>) {
    await updateIdeaApi(id, { ...payload, imageUrls: [] })
    await syncIdeas()
  }

  async function removeIdea(id: string) {
    await deleteIdeaApi(id)
    await syncIdeas()
    await syncTrash()
  }

  async function restoreIdea(id: string) {
    await restoreIdeaApi(id)
    await syncIdeas()
    await syncTrash()
  }

  async function toggleStar(id: string) {
    await toggleIdeaStarApi(id)
    await syncIdeas()
  }

  syncIdeas().catch(() => {})

  return { ideas, trash, addIdea, updateIdea, removeIdea, restoreIdea, toggleStar, syncIdeas, syncTrash }
})
