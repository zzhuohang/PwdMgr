<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon :size="24"><Lock /></el-icon>
        <span v-show="!isCollapse" class="title">密码管理系统</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        class="el-menu-vertical"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-menu-item index="/websites">
          <el-icon><Globe /></el-icon>
          <template #title>网站管理</template>
        </el-menu-item>

        <el-menu-item index="/credentials">
          <el-icon><Key /></el-icon>
          <template #title>账号密码</template>
        </el-menu-item>

        <el-menu-item index="/generator">
          <el-icon><Lock /></el-icon>
          <template #title>密码生成器</template>
        </el-menu-item>

        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>

          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 搜索框 -->
          <el-input
            v-model="searchKeyword"
            placeholder="搜索网站或账号..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="avatar">
                {{ username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item command="lock">锁定</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const searchKeyword = ref('')

// 当前路由
const currentRoute = computed(() => route)

// 激活的菜单
const activeMenu = computed(() => route.path)

// 用户名
const username = computed(() => userStore.username || 'Admin')

// 切换折叠
const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
}

// 搜索
const handleSearch = () => {
    if (searchKeyword.value.trim()) {
        router.push({
            path: '/credentials',
            query: { keyword: searchKeyword.value }
        })
    }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
    switch (command) {
        case 'settings':
            router.push('/settings')
            break
        case 'lock':
            // TODO: 实现锁定功能
            ElMessage.info('锁定功能开发中')
            break
        case 'logout':
            try {
                await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                await userStore.logoutAction()
                router.push('/login')
                ElMessage.success('已退出登录')
            } catch {
                // 取消操作
            }
            break
    }
}
</script>

<style scoped>
.layout-container {
    height: 100vh;
}

.aside {
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;
}

.logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid #3d4f65;
}

.logo .title {
    margin-left: 10px;
    white-space: nowrap;
}

.el-menu-vertical {
    border-right: none;
}

.el-menu-vertical:not(.el-menu--collapse) {
    width: 220px;
}

.header {
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
}

.header-left {
    display: flex;
    align-items: center;
}

.collapse-btn {
    font-size: 20px;
    cursor: pointer;
    color: #5a5e66;
    margin-right: 15px;
}

.collapse-btn:hover {
    color: #409eff;
}

.header-right {
    display: flex;
    align-items: center;
}

.search-input {
    width: 300px;
    margin-right: 20px;
}

.user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #5a5e66;
}

.avatar {
    background: #409eff;
    color: #fff;
    margin-right: 8px;
}

.username {
    margin-right: 5px;
}

.main {
    background: #f5f7fa;
    padding: 0;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>