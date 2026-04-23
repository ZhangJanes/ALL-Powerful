import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/echarts/register'
import 'vfonts/Lato.css'
import 'vfonts/FiraCode.css'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import './styles/illustration-antd.css'
import './styles/illustration-sketch.css'
import './styles/sketch-global-ui.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
