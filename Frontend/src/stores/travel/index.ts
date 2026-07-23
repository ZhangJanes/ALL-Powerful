import { ref } from 'vue'
import { defineStore } from 'pinia'
import { createTripApi, fetchTripListApi, updateTripApi } from '@/api/travel'
import { useMessageStore } from '@/stores/message'
import { uid } from '@/stores/shared/utils'

export type Trip = {
  id: string
  title: string
  category: string
  start: string
  end: string
  place: string
  companions?: string
  transport?: string
  remark?: string
  remindEnabled: boolean
  remindMinutesBefore?: number
  done: boolean
  checklist: { id: string; text: string; done: boolean }[]
}

export const useTravelStore = defineStore('travel', () => {
  const trips = ref<Trip[]>([
    {
      id: 'tr1',
      title: '周末带孩子去公园',
      category: '周末出游',
      start: '2026-03-22 09:00',
      end: '2026-03-22 16:00',
      place: '城市中央公园',
      companions: '我、孩子',
      transport: '自驾',
      remark: '带雨伞',
      remindEnabled: true,
      remindMinutesBefore: 60,
      done: false,
      checklist: [
        { id: 'c1', text: '零食', done: false },
        { id: 'c2', text: '雨伞', done: false },
      ],
    },
    {
      id: 'tr0',
      title: '春季踏青（已结束）',
      category: '周末出游',
      start: '2026-02-10 10:00',
      end: '2026-02-10 18:00',
      place: '近郊生态农庄',
      companions: '',
      transport: '自驾',
      remark: '',
      remindEnabled: false,
      done: true,
      checklist: [],
    },
  ])

  function addTrip(t: Omit<Trip, 'id'>) {
    trips.value.unshift({ ...t, id: uid() })
    useMessageStore().pushActivity(`新建行程：${t.title}`)
  }

  async function syncTrips() {
    const rows = await fetchTripListApi()
    trips.value = rows.map((x) => ({
      id: String(x.id),
      title: x.title,
      category: x.category,
      start: String(x.startAt).replace('T', ' '),
      end: String(x.endAt).replace('T', ' '),
      place: x.place,
      companions: x.companions || '',
      transport: x.transport || '',
      remark: x.remark || '',
      remindEnabled: Boolean(x.remindEnabled),
      remindMinutesBefore: x.remindMinutesBefore ? Number(x.remindMinutesBefore) : undefined,
      done: Boolean(x.done),
      checklist: (x.checklist || []).map((c) => ({ id: String(c.id), text: c.text, done: Boolean(c.done) })),
    }))
  }

  async function saveTripToServer(input: Omit<Trip, 'id'>, id?: string) {
    const payload = {
      title: input.title,
      category: input.category,
      startAt: input.start.replace(' ', 'T'),
      endAt: input.end.replace(' ', 'T'),
      place: input.place,
      companions: input.companions || '',
      transport: input.transport || '',
      remark: input.remark || '',
      remindEnabled: Boolean(input.remindEnabled),
      remindMinutesBefore: input.remindMinutesBefore,
      done: input.done,
      checklist: (input.checklist || []).map((c) => ({ text: c.text, done: c.done })),
    }
    if (id) await updateTripApi(id, payload)
    else await createTripApi(payload)
    await syncTrips()
  }

  return { trips, addTrip, syncTrips, saveTripToServer }
})
