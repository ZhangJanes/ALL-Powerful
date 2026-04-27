const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080/api'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

export async function apiRequest<T>(path: string, method: HttpMethod = 'GET', body?: unknown): Promise<T> {
  const token = localStorage.getItem('fm.auth.token') || ''
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  const json = await res.json().catch(() => null)
  if (!res.ok || !json?.success) {
    throw new Error(json?.message || `HTTP ${res.status}`)
  }
  return json.data as T
}
