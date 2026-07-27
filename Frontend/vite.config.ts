/*
 * @Author: zhangjianing 1026597665@qq.com
 * @Date: 2026-04-16 10:26:55
 * @LastEditors: zhangjianing 1026597665@qq.com
 * @LastEditTime: 2026-07-11 08:15:17
 * @FilePath: /ALL-Powerful/Frontend/vite.config.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
    server: {
        open: true,
        /** 监听所有网卡，手机可通过本机局域网 IP（如 http://192.168.x.x:5173）访问 */
        host: true,
        port: 5173,
        strictPort: false,
        /** 同源代理，避免局域网打开页面时浏览器拦截访问 127.0.0.1:8080 */
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,
            },
        },
    },
    preview: {
        host: true,
        port: 4173,
    },
    build: {
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('node_modules/echarts')) return 'echarts'
                    if (id.includes('node_modules/vue-echarts')) return 'echarts'
                    if (id.includes('node_modules/zrender')) return 'echarts'
                },
            },
        },
    },
    plugins: [
        vue(),
        AutoImport({
            imports: ['vue', 'vue-router', 'pinia'],
            dts: 'src/auto-imports.d.ts',
        }),
        Components({
            resolvers: [NaiveUiResolver()],
            dts: 'src/components.d.ts',
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
})
