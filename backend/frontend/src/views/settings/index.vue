<template>
  <div class="settings-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <el-form
            ref="basicFormRef"
            :model="basicForm"
            label-width="120px"
            style="max-width: 600px"
          >
            <el-form-item label="自动锁定时间">
              <el-select v-model="basicForm.autoLockTime" placeholder="选择自动锁定时间">
                <el-option label="不自动锁定" :value="0" />
                <el-option label="5分钟" :value="5" />
                <el-option label="15分钟" :value="15" />
                <el-option label="30分钟" :value="30" />
                <el-option label="1小时" :value="60" />
              </el-select>
            </el-form-item>

            <el-form-item label="剪贴板清除时间">
              <el-select v-model="basicForm.clipboardClearTime" placeholder="选择剪贴板清除时间">
                <el-option label="不自动清除" :value="0" />
                <el-option label="10秒" :value="10" />
                <el-option label="30秒" :value="30" />
                <el-option label="1分钟" :value="60" />
                <el-option label="5分钟" :value="300" />
              </el-select>
            </el-form-item>

            <el-form-item label="默认密码长度">
              <el-input-number
                v-model="basicForm.defaultPasswordLength"
                :min="8"
                :max="64"
              />
            </el-form-item>

            <el-form-item label="主题">
              <el-radio-group v-model="basicForm.theme">
                <el-radio value="light">浅色</el-radio>
                <el-radio value="dark">深色</el-radio>
                <el-radio value="auto">跟随系统</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="语言">
              <el-select v-model="basicForm.language" placeholder="选择语言">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form
            ref="securityFormRef"
            :model="securityForm"
            :rules="securityRules"
            label-width="120px"
            style="max-width: 600px"
          >
            <el-form-item label="修改密码">
              <el-button type="primary" @click="showChangePasswordDialog">修改密码</el-button>
            </el-form-item>

            <el-form-item label="修改主密码">
              <el-button type="primary" @click="showChangeMasterPasswordDialog">修改主密码</el-button>
              <div class="form-tip">
                主密码用于加密您的数据，修改后需要重新加密所有密码
              </div>
            </el-form-item>

            <el-form-item label="双因素认证">
              <el-switch v-model="securityForm.twoFactorAuth" />
              <div class="form-tip">
                启用双因素认证可以提高账户安全性
              </div>
            </el-form-item>

            <el-form-item label="登录日志">
              <el-button type="primary" @click="viewLoginLogs">查看登录日志</el-button>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 数据管理 -->
        <el-tab-pane label="数据管理" name="data">
          <el-form label-width="120px" style="max-width: 600px">
            <el-form-item label="导出数据">
              <el-button type="primary" @click="exportData">导出所有数据</el-button>
              <div class="form-tip">
                导出的数据将加密存储，需要主密码才能导入
              </div>
            </el-form-item>

            <el-form-item label="导入数据">
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :on-change="handleFileChange"
                accept=".json"
                :limit="1"
              >
                <el-button type="primary">选择文件</el-button>
                <template #tip>
                  <div class="form-tip">支持导入JSON格式的备份文件</div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="清除数据">
              <el-button type="danger" @click="clearAllData">清除所有数据</el-button>
              <div class="form-tip">
                此操作不可恢复，请谨慎操作
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 浏览器扩展 -->
        <el-tab-pane label="浏览器扩展" name="extension">
          <div class="extension-info">
            <h3>浏览器扩展</h3>
            <p>安装浏览器扩展可以实现以下功能：</p>
            <ul>
              <li>自动检测当前网站的登录页面</li>
              <li>一键填充账号密码</li>
              <li>快速保存新的账号密码</li>
              <li>实时监控网站登录状态</li>
            </ul>

            <div class="extension-status">
              <el-tag :type="extensionInstalled ? 'success' : 'info'">
                {{ extensionInstalled ? '已安装' : '未安装' }}
              </el-tag>
            </div>

            <div class="extension-actions">
              <el-button type="primary" @click="downloadExtension">
                下载扩展
              </el-button>
              <el-button @click="showExtensionGuide">
                安装指南
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="changePasswordVisible" title="修改密码" width="400px">
      <el-form
        ref="changePasswordFormRef"
        :model="changePasswordForm"
        :rules="changePasswordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="changePasswordForm.oldPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改主密码对话框 -->
    <el-dialog v-model="changeMasterPasswordVisible" title="修改主密码" width="400px">
      <el-form
        ref="changeMasterPasswordFormRef"
        :model="changeMasterPasswordForm"
        :rules="changeMasterPasswordRules"
        label-width="100px"
      >
        <el-form-item label="当前主密码" prop="oldMasterPassword">
          <el-input
            v-model="changeMasterPasswordForm.oldMasterPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新主密码" prop="newMasterPassword">
          <el-input
            v-model="changeMasterPasswordForm.newMasterPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmMasterPassword">
          <el-input
            v-model="changeMasterPasswordForm.confirmMasterPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changeMasterPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangeMasterPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changePassword, changeMasterPassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('basic')

// 基本设置表单
const basicForm = reactive({
    autoLockTime: 15,
    clipboardClearTime: 30,
    defaultPasswordLength: 16,
    theme: 'light',
    language: 'zh-CN'
})

// 安全设置表单
const securityForm = reactive({
    twoFactorAuth: false
})

// 修改密码相关
const changePasswordVisible = ref(false)
const changePasswordFormRef = ref(null)
const changePasswordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})
const changePasswordRules = {
    oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 8, max: 100, message: '密码长度在8-100个字符之间', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value !== changePasswordForm.newPassword) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}

// 修改主密码相关
const changeMasterPasswordVisible = ref(false)
const changeMasterPasswordFormRef = ref(null)
const changeMasterPasswordForm = reactive({
    oldMasterPassword: '',
    newMasterPassword: '',
    confirmMasterPassword: ''
})
const changeMasterPasswordRules = {
    oldMasterPassword: [
        { required: true, message: '请输入当前主密码', trigger: 'blur' }
    ],
    newMasterPassword: [
        { required: true, message: '请输入新主密码', trigger: 'blur' },
        { min: 8, max: 100, message: '主密码长度在8-100个字符之间', trigger: 'blur' }
    ],
    confirmMasterPassword: [
        { required: true, message: '请确认主密码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value !== changeMasterPasswordForm.newMasterPassword) {
                    callback(new Error('两次输入的主密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}

// 扩展状态
const extensionInstalled = ref(false)

// 保存基本设置
const saveBasicSettings = () => {
    // TODO: 保存到后端
    ElMessage.success('设置已保存')
}

// 保存安全设置
const saveSecuritySettings = () => {
    // TODO: 保存到后端
    ElMessage.success('设置已保存')
}

// 显示修改密码对话框
const showChangePasswordDialog = () => {
    changePasswordVisible.value = true
}

// 显示修改主密码对话框
const showChangeMasterPasswordDialog = () => {
    changeMasterPasswordVisible.value = true
}

// 修改密码
const handleChangePassword = async () => {
    if (!changePasswordFormRef.value) return

    await changePasswordFormRef.value.validate(async (valid) => {
        if (!valid) return

        try {
            await changePassword({
                oldPassword: changePasswordForm.oldPassword,
                newPassword: changePasswordForm.newPassword
            })
            ElMessage.success('密码修改成功')
            changePasswordVisible.value = false
            // 清空表单
            changePasswordForm.oldPassword = ''
            changePasswordForm.newPassword = ''
            changePasswordForm.confirmPassword = ''
        } catch (error) {
            ElMessage.error(error.message || '密码修改失败')
        }
    })
}

// 修改主密码
const handleChangeMasterPassword = async () => {
    if (!changeMasterPasswordFormRef.value) return

    await changeMasterPasswordFormRef.value.validate(async (valid) => {
        if (!valid) return

        try {
            await ElMessageBox.confirm(
                '修改主密码将重新加密所有密码数据，此过程可能需要一些时间。确定继续吗？',
                '警告',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }
            )

            await changeMasterPassword({
                oldMasterPassword: changeMasterPasswordForm.oldMasterPassword,
                newMasterPassword: changeMasterPasswordForm.newMasterPassword
            })
            ElMessage.success('主密码修改成功')
            changeMasterPasswordVisible.value = false
            // 清空表单
            changeMasterPasswordForm.oldMasterPassword = ''
            changeMasterPasswordForm.newMasterPassword = ''
            changeMasterPasswordForm.confirmMasterPassword = ''
            // 清除缓存的主密钥
            userStore.clearMasterKey()
        } catch {
            // 取消操作
        }
    })
}

// 查看登录日志
const viewLoginLogs = () => {
    // TODO: 跳转到登录日志页面
    ElMessage.info('登录日志功能开发中')
}

// 导出数据
const exportData = async () => {
    try {
        await ElMessageBox.confirm(
            '导出的数据将使用主密码加密，确定继续吗？',
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
            }
        )

        // TODO: 调用后端API导出数据
        ElMessage.success('数据导出成功')
    } catch {
        // 取消操作
    }
}

// 处理文件上传
const handleFileChange = (file) => {
    // TODO: 处理导入逻辑
    console.log('选择的文件:', file)
}

// 清除所有数据
const clearAllData = async () => {
    try {
        await ElMessageBox.confirm(
            '此操作将永久删除所有数据，且不可恢复。确定继续吗？',
            '危险操作',
            {
                confirmButtonText: '确定删除',
                cancelButtonText: '取消',
                type: 'error'
            }
        )

        // TODO: 调用后端API清除数据
        ElMessage.success('数据已清除')
    } catch {
        // 取消操作
    }
}

// 下载扩展
const downloadExtension = () => {
    // TODO: 提供扩展下载链接
    ElMessage.info('扩展下载功能开发中')
}

// 显示扩展安装指南
const showExtensionGuide = () => {
    // TODO: 显示安装指南
    ElMessage.info('安装指南功能开发中')
}
</script>

<style scoped>
.settings-container {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}

.extension-info {
    max-width: 600px;
}

.extension-info h3 {
    margin-bottom: 15px;
    color: #303133;
}

.extension-info p {
    color: #606266;
    margin-bottom: 10px;
}

.extension-info ul {
    color: #606266;
    margin-bottom: 20px;
    padding-left: 20px;
}

.extension-info li {
    margin-bottom: 5px;
}

.extension-status {
    margin-bottom: 20px;
}

.extension-actions {
    display: flex;
    gap: 10px;
}
</style>