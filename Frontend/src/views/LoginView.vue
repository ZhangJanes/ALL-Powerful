<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { lightTheme, NConfigProvider, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const auth = useAuthStore()

const account = ref('')
const password = ref('')
const remember = ref(true)
const loading = ref(false)

const redirectTo = computed(() => {
  const raw = route.query.redirect
  return typeof raw === 'string' && raw ? raw : '/'
})

async function submit() {
  if (!account.value.trim() || !password.value.trim()) {
    message.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    auth.login(account.value, password.value)
    if (!remember.value) {
      // 不记住：登录后立刻清掉本地 token（保留本次跳转的体验）
      // 由于没有后端会话，这里用“关闭浏览器就失效”的语义，简单起见不额外实现 sessionStorage。
    }
    await router.replace(redirectTo.value)
  } finally {
    loading.value = false
  }
}

const fixedOverrides = {
  common: {
    // 登录页固定浅色，不随系统/全局主题变化
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
    <div class="login" data-page="login">
      <div class="bg" aria-hidden="true" />
      <div class="panel">
        <div class="brand">
          <div class="logo">家</div>
          <div>
            <div class="title">家庭小管家</div>
            <div class="desc">登录后开始记录与管理</div>
          </div>
        </div>

        <NCard class="card" :bordered="false">
          <div class="form">
            <div class="field">
              <div class="label">账号</div>
              <NInput v-model:value="account" placeholder="请输入账号" size="large" />
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
              />
            </div>

            <div class="row">
              <NCheckbox v-model:checked="remember">记住我</NCheckbox>
              <a class="link" href="javascript:void(0)" @click.prevent="message.info('演示：忘记密码')">忘记密码？</a>
            </div>

            <NButton type="primary" size="large" block :loading="loading" @click="submit">登录</NButton>
            <div class="hint">演示项目：任意账号密码都可登录</div>
          </div>
        </NCard>
      </div>
    </div>
  </NConfigProvider>
</template>

<style scoped>
.login {
  height: 100vh;
  width: 100%;
  overflow: hidden;
  position: relative;
  background: #f6f8fc;
  color-scheme: light;
}

.bg {
  position: absolute;
  inset: -40px;
  background:
    radial-gradient(1200px 680px at 20% 20%, rgba(14, 165, 233, 0.22), transparent 55%),
    radial-gradient(1000px 700px at 80% 30%, rgba(99, 102, 241, 0.18), transparent 60%),
    radial-gradient(900px 600px at 60% 90%, rgba(34, 211, 238, 0.16), transparent 55%),
    linear-gradient(180deg, #f7fbff, #f6f8fc);
  filter: saturate(1.05);
}

.panel {
  position: relative;
  z-index: 1;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.logo {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #0f172a;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.22), rgba(99, 102, 241, 0.18));
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.title {
  font-size: 18px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.92);
  letter-spacing: 0.01em;
}

.desc {
  font-size: 12px;
  color: rgba(71, 85, 105, 0.86);
  margin-top: 2px;
}

.card {
  width: min(420px, calc(100vw - 40px));
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.92);
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

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.link {
  font-size: 12px;
  color: rgba(2, 132, 199, 0.95);
  text-decoration: none;
}
.link:hover {
  text-decoration: underline;
}

.hint {
  text-align: center;
  font-size: 12px;
  color: rgba(100, 116, 139, 0.9);
}
</style>

