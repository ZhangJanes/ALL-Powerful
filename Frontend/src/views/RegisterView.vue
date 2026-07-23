<script setup lang="ts">
/**
 * 兼容旧入口：注册已合并到 LoginView（同一页面切换表单）。
 * 路由 /register 现已直接指向 LoginView；本文件保留以免外部引用断裂。
 */
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

onMounted(() => {
  router.replace({ name: 'register', query: route.query })
})
</script>

<template>
  <div />
</template>
