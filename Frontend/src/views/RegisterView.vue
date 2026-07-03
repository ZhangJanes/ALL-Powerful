<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { lightTheme, NConfigProvider, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const auth = useAuthStore()

const username = ref('')
const displayName = ref('')
const password = ref('')
const confirmPassword = ref('')
const remember = ref(true)
const loading = ref(false)

const redirectTo = computed(() => {
  const raw = route.query.redirect
  return typeof raw === 'string' && raw.startsWith('/') ? raw : '/'
})

async function submit() {
  const name = username.value.trim()
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

function goLogin() {
  router.push({ name: 'login', query: route.query })
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
    <div class="auth-page" data-page="register">
      <div class="bg" aria-hidden="true" />
      <div class="panel">
        <div class="brand">
          <div class="logo">家</div>
          <div>
            <div class="title">创建账号</div>
            <div class="desc">注册后即可同步数据到云端</div>
          </div>
        </div>

        <NCard class="card" :bordered="false">
          <div class="form">
            <div class="field">
              <div class="label">用户名</div>
              <NInput v-model:value="username" placeholder="3～64 个字符，用于登录" size="large" />
            </div>
            <div class="field">
              <div class="label">昵称（可选）</div>
              <NInput v-model:value="displayName" placeholder="显示名称，默认同用户名" size="large" />
            </div>
            <div class="field">
              <div class="label">密码</div>
              <NInput
                v-model:value="password"
                type="password"
                show-password-on="click"
                placeholder="至少 6 位"
                size="large"
              />
            </div>
            <div class="field">
              <div class="label">确认密码</div>
              <NInput
                v-model:value="confirmPassword"
                type="password"
                show-password-on="click"
                placeholder="再次输入密码"
                size="large"
                @keydown.enter.prevent="submit"
              />
            </div>

            <NCheckbox v-model:checked="remember">记住我</NCheckbox>

            <NButton type="primary" size="large" block :loading="loading" @click="submit">注册</NButton>

            <div class="footer-row">
              <span class="hint">已有账号？</span>
              <a class="link" href="javascript:void(0)" @click.prevent="goLogin">去登录</a>
            </div>
          </div>
        </NCard>
      </div>
    </div>
  </NConfigProvider>
</template>

<style scoped>
.auth-page {
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
}
.link:hover {
  text-decoration: underline;
}

.hint {
  font-size: 12px;
  color: rgba(100, 116, 139, 0.9);
}
</style>
