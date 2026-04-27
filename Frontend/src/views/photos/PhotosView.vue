<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, LockClosedOutline, ImageOutline, AddOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { staggerDelay, useListDisplayMode } from '@/composables/useListDisplayMode'
import { useMessage } from 'naive-ui'
import FmDisplayModeToggle from '@/components/FmDisplayModeToggle.vue'

const router = useRouter()
const store = useAppStore()
const message = useMessage()
const { displayMode } = useListDisplayMode()

const cats = ['全部', '证件照', '病历', '发票', '家庭照片', '工作文件']

const viewMode = ref<'libraries' | 'library' | 'all-public'>('libraries')
const selectedLibraryId = ref(store.publicLibraryId)
const unlockedLibraries = ref<string[]>([])
const unlockPassword = ref('')
const viewCategory = ref('全部')

const uploadInput = ref<HTMLInputElement | null>(null)
const createUploadInput = ref<HTMLInputElement | null>(null)
const selectedFiles = ref<File[]>([])
const createSelectedFiles = ref<File[]>([])
const uploadModal = ref(false)
const createLibraryModal = ref(false)
const previewModal = ref(false)

const uploadName = ref('')
const uploadCategory = ref('家庭照片')
const uploadLibraryId = ref(store.publicLibraryId)
const uploadLibraryPassword = ref('')
const uploadPrivateUnlocked = ref(false)

const newLibraryName = ref('')
const newLibraryVisibility = ref<'public' | 'private'>('public')
const newLibraryPassword = ref('')
const createUploadCategory = ref('家庭照片')
const createUploadName = ref('')

const selectedPhoto = ref<(typeof store.photos)[number] | null>(null)

const libraryOptions = computed(() =>
  store.photoLibraries.map((x) => ({
    label: `${x.name}（${x.visibility === 'public' ? '可见' : '不可见'}）`,
    value: x.id,
  })),
)

const uploadTargetLibrary = computed(() => store.photoLibraries.find((x) => x.id === uploadLibraryId.value) || null)
const uploadNeedsPassword = computed(() => uploadTargetLibrary.value?.visibility === 'private' && !uploadPrivateUnlocked.value)
const currentLibrary = computed(() => store.photoLibraries.find((x) => x.id === selectedLibraryId.value) || null)
const currentLibraryLocked = computed(() => {
  if (!currentLibrary.value || currentLibrary.value.visibility === 'public') return false
  return !unlockedLibraries.value.includes(currentLibrary.value.id)
})

const libraryCards = computed(() =>
  store.photoLibraries.map((lib) => ({
    ...lib,
    total: store.photos.filter((p) => p.libraryId === lib.id).length,
    previews: store.photos.filter((p) => p.libraryId === lib.id).slice(0, 8),
  })),
)

const currentPhotos = computed(() => {
  let base =
    viewMode.value === 'all-public'
      ? store.publicPhotos
      : store.photos.filter((p) => p.libraryId === selectedLibraryId.value)
  if (viewCategory.value !== '全部') base = base.filter((p) => p.category === viewCategory.value)
  return base
})

watch(uploadLibraryId, () => {
  uploadLibraryPassword.value = ''
  uploadPrivateUnlocked.value = false
})

function tileStyle(id: string, i: number) {
  let h = 0
  for (let c = 0; c < id.length; c++) h = (h + id.charCodeAt(c) * 17) % 200
  return {
    minHeight: `${96 + (h % 100)}px`,
    animationDelay: `${staggerDelay(id, i, 40)}ms`,
  }
}

function nowYmd() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function toLibraryView(libraryId: string) {
  selectedLibraryId.value = libraryId
  viewMode.value = 'library'
}

function toAllPublicView() {
  viewMode.value = 'all-public'
}

function backToLibraries() {
  viewMode.value = 'libraries'
}

function unlockCurrentLibrary() {
  if (!currentLibrary.value || currentLibrary.value.visibility !== 'private') return
  if (!unlockPassword.value.trim()) return message.warning('请输入库密码')
  if (!store.verifyLibraryPassword(currentLibrary.value.id, unlockPassword.value.trim())) return message.error('密码错误')
  if (!unlockedLibraries.value.includes(currentLibrary.value.id)) unlockedLibraries.value.push(currentLibrary.value.id)
  unlockPassword.value = ''
  message.success('已解锁当前库')
}

function openCreateLibrary() {
  createLibraryModal.value = true
}

function openCreateUploadPicker() {
  createUploadInput.value?.click()
}

function handleCreateFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files
  createSelectedFiles.value = files ? Array.from(files) : []
  if (createSelectedFiles.value.length && !createUploadName.value.trim()) createUploadName.value = createSelectedFiles.value[0].name
  ;(e.target as HTMLInputElement).value = ''
}

function confirmCreateLibrary() {
  if (!newLibraryName.value.trim()) return message.warning('请填写库名称')
  if (newLibraryVisibility.value === 'private' && !newLibraryPassword.value.trim()) return message.warning('私有库必须设置密码')
  if (createSelectedFiles.value.length > 99) return message.warning('创建时单次最多上传 99 张')
  const created = store.createPhotoLibrary({
    name: newLibraryName.value,
    visibility: newLibraryVisibility.value,
    password: newLibraryVisibility.value === 'private' ? newLibraryPassword.value : undefined,
  })
  if (!created) return message.error('创建失败，名称可能重复')

  if (createSelectedFiles.value.length) {
    const nameBase = createUploadName.value.trim() || newLibraryName.value.trim()
    const createdPhotos = createSelectedFiles.value.map((file, idx) => ({
      id: `p_${Date.now()}_${idx}`,
      name: createSelectedFiles.value.length === 1 ? nameBase : `${nameBase}-${idx + 1}`,
      category: createUploadCategory.value,
      libraryId: created.id,
      locked: created.visibility === 'private',
      at: nowYmd(),
      previewUrl: URL.createObjectURL(file),
    }))
    store.photos.unshift(...createdPhotos)
  }

  selectedLibraryId.value = created.id
  viewMode.value = 'library'
  createLibraryModal.value = false
  newLibraryName.value = ''
  newLibraryVisibility.value = 'public'
  newLibraryPassword.value = ''
  createUploadCategory.value = '家庭照片'
  createUploadName.value = ''
  createSelectedFiles.value = []
  message.success('照片库创建成功')
}

function pickUpload() {
  uploadModal.value = true
  uploadLibraryId.value = store.publicLibraryId
  uploadPrivateUnlocked.value = false
  uploadLibraryPassword.value = ''
}

function openUploadPicker() {
  uploadInput.value?.click()
}

function handleUploadFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files
  selectedFiles.value = files ? Array.from(files) : []
  if (selectedFiles.value.length && !uploadName.value.trim()) uploadName.value = selectedFiles.value[0].name
  ;(e.target as HTMLInputElement).value = ''
}

function verifyUploadLibraryPassword() {
  if (!uploadTargetLibrary.value || uploadTargetLibrary.value.visibility !== 'private') return
  if (!uploadLibraryPassword.value.trim()) return message.warning('请输入库密码')
  if (!store.verifyLibraryPassword(uploadTargetLibrary.value.id, uploadLibraryPassword.value.trim())) return message.error('密码错误')
  uploadPrivateUnlocked.value = true
  message.success('密码校验通过')
}

function confirmUpload() {
  if (!uploadName.value.trim()) return message.warning('请填写照片名称')
  if (!selectedFiles.value.length) return message.warning('请选择照片文件')
  if (selectedFiles.value.length > 99) return message.warning('单次最多上传 99 张')
  if (!uploadTargetLibrary.value) return message.warning('请选择上传库')
  if (uploadTargetLibrary.value.visibility === 'private' && !uploadPrivateUnlocked.value) return message.warning('请先校验私有库密码')

  const createdPhotos = selectedFiles.value.map((file, idx) => ({
    id: `p_${Date.now()}_${idx}`,
    name: selectedFiles.value.length === 1 ? uploadName.value.trim() : `${uploadName.value.trim()}-${idx + 1}`,
    category: uploadCategory.value,
    libraryId: uploadTargetLibrary.value!.id,
    locked: uploadTargetLibrary.value!.visibility === 'private',
    at: nowYmd(),
    previewUrl: URL.createObjectURL(file),
  }))
  store.photos.unshift(...createdPhotos)
  uploadModal.value = false
  resetUploadForm()
  message.success(`上传成功，共 ${createdPhotos.length} 张`)
}

function resetUploadForm() {
  uploadName.value = ''
  uploadCategory.value = '家庭照片'
  uploadLibraryId.value = store.publicLibraryId
  uploadLibraryPassword.value = ''
  uploadPrivateUnlocked.value = false
  selectedFiles.value = []
}

function openPreview(p: (typeof store.photos)[number]) {
  selectedPhoto.value = p
  previewModal.value = true
}

onMounted(async () => {
  try {
    await store.syncPhotoLibrariesAndPhotos()
  } catch {
    // fallback to local mock
  }
})
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <div class="nav-left">
        <NButton quaternary circle @click="viewMode === 'libraries' ? router.push({ name: 'home' }) : backToLibraries()">
          <template #icon><NIcon :component="ArrowBackOutline" /></template>
        </NButton>
        <div class="page-title">{{ viewMode === 'libraries' ? '照片库' : viewMode === 'all-public' ? '所有公开照片' : currentLibrary?.name || '照片库' }}</div>
      </div>
      <div class="nav-actions">
        <FmDisplayModeToggle v-if="viewMode === 'libraries'" />
        <NButton size="tiny" quaternary @click="openCreateLibrary">
          <template #icon><NIcon :component="AddOutline" /></template>
          新建库
        </NButton>
        <NButton size="tiny" secondary @click="pickUpload">上传</NButton>
      </div>
    </div>

    <input ref="uploadInput" class="hidden-input" type="file" accept="image/*" multiple @change="handleUploadFileChange" />
    <input ref="createUploadInput" class="hidden-input" type="file" accept="image/*" multiple @change="handleCreateFileChange" />

    <template v-if="viewMode === 'libraries'">
      <div class="library-top-row">
        <NButton size="small" round secondary @click="toAllPublicView">查看所有照片（公开库）</NButton>
      </div>
      <div v-if="displayMode === 'list'" class="library-list">
        <NCard v-for="lib in libraryCards" :key="lib.id" class="library-list-item glass fm-card-tile" :bordered="false" @click="toLibraryView(lib.id)">
          <div class="library-card__head">
            <NSpace align="center" :size="8">
              <div class="library-name">{{ lib.name }}</div>
              <NTag size="small" round :type="lib.visibility === 'public' ? 'success' : 'warning'">
                {{ lib.visibility === 'public' ? '可见' : '不可见' }}
              </NTag>
            </NSpace>
            <div class="subtle">照片数：{{ lib.total }}</div>
          </div>
          <div class="library-strip">
            <div v-for="p in lib.previews" :key="p.id" class="library-strip__item" :class="{ 'is-private': lib.visibility === 'private' }">
              <img v-if="p.previewUrl" :src="p.previewUrl" :alt="p.name" class="library-strip__img" />
              <div v-else class="library-strip__placeholder">
                <NIcon :component="ImageOutline" />
              </div>
            </div>
            <div v-if="!lib.previews.length" class="library-strip__empty subtle">
              <NIcon :component="ImageOutline" />
            </div>
          </div>
        </NCard>
      </div>
      <div v-else class="library-grid">
        <NCard v-for="lib in libraryCards" :key="lib.id" class="library-card glass fm-card-tile" :bordered="false" @click="toLibraryView(lib.id)">
          <div class="library-card__head">
            <div class="library-name">{{ lib.name }}</div>
            <NTag size="small" round :type="lib.visibility === 'public' ? 'success' : 'warning'">
              {{ lib.visibility === 'public' ? '可见' : '不可见' }}
            </NTag>
          </div>
          <div class="library-carousel">
            <NCarousel v-if="lib.previews.length" autoplay dot-type="line" :show-arrow="false" :interval="3000">
              <div v-for="p in lib.previews" :key="p.id" class="library-carousel__item">
                <img v-if="p.previewUrl" :src="p.previewUrl" :alt="p.name" class="library-carousel__img" :class="{ 'is-private': lib.visibility === 'private' }" />
                <div v-else class="library-carousel__placeholder" :class="{ 'is-private': lib.visibility === 'private' }">
                  <NIcon :component="ImageOutline" />
                </div>
              </div>
            </NCarousel>
            <div v-else class="library-carousel__empty subtle">
              <NIcon :component="ImageOutline" />
            </div>
          </div>
          <div class="subtle">照片数：{{ lib.total }}</div>
        </NCard>
      </div>
    </template>

    <template v-else>
      <div class="cats">
        <NButton
          v-for="c in cats"
          :key="c"
          size="small"
          round
          :secondary="c === viewCategory"
          :quaternary="c !== viewCategory"
          @click="viewCategory = c"
        >
          {{ c }}
        </NButton>
      </div>

      <NAlert v-if="viewMode === 'library' && currentLibraryLocked" type="warning" title="当前库不可见，请先输入密码解锁" class="glass">
        <NSpace align="center">
          <NInput v-model:value="unlockPassword" type="password" show-password-on="click" placeholder="输入当前库密码" style="width: 220px" />
          <NButton type="primary" secondary @click="unlockCurrentLibrary">解锁库</NButton>
        </NSpace>
      </NAlert>

      <div v-else-if="currentPhotos.length" class="masonry">
        <div v-for="(p, idx) in currentPhotos" :key="p.id" class="masonry__item">
          <NCard class="tile glass fm-card-tile" :bordered="false" size="small" :style="tileStyle(p.id, idx)" @click="openPreview(p)">
            <div class="thumb">
              <img v-if="p.previewUrl" class="thumb-image" :src="p.previewUrl" :alt="p.name" />
              <NIcon v-else :component="ImageOutline" :size="32" />
              <NIcon v-if="p.locked" class="lock" :component="LockClosedOutline" />
            </div>
            <div class="name">{{ p.name }}</div>
            <div class="subtle meta">{{ p.at }}</div>
          </NCard>
        </div>
      </div>
      <FmEmptyIllustrated v-else :description="viewMode === 'all-public' ? '公开库暂无照片' : '当前库暂无照片'" variant="empty" />
    </template>

    <NModal v-model:show="createLibraryModal" preset="card" style="width: min(600px, calc(100vw - 24px))" title="创建照片库">
      <NForm label-placement="top">
        <NFormItem label="库名称">
          <NInput v-model:value="newLibraryName" placeholder="例如：宝宝成长记录" />
        </NFormItem>
        <NFormItem label="可见性">
          <NRadioGroup v-model:value="newLibraryVisibility">
            <NSpace>
              <NRadio value="public">可见（公开）</NRadio>
              <NRadio value="private">不可见（私有）</NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>
        <NFormItem v-if="newLibraryVisibility === 'private'" label="库密码">
          <NInput v-model:value="newLibraryPassword" type="password" show-password-on="click" placeholder="设置库密码" />
        </NFormItem>
        <NFormItem label="创建时可直接上传（可选，单次最多99张）">
          <NSpace align="center">
            <NButton secondary @click="openCreateUploadPicker">选择照片</NButton>
            <span class="subtle">{{ createSelectedFiles.length ? `已选择 ${createSelectedFiles.length} 张` : '未选择文件' }}</span>
          </NSpace>
        </NFormItem>
        <NFormItem v-if="createSelectedFiles.length" label="上传照片名称前缀（可选）">
          <NInput v-model:value="createUploadName" placeholder="例如：开学季" />
        </NFormItem>
        <NFormItem v-if="createSelectedFiles.length" label="上传照片类型">
          <NSelect v-model:value="createUploadCategory" :options="cats.filter((c) => c !== '全部').map((c) => ({ label: c, value: c }))" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="createLibraryModal = false">取消</NButton>
          <NButton type="primary" @click="confirmCreateLibrary">创建库</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="uploadModal" preset="card" style="width: min(560px, calc(100vw - 24px))" title="上传照片" @after-leave="resetUploadForm">
      <NForm label-placement="top">
        <NFormItem label="照片名称">
          <NInput v-model:value="uploadName" placeholder="例如：五一出游合照" />
        </NFormItem>
        <NFormItem label="照片类型">
          <NSelect v-model:value="uploadCategory" :options="cats.filter((c) => c !== '全部').map((c) => ({ label: c, value: c }))" />
        </NFormItem>
        <NFormItem label="上传到照片库">
          <NSelect v-model:value="uploadLibraryId" :options="libraryOptions" />
        </NFormItem>
        <NFormItem v-if="uploadNeedsPassword" label="私有库密码">
          <NSpace align="center">
            <NInput v-model:value="uploadLibraryPassword" type="password" show-password-on="click" placeholder="输入该库密码" style="width: 220px" />
            <NButton secondary @click="verifyUploadLibraryPassword">校验</NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="照片文件（单次最多99张）">
          <NSpace align="center">
            <NButton secondary @click="openUploadPicker">选择照片</NButton>
            <span class="subtle">{{ selectedFiles.length ? `已选择 ${selectedFiles.length} 张` : '未选择文件' }}</span>
          </NSpace>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="uploadModal = false">取消</NButton>
          <NButton type="primary" @click="confirmUpload">确认上传</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="previewModal" preset="card" style="width: min(820px, calc(100vw - 24px))" title="查看照片">
      <div v-if="selectedPhoto" class="preview-wrap">
        <div class="preview-stage">
          <img v-if="selectedPhoto.previewUrl" class="preview-image" :src="selectedPhoto.previewUrl" :alt="selectedPhoto.name" />
          <div v-else class="preview-empty">
            <NIcon :component="ImageOutline" :size="40" />
            <div class="subtle">当前是演示数据，暂无原图可预览</div>
          </div>
        </div>
        <NDescriptions :column="1" bordered size="small">
          <NDescriptionsItem label="名称">{{ selectedPhoto.name }}</NDescriptionsItem>
          <NDescriptionsItem label="类型">{{ selectedPhoto.category }}</NDescriptionsItem>
          <NDescriptionsItem label="日期">{{ selectedPhoto.at }}</NDescriptionsItem>
        </NDescriptions>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 6px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
  flex-wrap: nowrap;
  overflow: hidden;
}
.nav-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: nowrap;
  white-space: nowrap;
  flex-shrink: 0;
}
.nav-left .page-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.library-top-row,
.cats {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}
.library-top-row {
  margin-bottom: 8px;
}
.cats {
  margin-bottom: 4px;
}
.library-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}
.library-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.library-list-item {
  border-radius: 14px;
  cursor: pointer;
}
.library-card {
  border-radius: 14px;
  cursor: pointer;
}
.library-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.library-name {
  font-size: 15px;
  font-weight: 700;
}
.library-strip {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(72px, 1fr));
  gap: 8px;
}
.library-strip__item {
  height: 70px;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(100, 116, 139, 0.12);
}
.library-strip__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.library-strip__placeholder,
.library-strip__empty {
  height: 70px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed rgba(148, 163, 184, 0.35);
}
.library-carousel {
  margin: 8px 0 10px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(100, 116, 139, 0.08);
  padding: 6px;
}
.library-carousel__item {
  height: 146px;
  border-radius: 8px;
  overflow: hidden;
}
.library-carousel__img,
.library-carousel__placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.library-carousel__placeholder,
.library-carousel__empty {
  min-height: 146px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border: 1px dashed rgba(148, 163, 184, 0.35);
}
.is-private {
  filter: blur(8px) saturate(0.8);
}
.library-strip__item.is-private .library-strip__img {
  filter: blur(8px) saturate(0.8);
}
.masonry {
  column-count: 3;
  column-gap: 14px;
}
@media (max-width: 1100px) {
  .masonry {
    column-count: 2;
  }
}
@media (max-width: 640px) {
  .masonry {
    column-count: 1;
  }
}
.masonry__item {
  break-inside: avoid;
  margin-bottom: 14px;
}
.tile {
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  box-sizing: border-box;
  cursor: pointer;
}
.thumb {
  flex: 1;
  min-height: 64px;
  border-radius: 12px;
  background: linear-gradient(145deg, rgba(13, 148, 136, 0.15), rgba(100, 116, 139, 0.12));
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.thumb-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.lock {
  position: absolute;
  top: 8px;
  right: 8px;
  color: #fbbf24;
}
.name {
  margin-top: 10px;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.35;
  color: var(--fm-text-strong);
}
.meta {
  font-size: 11px;
  margin-top: 4px;
}
.hidden-input {
  display: none;
}
.preview-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.preview-stage {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(100, 116, 139, 0.08);
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-image {
  width: 100%;
  max-height: 62vh;
  object-fit: contain;
}
.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
</style>
