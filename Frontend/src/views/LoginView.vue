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

<style scoped>
.auth-page {
  --ease-out: cubic-bezier(0.23, 1, 0.32, 1);
  --ease-move: cubic-bezier(0.77, 0, 0.175, 1);
  --ease-back: cubic-bezier(0.34, 1.56, 0.64, 1);
  --copy-kicker: #99f6e4;
  --copy-lead: rgba(255, 255, 255, 0.55);
  --copy-main: #f8fafc;
  --copy-accent: #5eead4;
  --copy-body: rgba(226, 232, 240, 0.8);
  --g0: #f8fafc;
  --g1: #99f6e4;
  --g2: #5eead4;
  --g3: #e2e8f0;
  --g4: #a5f3fc;
  position: relative;
  min-height: 100svh;
  overflow: hidden;
  color: #f8fafc;
  background: #07090d;
  perspective: 1200px;
}

.auth-page[data-scene='journal'],
.hero-copy.tone-journal {
  --copy-kicker: #aecbfa;
  --copy-lead: rgba(232, 240, 254, 0.64);
  --copy-main: #e8f0fe;
  --copy-accent: #fbbc05;
  --copy-body: rgba(210, 227, 252, 0.88);
  --g0: #e8f0fe;
  --g1: #8ab4f8;
  --g2: #fbbc05;
  --g3: #aecbfa;
  --g4: #ea4335;
}

.auth-page[data-scene='health'],
.hero-copy.tone-health {
  --copy-kicker: #a8dab5;
  --copy-lead: rgba(230, 244, 234, 0.66);
  --copy-main: #e6f4ea;
  --copy-accent: #fbbc05;
  --copy-body: rgba(168, 218, 181, 0.9);
  --g0: #e6f4ea;
  --g1: #81c995;
  --g2: #fbbc05;
  --g3: #34a853;
  --g4: #ceead6;
}

.auth-page[data-scene='rhythm'],
.hero-copy.tone-rhythm {
  --copy-kicker: #fde293;
  --copy-lead: rgba(254, 247, 224, 0.66);
  --copy-main: #fef7e0;
  --copy-accent: #ea4335;
  --copy-body: rgba(253, 226, 147, 0.9);
  --g0: #fef7e0;
  --g1: #fbbc05;
  --g2: #ea4335;
  --g3: #fde293;
  --g4: #f9ab00;
}

.auth-page[data-scene='voyage'],
.hero-copy.tone-voyage {
  --copy-kicker: #ffb4a2;
  --copy-lead: rgba(255, 220, 230, 0.6);
  --copy-main: #fff0f3;
  --copy-accent: #f0abfc;
  --copy-body: rgba(255, 226, 232, 0.84);
  --g0: #fff0f3;
  --g1: #ffb4a2;
  --g2: #f0abfc;
  --g3: #fb7185;
  --g4: #e9d5ff;
}

.auth-page[data-scene='vault'],
.hero-copy.tone-vault {
  --copy-kicker: #a5f3fc;
  --copy-lead: rgba(199, 210, 254, 0.62);
  --copy-main: #eef2ff;
  --copy-accent: #818cf8;
  --copy-body: rgba(199, 210, 254, 0.86);
  --g0: #eef2ff;
  --g1: #a5f3fc;
  --g2: #818cf8;
  --g3: #c4b5fd;
  --g4: #67e8f9;
}

.stage {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.scene {
  position: absolute;
  inset: 0;
  opacity: 0;
  transform: scale(1.04);
  filter: saturate(0.92);
  transition:
    opacity 1400ms var(--ease-out),
    transform 7200ms var(--ease-move),
    filter 1400ms var(--ease-out);
}

.scene.active {
  z-index: 1;
  opacity: 1;
  transform: scale(1);
  filter: saturate(1.08);
}

.scene-wash,
.scene-orb {
  position: absolute;
  inset: 0;
}

.scene-orb {
  border-radius: 50%;
  filter: blur(80px);
  will-change: transform;
}

.scene--journal .scene-wash {
  background: linear-gradient(118deg, #0c2340 0%, #174ea6 34%, #4285f4 68%, #8ab4f8 100%);
  background: linear-gradient(118deg in oklab, #0c2340 0%, #174ea6 34%, #4285f4 68%, #8ab4f8 100%);
}
.scene--journal .scene-orb--a { width: 42vw; height: 42vw; left: -8%; top: -18%; background: rgba(66, 133, 244, 0.4); }
.scene--journal .scene-orb--b { width: 34vw; height: 34vw; right: -6%; bottom: -12%; background: rgba(234, 67, 53, 0.28); }
.scene--journal .scene-orb--c { width: 22vw; height: 22vw; left: 38%; top: 18%; background: rgba(251, 188, 5, 0.22); }

.scene--health .scene-wash {
  background: linear-gradient(126deg, #0b2e18 0%, #0d652d 32%, #34a853 62%, #a8dab5 100%);
  background: linear-gradient(126deg in oklab, #0b2e18 0%, #0d652d 32%, #34a853 62%, #a8dab5 100%);
}
.scene--health .scene-orb--a { width: 46vw; height: 46vw; left: -12%; top: -10%; background: rgba(52, 168, 83, 0.34); }
.scene--health .scene-orb--b { width: 36vw; height: 36vw; right: -10%; bottom: -8%; background: rgba(251, 188, 5, 0.22); }
.scene--health .scene-orb--c { width: 18vw; height: 18vw; left: 48%; top: 42%; background: rgba(168, 218, 181, 0.24); }

.scene--rhythm .scene-wash {
  background: linear-gradient(144deg, #3d2200 0%, #f9ab00 36%, #fbbc05 62%, #ea4335 100%);
  background: linear-gradient(144deg in oklab, #3d2200 0%, #f9ab00 36%, #fbbc05 62%, #ea4335 100%);
}
.scene--rhythm .scene-orb--a { width: 40vw; height: 40vw; right: -8%; top: -16%; background: rgba(251, 188, 5, 0.32); }
.scene--rhythm .scene-orb--b { width: 32vw; height: 32vw; left: -6%; bottom: -10%; background: rgba(234, 67, 53, 0.3); }
.scene--rhythm .scene-orb--c { width: 20vw; height: 20vw; left: 40%; top: 30%; background: rgba(253, 226, 147, 0.22); }

.scene--voyage .scene-wash {
  background: linear-gradient(148deg, #1a0b16 0%, #c2410c 36%, #db2777 68%, #4c1d95 100%);
}
.scene--voyage .scene-orb--a { width: 44vw; height: 44vw; left: -10%; top: -14%; background: rgba(251, 146, 60, 0.28); }
.scene--voyage .scene-orb--b { width: 38vw; height: 38vw; right: -12%; bottom: -12%; background: rgba(139, 92, 246, 0.24); }
.scene--voyage .scene-orb--c { width: 16vw; height: 16vw; left: 52%; top: 20%; background: rgba(244, 114, 182, 0.18); }

.scene--vault .scene-wash {
  background: linear-gradient(156deg, #020617 0%, #1e1b4b 38%, #6366f1 70%, #22d3ee 100%);
}
.scene--vault .scene-orb--a { width: 48vw; height: 48vw; right: -16%; top: -18%; background: rgba(99, 102, 241, 0.28); }
.scene--vault .scene-orb--b { width: 30vw; height: 30vw; left: -8%; bottom: -8%; background: rgba(34, 211, 238, 0.16); }
.scene--vault .scene-orb--c { width: 18vw; height: 18vw; left: 36%; top: 28%; background: rgba(165, 180, 252, 0.14); }

.scene.active .scene-orb--a { animation: drift-a 18s var(--ease-move) infinite; }
.scene.active .scene-orb--b { animation: drift-b 22s var(--ease-move) infinite; }
.scene.active .scene-orb--c { animation: drift-c 16s var(--ease-move) infinite; }

@keyframes drift-a {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(28px, 18px, 0); }
}
@keyframes drift-b {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(-24px, -16px, 0); }
}
@keyframes drift-c {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(16px, -22px, 0); }
}

.stage-veil {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(7, 9, 13, 0.38) 0%, rgba(7, 9, 13, 0.08) 48%, rgba(7, 9, 13, 0.42) 100%),
    linear-gradient(180deg, rgba(7, 9, 13, 0.18), transparent 28%, rgba(7, 9, 13, 0.28) 100%);
}

.stage-grain {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0.16;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='140' height='140'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='140' height='140' filter='url(%23n)' opacity='.55'/%3E%3C/svg%3E");
}

.topbar,
.shell {
  position: relative;
  z-index: 3;
  pointer-events: none;
}

.brand,
.scene-dots,
.panel,
.kicker,
.hero-copy,
.pillars {
  pointer-events: auto;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 36px 0;
}

.brand {
  margin: 0;
  color: rgba(226, 232, 240, 0.72);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.22em;
}

.scene-dots {
  display: flex;
  align-items: center;
  gap: 8px;
  pointer-events: auto;
}

.scene-dot {
  width: 8px;
  height: 8px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.32);
  cursor: pointer;
  transition:
    width 220ms var(--ease-out),
    background-color 220ms ease;
}

.scene-dot.active {
  width: 28px;
  height: 8px;
  background: var(--copy-accent);
}

.shell {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 400px);
  gap: clamp(32px, 6vw, 80px);
  align-items: center;
  min-height: calc(100svh - 72px);
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 36px 40px;
}

.kicker {
  width: fit-content;
  margin: 0 0 18px;
  color: var(--copy-kicker);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  transition:
    color 180ms ease,
    letter-spacing 180ms var(--ease-out),
    text-shadow 180ms ease;
}

.wave-filter {
  position: absolute;
  width: 0;
  height: 0;
  overflow: hidden;
}

.ui-sheet {
  position: relative;
  z-index: 3;
  filter: url(#auth-wave-cloth);
}

.hero-meta {
  display: flex;
  align-items: center;
  width: fit-content;
  gap: 12px;
  color: var(--copy-kicker);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.hero-meta b {
  font-family: var(--fm-font-mono), ui-monospace, monospace;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--copy-accent);
}

.hero-meta span {
  color: var(--copy-kicker);
}

.hero-meta::after {
  content: '';
  width: 36px;
  height: 1px;
  background: var(--copy-accent);
  opacity: 0.55;
}

.hero-title {
  margin: 14px 0 16px;
  color: var(--copy-main);
  font-size: clamp(36px, 4.8vw, 68px);
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: -0.04em;
  white-space: nowrap;
}

.hero-copy.tone-journal .hero-title {
  font-family: "Songti SC", "STSong", "Noto Serif SC", "Source Han Serif SC", Georgia, serif;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.hero-line {
  max-width: 28ch;
  margin: 0;
  color: var(--copy-body);
  font-size: 16px;
  line-height: 1.7;
}

.pillars {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 480px;
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
}

.pillars li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 11px;
  border: 1px solid color-mix(in srgb, var(--copy-accent) 32%, transparent);
  border-radius: 999px;
  background: color-mix(in srgb, var(--copy-accent) 10%, rgba(255, 255, 255, 0.06));
  backdrop-filter: blur(16px);
  color: var(--copy-main);
  font-size: 12px;
  transition:
    border-color 180ms ease,
    background-color 180ms ease,
    color 180ms ease,
    transform 180ms var(--ease-out);
}

.panel {
  position: relative;
  isolation: isolate;
}

.card {
  position: relative;
  z-index: 1;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.22) !important;
  border-radius: 24px !important;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(8, 12, 22, 0.18)) !important;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.28),
    0 28px 80px rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(22px) saturate(140%);
  -webkit-backdrop-filter: blur(22px) saturate(140%);
}

.card :deep(.n-card__content) {
  padding: 22px;
  background: transparent;
}

.mode-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  margin-bottom: 18px;
  padding: 4px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 12px;
  background: transparent;
}

.mode-tab {
  height: 36px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  color: rgba(226, 232, 240, 0.58);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: color 160ms ease, border-color 160ms ease, background-color 160ms ease;
}

.mode-tab.active {
  background: transparent;
  color: #fff;
  border-color: rgba(255, 255, 255, 0.38);
  box-shadow: none;
}

.form {
  display: grid;
  gap: 12px;
}

.form h2 {
  margin: 0;
  color: #f8fafc;
  font-size: 22px;
  letter-spacing: -0.03em;
}

.sub {
  margin: -4px 0 4px;
  color: rgba(226, 232, 240, 0.68);
  font-size: 13px;
}

.field {
  display: grid;
  gap: 6px;
}

.field span {
  color: rgba(241, 245, 249, 0.78);
  font-size: 12px;
  font-weight: 700;
}

.input-icon {
  color: rgba(226, 232, 240, 0.55);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.text-btn {
  padding: 0;
  border: 0;
  background: none;
  color: var(--copy-accent);
  font-size: 12px;
  cursor: pointer;
}

.card :deep(.n-checkbox .n-checkbox__label) {
  color: rgba(226, 232, 240, 0.78);
}

.card :deep(.n-button--primary-type) {
  min-height: 46px;
  margin-top: 4px;
  color: #f8fafc !important;
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.22) !important;
  box-shadow: none !important;
}

.card :deep(.n-button--primary-type:hover),
.card :deep(.n-button--primary-type:focus),
.card :deep(.n-button--primary-type:active),
.card :deep(.n-button--primary-type.n-button--disabled) {
  color: #ffffff !important;
  background: transparent !important;
  border-color: rgba(255, 255, 255, 0.38) !important;
  box-shadow: none !important;
}

.scene-copy-enter-active,
.scene-copy-leave-active,
.form-swap-enter-active,
.form-swap-leave-active {
  transition: opacity 280ms var(--ease-out), transform 280ms var(--ease-out);
}

.scene-copy-enter-from {
  opacity: 0;
  transform: translate3d(0, 12px, 0);
}
.scene-copy-leave-to {
  opacity: 0;
  transform: translate3d(0, -8px, 0);
}
.form-swap-enter-from,
.form-swap-leave-to {
  opacity: 0;
  transform: translate3d(0, 8px, 0);
}

@media (hover: hover) and (pointer: fine) {
  .scene-dot:hover:not(.active) {
    background: rgba(255, 255, 255, 0.7);
  }
  .mode-tab:hover:not(.active) {
    color: #f8fafc;
  }
  .card :deep(.n-button--primary-type:active) {
    transform: scale(0.985);
  }
  .kicker:hover {
    color: var(--copy-accent);
    letter-spacing: 0.22em;
    text-shadow: 0 0 18px color-mix(in srgb, var(--copy-accent) 55%, transparent);
  }
  .pillars li:hover {
    border-color: color-mix(in srgb, var(--copy-accent) 70%, transparent);
    background: color-mix(in srgb, var(--copy-accent) 22%, rgba(255, 255, 255, 0.08));
    transform: translate3d(0, -2px, 0);
  }
}

@media (max-width: 900px) {
  .shell {
    grid-template-columns: 1fr;
    min-height: auto;
    padding: 24px 20px 32px;
  }
  .panel {
    max-width: 420px;
    width: 100%;
    margin: 0 auto;
  }
  .hero-title {
    font-size: clamp(34px, 9vw, 52px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .scene,
  .scene-dot,
  .mode-tab,
  .scene-copy-enter-active,
  .scene-copy-leave-active,
  .form-swap-enter-active,
  .form-swap-leave-active,
  .kicker,
  .pillars li {
    transition: opacity 200ms ease, color 200ms ease;
    animation: none !important;
    transform: none !important;
  }
  .scene:not(.active) {
    opacity: 0;
  }
}
</style>

<style>
/* 非 scoped：压过 Naive 内联 --n-color，以及 Sketch 全局输入框实底/描边阴影 */
[data-page='auth'] .n-input,
html[data-preset='sketch'] [data-page='auth'] .n-input,
html[data-preset='sketch'] [data-page='auth'] .n-input-number {
  --n-color: transparent !important;
  --n-color-focus: transparent !important;
  --n-color-disabled: transparent !important;
  --n-color-focus-error: transparent !important;
  --n-color-focus-warning: transparent !important;
  --n-box-shadow-focus: none !important;
  background: transparent !important;
  background-color: transparent !important;
  background-image: none !important;
  box-shadow: none !important;
}

[data-page='auth'] .n-input.n-input--focus,
[data-page='auth'] .n-input.n-input--hover,
html[data-preset='sketch'] [data-page='auth'] .n-input.n-input--focus,
html[data-preset='sketch'] [data-page='auth'] .n-input.n-input--hover {
  background: transparent !important;
  background-color: transparent !important;
  box-shadow: none !important;
}

[data-page='auth'] .n-input .n-input-wrapper,
[data-page='auth'] .n-input .n-input__input-el,
[data-page='auth'] .n-input .n-input__textarea-el,
[data-page='auth'] .n-input .n-input__prefix,
[data-page='auth'] .n-input .n-input__suffix,
[data-page='auth'] .n-input .n-input__state-border,
[data-page='auth'] .n-input input,
[data-page='auth'] .n-input textarea,
html[data-preset='sketch'] [data-page='auth'] .n-input .n-input-wrapper,
html[data-preset='sketch'] [data-page='auth'] .n-input input,
html[data-preset='sketch'] [data-page='auth'] .n-input textarea {
  background: transparent !important;
  background-color: transparent !important;
  background-image: none !important;
}

[data-page='auth'] .n-input input:-webkit-autofill,
[data-page='auth'] .n-input input:-webkit-autofill:hover,
[data-page='auth'] .n-input input:-webkit-autofill:focus,
[data-page='auth'] .n-input input:-webkit-autofill:active {
  -webkit-text-fill-color: #f8fafc !important;
  caret-color: #f8fafc;
  transition: background-color 99999s ease-out 0s;
  box-shadow: 0 0 0 1000px transparent inset !important;
  -webkit-box-shadow: 0 0 0 1000px transparent inset !important;
}

[data-page='auth'] .n-button,
html[data-preset='sketch'] [data-page='auth'] .n-button {
  --n-color: transparent !important;
  --n-color-hover: transparent !important;
  --n-color-pressed: transparent !important;
  --n-color-focus: transparent !important;
  --n-color-disabled: transparent !important;
  --n-ripple-color: transparent !important;
  background: transparent !important;
  background-color: transparent !important;
  background-image: none !important;
  box-shadow: none !important;
}

[data-page='auth'] .n-button:hover,
[data-page='auth'] .n-button:focus,
[data-page='auth'] .n-button:active,
[data-page='auth'] .n-button.n-button--disabled,
html[data-preset='sketch'] [data-page='auth'] .n-button:hover,
html[data-preset='sketch'] [data-page='auth'] .n-button:focus,
html[data-preset='sketch'] [data-page='auth'] .n-button:active {
  background: transparent !important;
  background-color: transparent !important;
  box-shadow: none !important;
}

[data-page='auth'] .n-button--primary-type,
html[data-preset='sketch'] [data-page='auth'] .n-button--primary-type {
  --n-text-color: #f8fafc !important;
  --n-text-color-hover: #ffffff !important;
  --n-text-color-pressed: #e2e8f0 !important;
  --n-text-color-focus: #ffffff !important;
  --n-text-color-disabled: rgba(248, 250, 252, 0.45) !important;
  --n-border: 1px solid rgba(255, 255, 255, 0.22) !important;
  --n-border-hover: 1px solid rgba(255, 255, 255, 0.38) !important;
  --n-border-pressed: 1px solid rgba(255, 255, 255, 0.32) !important;
  --n-border-focus: 1px solid rgba(255, 255, 255, 0.5) !important;
  --n-border-disabled: 1px solid rgba(255, 255, 255, 0.14) !important;
}

[data-page='auth'] .n-button--primary-type .n-button__border,
[data-page='auth'] .n-button--primary-type .n-button__state-border,
html[data-preset='sketch'] [data-page='auth'] .n-button--primary-type .n-button__border,
html[data-preset='sketch'] [data-page='auth'] .n-button--primary-type .n-button__state-border {
  border-color: rgba(255, 255, 255, 0.22) !important;
  box-shadow: none !important;
}
</style>
