import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchPhotoLibraryListApi, fetchPublicPhotosApi } from '@/api/photo'
import { todayYmd, uid } from '@/stores/shared/utils'

export type PhotoItem = {
  id: string
  name: string
  category: string
  libraryId: string
  locked: boolean
  at: string
  previewUrl?: string
}

export type PhotoLibrary = {
  id: string
  name: string
  visibility: 'public' | 'private'
  password?: string
  createdAt: string
}

export const PUBLIC_LIBRARY_ID = 'lib_public'

export const usePhotoStore = defineStore('photo', () => {
  const today = todayYmd()

  const photos = ref<PhotoItem[]>([
    { id: 'p1', name: '2026体检报告', category: '病历', libraryId: PUBLIC_LIBRARY_ID, locked: true, at: '2026-03-10' },
    { id: 'p2', name: '春节全家福', category: '家庭照片', libraryId: PUBLIC_LIBRARY_ID, locked: false, at: '2026-02-01' },
    { id: 'p3', name: '身份证正反面', category: '证件照', libraryId: PUBLIC_LIBRARY_ID, locked: true, at: '2025-12-20' },
    { id: 'p4', name: '装修报价单', category: '发票', libraryId: PUBLIC_LIBRARY_ID, locked: false, at: '2026-01-15' },
    { id: 'p5', name: '孩子获奖证书', category: '家庭照片', libraryId: PUBLIC_LIBRARY_ID, locked: false, at: '2025-11-02' },
    { id: 'p6', name: '合同扫描件', category: '工作文件', libraryId: PUBLIC_LIBRARY_ID, locked: true, at: '2026-03-01' },
    { id: 'p7', name: '旅行机票截图', category: '工作文件', libraryId: PUBLIC_LIBRARY_ID, locked: false, at: '2026-04-01' },
    { id: 'p8', name: '老照片翻拍', category: '家庭照片', libraryId: PUBLIC_LIBRARY_ID, locked: false, at: '2024-08-10' },
  ])

  const photoLibraries = ref<PhotoLibrary[]>([
    {
      id: PUBLIC_LIBRARY_ID,
      name: '公共库',
      visibility: 'public',
      createdAt: `${today} 00:00:00`,
    },
  ])

  const publicPhotos = computed(() => {
    const allowed = new Set(photoLibraries.value.filter((x) => x.visibility === 'public').map((x) => x.id))
    return photos.value.filter((p) => allowed.has(p.libraryId))
  })

  function createPhotoLibrary(payload: { name: string; visibility: 'public' | 'private'; password?: string }) {
    const name = payload.name.trim()
    if (!name) return null
    if (photoLibraries.value.some((x) => x.name === name)) return null
    const lib: PhotoLibrary = {
      id: `lib_${uid()}`,
      name,
      visibility: payload.visibility,
      password: payload.visibility === 'private' ? payload.password?.trim() || '' : undefined,
      createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
    }
    photoLibraries.value.unshift(lib)
    return lib
  }

  function verifyLibraryPassword(libraryId: string, password: string) {
    const lib = photoLibraries.value.find((x) => x.id === libraryId)
    if (!lib) return false
    if (lib.visibility === 'public') return true
    return lib.password === password
  }

  async function syncPhotoLibrariesAndPhotos() {
    const libs = await fetchPhotoLibraryListApi()
    photoLibraries.value = libs.map((x) => ({
      id: String(x.id),
      name: x.name,
      visibility: x.visibility as PhotoLibrary['visibility'],
      createdAt: String(x.createdAt).replace('T', ' '),
      password: undefined,
    }))
    const all = await fetchPublicPhotosApi()
    photos.value = all.map((p) => ({
      id: String(p.id),
      name: p.name,
      category: p.category,
      libraryId: String(p.libraryId),
      locked: Boolean(p.locked),
      at: p.atDate,
      previewUrl: p.objectKey || undefined,
    }))
  }

  return {
    photos,
    photoLibraries,
    publicPhotos,
    publicLibraryId: PUBLIC_LIBRARY_ID,
    createPhotoLibrary,
    verifyLibraryPassword,
    syncPhotoLibrariesAndPhotos,
  }
})
