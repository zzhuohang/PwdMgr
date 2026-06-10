<template>
  <div class="dashboard">
    <h2>仪表盘</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="32"><Globe /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.websiteCount }}</div>
              <div class="stat-label">网站数量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="32"><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.credentialCount }}</div>
              <div class="stat-label">账号密码</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.weakPasswordCount }}</div>
              <div class="stat-label">弱密码</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon :size="32"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.expiredCount }}</div>
              <div class="stat-label">过期密码</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近使用的凭证 -->
    <el-card class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近使用</span>
          <el-button type="primary" link @click="goToCredentials">查看全部</el-button>
        </div>
      </template>

      <el-table :data="recentCredentials" style="width: 100%">
        <el-table-column prop="websiteName" label="网站" width="180">
          <template #default="{ row }">
            <div class="website-cell">
              <el-avatar :size="24" class="website-icon">
                {{ row.websiteName?.charAt(0) || 'W' }}
              </el-avatar>
              <span>{{ row.websiteName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="lastUsedTime" label="最后使用" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastUsedTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="strength" label="密码强度" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.strength"
              disabled
              :max="5"
              :colors="['#f56c6c', '#e6a23c', '#67c23a']"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="primary" link @click="copyCredential(row)">
              复制密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 密码强度分布 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>密码强度分布</span>
          </template>
          <div class="chart-container">
            <div
              v-for="(item, index) in strengthDistribution"
              :key="index"
              class="strength-item"
            >
              <div class="strength-label">{{ item.label }}</div>
              <el-progress
                :percentage="item.percentage"
                :color="item.color"
                :stroke-width="20"
              />
              <div class="strength-count">{{ item.count }}个</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>网站分类统计</span>
          </template>
          <div class="chart-container">
            <div
              v-for="(item, index) in categoryStats"
              :key="index"
              class="category-item"
            >
              <div class="category-info">
                <span class="category-name">{{ item.name }}</span>
                <span class="category-count">{{ item.count }}个</span>
              </div>
              <el-progress
                :percentage="(item.count / stats.websiteCount) * 100"
                :show-text="false"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主密码验证对话框 -->
    <el-dialog
      v-model="masterPasswordDialogVisible"
      title="验证主密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form @submit.prevent="handleMasterPasswordSubmit">
        <el-form-item label="主密码">
          <el-input
            v-model="masterPasswordInput"
            type="password"
            placeholder="请输入主密码"
            show-password
            :class="{ 'is-error': masterPasswordError }"
          />
          <div v-if="masterPasswordError" class="error-message">
            {{ masterPasswordError }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="masterPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleMasterPasswordSubmit" :loading="masterPasswordLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCredentialStats } from '@/api/credential'
import { useUserStore } from '@/stores/user'
import { verifyMasterPassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = reactive({
    websiteCount: 0,
    credentialCount: 0,
    weakPasswordCount: 0,
    expiredCount: 0
})

// 最近使用的凭证
const recentCredentials = ref([])

// 密码强度分布
const strengthDistribution = ref([
    { label: '非常弱', count: 0, percentage: 0, color: '#f56c6c' },
    { label: '弱', count: 0, percentage: 0, color: '#e6a23c' },
    { label: '中等', count: 0, percentage: 0, color: '#409eff' },
    { label: '强', count: 0, percentage: 0, color: '#67c23a' },
    { label: '非常强', count: 0, percentage: 0, color: '#00b894' }
])

// 网站分类统计
const categoryStats = ref([])

// 主密码验证相关
const masterPasswordDialogVisible = ref(false)
const masterPasswordInput = ref('')
const masterPasswordLoading = ref(false)
const masterPasswordError = ref('')

// 格式化时间
const formatTime = (time) => {
    if (!time) return '-'
    const date = new Date(time)
    const now = new Date()
    const diff = now - date

    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

    return date.toLocaleDateString('zh-CN')
}

// 验证主密码
const verifyMasterPasswordAsync = async () => {
    if (userStore.masterKey) {
        return true
    }

    masterPasswordDialogVisible.value = true
    masterPasswordInput.value = ''
    masterPasswordError.value = ''

    return new Promise((resolve) => {
        const unwatch = watch(masterPasswordDialogVisible, (val) => {
            if (!val) {
                unwatch()
                resolve(!!userStore.masterKey)
            }
        })
    })
}

// 提交主密码验证
const handleMasterPasswordSubmit = async () => {
    if (!masterPasswordInput.value) {
        masterPasswordError.value = '请输入主密码'
        return
    }

    masterPasswordLoading.value = true
    masterPasswordError.value = ''

    try {
        await verifyMasterPassword(masterPasswordInput.value)
        userStore.setMasterKey(masterPasswordInput.value)
        masterPasswordDialogVisible.value = false
        ElMessage.success('主密码验证成功')
        loadDashboardData()
    } catch (error) {
        masterPasswordError.value = error.message || '主密码验证失败'
    } finally {
        masterPasswordLoading.value = false
    }
}

// 复制凭证
const copyCredential = async (credential) => {
    try {
        await navigator.clipboard.writeText(credential.password)
        ElMessage.success('密码已复制到剪贴板')
    } catch {
        ElMessage.error('复制失败')
    }
}

// 跳转到凭证页面
const goToCredentials = () => {
    router.push('/credentials')
}

// 加载仪表盘数据
const loadDashboardData = async () => {
    try {
        const response = await getCredentialStats()
        const data = response.data

        // 处理后端返回的数据
        stats.websiteCount = data.websiteCount || data.totalCount || 0
        stats.credentialCount = data.credentialCount || data.totalCount || 0
        stats.weakPasswordCount = data.weakPasswordCount || data.weakCount || 0
        stats.expiredCount = data.expiredCount || 0

        // 更新密码强度分布
        if (data.strengthDistribution) {
            strengthDistribution.value = data.strengthDistribution
        } else {
            // 使用默认数据
            strengthDistribution.value = [
                { label: '非常弱', count: 0, percentage: 0, color: '#f56c6c' },
                { label: '弱', count: data.weakCount || 0, percentage: 0, color: '#e6a23c' },
                { label: '中等', count: 0, percentage: 0, color: '#409eff' },
                { label: '强', count: 0, percentage: 0, color: '#67c23a' },
                { label: '非常强', count: 0, percentage: 0, color: '#00b894' }
            ]
        }

        // 更新分类统计
        if (data.categoryStats) {
            categoryStats.value = data.categoryStats
        } else {
            // 使用默认数据
            categoryStats.value = [
                { name: '未分类', count: data.totalCount || 0 }
            ]
        }

        // 更新最近使用的凭证
        if (data.recentCredentials) {
            recentCredentials.value = data.recentCredentials.map(item => ({
                ...item,
                password: '***'
            }))
        } else {
            recentCredentials.value = []
        }
    } catch (error) {
        console.error('加载仪表盘数据失败:', error)
        ElMessage.error('加载仪表盘数据失败')
    }
}

// 加载数据
onMounted(async () => {
    const hasMasterPassword = await verifyMasterPasswordAsync()
    if (hasMasterPassword) {
        loadDashboardData()
    }
})
</script>

<style scoped>
.dashboard {
    padding: 20px 0;
}

.stat-cards {
    margin-bottom: 20px;
}

.stat-card {
    cursor: pointer;
    transition: transform 0.3s;
}

.stat-card:hover {
    transform: translateY(-5px);
}

.stat-content {
    display: flex;
    align-items: center;
}

.stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 15px;
}

.stat-info {
    flex: 1;
}

.stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
    line-height: 1;
}

.stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 5px;
}

.recent-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.website-cell {
    display: flex;
    align-items: center;
}

.website-icon {
    background: #409eff;
    color: #fff;
    margin-right: 8px;
    font-size: 12px;
}

.chart-row {
    margin-bottom: 20px;
}

.chart-container {
    padding: 10px 0;
}

.strength-item {
    margin-bottom: 15px;
}

.strength-label {
    font-size: 14px;
    color: #606266;
    margin-bottom: 5px;
}

.strength-count {
    font-size: 12px;
    color: #909399;
    text-align: right;
    margin-top: 5px;
}

.category-item {
    margin-bottom: 15px;
}

.category-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 5px;
}

.category-name {
    font-size: 14px;
    color: #606266;
}

.category-count {
    font-size: 14px;
    color: #909399;
}

.error-message {
    color: #f56c6c;
    font-size: 12px;
    margin-top: 5px;
}
</style>