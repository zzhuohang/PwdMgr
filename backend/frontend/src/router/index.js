import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录', requiresAuth: false }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('@/views/layout/index.vue'),
        redirect: '/dashboard',
        meta: { requiresAuth: true },
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '仪表盘', icon: 'Dashboard' }
            },
            {
                path: 'websites',
                name: 'Websites',
                component: () => import('@/views/websites/index.vue'),
                meta: { title: '网站管理', icon: 'Globe' }
            },
            {
                path: 'credentials',
                name: 'Credentials',
                component: () => import('@/views/credentials/index.vue'),
                meta: { title: '账号密码', icon: 'Key' }
            },
            {
                path: 'generator',
                name: 'Generator',
                component: () => import('@/views/generator/index.vue'),
                meta: { title: '密码生成器', icon: 'Lock' }
            },
            {
                path: 'settings',
                name: 'Settings',
                component: () => import('@/views/settings/index.vue'),
                meta: { title: '设置', icon: 'Setting' }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/404.vue'),
        meta: { title: '404' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 密码管理系统` : '密码管理系统'

    // 检查是否需要认证
    if (to.meta.requiresAuth !== false) {
        if (!userStore.token) {
            next('/login')
            return
        }
    }

    // 如果已登录且访问登录页，重定向到首页
    if (to.path === '/login' && userStore.token) {
        next('/')
        return
    }

    next()
})

export default router