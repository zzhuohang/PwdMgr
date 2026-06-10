import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
    // 状态
    const token = ref(localStorage.getItem('token') || '')
    const refreshToken = ref(localStorage.getItem('refreshToken') || '')
    const userInfo = ref(null)
    const masterKey = ref('')

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value?.username || '')

    // 登录
    async function loginAction(loginForm) {
        try {
            const response = await login(loginForm)
            const { data } = response

            token.value = data.token
            refreshToken.value = data.refreshToken
            userInfo.value = data.userInfo

            localStorage.setItem('token', data.token)
            localStorage.setItem('refreshToken', data.refreshToken)

            ElMessage.success('登录成功')
            return true
        } catch (error) {
            ElMessage.error(error.message || '登录失败')
            return false
        }
    }

    // 登出
    async function logoutAction() {
        try {
            await logout()
        } catch (error) {
            console.error('登出失败:', error)
        } finally {
            token.value = ''
            refreshToken.value = ''
            userInfo.value = null
            masterKey.value = ''

            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
        }
    }

    // 获取用户信息
    async function getUserInfoAction() {
        try {
            const response = await getUserInfo()
            userInfo.value = response.data
            return response.data
        } catch (error) {
            console.error('获取用户信息失败:', error)
            throw error
        }
    }

    // 设置主密钥
    function setMasterKey(key) {
        masterKey.value = key
    }

    // 清除主密钥
    function clearMasterKey() {
        masterKey.value = ''
    }

    return {
        token,
        refreshToken,
        userInfo,
        masterKey,
        isLoggedIn,
        username,
        loginAction,
        logoutAction,
        getUserInfoAction,
        setMasterKey,
        clearMasterKey
    }
})