<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
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

const router = useRouter()
const route = useRoute()
const message = useMessage()
const auth = useAuthStore()

const account = ref('')
const password = ref('')
const remember = ref(true)
const loading = ref(false)
const now = ref(new Date())
let clockTimer: ReturnType<typeof setInterval> | undefined
let featureTimer: ReturnType<typeof setInterval> | undefined

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

const greeting = computed(() => {
  const h = now.value.getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
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

async function submit() {
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

function goRegister() {
  router.push({ name: 'register', query: route.query })
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
    <div class="auth-page" data-page="login">
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
              <div class="greeting">{{ greeting }}，欢迎回来</div>
              <div class="time-line">{{ timeLine }}</div>
              <div class="date-line">{{ dateLine }}</div>
            </div>

            <div class="feature-carousel">
              <Transition name="fade-card" mode="out-in">
                <div :key="activeFeature.title" class="feature-card">
                  <div class="feature-icon" :style="{ background: `${activeFeature.color}24`, color: activeFeature.color }">
                    <NIcon :component="activeFeature.icon" :size="20" />
                  </div>
                  <div class="feature-body">
                    <div class="feature-head">
                      <div class="feature-label">{{ activeFeature.title }}</div>
                      <NTag size="small" :bordered="false" round class="feature-pill">{{ activeFeature.metric }}</NTag>
                    </div>
                    <div class="feature-desc">{{ activeFeature.desc }}</div>
                    <div class="feature-hint">{{ activeFeature.hint }}</div>
                    <div class="feature-points">
                      <span v-for="p in activeFeature.points" :key="p" class="point-chip">{{ p }}</span>
                    </div>
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

        <!-- 右侧登录表单 -->
        <section class="panel">
          <NCard class="card" :bordered="false">
            <div class="card-head">
              <div class="card-title">账号登录</div>
              <div class="card-sub">使用已注册的用户名登录</div>
            </div>

            <div class="form">
              <div class="field">
                <div class="label">用户名</div>
                <NInput
                  v-model:value="account"
                  placeholder="请输入用户名"
                  size="large"
                  clearable
                >
                  <template #prefix>
                    <NIcon :component="PersonOutline" class="input-icon" />
                  </template>
                </NInput>
              </div>

              <div class="field">
                <div class="label">密码</div>
                <NInput
                  v-model:value="password"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入密码"
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
                <a class="link" href="javascript:void(0)" @click.prevent="message.info('请联系管理员重置密码')">
                  忘记密码？
                </a>
              </div>

              <NButton type="primary" size="large" block :loading="loading" @click="submit">
                登录
              </NButton>

              <NDivider style="margin: 4px 0">或</NDivider>

              <NButton size="large" block secondary @click="goRegister">创建新账号</NButton>

              <div class="footer-row">
                <span class="hint">还没有账号？</span>
                <a class="link strong" href="javascript:void(0)" @click.prevent="goRegister">立即注册</a>
              </div>

              <NAlert type="info" :bordered="false" class="tip-alert">
                首次使用请先注册账号。登录成功后，备忘录等数据将保存至服务端数据库。
              </NAlert>
            </div>
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
  background:
    radial-gradient(980px 680px at 8% 12%, rgba(59, 130, 246, 0.24), transparent 66%),
    radial-gradient(960px 700px at 92% 16%, rgba(139, 92, 246, 0.2), transparent 70%),
    radial-gradient(880px 620px at 54% 88%, rgba(20, 184, 166, 0.18), transparent 66%),
    linear-gradient(130deg, #f8fbff 0%, #f1f5ff 42%, #ecfaf8 75%, #f7f9fc 100%);
  background-size: 140% 140%;
  animation: aurora-pan 22s ease-in-out infinite alternate;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(68px);
  opacity: 0.34;
}

.bg-orb--1 {
  width: 320px;
  height: 320px;
  top: -4%;
  left: -2%;
  background: rgba(56, 189, 248, 0.3);
  animation: orb-float-1 26s ease-in-out infinite;
}

.bg-orb--2 {
  width: 280px;
  height: 280px;
  top: 60%;
  left: 18%;
  background: rgba(45, 212, 191, 0.24);
  animation: orb-float-2 28s ease-in-out infinite;
}

.bg-orb--3 {
  width: 300px;
  height: 300px;
  top: 8%;
  right: -2%;
  background: rgba(167, 139, 250, 0.24);
  animation: orb-float-3 24s ease-in-out infinite;
}

@keyframes aurora-pan {
  0% {
    background-position: 0% 18%;
  }
  100% {
    background-position: 100% 82%;
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
  color: #0f172a;
  background: linear-gradient(135deg, rgba(20, 184, 166, 0.28), rgba(56, 189, 248, 0.22));
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
}

.title {
  font-size: 26px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.94);
  letter-spacing: 0.02em;
}

.tagline {
  font-size: 13px;
  color: rgba(71, 85, 105, 0.82);
  margin-top: 4px;
}

.greeting-block {
  margin-top: 28px;
}

.greeting {
  font-size: 22px;
  font-weight: 700;
  color: rgba(15, 23, 42, 0.9);
}

.time-line {
  margin-top: 10px;
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 0.06em;
  font-variant-numeric: tabular-nums;
  color: rgba(13, 148, 136, 0.92);
  line-height: 1.1;
}

.date-line {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(100, 116, 139, 0.92);
}

.hero-desc {
  margin: 18px 0 0;
  font-size: 14px;
  line-height: 1.65;
  color: rgba(51, 65, 85, 0.88);
}

.feature-carousel {
  margin-top: 24px;
  max-width: 420px;
}

.feature-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 162px;
  padding: 16px 16px 14px;
  border-radius: 16px;
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.5), rgba(255, 255, 255, 0.28));
  border: 1px solid rgba(255, 255, 255, 0.66);
  box-shadow:
    0 14px 28px rgba(15, 23, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px) saturate(1.06);
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.feature-body {
  min-width: 0;
  width: 100%;
}
.feature-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.feature-label {
  font-size: 14px;
  font-weight: 700;
  color: rgba(15, 23, 42, 0.9);
}
.feature-pill {
  color: rgba(15, 23, 42, 0.78);
  background: rgba(255, 255, 255, 0.52);
}

.feature-desc {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(71, 85, 105, 0.9);
  line-height: 1.45;
}
.feature-hint {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(51, 65, 85, 0.86);
  line-height: 1.45;
}
.feature-points {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.point-chip {
  font-size: 11px;
  color: rgba(51, 65, 85, 0.88);
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.2);
}
.feature-dots {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}
.dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  border: 0;
  background: rgba(100, 116, 139, 0.32);
  transition: all 0.22s ease;
  cursor: pointer;
  padding: 0;
}
.dot.active {
  width: 20px;
  background: rgba(15, 118, 110, 0.92);
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
  opacity: 0.92;
}

.panel {
  width: 100%;
}

.card {
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow:
    0 24px 60px rgba(15, 23, 42, 0.12),
    0 4px 16px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.94);
}

.card-head {
  margin-bottom: 4px;
}

.card-title {
  font-size: 20px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.92);
}

.card-sub {
  margin-top: 4px;
  font-size: 13px;
  color: rgba(100, 116, 139, 0.92);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field .label {
  font-size: 12px;
  font-weight: 700;
  color: rgba(51, 65, 85, 0.92);
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
