import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

/** 后端统一响应结构 */
export type ApiResult<T = unknown> = {
  success: boolean
  data: T
  message: string
}

export type RequestConfig = {
  /** 相对路径，如 `/auth/login`（会自动拼 baseURL） */
  url: string
  /** 请求方式，默认 GET */
  method?: HttpMethod
  /** URL 查询参数（?key=value） */
  params?: Record<string, unknown>
  /** 请求体（POST / PUT / PATCH） */
  data?: unknown
  /** 自定义请求头 */
  headers?: Record<string, string>
  /** 超时毫秒，默认 15000 */
  timeout?: number
  /** 是否携带 JWT，默认 true */
  withAuth?: boolean
  /** 透传 axios 配置 */
  axiosConfig?: Omit<AxiosRequestConfig, 'url' | 'method' | 'params' | 'data' | 'headers' | 'timeout'>
}

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080/api'
const TOKEN_KEY = 'fm.auth.token'

function readToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}

const http: AxiosInstance = axios.create({
  baseURL: API_BASE,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const withAuth = (config as AxiosRequestConfig & { withAuth?: boolean }).withAuth !== false
  if (withAuth) {
    const token = readToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const json = response.data as ApiResult | null
    if (!json?.success) {
      const message = json?.message || `HTTP ${response.status}`
      return Promise.reject(new Error(message))
    }
    // 将 data 挂到 response.data，便于 request 直接取
    response.data = json.data
    return response
  },
  (error) => {
    const status = error.response?.status
    const json = error.response?.data as ApiResult | undefined
    if (status === 403 && !json?.message) {
      return Promise.reject(new Error('无权限访问，请确认后端已重启并重新登录'))
    }
    const message = json?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

/**
 * 统一请求方法
 * @example
 * request({ url: '/memos', method: 'GET' })
 * request({ url: '/auth/login', method: 'POST', data: { username, password }, withAuth: false })
 */
export async function request<T = unknown>(config: RequestConfig): Promise<T> {
  const { url, method = 'GET', params, data, headers, timeout, withAuth = true, axiosConfig } = config
  const res = await http.request<T>({
    url,
    method,
    params,
    data,
    headers,
    timeout,
    withAuth,
    ...axiosConfig,
  } as AxiosRequestConfig & { withAuth?: boolean })
  return res.data as T
}

export function get<T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: Omit<RequestConfig, 'url' | 'method' | 'params'>,
) {
  return request<T>({ url, method: 'GET', params, ...config })
}

export function post<T = unknown>(
  url: string,
  data?: unknown,
  config?: Omit<RequestConfig, 'url' | 'method' | 'data'>,
) {
  return request<T>({ url, method: 'POST', data, ...config })
}

export function put<T = unknown>(
  url: string,
  data?: unknown,
  config?: Omit<RequestConfig, 'url' | 'method' | 'data'>,
) {
  return request<T>({ url, method: 'PUT', data, ...config })
}

export function del<T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: Omit<RequestConfig, 'url' | 'method' | 'params'>,
) {
  return request<T>({ url, method: 'DELETE', params, ...config })
}

export function patch<T = unknown>(
  url: string,
  data?: unknown,
  config?: Omit<RequestConfig, 'url' | 'method' | 'data'>,
) {
  return request<T>({ url, method: 'PATCH', data, ...config })
}

export { http as axiosInstance, API_BASE }

export default request
