import { get, post } from '../request'

export type LoginPayload = {
  username: string
  password: string
}

export type RegisterPayload = {
  username: string
  password: string
  displayName?: string
}

export type LoginResult = {
  token: string
  userId: number
  username: string
  displayName: string
}

export type MeResult = {
  id: number
  username: string
  displayName: string
}

/** 登录 */
export function loginApi(data: LoginPayload) {
  return post<LoginResult>('/auth/login', data, { withAuth: false })
}

/** 注册 */
export function registerApi(data: RegisterPayload) {
  return post<LoginResult>('/auth/register', data, { withAuth: false })
}

/** 当前用户信息 */
export function fetchMeApi() {
  return get<MeResult>('/auth/me')
}
