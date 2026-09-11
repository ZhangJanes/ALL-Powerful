<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { darkTheme, lightTheme, NConfigProvider, useMessage } from 'naive-ui'
import {
  AirplaneOutline,
  CheckboxOutline,
  DocumentTextOutline,
  ImagesOutline,
  LockClosedOutline,
  NutritionOutline,
  PersonOutline,
  WalletOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import LoginThreeField from '@/components/auth/LoginThreeField.vue'
import LoginFormAura from '@/components/auth/LoginFormAura.vue'
import { fillDisplacementMap, prunePulses } from '@/components/auth/loginWave'

type AuthMode = 'login' | 'register'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const auth = useAuthStore()

const mode = ref<AuthMode>(route.name === 'register' ? 'register' : 'login')
const account = ref('')
const displayName = ref('')
const password = ref('')
const confirmPassword = ref('')
const remember = ref(true)
const loading = ref(false)
const now = ref(new Date())
const sceneIndex = ref(0)
const paused = ref(false)
const reduceMotion = ref(
  typeof window !== 'undefined'
    ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
    : false,
)

let clockTimer: ReturnType<typeof setInterval> | undefined
let sceneTimer: ReturnType<typeof setInterval> | undefined
let wobbleRaf = 0
const pageRef = ref<HTMLElement | null>(null)
const waveMapRef = ref<SVGImageElement | null>(null)
const CLOTH_SCALE = 72
const clothCanvas = typeof document !== 'undefined' ? document.createElement('canvas') : null
if (clothCanvas) {
  clothCanvas.width = 96
  clothCanvas.height = 54
}
const clothCtx = clothCanvas?.getContext('2d', { willReadFrequently: true }) ?? null
const clothData = clothCtx ? clothCtx.createImageData(96, 54) : null
const waveRest = { cx: 0, cy: 0, w: 1, h: 1 }

function captureWaveRests() {
  const root = pageRef.value
  if (!root) return
  const sheet = root.querySelector<HTMLElement>('[data-wave-cloth]')
  if (!sheet) return
  const box = sheet.getBoundingClientRect()
  waveRest.cx = box.left + box.width * 0.5
  waveRest.cy = box.top + box.height * 0.5
  waveRest.w = box.width
  waveRest.h = box.height
}

function pushClothMap(t: number) {
  const img =
    waveMapRef.value ||
    (pageRef.value?.querySelector('#auth-wave-cloth feImage') as SVGImageElement | null)
  if (!clothCtx || !clothData || !clothCanvas || !img) return
  fillDisplacementMap(clothData, waveRest, t, CLOTH_SCALE)
  clothCtx.putImageData(clothData, 0, 0)
  const url = clothCanvas.toDataURL()
  img.setAttribute('href', url)
  img.setAttributeNS('http://www.w3.org/1999/xlink', 'href', url)
}

function floatOnWave(now: number) {
  wobbleRaf = requestAnimationFrame(floatOnWave)
  prunePulses(now * 0.001)
  pushClothMap(now * 0.001)
}

const scenes = [
  {
    key: 'journal',
    no: '01',
    kicker: 'LIFE JOURNAL',
    title: '一本生活手帐',
    lead: '一本',
    before: '生活',
    accent: '手帐',
    line: '把备忘、记账、习惯写进同一页纸上。',
    icon: DocumentTextOutline,
  },
  {
    key: 'health',
    no: '02',
    kicker: 'NUTRITION',
    title: '科学健康饮食',
    lead: '科学',
    before: '健康',
    accent: '饮食',
    line: '体脂分级、医学方案与每日打卡都落在自己身上。',
    icon: NutritionOutline,
  },
  {
    key: 'rhythm',
    no: '03',
    kicker: 'RHYTHM',
    title: '账本与坚持',
    lead: '看得见的',
    before: '账本',
    accent: '坚持',
    line: '预算、打卡、连续天数，让家庭节奏看得见。',
    icon: WalletOutline,
  },
  {
    key: 'voyage',
    no: '04',
    kicker: 'VOYAGE',
    title: '出行、灵感、相册',
    lead: '把散落的',
    before: '灵感',
    accent: '带上路',
    line: '行程清单、闪念和新照片，不再散落各处。',
    icon: AirplaneOutline,
  },
  {
    key: 'vault',
    no: '05',
    kicker: 'PRIVATE CLOUD',
    title: '只属于你们的空间',
    lead: '只属于',
    before: '你们的',
    accent: '空间',
    line: '独立账户、云端同步，生活数据安静地待在家里。',
    icon: ImagesOutline,
  },
] as const

const activeScene = computed(() => scenes[sceneIndex.value])

watch(
  () => route.name,
  (name) => {
    mode.value = name === 'register' ? 'register' : 'login'
  },
)

watch(
  () => [sceneIndex.value, mode.value],
  () => {
    window.setTimeout(() => captureWaveRests(), 320)
  },
)

function nextScene() {
  if (paused.value || reduceMotion.value) return
  sceneIndex.value = (sceneIndex.value + 1) % scenes.length
}

function jumpScene(i: number) {
  if (sceneIndex.value === i) return
  sceneIndex.value = i
  if (!reduceMotion.value) startSceneTimer()
}

function startSceneTimer() {
  if (sceneTimer) clearInterval(sceneTimer)
  sceneTimer = setInterval(nextScene, 7200)
}

onMounted(() => {
  reduceMotion.value = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  clockTimer = setInterval(() => {
    now.value = new Date()
  }, 1000)
  if (!reduceMotion.value) startSceneTimer()
  void nextTick(() => {
    captureWaveRests()
    pushClothMap(performance.now() * 0.001)
    wobbleRaf = requestAnimationFrame(floatOnWave)
  })
  window.addEventListener('resize', captureWaveRests)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (sceneTimer) clearInterval(sceneTimer)
  cancelAnimationFrame(wobbleRaf)
  window.removeEventListener('resize', captureWaveRests)
})

const redirectTo = computed(() => {
  const raw = route.query.redirect
  return typeof raw === 'string' && raw.startsWith('/') ? raw : '/'
})

const isRegister = computed(() => mode.value === 'register')

const greeting = computed(() => {
  const h = now.value.getHours()
  const base = h < 6 ? '夜深了' : h < 11 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
  return isRegister.value ? `${base}，建立家庭空间` : `${base}，欢迎回家`
})

function switchMode(next: AuthMode) {
  if (mode.value === next) return
  mode.value = next
  password.value = ''
  confirmPassword.value = ''
  router.replace({ name: next, query: route.query })
}

async function submitLogin() {
  if (!account.value.trim() || !password.value.trim()) {
    message.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(account.value, password.value, remember.value)
    message.success('登录成功，欢迎回来')
    await router.replace(redirectTo.value)
  } catch (e) {
    message.error(e instanceof Error ? e.message : '登录失败')
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  const name = account.value.trim()
  if (!name) {
    message.warning('请输入用户名')
    return
  }
  if (name.length < 3) {
    message.warning('用户名至少 3 个字符')
    return
  }
  if (!password.value) {
    message.warning('请输入密码')
    return
  }
  if (password.value.length < 6) {
    message.warning('密码至少 6 位')
    return
  }
  if (password.value !== confirmPassword.value) {
    message.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await auth.register(name, password.value, displayName.value, remember.value)
    message.success('注册成功，已自动登录')
    await router.replace(redirectTo.value)
  } catch (e) {
    message.error(e instanceof Error ? e.message : '注册失败')
  } finally {
    loading.value = false
  }
}

function submit() {
  if (isRegister.value) return submitRegister()
  return submitLogin()
}

const pageOverrides = {
  common: {
    fontFamily: 'var(--fm-font-sans)',
    borderRadius: '14px',
  },
} as const

const formOverrides = {
  common: {
    fontFamily: 'var(--fm-font-sans)',
    borderRadius: '12px',
    primaryColor: '#f1f5f9',
    primaryColorHover: '#ffffff',
    primaryColorPressed: '#cbd5e1',
    primaryColorSuppl: '#e2e8f0',
    textColorBase: '#f8fafc',
    placeholderColor: 'rgba(248, 250, 252, 0.38)',
    inputColor: 'transparent',
    inputColorDisabled: 'transparent',
    borderColor: 'rgba(255, 255, 255, 0.22)',
    hoverColor: 'transparent',
    cardColor: 'transparent',
    modalColor: 'transparent',
    bodyColor: 'transparent',
  },
  Card: {
    color: 'transparent',
    colorEmbedded: 'transparent',
  },
  Input: {
    color: 'transparent',
    colorFocus: 'transparent',
    colorDisabled: 'transparent',
    colorFocusError: 'transparent',
    colorFocusWarning: 'transparent',
    textColor: '#f8fafc',
    caretColor: '#f8fafc',
    placeholderColor: 'rgba(248, 250, 252, 0.4)',
    border: '1px solid rgba(255, 255, 255, 0.22)',
    borderHover: '1px solid rgba(255, 255, 255, 0.38)',
    borderFocus: '1px solid rgba(255, 255, 255, 0.5)',
    boxShadowFocus: 'none',
  },
  Button: {
    colorPrimary: 'transparent',
    colorHoverPrimary: 'transparent',
    colorPressedPrimary: 'transparent',
    colorFocusPrimary: 'transparent',
    colorDisabledPrimary: 'transparent',
    textColorPrimary: '#f8fafc',
    textColorHoverPrimary: '#ffffff',
    textColorPressedPrimary: '#e2e8f0',
    borderPrimary: '1px solid rgba(255, 255, 255, 0.22)',
    borderHoverPrimary: '1px solid rgba(255, 255, 255, 0.38)',
    borderPressedPrimary: '1px solid rgba(255, 255, 255, 0.32)',
    borderFocusPrimary: '1px solid rgba(255, 255, 255, 0.5)',
    rippleColor: 'transparent',
  },
} as const

const inputSkin = formOverrides.Input
</script>

<template>
  <NConfigProvider :theme="lightTheme" :theme-overrides="pageOverrides">
    <div ref="pageRef" class="auth-page" data-page="auth" :data-mode="mode" :data-scene="activeScene.key">
      <svg class="wave-filter" width="0" height="0" aria-hidden="true">
        <filter
          id="auth-wave-cloth"
          x="-14%"
          y="-14%"
          width="128%"
          height="128%"
          color-interpolation-filters="sRGB"
        >
          <feImage ref="waveMapRef" result="map" preserveAspectRatio="none" />
          <feDisplacementMap
            in="SourceGraphic"
            in2="map"
            scale="72"
            xChannelSelector="R"
            yChannelSelector="G"
          />
        </filter>
      </svg>
      <div class="stage">
        <article
          v-for="(scene, i) in scenes"
          :key="scene.key"
          class="scene"
          :class="[`scene--${scene.key}`, { active: i === sceneIndex }]"
          aria-hidden="true"
        >
          <div class="scene-wash" />
          <div class="scene-orb scene-orb--a" />
          <div class="scene-orb scene-orb--b" />
          <div class="scene-orb scene-orb--c" />
        </article>
        <div class="stage-veil" aria-hidden="true" />
        <div class="stage-grain" aria-hidden="true" />
        <LoginThreeField :scene-key="activeScene.key" />
      </div>

      <div class="ui-sheet" data-wave-cloth>
      <header class="topbar">
        <p class="brand">ALL-POWERFUL</p>
        <div class="scene-dots" role="tablist" aria-label="背景场景">
          <button
            v-for="(scene, i) in scenes"
            :key="scene.key"
            type="button"
            class="scene-dot"
            :class="{ active: i === sceneIndex }"
            :aria-label="scene.title"
            @mouseenter="jumpScene(i)"
            @focus="jumpScene(i)"
            @click="jumpScene(i)"
          />
        </div>
      </header>

      <main class="shell">
        <section class="hero">
          <p class="kicker">{{ greeting }}</p>
          <Transition name="scene-copy" mode="out-in">
            <div :key="activeScene.key" class="hero-copy" :class="`tone-${activeScene.key}`">
              <div class="hero-meta">
                <b>{{ activeScene.no }}</b>
                <span>{{ activeScene.kicker }}</span>
              </div>
              <h1 class="hero-title">{{ activeScene.title }}</h1>
              <p class="hero-line">{{ activeScene.line }}</p>
            </div>
          </Transition>
          <ul class="pillars">
            <li><NIcon :component="DocumentTextOutline" />手帐一体</li>
            <li><NIcon :component="NutritionOutline" />健康落库</li>
            <li><NIcon :component="CheckboxOutline" />习惯可追</li>
            <li><NIcon :component="ImagesOutline" />私密云端</li>
          </ul>
        </section>

        <section class="panel" @mouseenter="paused = true" @mouseleave="paused = false">
          <LoginFormAura :scene-key="activeScene.key" />
          <NConfigProvider :theme="darkTheme" :theme-overrides="formOverrides">
          <NCard class="card" :bordered="false">
            <div class="mode-tabs">
              <button type="button" class="mode-tab" :class="{ active: !isRegister }" @click="switchMode('login')">
                登录
              </button>
              <button type="button" class="mode-tab" :class="{ active: isRegister }" @click="switchMode('register')">
                注册
              </button>
            </div>

            <Transition name="form-swap" mode="out-in">
              <div :key="mode" class="form">
                <h2>{{ isRegister ? '创建账号' : '进入家庭空间' }}</h2>
                <p class="sub">{{ isRegister ? '注册后自动登录，数据写入云端。' : '一个账号，接住一家人的日常。' }}</p>

                <label class="field">
                  <span>用户名</span>
                  <NInput
                    v-model:value="account"
                    :placeholder="isRegister ? '至少 3 个字符' : '请输入用户名'"
                    size="large"
                    clearable
                    :theme-overrides="inputSkin"
                  >
                    <template #prefix>
                      <NIcon :component="PersonOutline" class="input-icon" />
                    </template>
                  </NInput>
                </label>

                <label v-if="isRegister" class="field">
                  <span>昵称（可选）</span>
                  <NInput
                    v-model:value="displayName"
                    placeholder="默认同用户名"
                    size="large"
                    clearable
                    :theme-overrides="inputSkin"
                  />
                </label>

                <label class="field">
                  <span>密码</span>
                  <NInput
                    v-model:value="password"
                    type="password"
                    show-password-on="click"
                    :placeholder="isRegister ? '至少 6 位' : '请输入密码'"
                    size="large"
                    :theme-overrides="inputSkin"
                    @keydown.enter.prevent="!isRegister && submit()"
                  >
                    <template #prefix>
                      <NIcon :component="LockClosedOutline" class="input-icon" />
                    </template>
                  </NInput>
                </label>

                <label v-if="isRegister" class="field">
                  <span>确认密码</span>
                  <NInput
                    v-model:value="confirmPassword"
                    type="password"
                    show-password-on="click"
                    placeholder="再次输入密码"
                    size="large"
                    :theme-overrides="inputSkin"
                    @keydown.enter.prevent="submit"
                  >
                    <template #prefix>
                      <NIcon :component="LockClosedOutline" class="input-icon" />
                    </template>
                  </NInput>
                </label>

                <div class="row">
                  <NCheckbox v-model:checked="remember">记住我</NCheckbox>
                  <button v-if="!isRegister" type="button" class="text-btn" @click="message.info('请联系管理员重置密码')">
                    忘记密码
                  </button>
                </div>

                <NButton type="primary" size="large" block :loading="loading" @click="submit">
                  {{ isRegister ? '注册并进入' : '登录' }}
                </NButton>
              </div>
            </Transition>
          </NCard>
          </NConfigProvider>
        </section>
      </main>
      </div>
    </div>
  </NConfigProvider>
</template>

<style scoped lang="scss" src="./LoginView.scss"></style>
