import { get, post } from '../request'

export type PhotoLibraryPayload = {
  name: string
  visibility: string
  password?: string
}

export type PhotoLibraryDto = {
  id: number
  name: string
  visibility: string
  createdAt: string
}

export type PhotoDto = {
  id: number
  libraryId: number
  name: string
  category: string
  locked: boolean
  objectKey?: string
  atDate: string
}

export type PhotoUploadItem = {
  name: string
  category: string
  objectKey?: string
  atDate: string
}

export type PhotoUploadPayload = {
  libraryId: number
  password?: string
  items: PhotoUploadItem[]
}

/** 照片库列表 */
export function fetchPhotoLibraryListApi() {
  return get<PhotoLibraryDto[]>('/photos/libraries')
}

/** 创建照片库 */
export function createPhotoLibraryApi(data: PhotoLibraryPayload) {
  return post<PhotoLibraryDto>('/photos/libraries', data)
}

/** 验证私有库密码 */
export function verifyPhotoLibraryApi(libraryId: number | string, password: string) {
  return post<boolean>(`/photos/libraries/${libraryId}/verify`, { password })
}

/** 库内照片列表 */
export function fetchLibraryPhotosApi(libraryId: number | string) {
  return get<PhotoDto[]>(`/photos/libraries/${libraryId}`)
}

/** 公共照片列表 */
export function fetchPublicPhotosApi() {
  return get<PhotoDto[]>('/photos/public')
}

/** 上传照片元数据 */
export function uploadPhotosApi(data: PhotoUploadPayload) {
  return post<PhotoDto[]>('/photos/upload', data)
}
