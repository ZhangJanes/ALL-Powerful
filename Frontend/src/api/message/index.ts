import { get } from '../request'

export type MessageDto = {
  sourceType: string
  sourceId: number
  title: string
  content: string
  plannedAt: string
  statusLabel: string
}

/** 消息聚合列表 */
export function fetchMessageListApi() {
  return get<MessageDto[]>('/messages')
}
