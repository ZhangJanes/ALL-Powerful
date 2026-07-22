import { ref } from 'vue'
import { defineStore } from 'pinia'
import { useMessageStore } from '@/stores/message'
import { uid } from '@/stores/shared/utils'

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

  function addIdea(i: Omit<Idea, 'id' | 'at'>) {
    ideas.value.unshift({ ...i, id: uid(), at: new Date().toISOString().slice(0, 10) })
    useMessageStore().pushActivity(`记录灵感：${i.title}`)
  }

  return { ideas, addIdea }
})
