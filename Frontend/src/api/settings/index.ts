import { get, put } from '../request'

export type UserSettingsDto = {
  themeMode: string
  themePreset: string
  fontMode: string
}

export type UserSettingsPayload = UserSettingsDto

/** 获取当前用户设置（不存在则创建默认值） */
export function fetchUserSettingsApi() {
  return get<UserSettingsDto>('/settings')
}

/** 更新当前用户设置 */
export function updateUserSettingsApi(data: UserSettingsPayload) {
  return put<UserSettingsDto>('/settings', data)
}
