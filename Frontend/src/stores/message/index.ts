import { ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchMessageListApi } from '@/api/message'
import { uid } from '@/stores/shared/utils'

export type Activity = {
  id: string
  text: string
  at: string
  route?: { name: string; params?: Record<string, string> }
}

export const useMessageStore = defineStore('message', () => {
  const activities = ref<Activity[]>([
    { id: 'a1', text: '新增记账：餐饮 ¥30', at: '10 分钟前' },
    { id: 'a2', text: '完成打卡：早起', at: '1 小时前' },
    { id: 'a3', text: '上传照片：病历', at: '昨天' },
  ])

  function pushActivity(text: string) {
    activities.value.unshift({ id: uid(), text, at: '刚刚' })
    if (activities.value.length > 20) activities.value.pop()
  }

  async function syncMessages() {
    const rows = await fetchMessageListApi()
    activities.value = rows.slice(0, 20).map((r, idx) => ({
      id: `msg_${idx}`,
      text: r.title,
      at: r.statusLabel,
    }))
  }

  return { activities, pushActivity, syncMessages }
})
