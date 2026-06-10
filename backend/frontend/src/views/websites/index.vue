<template>
  <div class="websites-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>网站管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加网站
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索网站名称或域名..."
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="selectedCategory"
          placeholder="选择分类"
          clearable
          style="width: 150px; margin-left: 10px"
        >
          <el-option
            v-for="item in categories"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </div>

      <!-- 网站列表 -->
      <el-table :data="websiteList" style="width: 100%" v-loading="loading">
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" class="website-icon">
              {{ row.name?.charAt(0) || 'W' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="网站名称" width="180" />
        <el-table-column prop="domain" label="域名" width="250">
          <template #default="{ row }">
            <el-link type="primary" :href="'https://' + row.domain" target="_blank">
              {{ row.domain }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.category || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credentialCount" label="账号数量" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="viewCredentials(row)">
              查看账号
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
      :title="isEdit ? '编辑网站' : '添加网站'"
      width="500px"
    >
      <el-form
        ref="websiteFormRef"
        :model="websiteForm"
        :rules="websiteRules"
        label-width="100px"
      >
        <el-form-item label="网站名称" prop="name">
          <el-input v-model="websiteForm.name" placeholder="请输入网站名称" />
        </el-form-item>

        <el-form-item label="域名" prop="domain">
          <el-input v-model="websiteForm.domain" placeholder="请输入域名，如 example.com">
            <template #prepend>https://</template>
          </el-input>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select
            v-model="websiteForm.category"
            placeholder="请选择分类"
            filterable
            allow-create
          >
            <el-option
              v-for="item in categories"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="登录页面" prop="loginUrl">
          <el-input v-model="websiteForm.loginUrl" placeholder="请输入登录页面URL（可选）" />
        </el-form-item>

        <el-form-item label="图标URL" prop="iconUrl">
          <el-input v-model="websiteForm.iconUrl" placeholder="请输入图标URL（可选）" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="websiteForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述（可选）"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWebsiteList, createWebsite, updateWebsite, deleteWebsite } from '@/api/website'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const websiteFormRef = ref(null)

const searchKeyword = ref('')
const selectedCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const websiteList = ref([])

// 分类列表
const categories = ref([
    '社交媒体',
    '开发工具',
    '购物',
    '金融',
    '娱乐',
    '学习',
    '工作',
    '其他'
])

// 网站表单
const websiteForm = reactive({
    id: null,
    name: '',
    domain: '',
    category: '',
    loginUrl: '',
    iconUrl: '',
    description: ''
})

// 表单验证规则
const websiteRules = {
    name: [
        { required: true, message: '请输入网站名称', trigger: 'blur' },
        { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' }
    ],
    domain: [
        { required: true, message: '请输入域名', trigger: 'blur' },
        { max: 255, message: '域名长度不能超过255个字符', trigger: 'blur' }
    ],
    category: [
        { max: 50, message: '分类长度不能超过50个字符', trigger: 'blur' }
    ]
}

// 格式化日期
const formatDate = (date) => {
    if (!date) return '-'
    return new Date(date).toLocaleString('zh-CN')
}

// 加载网站列表
const loadWebsiteList = async () => {
    loading.value = true
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchKeyword.value,
            category: selectedCategory.value
        }
        const response = await getWebsiteList(params)
        websiteList.value = response.data.records
        total.value = response.data.total
    } catch (error) {
        ElMessage.error('加载网站列表失败')
    } finally {
        loading.value = false
    }
}

// 搜索
const handleSearch = () => {
    currentPage.value = 1
    loadWebsiteList()
}

// 分页大小变化
const handleSizeChange = (val) => {
    pageSize.value = val
    loadWebsiteList()
}

// 当前页变化
const handleCurrentChange = (val) => {
    currentPage.value = val
    loadWebsiteList()
}

// 显示添加对话框
const showAddDialog = () => {
    isEdit.value = false
    resetForm()
    dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
    isEdit.value = true
    Object.assign(websiteForm, {
        id: row.id,
        name: row.name,
        domain: row.domain,
        category: row.category,
        loginUrl: row.loginUrl,
        iconUrl: row.iconUrl,
        description: row.description
    })
    dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
    if (websiteFormRef.value) {
        websiteFormRef.value.resetFields()
    }
    Object.assign(websiteForm, {
        id: null,
        name: '',
        domain: '',
        category: '',
        loginUrl: '',
        iconUrl: '',
        description: ''
    })
}

// 提交表单
const handleSubmit = async () => {
    if (!websiteFormRef.value) return

    await websiteFormRef.value.validate(async (valid) => {
        if (!valid) return

        submitLoading.value = true
        try {
            if (isEdit.value) {
                await updateWebsite(websiteForm.id, websiteForm)
                ElMessage.success('更新成功')
            } else {
                await createWebsite(websiteForm)
                ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            loadWebsiteList()
        } catch (error) {
            ElMessage.error(error.message || '操作失败')
        } finally {
            submitLoading.value = false
        }
    })
}

// 删除网站
const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除网站"${row.name}"吗？删除后该网站下的所有账号密码也将被删除。`,
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        await deleteWebsite(row.id)
        ElMessage.success('删除成功')
        loadWebsiteList()
    } catch {
        // 取消操作
    }
}

// 查看凭证
const viewCredentials = (row) => {
    router.push({
        path: '/credentials',
        query: { websiteId: row.id }
    })
}

onMounted(() => {
    loadWebsiteList()
})
</script>

<style scoped>
.websites-container {
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

.website-icon {
    background: #409eff;
    color: #fff;
    font-size: 16px;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>