<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { lightTheme, NConfigProvider, useMessage } from 'naive-ui'
import {
  AirplaneOutline,
  CheckboxOutline,
  DocumentTextOutline,
  ImagesOutline,
  LockClosedOutline,
  PersonOutline,
  WalletOutline,
} from '@vicons/ionicons5'
import FmAntdIllustration from '@/components/illustrations/FmAntdIllustration.vue'
import { useAuthStore } from '@/stores/auth'

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
let clockTimer: ReturnType<typeof setInterval> | undefined
let featureTimer: ReturnType<typeof setInterval> | undefined

watch(
  () => route.name,
  (name) => {
    mode.value = name === 'register' ? 'register' : 'login'
  },
)

onMounted(() => {
  clockTimer = setInterval(() => {
    now.value = new Date()
  }, 1000)
  featureTimer = setInterval(() => {
    featureIndex.value = (featureIndex.value + 1) % featureCards.length
  }, 4200)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (featureTimer) clearInterval(featureTimer)
})

const redirectTo = computed(() => {
  const raw = route.query.redirect
  return typeof raw === 'string' && raw.startsWith('/') ? raw : '/'
})

const isRegister = computed(() => mode.value === 'register')

const greeting = computed(() => {
  const h = now.value.getHours()
  const base = h < 6 ? '夜深了' : h < 11 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
  return isRegister.value ? `${base}，欢迎加入` : `${base}，欢迎回来`
})

const timeLine = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(now.value),
)

const dateLine = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }).format(now.value),
)

const featureCards = [
  {
    icon: DocumentTextOutline,
    title: '备忘录',
    desc: '待办提醒 · 云端同步',
    hint: '支持分类、提醒与快捷记录',
    metric: '今日清单可视化',
    points: ['提醒时间管理', '待办完成进度', '快速新增'],
    color: '#14b8a6',
  },
  {
    icon: WalletOutline,
    title: '家庭记账',
    desc: '收支统计 · 预算管理',
    hint: '每月预算进度一目了然',
    metric: '预算阈值提醒',
    points: ['日/周/月统计', '分类占比分析', '导出复盘'],
    color: '#4f46e5',
  },
  {
    icon: CheckboxOutline,
    title: '习惯打卡',
    desc: '坚持记录 · 成就激励',
    hint: '每日打卡与补卡能力',
    metric: '连续天数追踪',
    points: ['打卡日历', '补卡次数控制', '成就解锁'],
    color: '#0ea5e9',
  },
  {
    icon: AirplaneOutline,
    title: '出行计划',
    desc: '行程清单 · 一键备忘',
    hint: '出发前提醒，避免遗忘',
    metric: '待办与行程联动',
    points: ['清单管理', '行程倒计时', '历史归档'],
    color: '#f97316',
  },
  {
    icon: ImagesOutline,
    title: '照片库',
    desc: '分类归档 · 私密保护',
    hint: '证件、发票、病历安全管理',
    metric: '敏感文件保护',
    points: ['分类检索', '隐私访问控制', '资料可追溯'],
    color: '#0891b2',
  },
] as const
const featureIndex = ref(0)
const activeFeature = computed(() => featureCards[featureIndex.value])

function jumpFeature(i: number) {
  featureIndex.value = i
}

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

const fixedOverrides = {
  common: {
    fontFamily: 'var(--fm-font-sans)',
    borderRadius: '14px',
    primaryColor: '#0d9488',
    primaryColorHover: '#0f766e',
    primaryColorPressed: '#115e59',
    bodyColor: '#f6f8fc',
    cardColor: 'rgba(255, 255, 255, 0.92)',
  },
} as const
</script>

<template>
  <NConfigProvider :theme="lightTheme" :theme-overrides="fixedOverrides">
    <div class="auth-page" :data-page="mode">
      <div class="bg" aria-hidden="true">
        <div class="bg-orb bg-orb--1" />
        <div class="bg-orb bg-orb--2" />
        <div class="bg-orb bg-orb--3" />
      </div>

      <div class="shell">
        <!-- 左侧品牌区 -->
        <section class="hero">
          <div class="hero-inner">
            <div class="brand">
              <div class="logo">家</div>
              <div>
                <div class="title">家庭小管家</div>
                <div class="tagline">ALL-Powerful · 一家人的生活助手</div>
              </div>
            </div>

            <div class="greeting-block">
              <div class="greeting">{{ greeting }}</div>
              <div class="time-line">{{ timeLine }}</div>
              <div class="date-line">{{ dateLine }}</div>
            </div>

            <div class="feature-carousel">
              <Transition name="fade-card" mode="out-in">
                <div
                  :key="activeFeature.title"
                  class="feature-card"
                  :style="{ '--accent': activeFeature.color }"
                >
                  <div class="feature-top">
                    <div class="feature-icon">
                      <NIcon :component="activeFeature.icon" :size="22" />
                    </div>
                    <div class="feature-head">
                      <div class="feature-label">{{ activeFeature.title }}</div>
                      <div class="feature-desc">{{ activeFeature.desc }}</div>
                    </div>
                    <span class="feature-pill">{{ activeFeature.metric }}</span>
                  </div>
                  <div class="feature-hint">{{ activeFeature.hint }}</div>
                  <div class="feature-points">
                    <span v-for="p in activeFeature.points" :key="p" class="point-chip">{{ p }}</span>
                  </div>
                </div>
              </Transition>
              <div class="feature-dots">
                <button
                  v-for="(item, idx) in featureCards"
                  :key="item.title"
                  class="dot"
                  :class="{ active: idx === featureIndex }"
                  type="button"
                  @click="jumpFeature(idx)"
                />
              </div>
            </div>

            <div class="hero-illustration">
              <FmAntdIllustration variant="success" :width="200" />
            </div>
          </div>
        </section>

        <!-- 右侧表单：登录 / 注册切换 -->
        <section class="panel">
          <NCard class="card" :bordered="false">
            <div class="mode-tabs">
              <button type="button" class="mode-tab" :class="{ active: !isRegister }" @click="switchMode('login')">
                登录
              </button>
              <button type="button" class="mode-tab" :class="{ active: isRegister }" @click="switchMode('register')">
                注册
              </button>
            </div>

            <Transition name="fade-card" mode="out-in">
              <div :key="mode" class="form-wrap">
                <div class="card-head">
                  <div class="card-title">{{ isRegister ? '创建账号' : '账号登录' }}</div>
                  <div class="card-sub">
                    {{ isRegister ? '注册后即可同步数据到云端' : '使用已注册的用户名登录' }}
                  </div>
                </div>

                <div class="form">
                  <div class="field">
                    <div class="label">用户名</div>
                    <NInput
                      v-model:value="account"
                      :placeholder="isRegister ? '3～64 个字符，用于登录' : '请输入用户名'"
                      size="large"
                      clearable
                    >
                      <template #prefix>
                        <NIcon :component="PersonOutline" class="input-icon" />
                      </template>
                    </NInput>
                  </div>

                  <div v-if="isRegister" class="field">
                    <div class="label">昵称（可选）</div>
                    <NInput v-model:value="displayName" placeholder="显示名称，默认同用户名" size="large" clearable />
                  </div>

                  <div class="field">
                    <div class="label">密码</div>
                    <NInput
                      v-model:value="password"
                      type="password"
                      show-password-on="click"
                      :placeholder="isRegister ? '至少 6 位' : '请输入密码'"
                      size="large"
                      @keydown.enter.prevent="!isRegister && submit()"
                    >
                      <template #prefix>
                        <NIcon :component="LockClosedOutline" class="input-icon" />
                      </template>
                    </NInput>
                  </div>

                  <div v-if="isRegister" class="field">
                    <div class="label">确认密码</div>
                    <NInput
                      v-model:value="confirmPassword"
                      type="password"
                      show-password-on="click"
                      placeholder="再次输入密码"
                      size="large"
                      @keydown.enter.prevent="submit"
                    >
                      <template #prefix>
                        <NIcon :component="LockClosedOutline" class="input-icon" />
                      </template>
                    </NInput>
                  </div>

                  <div class="row">
                    <NCheckbox v-model:checked="remember">记住我</NCheckbox>
                    <a
                      v-if="!isRegister"
                      class="link"
                      href="javascript:void(0)"
                      @click.prevent="message.info('请联系管理员重置密码')"
                    >
                      忘记密码？
                    </a>
                  </div>

                  <NButton type="primary" size="large" block :loading="loading" @click="submit">
                    {{ isRegister ? '注册并登录' : '登录' }}
                  </NButton>

                  <div class="footer-row">
                    <span class="hint">{{ isRegister ? '已有账号？' : '还没有账号？' }}</span>
                    <a
                      class="link strong"
                      href="javascript:void(0)"
                      @click.prevent="switchMode(isRegister ? 'login' : 'register')"
                    >
                      {{ isRegister ? '去登录' : '立即注册' }}
                    </a>
                  </div>

                  <NAlert type="info" :bordered="false" class="tip-alert">
                    {{
                      isRegister
                        ? '注册成功后将自动登录，备忘录等数据将保存至服务端数据库。'
                        : '首次使用请先注册账号。登录成功后，备忘录等数据将保存至服务端数据库。'
                    }}
                  </NAlert>
                </div>
              </div>
            </Transition>
          </NCard>
        </section>
      </div>
    </div>
  </NConfigProvider>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  width: 100%;
  position: relative;
  background: #f6f8fc;
  color-scheme: light;
  overflow-x: hidden;
}

.bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  /* 色块更干净：少灰雾，分区清楚 */
  background:
    radial-gradient(720px 480px at 6% 8%, rgba(37, 99, 235, 0.12), transparent 58%),
    radial-gradient(640px 440px at 94% 10%, rgba(13, 148, 136, 0.11), transparent 60%),
    linear-gradient(165deg, #e8eef6 0%, #eef4f2 42%, #f5f7fa 100%);
  background-size: 120% 120%;
  animation: aurora-pan 32s ease-in-out infinite alternate;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(64px);
  opacity: 0.12;
}

.bg-orb--1 {
  width: 300px;
  height: 300px;
  top: -6%;
  left: -4%;
  background: rgba(59, 130, 246, 0.34);
  animation: orb-float-1 30s ease-in-out infinite;
}

.bg-orb--2 {
  width: 260px;
  height: 260px;
  top: 62%;
  left: 22%;
  background: rgba(45, 212, 191, 0.28);
  animation: orb-float-2 32s ease-in-out infinite;
}

.bg-orb--3 {
  width: 280px;
  height: 280px;
  top: 6%;
  right: -4%;
  background: rgba(14, 165, 233, 0.26);
  animation: orb-float-3 28s ease-in-out infinite;
}

@keyframes aurora-pan {
  0% {
    background-position: 8% 20%;
  }
  100% {
    background-position: 88% 78%;
  }
}

@keyframes orb-float-1 {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(42px, 20px, 0) scale(1.12);
  }
}

@keyframes orb-float-2 {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(-36px, -26px, 0) scale(1.08);
  }
}

@keyframes orb-float-3 {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(-24px, 34px, 0) scale(1.1);
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg,
  .bg-orb {
    animation: none !important;
  }
}

.shell {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 440px);
  gap: 40px;
  align-items: center;
  max-width: 1120px;
  margin: 0 auto;
  padding: 40px 28px;
}

.hero-inner {
  max-width: 520px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 22px;
  color: #fff;
  background: linear-gradient(135deg, #0d9488, #0284c7);
  box-shadow: 0 10px 22px rgba(13, 148, 136, 0.28);
}

.title {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.02em;
}

.tagline {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.greeting-block {
  margin-top: 28px;
}

.greeting {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.time-line {
  margin-top: 10px;
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 0.06em;
  font-variant-numeric: tabular-nums;
  color: #0f766e;
  line-height: 1.1;
}

.date-line {
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
}

.hero-desc {
  margin: 18px 0 0;
  font-size: 14px;
  line-height: 1.65;
  color: rgba(30, 41, 59, 0.9);
}

.feature-carousel {
  margin-top: 24px;
  max-width: 440px;
}

.feature-card {
  --accent: #0d9488;
  position: relative;
  min-height: 186px;
  padding: 20px 20px 18px 22px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow:
    0 14px 32px rgba(15, 23, 42, 0.1),
    0 2px 8px rgba(15, 23, 42, 0.05);
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: var(--accent);
}

.feature-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    125deg,
    color-mix(in srgb, var(--accent) 7%, transparent) 0%,
    transparent 42%
  );
  pointer-events: none;
}

.feature-top {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
}

.feature-icon {
  position: relative;
  z-index: 1;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  color: #fff;
  background: var(--accent);
  box-shadow: 0 6px 14px color-mix(in srgb, var(--accent) 35%, transparent);
}

.feature-head {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.feature-label {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
}

.feature-desc {
  margin-top: 4px;
  font-size: 13px;
  color: #475569;
  line-height: 1.45;
}

.feature-pill {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 11px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  color: #fff;
  background: var(--accent);
}

.feature-hint {
  position: relative;
  z-index: 1;
  margin-top: 16px;
  font-size: 13px;
  color: #334155;
  line-height: 1.55;
}

.feature-points {
  position: relative;
  z-index: 1;
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.point-chip {
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
  padding: 6px 11px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.feature-dots {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  border: 0;
  background: #cbd5e1;
  transition: all 0.22s ease;
  cursor: pointer;
  padding: 0;
}

.dot.active {
  width: 22px;
  background: #0f766e;
}

.fade-card-enter-active,
.fade-card-leave-active {
  transition: opacity 0.42s ease, transform 0.42s ease;
}
.fade-card-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-card-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.trust-row {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-illustration {
  margin-top: 24px;
}

.panel {
  width: 100%;
}

.card {
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.1);
  box-shadow:
    0 18px 40px rgba(15, 23, 42, 0.1),
    0 4px 12px rgba(15, 23, 42, 0.05);
  background: #ffffff;
}

.card-head {
  margin-bottom: 4px;
}

.mode-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  padding: 4px;
  margin-bottom: 14px;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.14);
}

.mode-tab {
  height: 36px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: rgba(71, 85, 105, 0.92);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-tab.active {
  background: #fff;
  color: rgba(15, 23, 42, 0.95);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.form-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-title {
  font-size: 20px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.92);
}

.card-sub {
  margin-top: 4px;
  font-size: 13px;
  color: rgba(71, 85, 105, 0.92);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field .label {
  font-size: 12px;
  font-weight: 700;
  color: rgba(30, 41, 59, 0.92);
  margin-bottom: 6px;
}

.input-icon {
  color: rgba(100, 116, 139, 0.85);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.footer-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.link {
  font-size: 12px;
  color: rgba(2, 132, 199, 0.95);
  text-decoration: none;
  cursor: pointer;
}

.link:hover {
  text-decoration: underline;
}

.link.strong {
  font-weight: 700;
}

.hint {
  font-size: 12px;
  color: rgba(100, 116, 139, 0.9);
}

.tip-alert {
  border-radius: 12px;
  font-size: 12px;
}

@media (max-width: 900px) {
  .shell {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 24px 16px 32px;
    align-items: start;
  }

  .hero-inner {
    max-width: none;
  }

  .hero-illustration {
    display: none;
  }

  .feature-carousel {
    max-width: none;
  }

  .greeting {
    font-size: 20px;
  }

  .time-line {
    font-size: 32px;
  }

  .title {
    font-size: 22px;
  }
}
</style>
