import { del, get, post, put } from '../request'

export type TripChecklistDto = {
  id?: number
  text: string
  done: boolean
}

export type TripPayload = {
  title: string
  category: string
  startAt: string
  endAt: string
  place: string
  companions?: string
  transport?: string
  remark?: string
  remindEnabled: boolean
  remindMinutesBefore?: number
  done: boolean
  checklist?: TripChecklistDto[]
}

export type TripDto = {
  id: number
  title: string
  category: string
  startAt: string
  endAt: string
  place: string
  companions?: string
  transport?: string
  remark?: string
  remindEnabled: boolean
  remindMinutesBefore?: number
  done: boolean
  checklist?: { id: number; text: string; done: boolean }[]
}

/** 出行计划列表 */
export function fetchTripListApi() {
  return get<TripDto[]>('/trips')
}

/** 创建出行计划 */
export function createTripApi(data: TripPayload) {
  return post<TripDto>('/trips', data)
}

/** 更新出行计划 */
export function updateTripApi(id: number | string, data: TripPayload) {
  return put<TripDto>(`/trips/${id}`, data)
}

/** 删除出行计划 */
export function deleteTripApi(id: number | string) {
  return del<void>(`/trips/${id}`)
}
