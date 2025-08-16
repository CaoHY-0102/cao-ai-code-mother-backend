import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import '@/access'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 导入中文语言包
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 设置dayjs语言为中文
dayjs.locale('zh-cn')

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// 全局配置中文语言
app.config.globalProperties.$locale = zhCN

app.mount('#app')
