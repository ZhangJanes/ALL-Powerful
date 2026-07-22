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
  ShieldCheckmarkOutline,
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

onMounted(() => {
  clockTimer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
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

const features = [
  { icon: DocumentTextOutline, label: '备忘录', desc: '待办提醒 · 云端同步', color: '#14b8a6' },
  { icon: WalletOutline, label: '家庭记账', desc: '收支统计 · 预算管理', color: '#f472b6' },
  { icon: CheckboxOutline, label: '习惯打卡', desc: '坚持记录 · 成就激励', color: '#34d399' },
  { icon: AirplaneOutline, label: '出行计划', desc: '行程清单 · 一键备忘', color: '#f97316' },
  { icon: ImagesOutline, label: '照片库', desc: '分类归档 · 私密保护', color: '#0d9488' },
] as const

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

            <p class="hero-desc">
              登录后可将备忘录、出行计划等数据同步至云端，多设备随时查看，家庭协作更省心。
            </p>

            <div class="feature-grid">
              <div v-for="f in features" :key="f.label" class="feature-item">
                <div class="feature-icon" :style="{ background: `${f.color}18`, color: f.color }">
                  <NIcon :component="f.icon" :size="20" />
                </div>
                <div>
                  <div class="feature-label">{{ f.label }}</div>
                  <div class="feature-desc">{{ f.desc }}</div>
                </div>
              </div>
            </div>

            <div class="trust-row">
              <NTag size="small" round :bordered="false" type="success">
                <template #icon><NIcon :component="ShieldCheckmarkOutline" /></template>
                JWT 安全鉴权
              </NTag>
              <NTag size="small" round :bordered="false">MySQL 持久化</NTag>
              <NTag size="small" round :bordered="false">多端同步</NTag>
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
  background:
    radial-gradient(1200px 680px at 12% 18%, rgba(14, 165, 233, 0.2), transparent 55%),
    radial-gradient(900px 620px at 88% 22%, rgba(99, 102, 241, 0.16), transparent 58%),
    radial-gradient(800px 560px at 50% 95%, rgba(13, 148, 136, 0.12), transparent 55%),
    linear-gradient(165deg, #f7fbff 0%, #f0f4fa 45%, #f6f8fc 100%);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.45;
}

.bg-orb--1 {
  width: 280px;
  height: 280px;
  top: 8%;
  left: 6%;
  background: rgba(20, 184, 166, 0.35);
}

.bg-orb--2 {
  width: 220px;
  height: 220px;
  top: 55%;
  left: 28%;
  background: rgba(56, 189, 248, 0.28);
}

.bg-orb--3 {
  width: 260px;
  height: 260px;
  top: 20%;
  right: 8%;
  background: rgba(129, 140, 248, 0.22);
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

.feature-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(8px);
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.feature-label {
  font-size: 13px;
  font-weight: 700;
  color: rgba(15, 23, 42, 0.9);
}

.feature-desc {
  margin-top: 2px;
  font-size: 11px;
  color: rgba(100, 116, 139, 0.9);
  line-height: 1.35;
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

  .feature-grid {
    grid-template-columns: 1fr;
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
