import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers['Authorization'] = `Bearer ${userStore.token}`
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 如果是文件下载，直接返回
        if (response.config.responseType === 'blob') {
            return response
        }

        // 检查业务状态码
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')

            // 401: 未认证
            if (res.code === 401) {
                const userStore = useUserStore()
                userStore.logoutAction()
                router.push('/login')
            }

            return Promise.reject(new Error(res.message || '请求失败'))
        }

        return res
    },
    error => {
        console.error('响应错误:', error)

        if (error.response) {
            const { status } = error.response

            switch (status) {
                case 401:
                    ElMessage.error('未认证，请重新登录')
                    const userStore = useUserStore()
                    userStore.logoutAction()
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                default:
                    ElMessage.error(`请求失败: ${status}`)
            }
        } else if (error.message.includes('timeout')) {
            ElMessage.error('请求超时，请稍后重试')
        } else if (error.message.includes('Network Error')) {
            ElMessage.error('网络连接异常，请检查网络')
        } else {
            ElMessage.error('请求失败，请稍后重试')
        }

        return Promise.reject(error)
    }
)

export default service