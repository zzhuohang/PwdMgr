<template>
  <div class="credentials-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账号密码管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加账号
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索网站、用户名..."
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="selectedWebsite"
          placeholder="选择网站"
          clearable
          filterable
          style="width: 200px; margin-left: 10px"
        >
          <el-option
            v-for="item in websiteOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <el-button
          type="primary"
          style="margin-left: 10px"
          @click="handleSearch"
        >
          搜索
        </el-button>
      </div>

      <!-- 凭证列表 -->
      <el-table :data="credentialList" style="width: 100%" v-loading="loading">
        <el-table-column prop="websiteName" label="网站" width="180">
          <template #default="{ row }">
            <div class="website-cell">
              <el-avatar :size="32" class="website-icon">
                {{ row.websiteName?.charAt(0) || 'W' }}
              </el-avatar>
              <span>{{ row.websiteName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="200">
          <template #default="{ row }">
            <div class="username-cell">
              <span>{{ row.username }}</span>
              <el-button
                type="primary"
                link
                size="small"
                @click="copyText(row.username, '用户名')"
              >
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="password" label="密码" width="200">
          <template #default="{ row }">
            <div class="password-cell">
              <span v-if="row.showPassword">{{ row.password }}</span>
              <span v-else>••••••••</span>
              <el-button
                type="primary"
                link
                size="small"
                @click="togglePassword(row)"
              >
                <el-icon>
                  <View v-if="!row.showPassword" />
                  <Hide v-else />
                </el-icon>
              </el-button>
              <el-button
                type="primary"
                link
                size="small"
                @click="copyText(row.password, '密码')"
              >
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="strength" label="强度" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.strength"
              disabled
              :max="5"
              :colors="['#f56c6c', '#e6a23c', '#67c23a']"
            />
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="lastUsedTime" label="最后使用" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastUsedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="useCredential(row)">
              使用
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑账号' : '添加账号'"
      width="500px"
    >
      <el-form
        ref="credentialFormRef"
        :model="credentialForm"
        :rules="credentialRules"
        label-width="100px"
      >
        <el-form-item label="网站" prop="websiteId">
          <el-select
            v-model="credentialForm.websiteId"
            placeholder="请选择网站"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in websiteOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="credentialForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="credentialForm.password"
            :type="showPasswordInput ? 'text' : 'password'"
            placeholder="请输入密码"
          >
            <template #append>
              <el-button @click="showPasswordInput = !showPasswordInput">
                <el-icon>
                  <View v-if="!showPasswordInput" />
                  <Hide v-else />
                </el-icon>
              </el-button>
              <el-button @click="generatePassword">生成</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="credentialForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCredentialList, createCredential, updateCredential, deleteCredential, getCredentialDetail } from '@/api/credential'
import { getWebsiteList } from '@/api/website'
import { useUserStore } from '@/stores/user'
import { verifyMasterPassword } from '@/api/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const masterPasswordDialogVisible = ref(false)
const masterPasswordInput = ref('')
const masterPasswordLoading = ref(false)
const masterPasswordError = ref('')
const isEdit = ref(false)
const credentialFormRef = ref(null)
const showPasswordInput = ref(false)

const searchKeyword = ref('')
const selectedWebsite = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const credentialList = ref([])
const websiteOptions = ref([])

// 凭证表单
const credentialForm = reactive({
    id: null,
    websiteId: null,
    username: '',
    password: '',
    notes: ''
})

// 表单验证规则
const credentialRules = {
    websiteId: [
        { required: true, message: '请选择网站', trigger: 'change' }
    ],
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { max: 100, message: '用户名长度不能超过100个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { max: 255, message: '密码长度不能超过255个字符', trigger: 'blur' }
    ]
}

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
    } catch (error) {
        masterPasswordError.value = error.message || '主密码验证失败'
    } finally {
        masterPasswordLoading.value = false
    }
}

// 复制文本
const copyText = async (text, label) => {
    try {
        await navigator.clipboard.writeText(text)
        ElMessage.success(`${label}已复制到剪贴板`)
    } catch {
        ElMessage.error('复制失败')
    }
}

// 切换密码显示
const togglePassword = async (row) => {
    if (row.showPassword) {
        row.showPassword = false
        return
    }

    const hasMasterPassword = await verifyMasterPasswordAsync()
    if (!hasMasterPassword) {
        return
    }

    try {
        const response = await getCredentialDetail(row.id, userStore.masterKey)
        row.password = response.data.password
        row.showPassword = true
    } catch (error) {
        ElMessage.error('获取密码失败')
    }
}

// 使用凭证
const useCredential = async (row) => {
    const hasMasterPassword = await verifyMasterPasswordAsync()
    if (!hasMasterPassword) {
        return
    }

    try {
        const response = await getCredentialDetail(row.id, userStore.masterKey)
        await copyText(response.data.password, '密码')
        // TODO: 更新最后使用时间
    } catch {
        ElMessage.error('获取密码失败')
    }
}

// 生成密码
const generatePassword = () => {
    const length = 16
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+'
    let password = ''

    for (let i = 0; i < length; i++) {
        password += charset.charAt(Math.floor(Math.random() * charset.length))
    }

    credentialForm.password = password
    showPasswordInput.value = true
}

// 加载凭证列表
const loadCredentialList = async () => {
    loading.value = true
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchKeyword.value,
            websiteId: selectedWebsite.value
        }
        const response = await getCredentialList(params)
        credentialList.value = response.data.records.map(item => ({
            ...item,
            showPassword: false
        }))
        total.value = response.data.total
    } catch (error) {
        ElMessage.error('加载凭证列表失败')
    } finally {
        loading.value = false
    }
}

// 加载网站选项
const loadWebsiteOptions = async () => {
    try {
        const response = await getWebsiteList({ size: 1000 })
        websiteOptions.value = response.data.records
    } catch (error) {
        console.error('加载网站列表失败:', error)
    }
}

// 搜索
const handleSearch = () => {
    currentPage.value = 1
    loadCredentialList()
}

// 分页大小变化
const handleSizeChange = (val) => {
    pageSize.value = val
    loadCredentialList()
}

// 当前页变化
const handleCurrentChange = (val) => {
    currentPage.value = val
    loadCredentialList()
}

// 显示添加对话框
const showAddDialog = () => {
    isEdit.value = false
    resetForm()
    dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = async (row) => {
    const hasMasterPassword = await verifyMasterPasswordAsync()
    if (!hasMasterPassword) {
        return
    }

    try {
        const response = await getCredentialDetail(row.id, userStore.masterKey)
        isEdit.value = true
        Object.assign(credentialForm, {
            id: row.id,
            websiteId: row.websiteId,
            username: response.data.username,
            password: response.data.password,
            notes: row.notes
        })
        showPasswordInput.value = true
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取凭证详情失败')
    }
}

// 重置表单
const resetForm = () => {
    if (credentialFormRef.value) {
        credentialFormRef.value.resetFields()
    }
    Object.assign(credentialForm, {
        id: null,
        websiteId: null,
        username: '',
        password: '',
        notes: ''
    })
    showPasswordInput.value = false
}

// 提交表单
const handleSubmit = async () => {
    if (!credentialFormRef.value) return

    await credentialFormRef.value.validate(async (valid) => {
        if (!valid) return

        const hasMasterPassword = await verifyMasterPasswordAsync()
        if (!hasMasterPassword) {
            return
        }

        submitLoading.value = true
        try {
            if (isEdit.value) {
                await updateCredential(credentialForm.id, credentialForm, userStore.masterKey)
                ElMessage.success('更新成功')
            } else {
                await createCredential(credentialForm, userStore.masterKey)
                ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            loadCredentialList()
        } catch (error) {
            ElMessage.error(error.message || '操作失败')
        } finally {
            submitLoading.value = false
        }
    })
}

// 删除凭证
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除账号"${row.username}"吗？`,
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        await deleteCredential(row.id)
        ElMessage.success('删除成功')
        loadCredentialList()
    } catch {
        // 取消操作
    }
}

// 监听路由参数变化
watch(
    () => route.query,
    (query) => {
        if (query.websiteId) {
            selectedWebsite.value = Number(query.websiteId)
        }
        if (query.keyword) {
            searchKeyword.value = query.keyword
        }
        handleSearch()
    },
    { immediate: true }
)

onMounted(() => {
    loadWebsiteOptions()
    loadCredentialList()
})
</script>

<style scoped>
.credentials-container {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.search-bar {
    margin-bottom: 20px;
    display: flex;
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
    font-size: 14px;
}

.username-cell,
.password-cell {
    display: flex;
    align-items: center;
    gap: 5px;
}

.password-cell span {
    min-width: 80px;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.error-message {
    color: #f56c6c;
    font-size: 12px;
    margin-top: 5px;
}
</style>